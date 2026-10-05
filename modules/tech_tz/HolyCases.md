# Техническое задание — модуль HolyCases

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/HolyCases`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | HolyCases |
| Артефакт (Maven) | `HolyCases` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `ua.holydev.holycases.HolyCases` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | Smail, HolyZako, NBK, itzHiti, KoroLKotov |
| Разработчик (баннер) | студия HolyDev (t.me/ExampleTG) |

**Назначение:** плагин игровых кейсов (сундуков удачи) для Minecraft-сервера.
Позволяет администраторам размещать физические кейсы в мире (блок + голограмма),
настраивать содержимое, шансы и «удачу», а игрокам — открывать кейсы ключами,
покупкой за опыт/валюту или иными способами, с анимацией выпадения приза,
историей выигрышей иBroadcast-объявлениями.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot с поддержкой API 1.16 (плагин написан под 1.16.5; impl-часть меню
  находится в пакете `menu.impl.version.v1_16_5`).
- Java 16+ (используются `instanceof`-паттерны, `String::isBlank`-эпоха синтаксиса).

### 2.2. Плагины (softdepend, все опциональны, кроме оговоренного)
| Плагин | Версия | Область применения | Обязательность |
|---|---|---|---|
| PlaceholderAPI | 2.12.3 | Плейсхолдеры в меню,価格-типы открытия кейса | Мягкая (код падает в ошибку only при использовании) |
| PlayerPoints | 3.3.5 | Валюта «сапфиры» для покупных кейсов | Мягкая (нужна только для `case-type: purchase`) |
| packetevents | 2.13.0 | Пакетная анимация сундука (BlockAction) | Мягкая (нужна для анимаций) |

### 2.3. Библиотеки (зашиваются shade-плагином в jar)
- HikariCP 4.0.3 — пул соединений MySQL.
- jdbi3-core / jdbi3-sqlobject / jdbi3-caffeine-cache 3.45.1 — доступ к БД
  (релокация в `shaded.org.jdbi`).
- jetbrains annotations 24.1.0 (provided).

### 2.4. База данных
- Режим `yaml` — все данные в `plugins/HolyCases/database.yml` (по умолчанию).
- Режим `sql` — MySQL/MariaDB. Параметры в `DatabaseConfig.yml`
  (`host`, `port`, `database`, `user`, `password`, `group-concat-length`).
  Схема создаётся автоматически из ресурса `template.sql` при старте.

---

## 3. Архитектура (структура пакетов)

```
ua.holydev.holycases
├── HolyCases              — главный класс: init конфигов, БД, менеджеров; API ключей/удачи (yaml-режим)
├── animation              — интерфейс Animation и реализации анимаций открытия
│   ├── CircleAnimation    — призы по кругу вокруг сундука с замедлением вращения
│   ├── SpinningAnimation  — предмет поднимается, крутится, «роняет» приз
│   └── entity.AnimationStand — невидимая стойка (armor stand) для отображения предметов анимации
├── cases
│   ├── GameCase           — абстрактный кейс: награды, удача, меню, голограмма, выдача призов
│   ├── ConfigurableCase   — кейс, открываемый «бесплатным» способом (ключ/клик/предмет)
│   ├── PurchasableCase    — покупной кейс (опыт/сапфиры)
│   ├── CaseHolder         — контейнер всех физических экземпляров кейса одного имени (+ фабрики-creators по мирам)
│   └── CaseManager        — реестр CaseHolder'ов, загрузка/перезагрузка кейсов, метадата блоков
├── command.CaseCommand    — команда /holycases (+TabCompleter)
├── configuration
│   ├── CasesConfig        — парсер CasesConfig.yml → объекты кейсов (анимации, награды, меню, локации)
│   └── DatabaseConfig     — обёртка DatabaseConfig.yml → ConnectionInfo
├── database
│   ├── ConnectionInfo     — record параметров подключения
│   ├── dao.PlayerKeysDao       — SQL CRUD ключей игрока
│   ├── dao.PlayerLuckDao       — SQL CRUD удачи игрока
│   ├── dao.PrizeHistoryDao     — SQL история призов
│   ├── dao.argument.UUID*      — маппинг UUID ↔ VARCHAR для JDBI
│   └── dao.entity.PrizeHistoryEntry — record строки истории
├── listener.GameListener  — клики по блоку-кейсу, админ-плейсер, загрузка миров, закрытие меню
├── lucks
│   ├── Luck               — именованный уровень удачи (сообщения history/replace/prize_message)
│   └── LuckManager        — загрузка LuckConfig.yml, выбор «снимаемой» удачи, строки удачи для меню
├── menu                   — абстракции Menu/MenuItem/MenuManager + фабрика по версии сервера
│   └── impl.version.v1_16_5 — реализация меню под 1.16.5 (InventoryMenu + listener)
├── misc                   — ColorUtil (HEX `&#rrggbb`), Messages (конфиг-сообщения), RandomCollection (взвешенный рандом)
├── nms.CustomArmorStand(+Handler) — кастомная невидимая стойка-голограмма (заголовок кейса) через NMS
├── opentype               — стратегия открытия кейса из меню
│   ├── Open (enum Type: KEY, RIGHT, LEFT, LEFTRIGHT, ITEM, PURCHASE)
│   └── impls: KeyOpen, ClickOpen, DoubleClickOpen, ItemOpen, PurchaseOpen
├── particle.WrappedParticle — обёртка параметров частиц (тип, count, speed, offsets, RGB)
├── placeholder.CasePlaceholder — экспансия PlaceholderAPI (идентификатор `cases`)
├── reward
│   ├── Reward             — «категория» награды (иконка, шансы по удаче, список призов)
│   ├── Prize              — record конкретного приза (name, title, subtitle, commands)
│   └── EntityReward       — вариация награды (зарезервировано)
└── util.ItemBuilder       — builder предметов (имя, лор, цвет, скрытие атрибутов, из ConfigurationSection)
```

---

## 4. Функциональные требования

### 4.1. Физический кейс
- Кейс = блок в мире (используется фактический блок; иконка блока в конфиге — `Material.ENDER_CHEST` на уровне кода), помеченный метадатой `case`.
- Над каждым кейсом спавнится невидимая стойка (CustomArmorStand, marker=true) с именем
  `title-above-case` (поддержка HEX `&#rrggbb` и `&x`) на высоте `title-above-case-y-offset`.
