# Техническое задание — модуль EventDeathMine

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/EventDeathMine`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | EventDeathMine |
| Артефакт (Maven) | `EventDeathMine` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0.0 |
| Главный класс | `ru.kotikstasika.eventdeathmine.EventDeathMine` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | не указаны |

**Назначение:** под-ивент «Смертельная шахта» для системы EventManager. По команде
`/deathmine spawn` (обычно её выполняет консоль после победы в голосовании EventManager)
в случайной подходящей точке мира появляется WorldEdit-схематика шахты. Специальной
фоновой задачей все блоки `MOVING_PISTON` схемы заменяются на «руду»: обсидиан (80 %),
плачущий обсидиан (15 %), древние обломки (5 %). Игроки **любой киркой** (или руками в
креативе) добывают эти блоки — за каждый выдаётся случайный лут из LootManager
(1/2/3 предмета по типу руды), при этом кирка после добычи обсидиана/плача/обломков
**телепортируется в случайный слот инвентаря или слот брони** (механика «шахты-ловушки»).
Вокруг шахты рисуется куб из частиц (зона, где запрещены жемчуг конца и плоды хоруса),
в радиусе видимости — BossBar с обратным отсчётом и процентом оставшейся руды.
По истечении таймера (или `/deathmine remove`) шахта откатывается, в чат выводится
топ-5 добытчиков, статус удаляется из `/event`.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot API 1.16+ (собирается против `paper-api:1.21.4-R0.1-SNAPSHOT`, Java 21).

### 2.2. Плагины
| Плагин | Версия (pom) | Обязательность (plugin.yml) | Использование |
|---|---|---|---|
| WorldEdit | 7.3.16 | depend + softdepend | чтение (async) и вставка схематики, откат |
| WorldGuard | 7.0.13 | depend + softdepend | регион `death_mine_event` с флагами |
| LootManager | 1.0 | depend + softdepend | `LootAPI.getRandomItems(min, max)` — лут за блок |
| EventManager | 1.0-SNAPSHOT | depend + softdepend | реестр `events` (ключ `death_mine`) |
| DecentHolograms | — | не используется | — |

### 2.3. Библиотеки
- `org.projectlombok:lombok:1.18.32` (provided, только `@Generated`).

### 2.4. Ресурсы на диске
- Схематика: сначала ищется `<dataFolder>/event_death_mine.schem`, затем
  `plugins/WorldEdit/schematics/event_death_mine.schem` (имя конфигурируется).

---

## 3. Архитектура (структура пакетов)

```
ru.kotikstasika.eventdeathmine
├── EventDeathMine                 — главный класс: менеджер, команда (executor+tab),
│                                    листенер, старт BossBar-обновлятора; onDisable → removeMine
├── command.DeathMineCommand       — /deathmine (CommandExecutor + TabCompleter)
├── config.ConfigManager           — обёртка config.yml (saveDefaultConfig; геттеры с дефолтами;
│                                    format(template, placeholders))
├── listener.DeathMineListener     — запрет жемчуга в кубе; добыча руды (лут, телепорт кирки,
│                                    учёт); внутренний enum ArmorSlot
├── manager.DeathMineManager       — оркестратор: поиск точки, спавн (async чтение схемы →
│                                    sync вставка), регион, BossBar, таймер, статистика,
│                                    частицы, замена поршней, broadcast топ-5, remove
├── model.DeathMineState           — enum {NOT_SPAWNED, SPAWNED}
├── service.LootService            — выдача лута за блок (min/max по типу руды), складирование/дроп
├── service.RegionService          — создание/удаление региона WorldGuard death_mine_event
├── service.SchematicService       — асинхронное чтение схематики (CompletableFuture<Clipboard>)
├── task.BossBarUpdaterTask        — раз в секунду: видимость BossBar по радиусу
├── task.MineTimerTask             — раз в секунду: тик таймера шахты
├── task.ParticleCubeTask          — рисование куба частицами DUST каждые 5 тиков (шаг 0.75)
├── task.RegionBarrierCleaner      — порционное удаление блоков BARRIER в радиусе (batch/tick)
├── task.ReplaceMovingPistonTask   — порционное сканирование области и замена MOVING_PISTON
│                                    на руду с регистрацией ресурсных блоков (4000 операций/тик)
└── util.ColorUtil                 — colorize: ChatColor.translateAlternateColorCodes('&', s)
```

---

## 4. Функциональные требования

### 4.1. Команда `/deathmine` (DeathMineCommand; алиасов нет)
| Синтаксис | Право | Поведение |
|---|---|---|
| `/deathmine` | — | Префикс `messages.prefix.commands` + справка (5 строк) |
| `/deathmine spawn` | `deathmine.spawn` (только для Player; консоль проходит) | `spawnMine(sender)` |
| `/deathmine remove` | `deathmine.remove` (аналогично) | `removeMine(sender)` |
| `/deathmine settimer <мин> <сек>` | `deathmine.settimer` | Установить остаток времени; шахта должна существовать; минуты ≤ 20, секунды ≤ 60 (при мин == 20 сек сбрасываются в 0); итог `мин*60+сек` секунд |
| `/deathmine tp` | — (право `deathmine.tp` проверяется внутри) | Телепорт игрока к шахте (+0.5, +2.0, +0.5 с сохранением yaw/pitch) |
| `/deathmine status` | — | Статус: Остановлено / Запущено / Неизвестно (`messages.status_*`) |

Tab-completion (право `eventdeathmine.admin`): подкоманды `spawn, remove, settimer, tp, status`; для `settimer` минуты `0,1,2,3,5,10,15,20` и секунды `0,5,10,15,20,30,45,60`. Без права — пустой список.

### 4.2. Права (permissions)
| Право | Объявлено | Где проверяется | Default |
|---|---|---|---|
| `eventdeathmine.admin` | plugin.yml | TabCompleter (показ подсказок) | op |
| `deathmine.spawn` | только код | `/deathmine spawn` (для Player) | не задано (op) |
| `deathmine.remove` | только код | `/deathmine remove` (для Player) | не задано (op) |
| `deathmine.settimer` | только код | `/deathmine settimer` (для Player) | не задано (op) |
| `deathmine.tp` | только код | `teleportToMine` | не задано (op) |

Особенность: проверки прав spawn/remove/settimer обходятся, если отправитель — **не игрок** (консоль/командный блок), поэтому EventManager может запускать ивент из консоли.

### 4.3. Спавн шахты (`spawnMine`)
1. Требуется `NOT_SPAWNED`, иначе «Шахта уже существует!».
2. Мир из `settings.world` (default `world`); поиск точки `findValidLocation`: до `maxSpawnAttempts` (50) случайных точек x,z ∈ [-2500, 2500], y = highestBlockYAt; блок под точкой — твёрдый, не жидкость, имя без `WATER`/`LAVA`; плюс `isAreaValid` — в квадрате ±10 на высоте y нет жидкостей.
3. Асинхронно читается схематика (SchematicService), затем в главном потоке вставляется (`ignoreAirBlocks(true)`); `EditSession` сохраняется для отката.
4. Далее: регион WorldGuard (`death_mine_event`, halfSize 40), BossBar, таймер (1200 с по умолчанию), фиксация `startTimeMillis`, куб частиц (size 50, duration 600 с из конфига), очистка барьеров (radius 135), запуск `ReplaceMovingPistonTask` (radius 35, 4000 оп/тик), state = SPAWNED.
5. Broadcast: ASCII-рамка, заголовок «Смертельная шахта ☠», координаты X/Y/Z; отправителю — `messages.created`.
6. В реестр EventManager кладётся ключ `death_mine` (сообщение обновляется каждый тик таймера).

### 4.4. Механика руды (ReplaceMovingPistonTask + слушатель)
- `ReplaceMovingPistonTask` сканирует область: X/Z ±35, Y от center-20 до center+30 (ограничено высотой мира), 4000 блоков/тик; каждый блок `MOVING_PISTON` заменяется броском «руда»: `r = rnd(0..99)`; r < 80 → `OBSIDIAN`, r < 95 → `CRYING_OBSIDIAN`, иначе `ANCIENT_DEBRIS`; блок регистрируется в `resourceLocations`.
- `BlockBreakEvent` (шахта SPAWNED, блок в радиусе `scanRadius`=40 от центра, блок — зарегистрированный ресурсный):
  - **Креатив**: блок удаляется, лут и учёт выдаются;
  - **Выживание**: в руке должна быть кирка (имя материала заканчивается на `_PICKAXE`), иначе событие **отменяется** и (особенность кода) клон инструмента ставится в случайный слот 0..35;
  - при добыче `OBSIDIAN`/`CRYING_OBSIDIAN`/`ANCIENT_DEBRIS` кирка **перемещается**: бросок 0..12 — 1..5 и default → обмен со случайным слотом, 6..9 → обмен со слотом шлема/нагрудника/штанов/ботинок (`relocatePickaxeAnywhere`);
  - событие отменяется, блок ставится в AIR (дроп-механика ванили отключена), блок снимается с учёта (`onResourceBlockBroken`), `incrementMined(ник)`;
  - лут: `LootService.giveLootForBlock` — OBSIDIAN/NETHERITE_BLOCK → 1 предмет, CRYING_OBSIDIAN → 2, ANCIENT_DEBRIS → 3 (`LootAPI.getRandomItems(min, max)`, берётся первый предмет списка; при нехватке места — дроп на землю).
- `NETHERITE_BLOCK` и `NETHERITE_BLOCK`-руда входят в `isAllowedMineBlock` (OBSIDIAN, CRYING_OBSIDIAN, ANCIENT_DEBRIS, NETHERITE_BLOCK), но поршневая замена их не создаёт.

### 4.5. Защитный куб (частицы и запрет телепортов)
- `ParticleCubeTask`: куб со стороной `settings.cube.size` (50) вокруг точки, ребра из частиц `DUST` (DustOptions цвет `Color.fromBGR(16759867)`, размер 1), шаг 0.75, задача каждые 5 тиков; автоотмена через `durationSeconds * 10000` мс (см. §9); центр остаётся в `activeCubes`, пока задача жива (проверка `isInsideAnyCube` — куб с фиксированным размером 50).
- Внутри куба запрещается ПКМ жемчугом конца (`PlayerInteractEvent`, отмена). Запрета плода хоруса, в отличие от EventGruz, в этом модуле нет.

### 4.6. BossBar
- Создаётся при спавне: цвет PINK, стиль SOLID, заголовок «До исчезновения "Смертельной шахты"».
- Обновление раз в секунду (`MineTimerTask` → `updateBossBar`): заголовок `До исчезновения "Смертельной шахты": MM:SS ( P% )`, где P — процент оставшихся ресурсных блоков; прогресс = timeLeft/totalDuration.
- Видимость: `BossBarUpdaterTask` каждую секунду добавляет/убирает игроков в радиусе `bossbarRadius` (100) от шахты (в том же мире).

### 4.7. Таймер и завершение
- `timeLeftSeconds` инициализируется из `settings.defaultDurationSeconds` (1200); тик каждую секунду; при 0 — `removeMine(null)` (авто-завершение).
- `setTimerSeconds` обновляет остаток и BossBar (без изменения totalDuration — прогресс может превысить 1, клампится).
- `removeMine`: broadcast топ-5 («Поздровляем участников мероприятия "Смертельная шахта"…», строки `N. Имя — K блоков.`; при пустой статистике блок не выводится), откат схематики, удаление региона, скрытие BossBar, отмена задач (частицы, таймер), очистка статистики и ресурсных блоков, удаление ключа `death_mine` из EventManager, state = NOT_SPAWNED.
- `onDisable` вызывает `removeMine(null)` и останавливает BossBar-обновлятор.

### 4.8. Очистка барьеров (RegionBarrierCleaner)
- По радиусу `settings.barrier.radius` (135) вокруг точки порционно (по `settings.barrier.opsPerTick`, default 2500 блоков/тик, конфиг ключом `settings.barrier.opsPerTick` — в поставленном config.yml отсутствует) заменяет блоки `BARRIER` на AIR (без физики), подгружая чанки; колонка Y 0..maxHeight. Назначение — расчистка воздуха от «барьерных» блоков (например, после иных ивентов).

### 4.9. Обрабатываемые события
- `BlockBreakEvent` — добыча руды (см. 4.4);
- `PlayerInteractEvent` — отмена жемчуга конца в кубе;
- команды `/deathmine`.

### 4.10. Плейсхолдеры сообщений
| Плейсхолдер | Сообщение | Значение |
|---|---|---|
| `%minutes%` | `messages.timer_set` | минуты установленного таймера |
| `%seconds%` | `messages.timer_set` | секунды установленного таймера |

---

## 5. Конфигурация (config.yml)

### 5.1. `messages.*` (строки, `&`-цвета)
| Параметр | Default | Описание |
|---|---|---|
| `prefix.error/warn/info/ok` | `&c&l▶ &f` / `&e&l▶ &f` / `&7&l▶ &f` / `&a&l▶ &f` | префиксы (в коде используется только `prefix.commands` = читается, но ключа `commands` в секции нет → пусто) |
| `not_started` | «Событие не запущено!» | remove/tp при отсутствии шахты |
| `only_players` | «Эта команда только для игроков!» | `/deathmine tp` из консоли |
| `bad_update_type` | «Неверный тип обновления! Доступные: shears, flowers» | в коде не используется (остаток шаблона EventPolana) |
| `status_stopped/searching/unknown/running` | «Статус: Остановлено/Поиск локации…/Неизвестно/Запущено» | `/deathmine status` |
| `event_stopped` | «Событие Цветочная поляна успешно остановлено!» | в коде не используется (остаток шаблона) |
| `created` | «Шахта создана!» | ответ на spawn |
| `removed` | «Шахта удалена!» | ответ на remove |
| `teleported` | «Вы телепортированы к событию!» | ответ на tp |
| `no_permission` | «У вас нет прав на использование этой команды!» | отказ подкоманд |
| `timer_set` | «Таймер установлен на %minutes% мин. %seconds% сек.» | ответ на settimer |
| `timer_minutes_max` | «Максимум 20 минут» | валидация |
| `timer_seconds_max` | «Максимум 60 секунд» | валидация |
| `displayName` (нет в файле; читается кодом) | `&4&lСмертельная шахта` | имя для `/event` |
| `color` (нет в файле; читается кодом) | `&4` | акцентный цвет для `/event` |

### 5.2. `settings.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `world` | string | `world` | мир спавна |
| `scanRadius` | int | 40 | радиус (блоков) принадлежности блока шахте (BlockBreak) |
| `bossbarRadius` | int | 100 | радиус показа BossBar |
| `maxSpawnAttempts` | int | 50 | попыток поиска точки |
| `defaultDurationSeconds` | int | 1200 | длительность шахты, сек |
| `schematic` | string | `event_death_mine.schem` | имя файла схематики |
| `cube.size` | int | 50 | сторона куба частиц |
| `cube.durationSeconds` | long | 600 | «длительность» куба (см. §9 про множитель 10000) |
| `barrier.radius` | int | 135 | радиус очистки BARRIER |
| `barrier.opsPerTick` (нет в файле) | int | 2500 | блоков за тик при очистке |

