# Техническое задание — модуль EventPolana

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/EventPolana`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | EventPolana |
| Артефакт (Maven) | `EventPolana` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0.0 |
| Главный класс | `ru.kotikstasika.eventpolana.EventPolana` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | не указаны |

**Назначение:** под-ивент «Цветочная поляна» (в конфиге голосования EventManager —
`svetpolana`) для системы EventManager. По команде `/polana spawn [редкость]` в случайной
подходящей точке мира появляется WorldEdit-схематика поляны, NPC-«садовник»
(неуязвимый житель с голограммой), который **один раз** выдаёт игроку специальные
ножницы (потом «отращивает» их по таймеру), и начинает периодически спавнить цветы
(10 ванильных типов) вокруг поляны. Игроки срывают цветы **только кастомными ножницами**;
каждый сорванный цветок дропает случайный предмет из LootManager (уровень зависит от
редкости ивента: DEFAULT/EPIC/LEGENDARY), ножницы теряют прочность и исчезают.
Через заданное число минут (по редкости) поляна автоматически удаляется: откат схемы,
удаление региона, NPC, голограмм; статус удаляется из `/event`.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot API 1.16+ (собирается против `paper-api:1.21.4-R0.1-SNAPSHOT`, Java 21).

### 2.2. Плагины (все объявлены в plugin.yml как depend — обязательны)
| Плагин | Версия (pom) | Использование |
|---|---|---|
| WorldEdit | 7.3.16 | вставка/откат схематики поляны |
| WorldGuard | 7.0.13 | регион `polana_event` (PvP-зона) |
| LootManager | 1.0 | `LootAPI.getRandomItems(1, level)` — лут за цветок |
| EventManager | 1.0-SNAPSHOT | реестр `events` (ключ `polana`) |
| DecentHolograms | 2.10.1 | голограмма над садовником |

### 2.3. Библиотеки
- Дополнительных нет (annotations/lombok не подключены).

### 2.4. Ресурсы на диске
- Схематика: `<dataFolder>/event_polana.schem`, при отсутствии — `plugins/WorldEdit/schematics/event_polana.schem` (имя конфигурируется). Отсутствие файла — спавн отменяется, в лог сообщение `messages.errors.schematic_not_found`.

---

## 3. Архитектура (структура пакетов)

```
ru.kotikstasika.eventpolana
├── EventPolana                    — главный класс: сборка сервисов и менеджера, команда /polana
│                                   (PolanaCommand и как executor, и как tabCompleter — два экземпляра),
│                                   листенер; onDisable → removePolana; геттеры состояния; giveShears()
├── command.PolanaCommand          — /polana: spawn/remove/status/give/update/tp/help (+tab: редкости,
│                                   онлайн-игроки, shears/flowers); всё под правом eventpolana.admin
├── config.PluginConfig            — типизированная обёртка config.yml (геттеры с дефолтами;
│                                   formatMessage с {плейсхолдерами})
├── listener.PolanaListeners       — урон по садовнику отменяется; ПКМ по садовнику (выдача ножниц /
│                                   отброс с уроном); BlockBreak цветов (ножницы, прочность, лут,
│                                   частицы, звук)
├── manager.PolanaManager          — оркестратор: асинхронный поиск точки (суше, плоско, твёрдое
│                                   основание; до 50 попыток × до 100 ретраев), вставка схемы, регион,
│                                   спавн/валидация садовника, «взгляд на игрока», голограмма,
│                                   спавн цветов, авто-удаление по таймеру, announceSpawn
├── manager.FlowerManager          — спавн цветов (AtomicInteger-счётчик, батчи 1..50, лимит
│                                   одновременных, GRASS_BLOCK/DIRT + воздух), static isFlower()
├── manager.LootManagerBridge      — дроп случайного лута в локацию + звук (обёртка LootAPI)
├── manager.RegionBarrierCleaner   — порционное удаление BARRIER-блоков (радиус 150, 3000/тик)
├── model.PolanaState              — enum {NOT_SPAWNED, SPAWNED}
├── model.PolanaRarity             — enum {DEFAULT, EPIC, LEGENDARY}
├── service.HologramService        — голограмма садовника (создание/обновление/удаление)
├── service.SchematicService       — чтение и вставка схематики (sync), undoLastPaste
├── service.ShearsService          — кастомные ножницы: PDC-прочность (ключ "shears"), лор,
│                                   зачарование-свечение, инверсия ванильной прочности
├── service.WorldGuardService      — регион polana_event с набором флагов
└── util.TextUtil                  — colorize (&-коды)
```

---

## 4. Функциональные требования

### 4.1. Команда `/polana` (PolanaCommand; алиасов нет; всё требует право `eventpolana.admin`)
| Синтаксис | Поведение |
|---|---|
| `/polana` / `/polana help` | Справка (7 строк) |
| `/polana spawn [редкость]` | Запустить ивент; редкость `DEFAULT\|EPIC\|LEGENDARY`, без аргумента — случайная (`ThreadLocalRandom` по `PolanaRarity.values()`); отказы: «Событие уже запущено!», «Поиск локации уже идет, подождите...», «Неверная редкость! …» |
| `/polana remove` | Остановить ивент («Событие не запущено!» если не активен) |
| `/polana status` | Статус: Запущено (+координаты +редкость) / Поиск локации... / Остановлено / Неизвестно |
| `/polana give <игрок>` | Выдать кастомные ножницы онлайн-игроку («Игрок X не найден!» если офлайн) |
| `/polana update <shears\|flowers>` | shears — вернуть садовнику ножницы немедленно (`setShearsTaken(false)`); flowers — перезапустить задачу спавна цветов; только при активном ивенте |
| `/polana tp` | Телепорт исполнителя-игрока к поляне (+0.5, +2.0, +0.5 с сохранением yaw/pitch); «Эта команда только для игроков!» из консоли |

Tab-completion (при праве): подкоманды `spawn, remove, status, tp, give, update, help`; для spawn — редкости; для give — ники онлайн; для update — `shears, flowers`.

### 4.2. Права (permissions)
| Право | Где объявлено | Где проверяется | Default |
|---|---|---|---|
| `eventpolana.admin` | только код (plugin.yml прав не содержит) | `onCommand` и `onTabComplete` | не задано (op) |

### 4.3. Редкости и их параметры (значения из config.yml)
| Параметр | DEFAULT | EPIC | LEGENDARY |
|---|---|---|---|
| Длительность ивента (`durations.delete.*`, мин) | 15 | 20 | 25 |
| Респаун ножниц (`respawn_shears.*`, с) | 60 | 45 | 35 |
| Интервал спавна цветов (`spawn_flower.*`, с) | 20 | 20 | 20 |
| Уровень лута (`loot.*`) | 1 | 2 | 3 |

### 4.4. Спавн поляны (`spawnPolana`)
1. Отказ если `state == SPAWNED` или идёт поиск локации.
2. Мир из `event.world` (default `world`).
3. Асинхронный поиск (`runTaskAsynchronously`): радиус = `размер worldBorder / 2 − 10`; до 50 случайных точек; пригодность: блок и блок выше — не жидкость/не из набора (WATER, LAVA, KELP, KELP_PLANT, SEAGRASS, TALL_SEAGRASS); поверхность плоская (квадрат ±3, перепад высот ≤ 1, без жидкостей); блок под точкой твёрдый. Не нашли → ретрай: до 100 повторных поисков с паузой 100 тиков (5 с), затем тихий отказ.
4. В главном потоке: вставка схематики (sync, `ignoreAirBlocks(true)`), при неудаче — `messages.errors.schematic_not_found` и отмена.
5. Регион WorldGuard `polana_event` (радиус = `event.flower_break_radius`).
6. Спавн садовника, старт спавна цветов, state = SPAWNED, удаление старого ключа `polana` из EventManager, announceSpawn, очистка барьеров (радиус 150), планирование авто-удаления.

### 4.5. Садовник (NPC)
- Перед спавном из **всех миров** удаляются жители с тегом `EventPolanaNPC`.
- Житель в точке `полана +0.5/+1.0/+0.5`: AI off, invulnerable, collidable false, silent, gravity off, persistent, canPickupItems false, removeWhenFarAway false; профессия/тип/уровень из `gardener.*` (при некорректных значениях — FARMER/PLAINS); setAdult, ageLock; тег `EventPolanaNPC`; в главной руке SHEARS (если ножницы ещё не взяты), оф-рука пуста.
- Через 1 тик — `validateGardener`: дедупликация дублей, респавн при исчезновении; жёстко ставит FARMER/PLAINS/уровень 5/adult/ageLock (конфиг здесь игнорируется).
- Задача «взгляда» каждые `gardener.look_update_interval` (10) тиков: найти ближайшего игрока в `gardener.player_search_radius` (50) и телепортом развернуть жителя к нему; если житель невалиден — респавн.
- Голограмма (HologramService) в точке `житель +2.8Y`, id `gardener_hologram_<ts>`: строка 0 — «Садовод» (HEX), строка 1 — `－ Пора! Бери ножницы! &a[ПКМ]` пока ножницы доступны, иначе `－ Жди, сейчас отрастут цветы.`

### 4.6. Ножницы (ShearsService)
- Предмет SHEARS, имя `shears.display_name` («&6Ножницы»), лор из `shears.description` с подстановкой `{durability}`; зачарование CHANNELING 1 с HIDE_ENCHANTS (свечение).
- Прочность: случайная `min_durability..max_durability` (1..10); хранится в PDC-ключе `eventpolana:shears` (INTEGER); ванильная прочность выставляется как `maxDurability − остаток` (инверсия, «обратный износ»); при каждом срыве PCT-прочность −1, первая строка лора «Прочность: N» обновляется; при 0 — предмет удаляется из инвентаря.
- Идентификация: тип SHEARS + наличие PDC-ключа.

### 4.7. Сбор цветов (PolanaListeners.onBlockBreak)
Условия: state == SPAWNED; мир совпадает; блок в радиусе **35** блоков (захардкожено, фактически = `flower_break_radius`) от центра; `FlowerManager.isFlower(тип)` (POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, CORNFLOWER).
- В руке кастомные ножницы → иначе **событие отменяется** (руками/другим инструментом цветок не сломать).
- Прочность ножниц −1 (при 0 — удалить), счётчик цветов −1.
- Лут: `LootManagerBridge.dropRandomLoot(локация блока, lootLevel)` — `LootAPI.getRandomItems(1, level)`, предметы дропаются на землю (`dropItemNaturally` +0.5/+0.5/+0.5), звук `sounds.loot_drop`.
- Частицы `particles.flower_break` (тип/count/offsets) в центре блока; звук `sounds.flower_break` (pitch 1.5).
- Блок ставится в AIR.

### 4.8. Выдача ножниц (ПКМ по садовнику, PlayerInteractEntityEvent)
- Событие отменяется.
- Если ножницы уже взяты (`shearsTaken`) — «отброс»: вектор от садовника ×(5 + rnd 0..5), Y 0.1, `damage(8.0)`, звук `ENTITY_VILLAGER_NO`.
- Иначе: в инвентарь кладутся новые кастомные ножницы, звук `sounds.shears_given`, `setShearsTaken(true)` (садовник теряет ножницы в руке, голограмма меняется), планируется возврат через `respawn_shears.<редкость>` секунд (`setShearsTaken(false)`).
- Урон по садовнику (`EntityDamageEvent`) всегда отменяется.

### 4.9. Спавн цветов (FlowerManager)
- Задача с периодом `spawn_flower.<редкость>` (20/20/20 с) в радиусе `event.flower_spawn_radius` (25).
- За тик задачи: если текущее число цветов < лимита (`flowers.max_concurrent_flowers`, default 100 — ключа в поставленном config.yml нет), спавнится батч `1 + rnd(0..49)` (не больше лимита), до `6 × батч` попыток размещения.
- Условия размещения: `world.getHighestBlockYAt(x,z)` — GRASS_BLOCK или DIRT, блок выше — воздух; ставится случайный цветок из 10 (physics off), счётчик +1.

### 4.10. Регион WorldGuard `polana_event`
- Кубоид ±`flower_break_radius` (35) X/Z, Y −1..300.
- Флаги: `holyliteitems = DENY` (кастомный флаг), `BUILD = DENY`, `BLOCK_BREAK = DENY`, `PVP = ALLOW`, `BLOCK_PLACE = DENY`, `INTERACT = ALLOW`, `WITHER_DAMAGE = DENY`, `CHEST_ACCESS = DENY`, `GHAST_FIREBALL = DENY`, `TNT = DENY`, `OTHER_EXPLOSION = DENY`, `CREEPER_EXPLOSION = DENY`.
- Итог: на поляне PvP разрешён, строительство/ломание блоков (ванильно) запрещено — цветы ломаются через отменяемый BlockBreakEvent только ножницами.

### 4.11. Анонс и статус `/event`
- `announceSpawn`: broadcast-блок (рамка, заголовок `messages.spawn.title`, «Редкость: <имя редкости>», «Появилась на координатах», строка `messages.spawn.coordinates_format` с `{x} {y} {z}`).
- В реестр EventManager кладётся ключ `polana`: title + строка «§a▶ <title> &fнаходится на координатах §a X Y Z» (захардкожена, не из messages).
- `removePolana`: удаление ключа `polana`.

### 4.12. Завершение
- Авто: задача `runTaskLater` на `1200 тиков × минуты` (`durations.delete.<редкость>`) → `removePolana()`.
- Ручное: `/polana remove` или `onDisable`.
- `removePolana`: голограмма удалена, регион удалён, откат схематики (`undoLastPaste`), садовник удалён, спавн цветов остановлен, задачи отменены, ключ `polana` удалён, state = NOT_SPAWNED. Broadcast-итогов нет.

### 4.13. Обрабатываемые события
- `EntityDamageEvent` (садовник), `PlayerInteractEntityEvent` (ПКМ садовник), `BlockBreakEvent` (цветы), команда `/polana`.

---

## 5. Конфигурация (config.yml)

### 5.1. `event.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `world` | string | `world` | мир ивента |
| `schematic_name` | string | `event_polana.schem` | имя файла схемы (dataFolder → plugins/WorldEdit/schematics) |
| `flower_spawn_radius` | int | 25 | радиус спавна цветов вокруг центра |
| `flower_break_radius` | int | 35 | радиус региона (сбор — 35 захардкожен в листенере) |

