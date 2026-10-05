# Техническое задание — модуль LiteBinds

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/LiteBinds`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | LiteBinds |
| Артефакт (Maven) | `LiteBinds` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `org.dimasik.litebinds.LiteBinds` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | nezxenka, Dimasik201O (aka sh4dowgirl) |
| Команды | `/binds` |

**Назначение:** «бинды» — переназначение действий с незеритовым мечом. Игрок в GUI-меню
назначает, какое из семи игровых предметов активируется при: (Q) выбрасывании меча,
(F) взятии меча во вторую руку, (ПКМ) клике с мечом в руке. При срабатывании события
событие отменяется, и плагин эмулирует использование нужного предмета из инвентаря
(запуск снежка HolyLiteItems, «Джек», альтернативная ловушка, Стэн, ловушка, взрывное,
рюкзак 4 уровня HolyBackPack). Выборы хранятся в SQLite/MySQL.

---

## 2. Окружение и зависимости

### 2.1. Сервер
- Paper (используется Paper-событие `com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent`)
  с поддержкой API 1.16+. Java 21 (родительский pom).

### 2.2. Плагины (мягкие, определяются через `Bukkit.getPluginManager().getPlugin(...)`,
в plugin.yml не объявлены)
| Плагин | Нужен для | Поведение при отсутствии |
|---|---|---|
| HolyLiteItems | PDC-ключ `item-type` на предметах (snowball, jake, alternative_trap, stan, explosive_trap, explosive_stuff) | действие молча не выполняется (return) |
| HolyBackPack | PDC-ключ `backpack-level` (значение 4) | действие BACKPACK молча не выполняется |

### 2.3. Библиотеки
- `com.zaxxer:HikariCP:4.0.3` (compile, шейдится в jar).
- `org.projectlombok:lombok:1.18.32` (provided), `org.jetbrains:annotations:24.1.0` (provided).
- Драйверы БД: подключается `com.mysql.cj.jdbc.Driver` либо `org.sqlite.JDBC` — в зависимостях
  pom не значатся, ожидаются на сервере/в classpath (см. §9).

---

## 3. Архитектура (структура пакетов)

```
org.dimasik.litebinds
├── LiteBinds                            — главный класс: init БД, команда, слушатели;
│                                          onDisable — закрыть меню типа Menu у всех, закрыть пул БД
├── command.CommandExecutor              — /binds (TabExecutor): открывает Menu; tab всегда пустой
├── database
│   ├── ActionType                       — enum действий: NONE, SNOWBALL, JAKE, ALTERNATIVE_TRAP,
│   │                                      STAN, TRAP, EXPLOSIVE, BACKPACK
│   ├── DatabaseManager                  — HikariCP (SQLite/MySQL), CRUD таблицы player_actions,
│   │                                      все операции асинхронные (CompletableFuture)
│   └── PlayerActions                    — POJO (lombok): player, actionDrop, actionSwap, actionInteract
├── listeners.EventListener              — игровые события (Q/F/ПКМ по незеритовому мечу),
│                                          таймер последнего предмета в руке, trigger() с логикой действий
├── menu
│   ├── abst.AbstractMenu                — база меню (InventoryHolder, compile/open, setPlayer)
│   ├── abst.AbstractListener            — базовый слушатель меню (onClick/onClose, self-register)
│   ├── listeners.MenuListener           — клики по Menu: ЛКМ/ПКМ циклически двигают привязку
│   │                                      товара по трём слотам (Q→F→ПКМ→нет), слот 22 — сброс всех
│   └── menus.Menu                       — само меню «Горячие клавиши» (27 слотов, слоты 10–16 — товары,
│                                          22 — «Выключить всё»)
└── utils.Parser                         — цвета &#hex + &-коды, кликабельные сообщения, формат
                                           чисел/дат; определение RGB-поддержки по версии сервера