- Локации задаются строками `мир x y z НАПРАВЛЕНИЕ` в `cases.<имя>.locations`
  (направление — BlockFace, читается из Directional-блока при установке).
- При загрузке мира (WorldLoadEvent) кейсы этого мира создаются заново
  (фабрики `Supplier<GameCase>` хранятся в `CaseHolder` по имени мира).
- Несколько блоков могут относиться к одному кейсу (один `CaseHolder` — много `GameCase`).
- Взаимодействие с блоком (любой клик):
  - если кейс сейчас никем не открывается — открывается меню кейса;
  - если открывает другой игрок — сообщение `case-already-opening`;
  - клик по кейсу отменяется (`event.setCancelled(true)`), защита от разрушения.

### 4.2. Типы кейсов (`case-type`)
| Тип | Класс | Способ открытия |
|---|---|---|
| `normal` (по умолчанию) | `ConfigurableCase` | По стратегии `open` из конфига (ключ / клик / предмет) |
| `purchase` | `PurchasableCase` | ПКМ — за уровни опыта (`exp-price`), ЛКМ — за сапфиры PlayerPoints (`sapphire-price`) |

При покупке за опыт: списывается `player.setLevel(current - expPrice)`.
При покупке за сапфиры: `PlayerPointsAPI.take()` при достаточном балансе.
Недостаток средств → сообщения `not-enough-exp-message` / `not-enough-sapphire-message`
(плейсхолдер `%price%`).

### 4.3. Стратегии открытия (`open.type` для normal-кейсов)
| Тип | Класс | Поведение |
|---|---|---|
| `KEY` | `KeyOpen` | Проверяется баланс виртуальных ключей (`<кейс>_amount`); при ≥1 ключ списывается и кейс открывается; иначе сообщение `not-have-keys-for-case` и закрытие меню |
| `RIGHT` / `LEFT` | `ClickOpen` | Открытие за валюту: баланс читается из плейсхолдера `right-price-type`/`left-price-type` (число!), при достатке — консоль выполняет `right-command-change-price` (с `%player%`, `%count%`), иначе сообщение `*-failed-buy-slot` (с `%amount%`) |
| `LEFTRIGHT` | `DoubleClickOpen` | Комбинация: ЛКМ и ПКМ имеют собственные цены/команды (два `ClickOpen`) |
| `ITEM` | `ItemOpen` | Открытие физическим предметом (виртуальный предмет-валюта, `amount`, слот-сообщение при нехватке) |
| `PURCHASE` | `PurchaseOpen` | Принудительно purchase-логика (для case-type: purchase) |