### 5.2. `gardener.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `player_search_radius` | double | 50.0 | радиус поиска игрока для «взгляда» |
| `look_update_interval` | int | 10 | период взгляда, тиков |
| `profession` | string | `FARMER` | профессия жителя (validateGardener игнорирует) |
| `villager_type` | string | `PLAINS` | тип жителя |
| `level` | int | 5 | уровень торговли (визуальный) |

### 5.3. `shears.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `min_durability` | int | 1 | минимальная случайная прочность |
| `max_durability` | int | 10 | максимальная случайная прочность |
| `display_name` | string | `&6Ножницы` | имя предмета |
| `description` | list[string] | лор с `{durability}` | лор (при пустой секции — встроенный лор по умолчанию) |

### 5.4. Прочее
| Параметр | Тип | Default (файл) | Default (код) | Описание |
|---|---|---|---|---|
| `durations.delete.default/epic/legendary` | int | 15/20/25 | 10/20/30 | минуты авто-удаления по редкости |
| `respawn_shears.default/epic/legendary` | int | 60/45/35 | 30/20/10 | секунды до возврата ножниц |
| `spawn_flower.default/epic/legendary` | int | 20/20/20 | 30/20/10 | период спавна цветов, с |
| `loot.default/epic/legendary` | int | 1/2/3 | 1/2/3 | уровень лута LootManager |
| `flowers.max_concurrent_flowers` | int | (нет в файле) | 100 | лимит одновременно существующих цветов |
| `messages.errors.schematic_not_found` | string | (нет в файле) | «&cНе удалось загрузить схематику поляны!» | лог при неудаче схемы |