```

---

## 4. Функциональные требования

### 4.1. Команда
| Команда | Право | Поведение |
|---|---|---|
| `/binds` | не задано (доступна всем) | Только для игроков: открывает меню «Горячие клавиши» (`new Menu().setPlayer(player).compile().open()`). Tab-дополнения отсутствуют (пустой список) |

### 4.2. Права (permissions)
В plugin.yml права не объявлены; в коде проверки прав нет — команда доступна всем игрокам.

### 4.3. Обрабатываемые события
| Событие | Условие срабатывания | Действие |
|---|---|---|
| `PlayerDropItemEvent` | выброшенный предмет совпадает (`isSimilar`) с последним удерживаемым и это `NETHERITE_SWORD` | выполняется привязка `action_drop` |
| `PlayerSwapHandItemsEvent` | предмет, уходящий в офф-хенд (`getOffHandItem`), — `NETHERITE_SWORD` | выполняется привязка `action_swap` |
| `PlayerInteractEvent` | `RIGHT_CLICK_AIR`/`RIGHT_CLICK_BLOCK`, `event.getItem()` — `NETHERITE_SWORD` | выполняется привязка `action_interact` |
| `InventoryClickEvent` | верхний инвентарь — Menu | логика меню (4.5) |
| `InventoryCloseEvent` | у AbstractListener — пустая реализация | — |

Для `PlayerDropItemEvent` слушатель ведёт `Map<UUID, ItemStack> lastHeldItems`: таймер
каждый тик (runTaskTimer с периодом 1, отложено на 1 тик) запоминает предмет в главной руке
каждого онлайн-игрока — так проверяется, что выбросили именно удерживаемый меч.

### 4.4. Действия (ActionType → поведение trigger())
Все действия сначала ищут в инвентаре предмет с нужным PDC-тегом; событие отменяется
(`setCancelled(true)`) при найденном плагине-источнике. Если плагин HolyLiteItems/HolyBackPack
отсутствует — действие пропускается, событие НЕ отменяется.

| ActionType | Что ищет в инвентаре | Что происходит |
|---|---|---|
| `SNOWBALL` | PDC `holyliteitems:item-type` = `snowball` | Запускается снаряд `Snowball` с иконкой предмета, вручную вызывается `PlayerLaunchProjectileEvent`; если не отменено — расходуется 1 шт. |
| `JAKE` | `item-type` = `jake` | Синтеcraft `PlayerInteractEvent(RIGHT_CLICK_AIR)` с этим предметом (обрабатывает HolyLiteItems) |
| `ALTERNATIVE_TRAP` | `item-type` = `alternative_trap` | аналогично JAKE |
| `STAN` | `item-type` = `stan` | аналогично JAKE |
| `TRAP` | `item-type` = `explosive_trap` | аналогично JAKE |
| `EXPLOSIVE` | `item-type` = `explosive_stuff` | аналогично JAKE |
| `BACKPACK` | PDC `holybackpack:backpack-level` (INTEGER) = 4 | `PlayerInteractEvent(RIGHT_CLICK_AIR)` с рюкзаком 4 уровня |
| `NONE` | — | ничего (кейс default в switch отсутствует) |

### 4.5. GUI «Горячие клавиши» (Menu, 27 слотов)
- Слоты 10–16 — семь товаров (иконка, HEX-градиентное имя, лор с тремя строками-режимами
  «Выбросить предмет [Q] / Вторая рука [F] / Использовать [ПКМ]», активный режим выделен
  цветом `&6●`, прочие приглушены; пояснение, при каком действии с мечом активируется предмет):

| Слот | ActionType | Материал | Отображаемое имя |
|---|---|---|---|
| 10 | SNOWBALL | SNOWBALL | Снежка (градиентное имя) |
| 11 | JAKE | JACK_O_LANTERN | Снежный Джейк |
| 12 | ALTERNATIVE_TRAP | POPPED_CHORUS_FRUIT | Трапка |
| 13 | STAN | NETHER_STAR | Стэн |
| 14 | TRAP | PRISMARINE_SHARD | Взрывная трапка |
| 15 | EXPLOSIVE | FIRE_CHARGE | Взрывная штучка |
| 16 | BACKPACK | MAGENTA_SHULKER_BOX | Рюкзак (IV уровень) |

- Слот 22 — красная панель «Выключить всё» (зачарование SOUL_SPEED 1 + HIDE_ENCHANTS):
  сбрасывает все три привязки в NONE и перерисовывает меню.
- ЛКМ по товару — «следующий» цикл: слот сдвигается вперёд по цепочке (drop → swap →
  interact → NONE → на первое свободное из drop/swap/interact; детальная логика
  перестановок в `MenuListener.relative(next=true)`).
- ПКМ по товару — «назад»: привязка перемещается в предыдущий слот
  (interact → swap → drop → NONE; либо назначается в последний свободный).
- Все клики по верхнему инвентарю отменяются; клики по нижнему (свой инвентарь) — нет.
- Клик по товару, который уже где-то привязан, перемещает его между слотами действий;
  клик по свободному товару добавляет его на первое (ЛКМ) / последнее (ПКМ) свободное
  действие. Один и тот же товар не может занимать два действия.
- После каждого изменения меню пересобирается (async запись → runTask → compile().open()).

### 4.6. Формат сообщений
Плагин не отправляет игроку сообщений в чат — вся обратная связь через GUI.

---

## 5. Конфигурация (config.yml)
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `database.type` | string | `sqlite` | Тип БД: `sqlite` или `mysql` (сравнение `equalsIgnoreCase("mysql")`) |
| `mysql.host` | string | `localhost` | Хост MySQL |
| `mysql.port` | int | `3306` | Порт MySQL |
| `mysql.user` | string | `root` | Пользователь MySQL |
| `mysql.password` | string | `""` | Пароль MySQL |
| `mysql.database` | string | `lite_binds` | Имя базы MySQL |
| `sqlite.file` | string | `plugins/LiteBinds/binds.db` | Путь к файлу SQLite |

Параметров поведения (названия товаров, состав действий) в конфиге нет — они зашиты
в `Menu` и `EventListener`.

---

## 6. Хранение данных

### 6.1. Схема (создаётся автоматически `CREATE TABLE IF NOT EXISTS`)
MySQL:
```sql
player_actions (
  player VARCHAR(16) PRIMARY KEY,
  action_drop   VARCHAR(32) NOT NULL DEFAULT 'NONE',
  action_swap   VARCHAR(32) NOT NULL DEFAULT 'NONE',
  action_interact VARCHAR(32) NOT NULL DEFAULT 'NONE'
)
```
SQLite: то же, но колонки TEXT. Ключ — **ник игрока** (не UUID).

### 6.2. Операции DatabaseManager (все CompletableFuture, пул MySQL 10 / SQLite 1 соединение)
- `savePlayerActions` — upsert всей строки (`ON DUPLICATE KEY UPDATE` / `ON CONFLICT DO UPDATE`);
- `getPlayerActions` — SELECT по нику;
- `deletePlayerActions`, `playerExists`;
- `updateActionDrop/Swap/Interact` — upsert отдельной колонки (для MySQL вставляет только
  нужную колонку, для SQLite — вставляет с NONE в остальных).

HikariCP-настройки: MySQL — cachePrepStmts/prepStmtCacheSize/useServerPrepStmts/
rewriteBatchedStatements и др.; SQLite — journal_mode WAL, busy_timeout 5000,
synchronous NORMAL, cache_size 10000, temp_store MEMORY, mmap_size 256 MiB.
Адрес MySQL: `jdbc:mysql://host:port/db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.

