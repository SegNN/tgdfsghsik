# Техническое задание — модуль EventContainer

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/EventContainer`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | EventContainer |
| Артефакт (Maven) | `EventContainer` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `org.dimasik.eventcontainer.EventContainer` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | не указаны |

**Назначение:** под-ивент «Контейнер» для системы EventManager. По команде (обычно —
из голосования EventManager, команда `econtainer spawn <тип>`) в случайной точке мира
`world` появляется парящая схема-контейнер (WorldEdit-схематика) с голограммой
(DecentHolograms) и PvP-регионом WorldGuard. Через 3 минуты контейнер «открывается»:
первый игрок, кликнувший по блоку-контейнеру, получает анимированное GUI, которое
по одному выдаёт ему случайные предметы (LootManager); за каждым изъятием уровень
лута растёт. Когда предметы заканчиваются (или время истекает) — контейнер исчезает,
в чат выводится топ-5 игроков по числу унесённых предметов, статус из `/event` удаляется.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot API 1.16+ (собирается против `paper-api:1.21.4-R0.1-SNAPSHOT`, Java 21).

### 2.2. Плагины
| Плагин | Версия (pom) | Scope | Обязательность | Использование |
|---|---|---|---|---|
| WorldEdit | 7.3.16 (`local.plugins:worldedit`) | provided | По коду — обязательно (вставка/откат схематики) | `EditSession`, `Clipboard`, paste/undo |
| WorldGuard | 7.0.13 (`local.plugins:worldguard`) | provided | По коду — обязательно | регион `container_event` и флаги |
| DecentHolograms | 2.10.1 (`local.plugins:decentholograms`) | provided | По коду — обязательно | голограмма над контейнером |
| EventManager | 1.0-SNAPSHOT (`world.example`) | provided | По коду — обязательно | реестр `events` (ключ `container`), `/event` |
| LootManager | 1.0 (`world.example`) | provided | По коду — обязательно | `LootAPI.getRandomItems(count, level)` |

**Важно:** в `plugin.yml` depend/softdepend **не объявлены** — фактический порядок загрузки не гарантируется; при отсутствии плагинов на сервере — ошибки в рантайме.

### 2.3. Библиотеки
- `org.jetbrains:annotations:24.1.0` (provided).

### 2.4. Ресурсы на диске
- Схематики: `plugins/WorldEdit/schematics/container_default.schem` (тип DEFAULT) и
  `plugins/WorldEdit/schematics/container_epic.schem` (тип EPIC). Отсутствие файла — ивент не стартует (warning в лог).

---

## 3. Архитектура (структура пакетов)

```
org.dimasik.eventcontainer
├── EventContainer      — главный класс: singleton, ContainerManager (листенер),
│                         команда /econtainer (внутренний класс GruzCommand),
│                         статический ContainerHolder; onDisable → removeContainer()
├── ContainerManager    — вся логика ивента: состояние (ContainerState), выбор точки,
│                         вставка схематики (WorldEdit), регион WorldGuard, голограмма
│                         (DecentHolograms), таймеры фаз, обработка кликов по блоку,
│                         учёт предметов по игрокам, топ-5 и очистка; enums:
│                         ContainerState {NOT_SPAWNED, WAITING, OPENED, REMOVING} и
│                         ContainerType {DEFAULT, EPIC}
├── ContainerMenu       — анимированное GUI выдачи (27 слотов, стеклянные панели,
│                         посекционное «прокручивание» предмета, звуки, апгрейд уровня,
│                         складирование предмета игроку или дроп на землю)
└── ContainerHolder     — InventoryHolder-маркер для инвентарей ивента
```

---

## 4. Функциональные требования

### 4.1. Команда `/econtainer` (внутренний класс `EventContainer.GruzCommand`)
Право на команду: `container.use` (задаётся в plugin.yml на всю команду, алиасов нет).
| Синтаксис | Поведение |
|---|---|
| `/econtainer` | «Использование: /econtainer [spawn/remove/settimer/setitems]» |
| `/econtainer spawn <default\|epic>` | Запустить ивент указанного типа (некорректный тип → повтор usage). Если уже создан — в лог «Контейнер уже создан!» (отправителю молча) |
| `/econtainer settimer <секунды>` | Перезапустить таймер текущей фазы на новое время |
| `/econtainer setitems <количество>` | Установить остаток предметов `manager.items` (может как увеличить, так и уменьшить запас) |
| `/econtainer remove` | Немедленно удалить контейнер с подведением итогов |

### 4.2. Права (permissions)
| Право | Где объявлено | Назначение |
|---|---|---|
| `container.use` | plugin.yml (команда `econtainer`) | Доступ ко всей команде (default-группа не указана → стандартно true только для op) |

Иных прав нет; GUI и клики игроками правами не ограничены.

### 4.3. Старт ивента (`start`)
1. Требуется состояние `NOT_SPAWNED`; мир — жёстко `world` (иначе warning и отмена).
2. Точка: `x = rnd(-2500..2500)`, `z = rnd(-2500..2500)`, `y = world.getHighestBlockYAt(x,z)`, затем `+0.5 / +8.5 / +0.5` — контейнер парит ~8,5 блоков над землёй.
3. Запас предметов: `maxitems = rnd(33..64)`, `items = maxitems`.
4. Схематика `container_default.schem` / `container_epic.schem` вставляется WorldEdit-ом (`ignoreAirBlocks(true)`); `EditSession` сохраняется для отката.
5. Создаётся регион WorldGuard `container_event` (см. 4.6).
6. Broadcast-блок: ASCII-рамка, заголовок «Контейнер», строка редкости («Обычный» для DEFAULT / «Эпический» для EPIC), координаты X/Y/Z.
7. `displayName` = HEX-строка «Контейнер», `color` = HEX `&x&5&2&4&D&F&F`.
8. Голограмма + запись в EventManager; таймер фазы WAITING = **180 с** (захардкожено `updateTimer(180)`).

### 4.4. Жизненный цикл (состояния)
| Состояние | Переход | Что происходит |
|---|---|---|
| `NOT_SPAWNED` | `/econtainer spawn` | см. 4.3 |
| `WAITING` (180 с) | тик таймера → 0 | `handleContainerOpen()`: state=OPENED, обновление голограммы |
| `OPENED` | клик по блоку контейнера | один игрок («открыватель») получает `ContainerMenu` |
| `OPENED` → `REMOVING` | предметы `items <= 0` (из GUI) | `startRemovingContainer()`: отсчёт 180 с → `removeContainer()` |
| любой | `/econtainer remove`, `onDisable` | `removeContainer()` |

Таймер: задача раз в секунду пишет `containerTime`, обновляет голограмму и строку `/event`; `settimer <сек>` перезапускает задачу с новым значением (действует на текущую фазу).

### 4.5. Взаимодействие с контейнером
- `PlayerInteractEvent` по блоку в точке `containerLocation` (сравнение по X/Y/Z и миру): событие отменяется всегда (защита блока); при состоянии OPENED — `handleClick`:
  - если `currentOpener` пуст → игрок становится открывателем, получает `PotionEffect GLOWING` (1200 тиков = 60 с, без частиц/иконки), открывается `ContainerMenu` (level 1 для DEFAULT, 2 для EPIC);
  - иначе сообщение «▶ Контейнер уже открывает <имя>».
- `InventoryCloseEvent` (инвентарь с `ContainerHolder`) → `currentOpener = ""` (контейнер снова можно открывать).
- `InventoryClickEvent` в этом инвентаре — отмена (предметы нельзя двигать руками).

### 4.6. Регион WorldGuard `container_event`
- Кубоид ±40 блока по X/Z от точки, Y 0..255 (при отрицательных координатах точки — сдвиг +1).
- Флаги: `USE = DENY`, `PVP = ALLOW`, `holyliteitems = DENY` (кастомный флаг сервера, ищется в реестре по имени; см. модуль HolyLiteItems), `CHEST_ACCESS = DENY`, `TNT = DENY`, `OTHER_EXPLOSION = DENY`, `FIRE_SPREAD = DENY`, `LAVA_FIRE = DENY`.
- Итог: на территории контейнера разрешён PvP, запрещены использование блоков/сундуков, ТНТ/взрывы/огонь и «светящиеся предметы» HolyLiteItems.

### 4.7. GUI выдачи (`ContainerMenu`)
- Инвентарь 27 слотов «Контейнер» (holder `ContainerHolder`), залит чёрными стеклянными панелями.
- Задача каждый тик работает с колонкой слотов 9..17 (средняя строка), поочерёдно:
  - на тиках 0/3/7/12/19/27/35 — звук `UI_BUTTON_CLICK` (pitch 2.0; на тике 0 ещё `ITEM_BOOK_PAGE_TURN`) и смена предмета в текущем слоте: `new LootAPI().getRandomItems(1, level + itemlevel/3)` (level = 1 или 2 по типу контейнера);
  - слоты выше (`slot-9`) и ниже (`slot+9`) текущего — «дизайн-панель», цвет которой отражает `itemlevel`: 1 — BLACK, 2 — BLUE, 3 — ORANGE, 4 — RED (стеклянные панели);
  - на тике 50 — звук `ENTITY_FIREWORK_ROCKET_BLAST`, предмет **окончательно выдаётся** в инвентарь игрока (стакается/кладётся в пустой слот, иначе дропается на землю у игрока), с шансом 1/4 `itemlevel` повышается (макс 4), счётчик игрока `playerItems+1`, общий `items-1`, голограмма/`/event` обновляются, переход к следующему слоту;
  - условие остановки: `items <= 0` (→ закрыть GUI, `startRemovingContainer()`), `slot >= 18`, состояние перестало быть OPENED, игрок вышел/закрыл инвентарь.
- `setitems <n>` меняет `items` на лету (может «продлить» ивент).

### 4.8. Завершение и итоги (`removeContainer`)
- Откат схематики (`editSession.undo`), удаление региона `container_event`, удаление голограммы, отмена таймера.
- Broadcast: «Ивент <Имя> был завершен. Топ игроков, которые унесли больше всего предметов…» и 5 строк вида `#N <игрок> — <кол-во> предметов` (сортировка по убыванию; недостающие места — `---`).
- Из реестра EventManager удаляется ключ `container`; `containerLocation = null`; state = NOT_SPAWNED.
- `onDisable` плагина вызывает то же самое (топ выводится и при выключении сервера).