---

## 6. Хранение данных
- Постоянного хранения нет. Всё состояние (позиция, EditSession, счётчики `playerMinedBlocks` по именам, `resourceLocations`) — в памяти; при выключении шахта откатывается, статистика теряется (топ выводится до отката).

---

## 7. Интеграции
- **EventManager** — ключ `death_mine` в реестре; строка обновляется каждый тик (координаты + «уже N сек.» — фактически выводятся только секунды с начала ивента).
- **LootManager** — `LootAPI.getRandomItems(min, max)` для лута.
- **WorldEdit** — схематика (async чтение, sync вставка, undo).
- **WorldGuard** — регион `death_mine_event`: флаги BUILD DENY, WITHER_DAMAGE DENY, BLOCK_PLACE DENY, CHEST_ACCESS DENY, GHAST_FIREBALL DENY, INTERACT ALLOW, TNT DENY, PVP ALLOW, OTHER_EXPLOSION DENY, CREEPER_EXPLOSION DENY, кастомный флаг `holyliteitems` DENY (если зарегистрирован). Геометрия: ±40 X/Z, Y −1..300.
- PlaceholderAPI / Vault — не используются.

---

## 8. Сценарий работы (основной поток)
1. Подготовка: EventManager (голосование) выполняет консолью `deathmine spawn`.
2. Поиск точки (до 50 попыток, критерии суши), async чтение схемы, вставка.
3. Регистрация региона, BossBar, таймер 20 мин, куб частиц, очистка барьеров, конвертация MOVING_PISTON → руда (80/15/5), broadcast координат.
4. Процесс: игроки добывают руду любой киркой; за блок — лут (1/2/3 предмета) и «скачок» кирки по инвентарю; статистика копится; BossBar показывает % оставшейся руды и время; в кубе запрещён жемчуг; на территории PvP.
5. Завершение: таймер 0 или `/deathmine remove`.
6. Итоги: broadcast топ-5 добытчиков, откат схемы, удаление региона/BossBar/частиц/ключа `/event`.