Все операции списания/проверки в SQL-режиме выполняются асинхронно через `JdbiExecutor`,
переход к анимации — в главном потоке (`Bukkit.getScheduler().runTask`).

### 4.4. Система удачи (lucks)
- Файл `LuckConfig.yml`: именованные уровни удачи (`no_luck` — обязательный, дефолт;
  в конфиге-примере `luck15` = +15%, `luck30` = +30%).
- Каждый уровень задаёт:
  - `history` — подпись в истории выигрышей (например «+15%»);
  - `replace` — строка в меню с `%count%` (сколько открытий с бонусом осталось);
  - `prize_message` — префикс в broadcast-сообщении о выигрыше.
- У игрока может быть несколько уровней удачи одновременно (хранится количество
  «зарядов» каждого уровня). При открытии кейса `LuckManager.removeLast()` выбирает
  **старший** уровень (по порядку в конфиге) с зарядами > 0 и списывает 1 заряд.
- Если у кейса `luck_work: false` — удача игнорируется, всегда используется `no_luck`.
- Шансы наград задаются **отдельно для каждого уровня удачи**:
  `rewards.<награда>.chances.<имя_удачи>` и `...prizes.<приз>.chances.<имя_удачи>`.
  Отсутствующий шанс fallback'ится на `no_luck`.

### 4.5. Награды (двухуровневая модель)
1. **Reward (категория)** — то, что разыгрывается первым:
   - иконка (`material`, опционально `color` для кожи/зелий, `glow` — зачарование+hide),
   - `chances` по уровням удачи (веса, не обязаны давать в сумме 100 — используется
     взвешенный рандом `RandomCollection`),
   - `send-title` — показать Title/Subtitle приза игроку,
   - `add-to-history` — записать выигрыш в историю.
2. **Prize (приз внутри категории)** — разыгрывается вторым, тоже по весам `chances`:
   - `name` — отображаемое имя (в broadcast и истории),
   - `title.title` / `title.subtitle` — заголовок игроку,
   - `commands` — команды от имени консоли с `%player_name%` и `%luck%`
     (пример в конфиге — выдача донат-рангов через команды).

Выбор: `GameCase.getRandomReward()` → `Reward.getRandomPrize(luck)`.

### 4.6. Выдача приза (`GameCase.givePrize`)
- Если включён `send-title` и игрок онлайн — `player.sendTitle(...)`.
- Каждая строка `prize-messages` отправляется в общий чат (`Bukkit.broadcastMessage`)
  с подстановками `%player_name%`, `%prize_name%`, `%luck%`.
- Каждая команда приза выполняется консолью с `%player_name%`, `%luck%`.
- При `add-to-history: true` — запись в историю (SQL или yaml):
  `opener, case_name, luck_name, prize_name, prize_material (+ :R:G:B для окрашенных предметов), timestamp, server_name`.
- Гарантия при выключении сервера: в `onDisable` всем, у кого открытие «в процессе»,
  принудительно выдаётся случайный приз (`getRandomReward()` + `givePrize`) — приз не теряется.
- У всех стоек-анимаций при выключении вызывается очистка; метадата `case` снимается со всех блоков.

### 4.7. Анимации открытия (`animation.type`)
Общие свойства: выполняются в тиках, используют невидимые `AnimationStand`,
по завершении вызывают `GameCase.onOpenEnd()` (сброс `opening`, возврат голограммы).