### 4.9. Голограмма и статус `/event`
- Голограмма DecentHolograms с именем `container_X_Y_Z` в точке `containerLocation +1.5`:
  - строка 0 — displayName («Контейнер»);
  - строка 1 по фазе: WAITING — «Будет доступен через M мин., S сек.»; OPENED — «Осталось предметов: [ items / maxitems ]»; REMOVING — «Будет удален через M мин., S сек.».
- В реестр `EventManager.events` по ключу `container` кладётся `Event` с координатами и статусом текущей фазы (обновляется теми же сообщениями, что и голограмма).

### 4.10. Обрабатываемые события
- `InventoryClickEvent`, `InventoryCloseEvent`, `PlayerInteractEvent` (ContainerManager);
- команды `/econtainer`.

---

## 5. Конфигурация
Конфигурационных файлов нет (`config.yml` отсутствует, `saveDefaultConfig` не вызывается).
Все параметры захардкожены в коде:

| Параметр | Значение в коде |
|---|---|
| Мир | `world` |
| Разброс точки спавна | x, z ∈ [-2500, 2500] |
| Высота парения | highestY + 8.5 |
| Запас предметов | 33..64 (случайно) |
| Таймеры фаз WAITING / REMOVING | 180 с / 180 с |
| Схематики | `plugins/WorldEdit/schematics/container_default.schem`, `container_epic.schem` |
| Регион | `container_event`, ±40, Y 0..255 |
| Название инвентаря GUI | «Контейнер» |
| Уровни лута | DEFAULT → 1, EPIC → 2 (+ itemlevel/3 при апгрейдах) |
| Шанс апгрейда itemlevel | 1/4 за выданный предмет, максимум 4 |
| Свечение открывателя | GLOWING 60 с |

