# Техническое задание — модуль HolyCore

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/HolyCore`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | HolyCore |
| Артефакт (Maven) | `HolyCore` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 0.1 |
| Главный класс | `org.impldev.holycore.HolyCore` |
| API-версия Bukkit | 1.16 |
| Компиляция | Java 21 (`maven.compiler.release=21` в родительском POM) |
| Авторы (plugin.yml) | не указаны |

**Назначение:** ядро игрового сервера. Объединяет автономные подсистемы, не связанные
с одной конкретной механикой:

- **личные timed-бустеры** (опыт, монеты с мобов, скупщик, культуры, урон по мобам) с покупкой
  уровней за сапфиры (PlayerPoints);
- **глобальные инвестиции** — прокачка серверных направлений (порох, опыт, автошахты,
  голосования, сокровища, границы миров, скупщик) за внутриигровую валюту (Vault),
  недельный рейтинг игроков и ежедневный призовой пул сапфиров;
- **титулы** — меню, надевание/снятие суффикса через LuckPerms, выдача/передача титула предметом;
- **награды за достижения** (advancements и активация портала в Энд) сапфирами с лимитами;
- **награда за первый вход** (команды из конфига, фиксация в MySQL);
- набор **игровых правил и ограничений** (лимиты шалкеров/тотемов, запрет щитов,
  переработка дропа мобов, режущий спавн мобов, AFK-режим, PvP-трезубец, команды по регионам
  WorldGuard и др.) и **утилитарные команды** (`/afk`, `/bc`, `/suicide`, `/links`, `/baltop`, …);
- два кастомных **рецепта крафта** (зачарованное золотое яблоко, обломок древних).

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper 1.21.4 (`io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT`, scope provided, из родительского POM);
  `api-version: '1.16'` в plugin.yml.
- Используются Paper-события (`PlayerPickupExperienceEvent`) — на чистом Spigot класс не загрузится.
- Java 21.

### 2.2. Плагины (все — scope provided)
`plugin.yml` не содержит ни `depend`, ни `softdepend` — все зависимости проверяются
в рантайме, плагин включается даже без них (с деградацией функций).

| Плагин | Версия из POM | Область применения | Обязательность |
|---|---|---|---|
| EssentialsX | 2.22.0 | Vanish-игроки не подбирают предметы (`ObjectExtension`); если не найден — фича подбора отключается с warning | Мягкая (фича отключается) |
| WorldEdit | 7.3.16 | Адаптация `Location`/`World` для WorldGuard | Мягкая (код упадёт только при использовании регионов) |
| WorldGuard | 7.0.13 | `/sethome` только в своём регионе; команды по входу в регион; проверка PvP-флага для трезубца | Мягкая (проверки при событиях) |
| PlayerPoints | 3.3.5 | Валюта «сапфиры» ☀ — API `Sapphires`, покупка бустеров, выплаты инвестиций/наград | Мягкая (все операции молча возвращают 0/false) |
| Vault | 1.7.3 | Экономика (монеты ¤): покупка очков инвестиций, `/baltop` | Мягкая |
| PlaceholderAPI | 2.12.3 | Экспансии `holyinvest`, `holyindicator`; чтение `%luckperms_suffix%`, `%antirelog_isinpvp%` | Мягкая (без него `/healthindicator` не может управлять индикатором TAB) |
| LuckPerms | — (не в POM) | Титулы: команды `lp user … meta setsuffix / permission set` от консоли; чтение суффикса через PAPI | Мягкая |
| DeluxeMenus | — (не в POM) | Источник списка титулов: чтение `plugins/DeluxeMenus/gui_menus/titul/*season*.yml` | Мягкая (без файлов — 0 титулов, warning) |
| LiteBans | — (не в POM) | Broadcast наград за достижения через команду `litebans broadcast` | Мягкая (команда консоли) |
| TAB | — (не в PAPOM) | Косвенно: гасит belowname-индикатор здоровья по `%holyindicator_state%` | — |

### 2.3. Библиотеки
- Lombok 1.18.32, jetbrains annotations 24.1.0 — provided, в jar не попадают.
- Своей шэйдимой логики нет; shade-плагин родительского POM применяется ко всем модулям
  (см. §10).

### 2.4. База данных
- MySQL/MariaDB для двух таблиц наград (`holy_first_join_rewards`, `holy_rewards`).
  Параметры в `config.yml` → `database.*`. Без БД плагин продолжает работать — падают только
  записи о наградах (ошибка логируется `severe` при старте).
- Всё остальное состояние — yml-файлы в `plugins/HolyCore/`.

---

## 3. Архитектура (структура пакетов)

```
org.impldev.holycore
├── HolyCore                    — главный класс: регистрация рецептов, листенеров, команд,
│                                 инициализация БД, канала BungeeCord, запуск таймеров
├── boosters                    — ЛИЧНЫЕ бустеры (срок действия, сапфиры)
│   ├── Booster                 — record описания бустера (id, slot, иконка, base/step/price, эффект)
│   ├── Boosters                — реестр 5 бустеров, лестница цен «buyer», признак permanent
│   ├── BoosterData             — состояние в boosters.yml: уровень + абсолютный момент окончания
│   ├── BoosterMenu             — GUI «Бустеры» (45 слотов), покупка уровней за сапфиры
│   ├── BoosterCommand          — /boosters → открытие меню
│   └── Sapphires               — статическая обёртка PlayerPointsAPI (balance/take/give по UUID)
├── invest                      — ГЛОБАЛЬНЫЕ инвестиции
│   ├── Category                — record направления (maxLevel, need, price, perLevel, baseValue) + Kind
│   ├── Categories              — реестр 7 направлений (buyer, gunpowder, experience, automine,
│   │                             voting, treasures, borders)
│   ├── InvestData              — состояние в invest.yml: уровни+прогресс направлений, очки игроков
│   │                             (всего/за неделю), недельный топ, ежедневная выплата 2500 сапфиров
│   ├── InvestEffects           — применение эффектов: множитель опыта/пороха, границы миров
│   │                             (base * 1.05^level), формула времени
│   ├── InvestMenu              — GUI «Меню инвестиций» (54 слота): подтверждение в 2 клика,
│   │                             покупка очка за монеты Vault, топ-10, broadcast о новом уровне
│   ├── InvestCommand           — /invest → открытие меню
│   └── InvestPlaceholders      — экспансия PAPI `holyinvest`
├── tituls                      — титулы
│   ├── Titul                   — record титула (permission, text, season, order), дата сезона
│   ├── TitulRegistry           — чтение титулов из конфигов DeluxeMenus (gui_menus/titul/*season*.yml)
│   ├── TitulMenu               — GUI «Титулы <ник>» (54 слота): страницы, 3 сортировки,
│   │                             надеть/снять суффикс LuckPerms, забрать титул предметом
│   ├── TitulCommand            — /tituls [ник] — свои или чужие (только онлайн) титулы
│   └── TitulItemListener       — активация титула-предмета (NAME_TAG) по ПКМ
├── rewards.Rewards             — награды за достижения (15 типов): лимиты, выдача сапфиров
│                                 через `p give`, broadcast через litebans, запись в MySQL
├── commands                    — утилитарные команды
│   ├── AfkExtension-команда /afk  — см. extensions (исполнитель и листенер один класс)
│   ├── Bc                      — /broadcast|/bc с кулдауном
│   ├── Suicide                 — /suicide с подтверждением и кулдауном
│   ├── Links                   — /links (+ алиасы) — блок ссылок из конфига
│   ├── Wolker                  — /wolker — фейковое ванильное сообщение о выдаче оператора
│   ├── HealthIndicator         — /healthindicator — переключатель + экспансия PAPI holyindicator
│   ├── BalTop                  — /baltop, /baltop2, /baltop3 — топ балансов Vault (async + кэш 60 c)
│   └── WExtend                 — /wextend — заглушка «переехало в /invest»
├── checker
│   ├── ShulkerTotemChecker     — периодическая проверка (раз в 50 тиков): предупреждение о >1
│   │                             шалкере, выбрасывание тотемов сверх 4
│   └── SpawnerChunkChecker     — лимит 16 спавнеров на чанк (static canPlaceSpawner)
├── database.DatabaseManager    — JDBC к MySQL: создание 2 таблиц, async-запросы наград
├── extensions                  — игровые правила/модификаторы (все — Listener)
│   ├── AfkExtension            — /afk только в мире «spawn»: коллизия off, metadata isAFK, title
│   ├── CommandExtension        — запрет /tpa|/call игроку в PvP-режиме (PAPI %antirelog_isinpvp%)
│   ├── CraftingExtension       — запрет крафта щита
│   ├── EntityExtension         — дроп крипера/гаста/виндикатора/призывателя, спавн 1/12, с
│   │                             визерв, взрывы без разрушения блоков
│   ├── ExperienceExtension     — опыт сохраняется при смерти; множители опыта по правам experience.*
│   ├── InventoryExtension      — шалкеры в эндер-сундук запрещены; тотемы ≤4 (прогноз клика); щиты
│   ├── JoinExtension           — сообщения при входе, отложенное сообщение, награда первого входа
│   ├── ObjectExtension         — requires Essentials: гашение join/quit/death-сообщений, сброс
│   │                             glowing/invulnerable, ANNOUNCE_ADVANCEMENTS off, /sethome в чужом
│   │                             регионе запрещён, /warp logo, mending-нерф, кик за спам, шалкеры при
│   │                             выходе, тотемы при подборе, спавнер-яйца, частицы выхода
│   ├── PlayerExtension         — частицы при выходе (дым) и кике (кольцо взрывов)
│   ├── RegionCommandExtension  — команды по входу в регион WorldGuard (config region-commands)
│   ├── SpawnExtension          — блоки стола зачарований/наковальни/кузн. стола в мире «spawn»
│   ├── SpawnerLimitExtension   — лимит установки спавнеров (16 на чанк)
│   └── TridentExtension        — переработанный PvP-трезубец (кулдауны, урон от дистанции)
└── utils
    ├── Parser                  — HEX-цвета `&#rrggbb` + `&x`, кликабельные сообщения, формат дат/чисел
    ├── Particles               — enum-обёртка частиц (800 строк, рефлексия, цвета/данные)
    ├── UtilParticles           — линии/кольца из частиц
    ├── ReflectionUtils         — рефлексивные утилиты (DataType, вызовы методов)
    └── PackageType             — enum пакетов NMS-версий
```

---

## 4. Функциональные требования

### 4.1. Личные бустеры (пакет `boosters`)

#### 4.1.1. Список бустеров (зашит в коде, `Boosters`)
| id | Слот | Иконка | Название | base % | step % | Цена 1 ур. (сапфиры) | Особенность |
|---|---|---|---|---|---|---|---|
| `experience` | 20 | EXPERIENCE_BOTTLE | Бустер Опыта | 10 | 11 | 138 | — |
| `mobmoney` | 21 | SUNFLOWER | Бустер Монет с Мобов | 25 | 19 | 75 | — |
| `buyer` | 22 | NETHERITE_INGOT | Бустер Скупщика | 2 | 2 | 138 | лестница цен, permanent |
| `crops` | 23 | CARROT | Бустер Культур | 15 | 8 | 107 | — |
| `mobdamage` | 24 | NETHERITE_SWORD | Бустер Урона по Мобам | 5 | 3 | 107 | — |

- **Бонус уровня L**: `bonus(L) = base + step * (L - 1)`, уровень 0 = бустера нет.
- **Цена улучшения**: по умолчанию `price * max(1, L)` (L — текущий уровень, покупается L+1).
  Для `buyer` задана точная лестница цен по уровням (20 значений, из вики режима):
  `100, 138, 185, 244, 318, 410, 525, 669, 849, 1074, 1355, 1707, 2147, 2697, 3384, 4243, 5317, 6659, 8337, 10434`
  → потолок уровня 20; на последнем уровне цена 0 («максимального уровня»).
- `buyer` — permanent: покупается «до конца вайпа» (срок 3650 дней), остальные — на 48 часов.
- Срок хранится абсолютным моментом (`until`, ms): истёкший бустер считается уровнем 0.
  Повторная покупка истёкшего начинает снова с уровня 1.

#### 4.1.2. Меню `/boosters`
- Инвентарь 45 слотов, заголовок «Бустеры». Рамки: серые панели (0, 8, 36, 44), оранжевые
  (9, 17, 18, 26, 27, 35). Слот 37 — FILLED_MAP «Список бустеров» (все уровни + «всего потрачено
  сапфиров»), слот 43 — «◀ Назад» (закрывает меню).
- Иконка бустера: имя с текущим бонусом `(+N%)`, лор: описание эффекта (2 строки),
  «Следующее улучшение: +M%», цена следующего уровня в сапфирах ☀, «Будет активен ещё: 43ч. 24м. 44с.».
- **Клик по бустеру**:
  1. достигнут потолок (только `buyer`, 20 ур.) → сообщение «уже максимального уровня»;
  2. `Sapphires.take(player, price)` — атомарная проверка+списание PlayerPoints;
     не хватило → «недостаточно сапфиров»;
  3. уровень +1, срок = теперь + 48 ч (или 3650 дней для `buyer`), потраченное суммируется;
  4. меню перерисовывается, сообщение «<Название> улучшен до N ур.».
- Все клики в меню отменяются; нижний инвентарь игнорируется.
- **Внутри HolyCore бонусы бустеров нигде не применяются** — модуль хранит уровни и рисует
  меню; применение лежит на других моделях сборки (сапфиры/опыт/скупщик — отдельные плагины).

#### 4.1.3. Сапфиры (`Sapphires`)
- Сапфир ☀ = балланс PlayerPoints. `balance(Player)`, `take(Player, int)` (true только при
  фактическом списании), `give(UUID, int)` (выдача оффлайн-игрокам топа инвестиций).
- Через Vault сапфиры не доступны намеренно (Vault на сервере отдаёт монеты Essentials).
- Отсутствие PlayerPoints: balance = 0, take/give = false (покупки невозможны).

### 4.2. Глобальные инвестиции (пакет `invest`)

#### 4.2.1. Направления (зашиты в коде, `Categories`)
| id | Слот | Иконка | Название | maxLvl | need (очков/уровень) | цена очка ¤ | perLevel | база (сек) | Kind |
|---|---|---|---|---|---|---|---|---|---|
| `buyer` | 11 | DIAMOND | Товар скупщика | 100 | 250 | 10 000 | — | — | EXTERNAL |
| `gunpowder` | 13 | GUNPOWDER | Порох | 30 | 75 | 25 000 | 15 % | — | BONUS |
| `experience` | 15 | EXPERIENCE_BOTTLE | Опыт | 50 | 150 | 50 000 | 5 % | — | BONUS |
| `automine` | 28 | DIAMOND_PICKAXE | Автошахта | 70 | 100 | 25 000 | 1 % | 600 | TIME |
| `voting` | 30 | CAMPFIRE | Голосование | 30 | 250 | 25 000 | 1 % | 3000 | TIME |
| `treasures` | 32 | COMPASS | Сокровища | 50 | 50 | 50 000 | 1 % | 6000 | TIME |
| `borders` | 34 | STRUCTURE_VOID | Границы миров | 20 | 75 | 400 000 | — | — | BORDER |

- `Kind` — способ показа/расчёта: BONUS («Сейчас: +N%»), TIME («По умолчанию: Xм.» /
  «Сейчас: Y (-Z%)»), BORDER (радиусы трёх миров), EXTERNAL (только счётчик уровня,
  эффект живёт в другом плагине).
- **Уровни направлений глобальные (на весь сервер), очки — личные.** Личный счётчик хранится
  дважды: всего и за неделю.

#### 4.2.2. Покупка очка
- Стоимость одного очка = `price` монет (Vault Economy). Проверки: уровень < maxLevel,
  провайдер Economy доступен, `economy.getBalance >= price`, `withdrawPlayer` успешен.
- `invest()`: очки игрока (всего и за неделю) +1, прогресс направления +1; при
  `прогресс >= need` и `уровень < maxLevel` — уровень +1, прогресс в 0, возвращается `levelUp`.
- При levelUp: пересчитываются границы миров (`InvestEffects.apply()`), broadcast-объявление
  (формат ниже).
- Broadcast о новом уровне — 5 строк: «Уровень инвестиций в <что> был увеличен до N уровня!» +
  эффект (для каждого id своя формулировка; для borders — «шире на N блоков», N = level × 10).

#### 4.2.3. Формулы эффектов (`InvestEffects`)
- Множитель BONUS: `1 + level * perLevel / 100` (применяется к дропу опыта всех мобов и
  пороху криперов, кроме крипов `fromMobSpawner()`).
- Время TIME: `base * (1 - min(0.95, level * perLevel / 100))` — сокращение максимум на 95 %.
- Границы: радиус = `base * 1.05^level`, base: ад 1000, обычный мир 1750, энд 2500.
  Устанавливается `worldborder.setSize(radius * 2)` при расхождении > 0.5 блока; вызывается
  при старте (через 1 тик, после загрузки миров) и после каждого levelUp.
- Скупщик/автошахты/голосование/сокровища — эффекты внешние; величину отдают плейсхолдеры (§4.9).

#### 4.2.4. Недельный рейтинг и ежедневная выплата (`InvestData`)
- Топ строится по **недельным** очкам, топ-10.
- **Сброс недели**: каждый понедельник (Europe/Moscow, `previousOrSame(MONDAY)`) недельные
  очки всех обнуляются. Проверка по дате — сервер, лежавший в понедельник, сбросит при старте.
- **Ежедневная выплата**: раз в сутки (сравнение даты, МСК) пул **2500 сапфиров** делится
  между топ-10 пропорционально очкам: `reward = round(2500 * points_i / total)`; выдача
  `Sapphires.give(uuid, reward)` (работает и для оффлайн), лог в консоль.
  Первый запуск дату только запоминает (награды за старые очки не раздаются).
- Обе проверки запускаются при старте и далее таймером раз в 1200 тиков (минуту).

#### 4.2.5. Меню `/invest`
- Инвентарь 54 слота, заголовок «Меню инвестиций». Слот 45 — BOOK «Помощь по инвестициям»,
  слот 53 — MAP «Топ по очкам инвестиций (обновление — 5 мин.)» (топ-10, счётчик своих очков,
  примечание об обнулении в понедельник).
- Иконка направления: имя с `[L/maxLvl ур.]`, лор категории, строка эффекта по Kind
  (для BORDER — три радиуса), `Прогресс: [p/need]`, `Цена: price ¤`, приглашение купить.
- **Подтверждение в два клика**: первый клик по направлению ставит в слот LIME_STAINED_GLASS_PANE
  «Вы уверены?» (одновременно подтверждение может быть только одно — прежний слот возвращается);
  повторный клик по тому же слоту выполняет покупку. `pending` сбрасывается при закрытии меню.
- Сообщения: «уже прокачано до конца», «Экономика недоступна», «недостаточно денег»,
  «Вы вложили 1 Очко Инвестиций в <название>». После покупки меню перерисовывается целиком.
- Все клики отменяются; нижний инвентарь игнорируется.

### 4.3. Титулы (пакет `tituls`)

#### 4.3.1. Источник данных (`TitulRegistry`)
- Своего хранилища нет: титулы читаются из файлов DeluxeMenus
  `plugins/DeluxeMenus/gui_menus/titul/*season*.yml`.
- Из каждого файла берутся items, у которых
  `view_requirement.requirements.perm_check.type == "has permission"`; титул =
  (`permission` из `perm_check.permission`, текст = `display_name`).
- Сезон — второй сегмент права (`tituls.summer2024.1` → `summer2024`), порядок — позиция в файле.
- Сортировка реестра: `seasonRank` (= год×10 + номер сезона: spring 1, summer 2, autumn 3,
  winter 4), затем order. Дата появления: spring → 01.03, summer → 01.08, autumn → 01.10,
  winter → 01.12 + год.
- Владение титулом = наличие права LuckPerms (проверяется `player.hasPermission`).

#### 4.3.2. Меню `/tituls [ник]`
- Без аргументов — свои титулы; с аргументом — титулы игрока, но **только если он онлайн**
  (права оффлайна без LuckPerms-API не читаются); «Игрок <ник> сейчас не в сети.»
- Инвентарь 54 слота, заголовок «Титулы <ник>». Слоты контента: 11–15, 20–24, 29–33 (15 на
  страницу); рамки серые (0, 8, 45, 53) и оранжевые (9, 17, 18, 26, 27, 35, 36, 44).
- Слот 46 — RED_DYE «Снять активный титул»; 48/50 — стрелки «Предыдущая/Следующая страница»
  (если нет — светло-серяя панель); 49 — HOPPER «Сортировка» с тремя режимами:
  0 — сначала новые, 1 — сначала старые, 2 — сначала кастомные (по строке сезона).
  Лор сортировки показывает «Доступно титулов: N».
- Иконка титула: NAME_TAG с текстом титула; лор: «Редкость: Кейсовый» (одинакова для всех),
  дата появления.
- **ЛКМ по титулу (только по своему списку)**: консоль выполняет
  `lp user <ник> meta setsuffix 1 "<текст>" server=lite`; сообщение «Вы успешно установили себе
  титул!», звук ENTITY_VILLAGER_YES. Чужой список — только просмотр (клики игнорируются).
- **ПКМ по титулу (только свой)**: если в инвентаре есть место —
  1. если этот титул сейчас надет — суффикс снимается;
  2. `lp user <ник> permission unset <право>`;
  3. выдаётся предмет титула (см. ниже); титул исчезает из списка (перерисовка через 10 тиков —
     права применяются не мгновенно).
- **Слот 46 (только свой)**: `lp user <ник> meta clear suffix server=lite` — контекст
  `server=lite` обязателен (суффикс ставится с ним). Сообщение «Титул снят.»
- «Надет ли титул» определяется плейсхолдером `%luckperms_suffix%` (сравнение без цветов);
  если плейсхолдер не разрешился (вернулся с `%`) — считается «надет» (безопасная сторона).

#### 4.3.3. Титул-предмет (`TitulItemListener`)
- ПКМ с NAME_TAG, у которого последняя строка лора (без цветов) совпадает с правом известного
  титула: событие отменяется.
- Если право уже есть — «Этот титул у вас уже есть.» Иначе:
  `lp user <ник> permission set <право> true`, предмет расходуется (-1),
  «Титул добавлен в /tituls.», звук ENTITY_VILLAGER_YES.
- Право в предмете хранится строкой лора (не PDC), чтобы предмет переживал передачу через
  сундуки/аукцион.

### 4.4. Награды за достижения (`rewards.Rewards`)
- 14 триггеров — advancements (`PlayerAdvancementDoneEvent`) + 1 триггер — активация портала в Энд.
- Лимит = сколько игроков всего получат награду (проверка `COUNT(*)` по таблице `holy_rewards`
  c `reward_given = TRUE`); кэш текущей сессии — in-memory `Set<UUID>` на каждый тип.

| Тип (reward_type) | Триггер (advancement / событие) | Сапфиры | Лимит игроков |
|---|---|---|---|
| END | портал в Энд полностью активирован | 500 | без лимита (Integer.MAX_VALUE) |
| NETHER | `story/enter_the_nether` | 50 | 20 |
| VILLAGER_TRADE | `adventure/trade` | 100 | 20 |
| BEACON | `nether/create_beacon` | 200 | 20 |
| HERO | `adventure/hero_of_the_village` | 300 | 20 |
| DIAMOND_ARMOR | `story/shiny_gear` | 150 | 20 |
| NETHERITE_ARMOR | `nether/netherite_armor` | 400 | 20 |
| ADVENTURING_TIME | `adventure/adventuring_time` | 350 | 20 |
| TAME_ANIMAL | `husbandry/tame_an_animal` | 80 | 20 |
| BALANCED_DIET | `husbandry/balanced_diet` | 250 | 20 |
| RETURN_TO_SENDER | `nether/return_to_sender` | 180 | 20 |
| FOLLOW_ENDER_EYE | `story/follow_ender_eye` | 120 | 20 |
| LAVA_BUCKET | `story/lava_bucket` | 90 | 20 |
| CURE_ZOMBIE_VILLAGER | `story/cure_zombie_villager` | 280 | 20 |
| SLEEP_IN_BED | `adventure/sleep_in_bed` | 60 | 20 |

- **Условие END**: ПКМ ENDER_EYE по END_PORTAL_FRAME без глаза; через 2 тика проверяется
  активация: в квадрате 5×5 вокруг блока ищется «центр», у которого на 16 позициях рамки
  ≥ 12 рамок с глазами. При успехе и `COUNT < MAX` — награда.
- **Выдача**: консольные команды `p give <ник> <кол-во> -s` (PlayerPoints) и
  `litebans broadcast ▶ Игрок <ник> … <кол-во> сапфиров ☀` (у каждого типа свой цвет HEX
  в тексте). Затем асинхронная запись в `holy_rewards`.
- Все обращения к БД асинхронные (`CompletableFuture`), возврат в main thread через `runTask`.

### 4.5. Награда за первый вход (`JoinExtension` + `DatabaseManager`)
- На `PlayerJoinEvent` асинхронно проверяется `holy_first_join_rewards.reward_given` по UUID.
- Если награды ещё не было: выполняются команды из `config.yml → firstjoincommands`
  с подстановкой `%player%` (по умолчанию `experience add %player% 1395 points`), игрок
  добавляется in-memory, асинхронно пишется в таблицу (`ON DUPLICATE KEY UPDATE`).
- Проверка устойчива к рестартам — источник истины MySQL (in-memory set — только антидубль
  в рамках сессии).

### 4.6. Команды (полный синтаксис)

| Команда (алиасы) | Право | Поведение |
|---|---|---|
| `/afk` | `holycore.afk.use` (default op) | Переключает AFK. Только в мире `spawn`, иначе «АФК возможно активировать только находясь на спавне.». Консоли — «Ле ну ты ухуел уже». AFK: metadata `isAFK`, `setCollidable(false)`, вечный title «AFK», сообщение. Выход: любое перемещение координат или телепорт из spawn. |
| `/broadcast`, `/bc` | `holycore.bc.use` (default op) | Broadcast-объявление. Формат — `settings.bc.message` с `{player}`, `{msg}`, `{color}` (заменяется на пустую строку). Кулдаун `settings.bc.cooldown` (по умолчанию 1800 c) на игрока; обход — `holycore.bc.bypass`. Если `settings.bc.enable: false` — молчание. Без аргументов — подсказка «/bc [сообщение]». |
| `/suicide` | `holycore.suicide.use` (default op) | Двухшаговое подтверждение: первый вызов показывает предупреждение (`settings.suicide.message`), повтор в течение `settings.suicide.cooldown` (по умолчанию 10 c) — `setHealth(0)` и broadcast `settings.suicide.message-died` с `%player_suicide%`. Таймер подтверждения снимается задачей через cooldown. |
| `/links`, `/сообщества`, `/тгк`, `/дискорд`, `/сайт` | — (нет) | Построчно выводит `settings.links.message`. |
| `/wolker` | `holycore.wolker.use` (default true) | Серым курсивом: «[Server: <ник> назначен оператором сервера]» — имитация ванильного отчёта, прав не выдаёт. |
| `/healthindicator` | `holycore.healthindicator.use` (default true) | Молча переключает показ индикатора здоровья (по умолчанию включён у всех). Список отключивших — `healthindicator.yml`. Индикатор рисует TAB по плейсхолдеру `%holyindicator_state%` (§4.9). |
| `/baltop`, `/baltop2`, `/baltop3` | `holycore.baltop.use` (default true) | Топ богачей по Vault: страницы 1–10 / 11–20 / 21–30. Перебор всех оффлайн-игроков в async-потоке, кэш на 60 с (один исполнитель на все три команды — общий кэш). Формат чисел — Locale.GERMAN (точка-разделитель тысяч). Во время пересчёта: «Топ считается, повторите через пару секунд.» |
| `/wextend` | `holycore.wextend.use` (default true) | Заглушка: «Команда /wextend была удалена. Расширение миров теперь происходит через инвестиции — /invest». |
| `/invest` | `holycore.invest.use` (default true) | Открывает меню инвестиций (§4.2.5). Только для игроков. |
| `/boosters` | `holycore.boosters.use` (default true) | Открывает меню бустеров (§4.1.2). Только для игроков. |
| `/tituls [ник]` | `holycore.tituls.use` (default true) | Меню титулов (§4.3.2). Только для игроков. |

### 4.7. Игровые правила (пакет `extensions` + `checker`)

#### 4.7.1. Сущности и дроп (`EntityExtension`)
- Крипер: порох пересчитывается — `nextInt(4)`: 0 → без изменений; 1 → случайное количество
  1..amount; 2–3 → порох изымается.
- Гаст: порох не выпадает. Виндикатор: изумруды не выпадают. Призыватель: тотем выпадает
  с шансом 1/25.
- `CreatureSpawnEvent`: NATURAL-спавн отменяется с вероятностью 11/12 (выживает 1/12);
  `BUILD_WITHER` отменяется всегда.
- `EntityExplodeEvent`: у END_CRYSTAL и CREEPER список блоков очищается (без разрушений).

#### 4.7.2. Опыт (`ExperienceExtension`)
- Смерть игрока: `keepLevel(true)`, `droppedExp(0)` — опыт не теряется и не выпадает.
- Подбор orb'а (`PlayerPickupExperienceEvent`): множитель по первому найденному праву:
  `experience.infinity` ×2, `experience.eternity` ×2, `experience.stinger` ×1.8,
  `experience.dragon` ×1.6, `experience.kraken` ×1.4, `experience.wither` ×1.2,
  `experience.ghast` ×1.2, `experience.mustang` ×1.1, `experience.griefer` ×1.1.

#### 4.7.3. Шалкеры, тотемы, щиты (`InventoryExtension`, `ObjectExtension`, `ShulkerTotemChecker`)
- **Шалкеры в эндер-сундуке запрещены** — перекрываются все пути: цифровые клавиши,
  shift-клик, swap offhand, collect-to-cursor, курсор, drag. Действие отменяется,
  action-bar «Вы не можете класть шалкера в эндер-сундук».
- **Не больше 1 шалкера в инвентаре**: периодическая задача (каждые 50 тиков) при >1
  показывает action-bar «Режим шалкера. Ливнешь — все шалкера выпадут.». При выходе/кике
  игрока все шалкеры сверх первого выпадают на землю.
- **Не больше 4 тотемов бессмертия**: при кликах — прогнозирующий подсчёт (учитывает PICKUP_*,
  PLACE_*, swap, hotbar-swap, drop, контейнеры); при превышении клик отменяется с action-bar
  «Вы не можете носить с собой больше x4 Тотем(ов) бессмертия». Периодическая проверка
  выбрасывает излишки на землю. Подбор пятого тотема с земли отменяется.
- **Щиты полностью запрещены**: крафт отменяется (`CraftingExtension`), клик по щиту в
  инвентаре удаляет его, использование в руке удаляет предмет.
- **Спавнер-яйца**: CREEPER/WITCH/VILLAGER spawn egg нельзя вставить в спавнер («Этого моба
  нельзя вставить в спавнер!»).

#### 4.7.4. Спавн-мир и спавнеры
- В мире `spawn` ПКМ по ENCHANTING_TABLE открывает стол зачарований, по ANVIL и
  SMITHING_TABLE — верстак (`SpawnExtension`).
- Установка SPAWNER отменяется, если в чанке уже ≥ 16 спавнеров (`SpawnerLimitExtension`,
  `SpawnerChunkChecker`; сообщение «достигнут лимит в 16 спавнеров»).

#### 4.7.5. Сообщения и вход (`ObjectExtension`, `JoinExtension`, `PlayerExtension`)
- Join/quit/death-сообщения сервера гасятся (`setJoinMessage(null)` и т.п.); при входе
  сбрасываются glowing и invulnerable; во всех мирах `ANNOUNCE_ADVANCEMENTS = false`
  (переприменяется на каждый вход).
- Вместо них: `alwaysjoinmessage` — сразу при входе; `settings.join.message` — с задержкой
  100 тиков (5 c), если нет права `holycore.disable_join_message`.
- Смерть: вместо общего сообщения убитому в личку «Вы убиты игроком <ник> на координатах
  X, Y, Z.» / «Вы погибли на координатах …».
- Кик с причиной «Kicked for spamming» отменяется.
- Выход: 50 частиц LARGE_SMOKE; кик: кольцо из 180 точек взрывных частиц радиуса 0.125.

#### 4.7.6. Mending-нерф (`ObjectExtension.onMending`)
- Только для брони (HELMET/CHESTPLATE/LEGGINGS/BOOTS): игроку возвращается 25 % опыта
  починки, ремонт уменьшается до `repair * 0.75 * mendingLevel / 2` (минимум 1, если есть
  износ).

#### 4.7.7. Команды и регионы
- `/tpa`/`/call <ник>`: если цель в PvP-режиме (`%antirelog_isinpvp%` == «yes») — отмена,
  «Игрок находится в режиме PvP!» (`CommandExtension`).
- `/warp logo`: без права `essentials.warp.logo` — отмена, «Такого варпа не существует.».
- `/sethome`: если игрок стоит в регионе WorldGuard, где он не owner и не member — отмена,
  «Вы не можете использовать эту команду в чужом регионе».
- **Команды по входу в регион** (`RegionCommandExtension`, конфиг `region-commands`):
  при пересечении границы блока проверяются регионы WG в точке; для региона из конфига
  команды выполняются один раз (пока игрок в регионе; трекинг `uuid:region`). Строка команды:
  подстановки `%player%`, `%uuid%`; префикс `console:` — выполнение консолью, иначе
  `player.performCommand`. Конфиг-пример: регион `rtp` → `rtp defualt`.

#### 4.7.8. PvP-трезубец (`TridentExtension`)
- Бросок: кулдаун TRIDENT 40 тиков (2 c); при активном кулдауне бросок отменяется.
  Запоминается точка броска.
- Попадание по игроку: требуется PvP-флаг WG (не DENY) в точке броска, жертвы и стрелка,
  иначе отмена. Урон = дистанция(жертва↔точка броска) × 0.5, ограничен `health жертвы − 2.0`
  (не убивает ниже 1 сердца). После удара: кулдаун 100 тиков (5 c), износ предмета
  `+damage × 0.85` (при превышении прочности трезубец удаляется), консольная команда
  `updatepvp <жертва> <стрелок>`.
- Не-игрок как стрелок, попадание не в игрока — событие отменяется / запись очищается.

#### 4.7.9. Vanish (`ObjectExtension`, требует Essentials)
- `PlayerAttemptPickupItemEvent`: если игрок в vanish Essentials — подбор отменяется.

### 4.8. Кастомные рецепты (`HolyCore.setupRecipes`)
| Ключ | Рецепт (shape) | Результат |
|---|---|---|
| `holycore:gapple` | 8× GOLD_BLOCK вокруг APPLE («AAA», «ABA», «AAA») | ENCHANTED_GOLDEN_APPLE ×1 |
| `holycore:ancientdebris` | 8× NETHERRACK вокруг NETHERITE_SCRAP | ANCIENT_DEBRIS ×1 |

Оба снимаются в `onDisable` (`Bukkit.removeRecipe` по ключам).

### 4.9. Плейсхолдеры (PlaceholderAPI)

#### Экспансия `holyinvest` (InvestPlaceholders, persist=true)
| Плейсхолдер | Значение |
|---|---|
| `%holyinvest_points%` | личные очки инвестиций (всего); оффлайн-контекст → «0» |
| `%holyinvest_week%` | очки за текущую неделю |
| `%holyinvest_level_<id>%` | уровень направления (`<id>` ∈ buyer, gunpowder, experience, automine, voting, treasures, borders) |
| `%holyinvest_progress_<id>%` | прогресс до следующего уровня |
| `%holyinvest_need_<id>%` | очков на уровень направления |
| `%holyinvest_bonus_<id>%` | величина бонуса в процентах (`round((bonus−1)×100)`) |
| `%holyinvest_time_<id>%` | время действия в секундах с учётом прокачки |

#### Экспансия `holyindicator` (HealthIndicator, persist=true)
| Плейсхолдер | Значение |
|---|---|
| `%holyindicator_state%` | `on` / `off` — включён ли индикатор здоровья у игрока |

#### Потребляемые внешние плейсхолдеры
| Плейсхолдер | Источник | Где используется |
|---|---|---|
| `%luckperms_suffix%` | LuckPerms | TitulMenu — текущий надетый титул |
| `%antirelog_isinpvp%` | анти-релоад плагин | CommandExtension — запрет /tpa в PvP |

### 4.10. Права (полный перечень из кода и plugin.yml)

| Право | Default | Назначение |
|---|---|---|
| `holycore.afk.use` | op (не объявлено) | команда /afk |
| `holycore.bc.use` | op (не объявлено) | команда /broadcast, /bc |
| `holycore.bc.bypass` | op (не объявлено; проверка в коде) | обход кулдауна /bc |
| `holycore.suicide.use` | op (не объявлено) | команда /suicide |
| `holycore.wolker.use` | true | команда /wolker |
| `holycore.healthindicator.use` | true | команда /healthindicator |
| `holycore.baltop.use` | true | команды /baltop, /baltop2, /baltop3 |
| `holycore.wextend.use` | true | команда /wextend |
| `holycore.invest.use` | true | команда /invest |
| `holycore.boosters.use` | true | команда /boosters |
| `holycore.tituls.use` | true | команда /tituls |
| `holycore.disable_join_message` | op (проверка в коде) | отключает отложенное join-сообщение |
| `experience.infinity` | op (проверка в коде) | ×2 к подбираемому опыту |
| `experience.eternity` | op (проверка в коде) | ×2 |
| `experience.stinger` | op | ×1.8 |
| `experience.dragon` | op | ×1.6 |
| `experience.kraken` | op | ×1.4 |
| `experience.wither` | op | ×1.2 |
| `experience.ghast` | op | ×1.2 |
| `experience.mustang` | op | ×1.1 |
| `experience.griefer` | op | ×1.1 |
| `essentials.warp.logo` | (право Essentials) | доступ к /warp logo |
| `tituls.<сезон>.<порядок>` (например `tituls.summer2024.1`) | — (выдаётся LuckPerms) | владение титулом; список прав динамический, из файлов DeluxeMenus |

Команда `/links` (и её алиасы) права не требует.

---

## 5. Конфигурация (`config.yml`)

| Параметр | Тип | Default | Использование |
|---|---|---|---|
| `database.host` | string | `localhost` | MySQL-хост |
| `database.port` | int | `3306` | MySQL-порт |
| `database.user` | string | `root` | пользователь MySQL (чтение: `database.user`) |
| `database.password` | string | `""` | пароль MySQL |
| `database.database` | string | `holycore` | имя БД |
| `firstjoincommands` | list | `['experience add %player% 1395 points']` | команды при первом входе, `%player%` → ник |
| `alwaysjoinmessage` | list | многострочный блок (набор в команду + реклама набора стажёров) | отправляется каждому при входе сразу; пустые строки пропускаются |
| `region-commands.<ключ>.enable` | bool | `true` | включён ли блок |
| `region-commands.<ключ>.region` | string | `"rtp"` (пример) | id региона WorldGuard (регистронезависимо) |
| `region-commands.<ключ>.commands` | list | `["rtp defualt"]` | команды по входу в регион; `%player%`, `%uuid%`; префикс `console:` — от консоли |
| `settings.connect` | string | `lobby` | **не используется кодом** (мёртвый ключ) |
| `settings.links.message` | list | блок «Информация о сервере» (TG, VK, сайт) | вывод /links |
| `settings.join.message` | list | блок о наборе в команду | вывод через 5 c после входа (если нет `holycore.disable_join_message`) |
| `settings.bc.enable` | bool | `true` | включена ли /bc |
| `settings.bc.cooldown` | int | `1800` | кулдаун /bc, секунды |
| `settings.bc.message` | list | `"&7[Обьявление] {color} {msg} [Автор: {player}]"` | шаблон broadcast; `{player}`, `{msg}`, `{color}` (→ пусто) |
| `settings.suicide.enable` | bool | `true` | включена ли /suicide |
| `settings.suicide.cooldown` | int | `10` | окно подтверждения, секунды |
| `settings.suicide.message` | list | предупреждение о самоубийстве | показ при первом вызове |
| `settings.suicide.message-died` | list | `"Прощай жесткий мир…"`, `"Игрок %player_suicide% покончил жизнь самоубийством."` | broadcast при смерти; `%player_suicide%` |

Цветовые коды: `&x&r&r&g&g&b&b` и `&c` (в новых классах также `&#rrggbb` через `Parser.color`).
`saveDefaultConfig()` вызывается при старте; команды `/… reload` в модуле нет (перечитать
конфиг можно только рестартом).

---

## 6. Хранение данных

### 6.1. yml-файлы в `plugins/HolyCore/` (создаются на лету)

**`boosters.yml`**
```
players.<uuid>.spent                      — всего сапфиров потрачено на бустеры
players.<uuid>.boosters.<id>.level        — купленный уровень
players.<uuid>.boosters.<id>.until        — epoch-ms окончания действия
```
Сохранение — целиком после каждой покупки.

**`invest.yml`**
```
week-start                                — дата понедельника текущей недели (ISO, МСК)
last-payout                               — дата последней ежедневной выплаты
categories.<id>.level                     — уровень направления (глобальный)
categories.<id>.progress                  — очки, накопленные к следующему уровню
players.<uuid>.points                     — личные очки всего
players.<uuid>.week                       — личные очки за неделю
players.<uuid>.name                       — ник (для топа)
```

**`healthindicator.yml`**
```
disabled                                  — список UUID отключивших индикатор
```

### 6.2. MySQL (создаётся автоматически при старте)

| Таблица | Поля | Назначение |
|---|---|---|
| `holy_first_join_rewards` | `id INT AI PK`, `player_uuid VARCHAR(36) UNIQUE`, `player_name VARCHAR(16)`, `reward_given BOOLEAN`, `created_at TIMESTAMP` | фиксация выдачи награды за первый вход |
| `holy_rewards` | `id INT AI PK`, `reward_type VARCHAR(20)`, `player_uuid VARCHAR(36)`, `player_name VARCHAR(16)`, `reward_given BOOLEAN`, `created_at TIMESTAMP` | журнал наград за достижения; `COUNT(*) WHERE reward_type=? AND reward_given=TRUE` — счётчик лимита |

- Подключение: `jdbc:mysql://<host>:<port>/<db>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`,
  чистый `DriverManager`, **без пула** — новое соединение на каждый запрос.
- Все запросы — `CompletableFuture.supplyAsync` (ForkJoinPool); SQL-ошибки проглатываются
  с возвратом `false`/`0`.
- Ошибка инициализации логируется `severe`, плагин продолжает работу.

---

## 7. Интеграции

| Система | Способ | Детали |
|---|---|---|
| PlaceholderAPI | 2 экспансии | `holyinvest` (§4.9), `holyindicator` (§4.9); регистрация только если PAPI включён; `persist()=true`. Потребление: `%luckperms_suffix%`, `%antirelog_isinpvp%` |
| Vault (Economy) | ServicesManager | монеты ¤: покупка очков инвестиций, топ `/baltop` (балансы всех оффлайн-игроков) |
| PlayerPoints | API (`PlayerPointsAPI`) | сапфиры ☀: `Sapphires.balance/take/give`; команда `p give <ник> <N> -s` в наградах. Сапфиры намеренно не через Vault |
| LuckPerms | консольные команды | `lp user <ник> meta setsuffix 1 "<текст>" server=lite`; `lp user <ник> meta clear suffix server=lite`; `lp user <ник> permission set/unset <право>`; права титулов как источник владения |
| WorldGuard / WorldEdit | API | `/sethome` в своём регионе; команды по регионам; PvP-флаг для трезубца |
| EssentialsX | API | vanish → запрет подбора предметов; `/warp logo` (право `essentials.warp.logo`) |
| DeluxeMenus | чтение yml | `plugins/DeluxeMenus/gui_menus/titul/*season*.yml` — источник титулов |
| LiteBans | консольная команда | `litebans broadcast …` — объявления о наградах |
| BungeeCord | канал | при старте регистрируется исходящий канал `BungeeCord` (в коде модуля не используется — задел на будущие пересылки) |
| TAB | косвенно | `%holyindicator_state%` для disable-condition belowname-объектива |
| Внешние плагины эффектов инвестиций | косвенно | плейсхолдеры `holyinvest_*` для конфигов скупщика/автошахт/голосований/сокровищ |

---

## 8. Сценарии работы по подсистемам

### 8.1. Личные бустеры
1. Игрок выполняет `/boosters` → меню с 5 бустерами, своими уровнями и сроками.
2. Клик по бустеру → проверка потолка → списание сапфиров (PlayerPoints API).
3. Уровень +1, срок +48 ч (или навсегда для скупщика), `boosters.yml` сохраняется.
4. Меню перерисовывается; бонусы применяют другие плагины сервера (HolyCore только хранит).
5. По истечении срока `level()` возвращает 0 — бустер «пропадает» без удаления записи.

### 8.2. Инвестиции
1. `/invest` → меню направлений с уровнями и прогрессом сервера.
2. Первый клик по направлению → слот превращается в «Вы уверены?».
3. Повторный клик → списание `price` монет (Vault) → +1 очко (личное и недельное) и +1 прогресс.
4. Прогресс достиг `need` → уровень направления растёт, границы миров пересчитываются,
   broadcast по серверу.
5. Раз в минуту: если наступил понедельник — недельные очки обнуляются; если новые сутки —
   2500 сапфиров делятся между топ-10 недельного рейтинга (пропорционально очкам, выдача
   по UUID через PlayerPoints).

### 8.3. Титулы
1. `/tituls` → постраничный список своих титулов (права LuckPerms), сортировка переключается.
2. ЛКМ — титул надевается (суффикс LuckPerms, контекст `server=lite`).
3. ПКМ — титул изымается в предмет NAME_TAG (право снимается); предмет можно передать/продать.
4. ПКМ с предметом в руке — титул активируется (право выдаётся, предмет расходуется).
5. `/tituls <ник>` — просмотр чужих титулов (только онлайн; клики заблокированы).
6. Кнопка «Снять активный титул» — суффикс очищается.

### 8.4. Награды за достижения / первый вход
1. Игрок получает advancement (или активирует портал в Энд с ≥12 глазами).
2. Асинхронный `COUNT` по типу в MySQL; если счётчик < лимита и в этой сессии не выдавалось —
   `p give` + `litebans broadcast`, запись в таблицу.
3. При первом в истории входе — `firstjoincommands` от консоли и запись в
   `holy_first_join_rewards`.

### 8.5. Игровые правила
- Выход игрока с 2+ шалкерами → лишние выпадают; тотемов больше 4 → выпадают (периодическая
  проверка раз в 2.5 c + при кликах/подборе).
- Попытка положить шалкер в эндер-сундук любым способом → отмена.
- Крипер умирает → порох режется случайно; гаст/виндикатор теряют дроп; спавн мобов 1/12.
- PvP: трезубец с дистанции наносит 0.5 урона за блок (не смертельный), /tpa в PvP запрещён.
- Вход в регион `rtp` → однократный запуск команд из конфига.

---

## 9. Особенности реализации и известные ограничения

- **plugin.yml без depend/softdepend** — порядок загрузки не гарантирован; все интеграции
  защищены runtime-проверками (`isPluginEnabled`, `getPlugin(...) != null`), при отсутствии —
  деградация с warning.
- **`api-version: 1.16` при сборке под Paper 1.21.4** — плагин полагается на новые API
  (`PlayerPickupExperienceEvent` — Paper; `Bukkit.removeRecipe` — 1.15+; `WorldBorder`);
  задекларированная api-версия занижена.
- **`settings.connect` не используется** (мёртвый ключ), исходящий канал `BungeeCord`
  регистрируется, но в модуле не используется.
- **Бустеры**: эффекты не применяются самим HolyCore (только хранение и меню); цены/прогрессии
  зашиты в код (`Boosters`/`Categories`), конфигурацией не управляются.
- **Титулы зависят от файлов другого плагина** (DeluxeMenus): смена формата меню ломает реестр;
  чужой список — только онлайн-цели; проверка «надет ли титул» деградирует при недоступности
  PAPI/LuckPerms (принимает «надет»).
- **MySQL без пула соединений** — соединение открывается на каждый запрос; при недоступной БД
  модуль продолжает работать, награды не фиксируются (in-memory наборы `Rewards` сбрасываются
  рестартом — защита от дубля остаётся за `COUNT` в БД).
- **Кулдауны `/bc`, `/suicide`, pending-инвестиции, `pendingSuicide`** — обычные `HashMap`
  (не потокобезопасны, но все обращения из main thread).
- **`BalTop`**: один исполнитель на три команды (общий кэш 60 c) — иначе тройной пересчёт;
  перебор `Bukkit.getOfflinePlayers()` тяжёлый, вынесен в async; записи без данных экономики
  пропускаются.
- **Даты инвестиций — по Europe/Moscow**; сверка по дате, а не по таймеру (переживает простои
  и рестарты; дата последней выплаты хранится в yml — защита от повторной выдачи).
- **`InventoryExtension.handleTotemCheck`** — сложный прогноз итогового количества тотемов по
  типам InventoryAction (включая контейнеры, hotbar-swap, drop); граничные случаи незакрытого
  инвентаря добирает периодический `ShulkerTotemChecker`.
- **Устаревшие API**: `PlayerPickupItemEvent` (deprecated, используется наряду с
  `PlayerAttemptPickupItemEvent`), `ItemStack.setDurability`, `Player.sendTitle`.
- **Сообщения и мультистрочные тексты** в основном зашиты в код (HEX-коды `&x&F&B…`);
  конфигом управляются только блоки из §5.
- **`EntityExtension.onCreatureSpawn`** отменяет 11/12 натуральных спавнов — тяжёлый слушатель
  на частое событие; `new Random()` создаётся на каждый вызов.
- **`TitulMenu`/`BoosterMenu`/`InvestMenu`** идентифицируют своё меню по строке заголовка —
  коллизия заголовков с меню других плагинов теоретически возможна.
- **`ObjectExtension.onCommand`**: `/sethome` отменяется, если игрок стоит хотя бы в одном
  регионе, где он не участник (даже если является участником соседнего пересекающегося).
- **Mending-нерф** применяется только к броне; оружие/инструменты чинятся как обычно.

---

## 10. Сборка

```
mvn -pl HolyCore package     # из каталога modules (или mvn package в HolyCore)
```

- Родительский POM `world.example:exampleworld-parent:1.0.0` задаёт `release 21`,
  репозитории (papermc, spigotmc, sonatype, jitpack) и общий `maven-shade-plugin 3.5.3`
  (package-phase; фильтр META-INF-подписей и module-info; релокаций для HolyCore нет —
  shade используется формально, зависимостей со scope compile у модуля нет).
- Все зависимости (`essentialsex`, `worldedit`, `worldguard`, `playerpoints`, `vault`,
  `placeholderapi`, `lombok`, `annotations`) — provided; Paper API — provided из родителя.
- Результат: `target/HolyCore-0.1.jar`.