---

## 7. Интеграции
- **HolyLiteItems** — распознавание предметов по NamespacedKey `item-type` (значения:
  snowball, jake, alternative_trap, stan, explosive_trap, explosive_stuff) и обработка
  эмулированных `PlayerInteractEvent`.
- **HolyBackPack** — распознавание рюкзака по ключу `backpack-level` == 4.
- PlaceholderAPI/Vault/PlayerPoints/LuckPerms не используются.
- Paper API — событие `PlayerLaunchProjectileEvent` (запуск снежки).

---

## 8. Сценарий работы
1. Игрок выполняет `/binds` → открывается меню «Горячие клавиши» с текущими привязками из БД.
2. Игрок ЛКМ-кликает «Снежка» → она назначается на действие «Выбросить предмет [Q]»
   (первое свободное), меню перерисовывается, выбор асинхронно сохранён.
3. Игрок ЛКМ по «Снежка» ещё раз — привязка сдвигается на «Вторая рука [F]», затем на
   «Использовать [ПКМ]», затем снимается.
4. Игрок с незеритовым мечом в руке нажимает ПКМ по воздуху → PlayerInteractEvent:
   предмет — NETHERITE_SWORD, срабатывает action_interact.
5. EventListener.trigger: ищет в инвентаре предмет с `item-type: stan`; событие отменяется,
   вызывается синтетический PlayerInteractEvent с этим предметом — HolyLiteItems исполняет
   способность Стэна.
6. Игрок бросает меч клавишей Q → PlayerDropItemEvent (предмет = последний удерживаемый
   NETHERITE_SWORD) → action_drop → например запуск снаряда-снежки с расходом 1 шт.
7. Игрок меняет руки (F) с мечом → PlayerSwapHandItemsEvent → action_swap → например
   открытие рюкзака 4 уровня.
8. Клик по красной панели (слот 22) сбрасывает все привязки в NONE.
9. При выключении плагина открытые меню закрываются, пул БД закрывается.

---

## 9. Особенности реализации и известные ограничения
- Чтение профиля в обработчиках событий блокирующее: `getPlayerActions(...).get()` в главном
  потоке — при деградации БД возможны фризы сервера; выброс меча дополнительно завёрнут в
  `throw new RuntimeException`.
- Ключ в БД — ник (VARCHAR(16)); смена ника теряет привязки, на серверах с возможностью
  одинаковых ников в разных сервисах возможны коллизии.
- Цепочка «последний предмет в руке» обновляется таймером с задержкой 1 тик на каждого
  игрока каждый тик — нагрузка O(онлайн) задач в тик.
- Клик-«назад» (ПКМ) и «вперёд» (ЛКМ) имеют асимметричную логику заполнения свободных
  слотов (next заполняет с начала, prev — с конца); оба не позволяют дублировать товар.
- SQLite-версии upsert'ов частичных обновлений вставляют 'NONE' в остальные колонки —
  при конкурентных одиночных обновлениях возможна перезапись чужой колонки значением NONE.
- Драйверы MySQL/SQLite не объявлены в pom — работоспособность зависит от их наличия
  в classpath сервера (Paper обычно предоставляет org.sqlite; MySQL-драйвер нужно
  устанавливать отдельно, иначе создание DataSource упадёт).
- Меню не защищено от кликов-перетаскиваний в нижнем инвентаре (обрабатывается только
  InventoryClickEvent верхнего).
- Права/алиасы: команда без права, алиасов нет; ` getMenu()` у Menu не переопределяет
  getInventory (реализовано в AbstractMenu).

---

## 10. Сборка
```
mvn -pl LiteBinds package     # из каталога modules (или mvn package в LiteBinds)
```
HikariCP 4.0.3 шейдится в jar (родительский maven-shade-plugin, без релокаций);
lombok и annotations — provided.