### 5.5. `messages.spawn.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `title` | string | `&x&4&3&B&C&0&6&lЦветочная поляна` | заголовок анонса и записи `/event` |
| `rarity_names.default` | string | HEX «Обычная» | имя редкости в анонсе |
| `rarity_names.epic` | string | HEX «Эпическая» | — |
| `rarity_names.legendary` | string | HEX «Легендарная» | — |
| `coordinates_format` | string | `&fX: …{x} &fY: …{y} &fZ: …{z}` | формат координат (плейсхолдеры `{x} {y} {z}`) |

### 5.6. `sounds.*`, `particles.*`
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `sounds.shears_given` | string | `ENTITY_VILLAGER_YES` | звук выдачи ножниц (fallback — тот же) |
| `sounds.flower_break` | string | `BLOCK_GRASS_BREAK` | звук сбора цветка (fallback BLOCK_GRASS_BREAK) |
| `sounds.loot_drop` | string | `ENTITY_ITEM_PICKUP` | звук дропа лута (fallback ENTITY_ITEM_PICKUP) |
| `particles.flower_break.type` | string | `VILLAGER_HAPPY` | тип частиц (fallback HAPPY_VILLAGER) |
| `particles.flower_break.count` | int | 10 | количество частиц |
| `particles.flower_break.offset_x/y/z` | double | 0.3 | разброс |