---

## 9. Особенности реализации и известные ограничения
- `ParticleCubeTask` сравнивает elapsed с `durationSeconds * 10000L` — фактически длительность в **10 раз** больше заданной (600 с → 100 мин); вероятно, опечатка (должно быть `* 1000`).
- `buildEventMessage` форматирует только `%02d сек.` от `startTimeMillis` — минуты не выводятся, число секунд растёт без ограничения.
- Проверка «в руке кирка» при её отсутствии: событие отменяется, но клон инструмента (любого предмета в руке) кладётся в случайный слот — побочный эффект.
- `setTimerSeconds` не меняет `totalDurationSeconds`, поэтому прогресс-бар после изменения времени может показывать клампированное значение.
- Права `deathmine.*` не объявлены в plugin.yml; `eventdeathmine.admin` влияет только на tab-completion, а не на исполнение.
- `depend` и `softdepend` в plugin.yml содержат один и тот же список плагинов (дублирование; фактически работают как depend).
- Сообщения `bad_update_type`, `event_stopped` и префиксы — неиспользуемые остатки шаблона EventPolana.
- Схема поиска точки `isAreaValid` проверяет только слой на высоте y — вода ниже/выше соседних слоёв не учитывается.
- Чтение схематики асинхронное, но вставка — в главном потоке (большая схема может дать фриз).
- `resourceLocations`/`playerMinedBlocks` не персистентны и не синхронизированы между серверами (если мир общий).

---

## 10. Сборка
```
mvn -pl modules/EventDeathMine -am package   # из корня репозитория
```
- `worldedit`/`worldguard` из группы `local.plugins` должны быть в локальном Maven-репозитории (repository в pom не объявлен).
- `EventManager`, `LootManager` — модули reactor (собираются с `-am`); lombok — provided (аннотация `@Generated`).
- Shade отсутствует; jar без зависимостей.