---

## 6. Хранение данных
- Постоянного хранения нет. Состояние (позиция, тип, счётчики `playerItems`, запас `items`) — только в памяти; при рестарте сервера `onDisable` удаляет контейнер, прогресс теряется.

---

## 7. Интеграции
- **EventManager** — реестр активных ивентов (ключ `container`), сообщения `/event`.
- **LootManager** — `LootAPI.getRandomItems(count, level)` для предметов контейнера.
- **WorldEdit** — вставка/откат схематики.
- **WorldGuard** — временный PvP-регион `container_event`, включая кастомный флаг `holyliteitems`.
- **DecentHolograms** — голограмма статуса.
- PlaceholderAPI / Vault / PlayerPoints — не используются.

---

## 8. Сценарий работы (основной поток)
1. Подготовка: EventManager выигрывает голосование → консоль выполняет `econtainer spawn default|epic` (или админ вручную).
2. Старт: случайная точка, вставка схематики, регион, голограмма, broadcast с координатами; статус в `/event`.
3. Ожидание (3 минуты): голограмма/`/event` показывают время до открытия; игроки собираются, на территории PvP.
4. Открытие: state=OPENED; первый кликнувший по блоку получает свечение и GUI.
5. Процесс: GUI каждые ~50 тиков выдаёт один случайный предмет (уровень лута растёт с каждым предметом); запас уменьшается; при переполнении инвентаря — дроп на землю.
6. Завершение: `items == 0` → фаза REMOVING (180 с) → либо сразу `/econtainer remove`.
7. Награды/итоги: откат схемы, удаление региона и голограммы, broadcast топ-5 «унесших больше всего предметов», удаление статуса из `/event`.