---

## 6. Хранение данных
- Постоянного хранения нет. Состояние (позиция, редкость, shearsTaken, EditSession, счётчик цветов) — в памяти; при выключении поляна откатывается, невыданные ножницы у игроков остаются (это обычные предметы).

---

## 7. Интеграции
- **EventManager** — ключ `polana` (title + координаты; добавляется при спавне, удаляется при завершении).
- **LootManager** — `LootAPI.getRandomItems(1, level)` (через LootManagerBridge).
- **WorldEdit** — схематика (sync вставка/undo).
- **WorldGuard** — регион `polana_event` + флаг `holyliteitems`.
- **DecentHolograms** — голограмма садовника (id `gardener_hologram_<millis>`).
- PlaceholderAPI / Vault — не используются.

---

## 8. Сценарий работы (основной поток)
1. Подготовка: EventManager (голосование, конфиг `items.svetpolana.commands` → `polana spawn`) или `/polana spawn [редкость]`.
2. Поиск точки (async, до 50 попыток × 100 ретраев; суша, плоскость, твёрдое основание).
3. Старт: вставка схемы, регион, садовник + голограмма «Бери ножницы», анонс с координатами и редкостью, статус `/event`, очистка барьеров, планирование авто-удаления (15/20/25 мин).
4. Процесс: задача спавнит цветы (батчи до 50, лимит 100); первый ПКМ-нувший садовника получает ножницы (голограмна меняется на «Жди…»); игроки срывают цветы ножницами — дроп лута уровня редкости, прочность ножниц тратится; ножницы «отрастают» через 60/45/35 с; повторные ПКМ до отрастания — отброс + 8 урона; на поляне PvP.
5. Завершение: таймер авто-удаления, `/polana remove` или выключение сервера.
6. Очистка: голограмма, регион, откат схемы, удаление садовника, остановка задач, удаление ключа `/event`.