**`circle`** (CircleAnimation, параметры из `circle-animation.*`):
- призы (иконки всех Reward'ов) выстраиваются по кругу радиуса `reward-radius` вокруг сундука
  (`random-rewards: false` — по порядку конфига);
- фазы: `start` (delay, spawn-interval — появление стоек), `open`
  (reward-up-ticks — подъём, rotate-into-place-angle), `spin` (вращение с замедлением:
  start/minimal-angle-per-tick, min/max-slowdown-ticks, min/max-idle-ticks),
  `end` (delay, down-ticks — опускание, rotate-into-place-angle);
- звуки `chest-open-sound` / `chest-close-sound`; открывание сундука эмулируется
  пакетом `WrapperPlayServerBlockAction` (packetevents).

**`spinning`** (SpinningAnimation, параметры из `spinning-animation.*`):
- предмет-иконка поднимается на `lift-height` за `lift-duration`, крутится `spin-duration`
  со сменой иконки каждые `change-interval`, падает `drop-duration`;
- звуки `lift-sound` / `drop-sound`.

### 4.8. Частицы
Секция `particles.<имя>`: `particle` (тип), `count`, `speed`, `xOffset/yOffset/zOffset`,
для `REDSTONE` — `r/g/b`. Частицы спавнятся вокруг блока кейса постоянно (задача держит
живыми оборачивания `WrappedParticle`).

### 4.9. Меню кейса (`menu.*`)
- Открывается по клику на блок кейса (при свободном кейсе).
- `title`, `size` (кратность 9, дефолт 54).
- **Кулдаун кликов**: `cooldown.type` = `GLOBAL` (общий) / `LOCAL` (на игрока), `cooldown.time` — тики.
- **Карта инвентаря** `inventory` — строки из символов, ссылающихся на `item-types`:
  - `O` — пусто (AIR), `X` — рамка, `P` — описание/цены, `W` — «последние выигрыши»,
    `C` — кнопка «Открыть кейс»;
  - любой `item-types`-символ: `material`, `name`, `lore` с плейсхолдерами.
- Плейсхолдеры внутри меню: `%player_keys%` (виртуальные ключи этого кейса),
  `%lucks%` (многострочный блок активной удачи), а также любые PAPI-плейсхолдеры
  (`PlaceholderAPI.setPlaceholders`).
- Клик по `C`:
  - кейс занят → закрыть меню + `case-already-opening`;
  - PurchasableCase → `openWithPurchase` (тип клика ЛКМ/ПКМ выбирает валюту);
  - иначе — стратегия `Open` для кейса.
- Клик по `W` — заполняет следующие слоты строки (до конца строки) предметами истории
  выигрышей; шаблоны `last-rewards.item-name` / `item-lore` с `%player_name%`,
  `%prize_name%`, `%server_name%`, `%date%` (формат `dd MMM HH:mm:ss`, таймзона
  Europe/Moscow), `%luck%`. Иконка восстанавливается из `prize_material` (+ RGB-цвет).
- Все клики в меню по умолчанию отменены.
- Реализация меню выбирается фабрикой `MenuManagerFactory` (v1_16_5); слушатель меню
  регистрируется реализацией.

### 4.10. Управление (команда `/holycases`, алиасы `/hc`, `/fc`)
| Подкоманда | Право | Описание |
|---|---|---|
| `/hc help` | — | Справка (только доступные по правам пункты) |
| `/hc item <кейс>` | `holycases.command.item` | Взять в руку предмет — он становится иконкой открытия для типа `item` (сохраняется в `database.yml`, `items.<кейс>`) |
| `/hc keys <кейс> add\|withdraw <игрок> <кол-во>` | `holycases.command.keys` | Выдать/списать виртуальные ключи |
| `/hc keys <кейс> count <игрок>` | `holycases.command.keys` | Узнать баланс ключей |
| `/hc luck <удача> add\|withdraw <игрок> <кол-во>` | `holycases.command.luck` | Выдать/списать «заряды удачи» |
| `/hc luck <удача> count <игрок>` | `holycases.command.luck` | Узнать баланс удачи |
| `/hc place <кейс>` | `holycases.command.place` | Выдать плейсер (ENDER_CHEST с именем кейса) |

TabComplet подбирает подкоманды/имена кейсов/имена удач по правам отправителя.

**Установка кейса плейсером** (GameListener, право `holycases.command.place`):
- ПКМ плейсером по блоку → блок становится кейсом (запись в `CasesConfig.yml`
  `cases.<кейс>.locations` с направлением блока), перезагрузка кейсов;
  отказ, если на блоке уже стоит другой кейс.
- ЛКМ плейсером по блоку-кейсу → удаление локации из конфига и метадаты.

### 4.11. Плейсхолдеры (PlaceholderAPI, идентификатор `cases`)
| Плейсхолдер | Значение |
|---|---|
| `%cases_luck15_amount%` | заряды удачи luck15 |
| `%cases_luck30_amount%` | заряды удачи luck30 |
| `%cases_all_amount%` | список «кейс: N ключей» по всем кейсам |
| `%cases_<кейс>_amount%` | виртуальные ключи кейса (SQL — из БД; в текстах меню — быстрая замена через `replacePlaceholders`) |

---

## 5. Хранение данных

### 5.1. Режим yaml (`database: yaml`) — файл `database.yml`
```
keys.<имя_кейса>.<uuid>          — количество виртуальных ключей
lucks.<имя_удачи>.<uuid>         — заряды удачи
items.<имя_кейса>                — ItemStack-иконка для типа открытия item
history.<рандомный-uuid>.{opener, caseName, luckName, prizeName, prizeMaterial, timestamp, serverName}
```

### 5.2. Режим sql (`database: sql`) — MySQL, схема из `template.sql`
| Таблица | Назначение | Ключи |
|---|---|---|
| `player_keys(player, key_name, count)` | виртуальные ключи | PK (player, key_name) |
| `player_luck(player, luck_name, count)` | заряды удачи | PK (player, luck_name) |
| `prize_history(opener, case_name, luck_name, prize_name, prize_material, timestamp, server_name)` | история выигрышей (общая для всех серверов сети) | — |

- Подключение: HikariCP (`jdbc:mysql://host:port/db?autoReconnect=true&useSSL=false&characterEncoding=utf8`),
  пул + JDBI (SqlObjectPlugin, CaffeineCachePlugin), асинхронный пул из 8 потоков (`JdbiExecutor`).
- UUID хранится как VARCHAR (кастомный `UUIDArgument(Factory)`).
- Списание ключей/удачи: `INSERT ... ON DUPLICATE KEY UPDATE count = count ± N`
  (атомарно); чтение — `SUM(count)` c `COALESCE`.
- При недоступности БД плагин отключается с сообщением «Настройте данные для подключения...».

### 5.3. Конфигурационные файлы
| Файл | Содержимое |
|---|---|
| `config.yml` | `server_name` (метка в истории), `database: yaml\|sql` |
| `CasesConfig.yml` | Полное описание кейсов (см. §4.2–4.9) |
| `LuckConfig.yml` | Уровни удачи |
| `DatabaseConfig.yml` | Параметры MySQL |
| `database.yml` | Данные yaml-режима (создаётся пустым) |

`reloadConfig()` пересоздаёт `CasesConfig` и перечитывает `config.yml`.

---

## 6. Сценарий работы (основной поток)
1. Администратор описывает кейс в `CasesConfig.yml`, ставит `/hc place`, ПКМ по блоку.
2. Игрок кликает блок → меню кейса (баланс ключей, активная удача, история).
3. Клик «Открыть» → проверка способа оплаты (ключ/клик/предмет/покупка) → списание.
4. Снимается 1 заряд старшей удачи (если `luck_work`).
5. Анимация (circle/spinning) с иконками наград.
6. Розыгрыш: Reward (вес по удаче) → Prize (вес по удаче).
7. Title, broadcast `prize-messages`, команды приза от консоли.
8. Запись в историю (опционально), обновление меню (W-строка) при следующем открытии.
9. `onOpenEnd()` — кейс снова доступен, голограмма возвращается.

---

## 7. Нефункциональные требования и особенности реализации
- Потокобезопасность: карта «кто открыл кейс» — `ConcurrentHashMap`; конкурентный доступ
  к блоку защищён synchronized-блоком + отложенной задачей (1 тик).
- SQL-режим: все запросы асинхронные, UI-действия возвращаются в main thread.
- Отказоустойчивость: при shutdown принудительная выдача приза открытий in-flight;
  очистка стоек и метадаты блоков.
- Валидация конфига: некорректные `Material`/`Sound`/`Particle` заменяются
  fallback-значениями (STONE / NOTE_BLOCK_PLING / CRIT) c логом.
- HEX-цвета `&#rrggbb` и `&x` поддерживаются везде через `ColorUtil.parse`.
- Совместимость версий: реализация меню изолирована в `menu.impl.version.*`,
  выбирается `MenuManagerFactory` (расширяемая точка для других версий сервера).

## 8. Точки расширения (для доработок)
- Новый тип открытия: `Open.Type` + класс в `opentype.impls` + ветка в `CasesConfig.getCases()`.
- Новая анимация: реализация `Animation` + выбор по `animation.type` в `CasesConfig`.
- Новая версия сервера: пакет `menu.impl.version.<v>` + ветка в `MenuManagerFactory`.
- Интеграция новой валюты в ClickOpen — достаточно плейсхолдера баланса + консольной команды списания.

## 9. Сборка
```
mvn -pl HolyCases package     # из каталога modules (или mvn package в HolyCases)
```
Maven Shade склеивает HikariCP и jdbi3 с релокацией `org.jdbi → shaded.org.jdbi`;
`packetevents`, `placeholderapi`, `playerpoints`, `annotations` — provided (берутся с сервера).