---

## 9. Особенности реализации и известные ограничения
- **plugin.yml не объявляет depend** ни для одного из 5 фактически обязательных плагинов — при неправильном порядке загрузки (`getInstance() == null`, отсутствие флага `holyliteitems`) возможны NPE/тихие отказы.
- `container.use` — единственное право; подкоманды между собой не разграничены (любой владелец права может и спавнить, и удалять).
- Мир жёстко `world`; таймеры фаз и запас предметов не конфигурируются.
- `updateTimer` стартует новую задачу только после отмены старой — двойных таймеров нет; но `settimer` действует на текущую фазу и не сбрасывает состояние.
- GUI «прокручивает» предметы по жёсткой сетке тиков (0/3/7/12/19/27/35/50) — итоговая длительность одного слота фиксирована (~2,5 с); игрок не может ускорить выдачу.
- `addItemInventory` при отсутствии места дропает предмет на землю рядом с игроком — предметы могут доставаться другим игрокам (на территории разрешён PvP).
- Счёт `playerItems` ведётся по имени игрока (String), не по UUID.
- `InventoryCloseEvent` освобождает `currentOpener`, но уже открытая задача `ContainerMenu` не проверяет смену открывателя — теоретически два меню при гонке кликов (закрытие/открытие в один тик).
- Hex-цвета обрабатываются `ChatColor.translateAlternateColorCodes` (формат `&x&R&R&G&G&B&B`).

---

## 10. Сборка
```
mvn -pl modules/EventContainer -am package   # из корня репозитория
```
- Зависимости `local.plugins:*` (worldedit, worldguard, decentholograms) не объявлены ни в одном repository корневого pom — они должны быть предварительно установлены в локальный Maven-репозиторий.
- `EventManager` и `LootManager` — модули того же reactor (собираются при `-am`).
- Shade не используется; jar не содержит зависимостей.