---

## 9. Особенности реализации и известные ограничения
- `PolanaCommand` инстанцируется дважды (executor и tabCompleter — разные объекты); plugin.yml права не содержит (`eventpolana.admin` работает только как непровозглашённое право = op по умолчанию).
- Дефолты кода и config.yml расходятся (`durations.delete`, `respawn_shears`, `spawn_flower`): при наличии файла действуют значения файла.
- Радиус сбора 35 захардкожен в листенере и совпадает с `flower_break_radius` лишь по умолчанию; изменение ключа конфига изменит только регион.
- `validateGardener` жёстко перезаписывает профессию/тип/уровень (FARMER/PLAINS/5), игнорируя `gardener.*` (конфиг действует только при первом спавне).
- Имена частиц/звуков в config.yml (`VILLAGER_HAPPY`, `BLOCK_GRASS_BREAK`) — старые имена до 1.20.5; на 1.21.4 `valueOf` бросает IllegalArgumentException и срабатывает fallback (`HAPPY_VILLAGER`, `BLOCK_GRASS_BREAK`→fallback тоже устаревший — фактически звук может не сыграть; fallback для звука сбора тоже BLOCK_GRASS_BREAK, который в 1.21.4 невалиден).
- Схематика вставляется синхронно в главном потоке (крупная схема — фриз), поиск точки — асинхронный (обращения к `getHighestBlockYAt` из async-потока — потенциально небезопасно).
- `scheduleEventRemoval` использует `1200L * minutes` тиков = корректные минуты (60 с × 20 тиков).
- `RegionBarrierCleaner` — радиус 150 и батч 3000 захардкожены (в EventDeathMine аналогичное класс configurable).
- Лут дропается на землю — в PvP-зоне его могут подобрать другие игроки.
- Счётчиков/топа игроков нет; итогового broadcast нет.
- `TextUtil.colorize` без null-guard (NPE при null-строке — на практике вызовы с константами).

---

## 10. Сборка
```
mvn -pl modules/EventPolana -am package   # из корня репозитория
```
- Зависимости `local.plugins:*` (worldedit, worldguard, decentholograms) должны быть в локальном Maven-репозитории.
- `EventManager`, `LootManager` — модули того же reactor (собираются с `-am`).
- Shade отсутствует; jar без зависимостей.
