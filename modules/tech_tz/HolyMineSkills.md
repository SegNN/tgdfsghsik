# Техническое задание — модуль HolyMineSkills

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/HolyMineSkills`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | HolyMineSkills |
| Артефакт (Maven) | `HolyMineSkills` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `ua.holymine.holymineskills.HolyMineSkills` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | KotikStasika, HolyZako, Smail |
| Описание (plugin.yml) | «Современный плагин для системы бонусов и очков в автошахте с интеграцией WorldGuard, PlaceholderAPI и DecentHolograms» |

**Назначение:** плагин прогресса «квестовика Автошахты». Игрокам начисляются очки
(«опыт») за добычу блоков в шахтных регионах WorldGuard; за накопленные очки
автоматически разблокируются пассивные бонусы (скорость, удача, авто-плавка,
изумрудизация и др.). Плагин показывает меню бонусов (две страницы), голограмму
топ-10 игроков по очкам (DecentHolograms) и экспортирует статистику в PlaceholderAPI.
Прогресс хранится в MySQL.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot, API 1.16 (сборка под ветку mc-1.21.4, Java 21 — `maven.compiler.release=21`).
- MySQL-сервер: без подключения БД плагин отключает сам себя при старте.

### 2.2. Плагины (все перечислены одновременно в `depend` и `softdepend` plugin.yml — фактическая обязательность определяется кодом)
| Плагин | Версия (pom) | Область применения | Обязательность по коду |
|---|---|---|---|
| WorldEdit | 7.3.16 (provided) | Адаптация локаций для WorldGuard (`BukkitAdapter`) | Да (imports в листенерах) |
| WorldGuard | 7.0.13 (provided) | Определение регионов шахты `mine`/`mine2` в мире `spawn` | Да |
| PlaceholderAPI | 2.12.3 (provided) | Экспансия плейсхолдеров | Мягкая (без него плейсхолдеры не работают, плагин стартует) |
| DecentHolograms | 2.10.1 (provided) | Голограмма топа игроков | Мягкая (голограмма не создаётся) |

### 2.3. Библиотеки
- HikariCP 4.0.3 (scope `compile`, вшивается в jar родительским `maven-shade-plugin` без релокации).

### 2.4. Скрытые интеграции через консольные команды (см. §4.6)
- `KotikDang` (выдача универсального ключа), `LiteBans` (broadcast), зачарование
  `magnet` плагина `HolyEnchantments` (чтение PDC-ключа предмета).

---

## 3. Архитектура (структура пакетов)

```
ua.holymine.holymineskills
├── HolyMineSkills            — главный класс: последовательная инициализация ConfigManager →
│                               DatabaseManager → MenuManager/HologramManager/PlaceholderManager/
│                               CommandManager, регистрация событий и команды, отключение при ошибке
├── bonus
│   ├── BonusSystem           — статический реестр бонусов: 8 эффектов и 42 «бонуса» (эффект+уровень+
│   │                           порог очков+слот меню); выборка активных эффектов, проверка разблокировок,
│   │                           уведомление о новых уровнях
│   ├── BonusSystem.Bonus     — конкретный бонус (слот, эффект, уровень, requiredPoints, страница);
│   │                           строит предмет меню (LIME/GRAY pane + лор с прогрессом)
│   └── BonusSystem.BonusEffect — тип эффекта: имя, материал-иконка, массив значений по уровням (%)
├── bonus.BonusEffectsListener — применение эффектов: изумрудизация/ремонт/универсальный ключ при
│                                BlockBreakEvent, авто-плавка/удача/ярость/магнит при BlockDropItemEvent,
│                                скорость при PlayerMoveEvent; проверка региона шахты с кэшем
├── command.CommandManager    — исполнитель /mineskills: меню + админ-подкоманды add/remove/get/reset
├── command.CommandTabCompleter — tab-подсказки подкоманд, онлайн-игроков, количеств (1/10/100/1000)
├── config.ConfigManager      — загрузка/валидация config.yml, кэш «материал → очки», геттеры настроек
├── database.DatabaseManager  — MySQL через HikariCP: очки, счётчик блоков, история бонусов, топ;
│                                асинхронный пул из 2 потоков; кэш очков на 5 минут
│   ├── .PlayerData           — record-класс строки топа (uuid, username, points)
│   └── .PlayerStats          — обёртка списка PlayerData
├── event.BlockBreakListener  — начисление очков за добычу в регионе шахты (MONITOR), расчёт множителей,
│                                запись в БД, проверка новых разблокировок
├── hologram.HologramManager  — голограмма «points_top» (топ-10 по очкам), периодическое обновление
├── menu.MenuManager          — GUI-меню бонусов 54 слота, 2 страницы; рамка, инфо-книга (слот 45),
│   │                           лектерн с активными эффектами (слот 53), предметы бонусов
│   └── .MenuHolder           — маркерный InventoryHolder для идентификации меню
├── menu.MenuEventListener    — отмена кликов в меню, сообщение о статусе бонуса при клике
├── placeholder.PlaceholderManager — экспансия PlaceholderAPI (id `holymineskills`), кэш значений 30 с
│   └── .CachedValue          — значение + timestamp
└── util.Logger               — логгер с префиксом [HolyMineSkills] (цвета вырезаются перед выводом)
```

---

## 4. Функциональные требования

### 4.1. Начисление очков (BlockBreakListener, `BlockBreakEvent`, MONITOR, ignoreCancelled)
- Учитываются блоки, сломанные в «шахтной зоне»:
  - метод `isBlockInProtectedRegion`: если плагин WorldGuard не найден — `true` (весь мир);
    если мир называется `spawn` — `true` (**весь мир spawn, без проверки региона**);
    иначе — проверка вхождения в WG-регионы `mine` или `mine2`.
- Базовые очки за блок берутся из `blocks.<MATERIAL>` конфига (кэшируются по материалу;
  неизвестный материал → 0 очков, обработка пропускается).
- Итоговые очки = `basePoints * totalMultiplier`, где `totalMultiplier` собирается так:
  1. `+ quest_exp_%/100` — эффект «Опыт квестовика» (значение макс. разблокированного уровня);
  2. если блок `DIAMOND_ORE` и `random < emeraldization_%/100` — `+0.5`;
  3. `+ luck_%/100` — эффект «Удача»;
  4. если `random < rage_value` (0.001 / 0.002) — множитель `*2` («Ярость»).
- Обработка асинхронная: чтение текущих очков → запись `addPointsAsync` +
  `addTotalBlocksAsync(+1 блок)` → после записи `BonusSystem.checkForNewUnlocks`.

### 4.2. Система бонусов (BonusSystem, статическая инициализация)
Типы эффектов (значения по уровням, в %; уровень больше длины массива — берётся последний):

| Ключ эффекта | Название | Значения по уровням (1..N) | Смысл (из описаний в коде) |
|---|---|---|---|
| `emeraldization` | Изумрудизация | 0.1 / 0.2 / 0.3 | Превращает выпадающие алмазы в изумруды |
| `auto_smelting` | Авто-плавка | 100 / 200 / 300 | Сразу переплавляет руду при добыче |
| `luck` | Удача | 25 / 40 / 50 / 75 / 100 / 125 | Увеличивает количество выпадаемых ресурсов |
| `quest_exp` | Опыт квестовика | 10 / 15 / 30 / 45 / 60 / 75 | Увеличивает выпадаемый опыт |
| `repair` | Восстановление | 1.0 | Ремонтирует инструмент при добыче |
| `speed` | Скорость | 10 / 15 | Увеличивает скорость передвижения по автошахте |
| `rage` | Ярость | 0.001 / 0.002 | Режим ярости: ресурсы ×2 |
| `universal_key` | Универсальный ключ | 0.00001 / 0.00002 / 0.00003 / 0.00004 | Шанс получить уникальный ключ |

Полная таблица бонусов (`createBonus(эффект, страница, слот, requiredPoints, уровень)`) —
бонус разблокирован, когда очки игрока ≥ requiredPoints:

| Эффект | Стр. | Слот | Уровень | Порог очков |
|---|---|---|---|---|
| emeraldization | 1 | 10 | 1 | 250 |
| auto_smelting | 1 | 11 | 1 | 400 |
| luck | 1 | 12 | 1 | 500 |
| quest_exp | 1 | 14 | 1 | 2800 |
| repair | 1 | 15 | 1 | 3200 |
| emeraldization | 1 | 16 | 2 | 4000 |
| repair | 1 | 19 | 1 | 210 |
| speed | 1 | 21 | 1 | 600 |
| speed | 1 | 23 | 2 | 2400 |
| rage | 1 | 25 | 1 | 6400 |
| quest_exp | 1 | 28 | 1 | 175 |
| quest_exp | 1 | 30 | 2 | 700 |
| luck | 1 | 32 | 2 | 2000 |
| luck | 1 | 34 | 3 | 7040 |
| luck | 1 | 36 | 1 | 25 |
| speed | 1 | 37 | 1 | 125 |
| repair | 1 | 39 | 2 | 800 |
| emeraldization | 1 | 40 | 2 | 1000 |
| universal_key | 1 | 41 | 1 | 1600 |
| speed | 1 | 43 | 3 | 7680 |
| quest_exp | 1 | 44 | 3 | 8320 |
| speed | 2 | 10 | 4 | 15360 |
| quest_exp | 2 | 11 | 4 | 16640 |
| repair | 2 | 12 | 4 | 19200 |
| emeraldization | 2 | 14 | 5 | 44800 |
| universal_key | 2 | 15 | 2 | 51200 |
| luck | 2 | 16 | 5 | 56320 |
| luck | 2 | 19 | 4 | 14080 |
| emeraldization | 2 | 21 | 4 | 22400 |
| repair | 2 | 23 | 5 | 38400 |
| speed | 2 | 25 | 5 | 61440 |
| universal_key | 2 | 28 | 3 | 12800 |
| rage | 2 | 30 | 2 | 25600 |
| quest_exp | 2 | 32 | 5 | 33280 |
| quest_exp | 2 | 34 | 6 | 66560 |
| repair | 2 | 36 | 6 | 9600 |
| emeraldization | 2 | 37 | 6 | 11200 |
| universal_key | 2 | 39 | 4 | 25600 |
| luck | 2 | 40 | 6 | 28160 |
| speed | 2 | 41 | 6 | 30720 |
| repair | 2 | 43 | 7 | 76800 |
| emeraldization | 2 | 44 | 7 | 89600 |

- Пороги в рамках одной страницы не монотонны; разблокировка проверяется по каждому
  бонусу отдельно (`points >= requiredPoints`).
- `hasBonus(points, тип, minLevel)` — истинно, если максимальный разблокированный уровень
  эффекта ≥ minLevel (достаточно любого бонуса этого эффекта с нужным уровнем).
- Доступная страница меню: `getAvailablePage` — страница 2 при очках ≥ 8320, иначе 1.
- **Уведомление о новых уровнях** (`checkForNewUnlocks`): при росте очков через порог
  каждого задетого бонуса в БД пишется запись `bonus_history`; игроку отправляется
  сообщение про **старший** из новых бонусов:
  `&b▶ &fВы получили новый &bуровень &fэффекта <цветное имя> &fу квестовика Автошахт (&x&0&0&F&1&F&d/mineskills&f).`
  + звук `ENTITY_PLAYER_LEVELUP` (1.0/1.0).
- `getActiveEffects(points)` — список строк активных эффектов (макс. уровень каждого типа):
  `&5&o  &x&0&0&D&8&F&F• <имя> — <римская цифра уровня> &f(<значение>%)`.
- Цветовые коды имён эффектов и длинные описания зашиты в коде (`getColoredEffectName`,
  `getEffectLongDescription`).

### 4.3. Применение эффектов (BonusEffectsListener)
Региона шахты (`isInMineRegion`): мир обязан называться `spawn`, и локация должна лежать
в WG-регионах `mine` или `mine2` (кэш проверки на 10 с по игроку — только для скорости).

**`BlockBreakEvent` (HIGH, ignoreCancelled), только в регионе шахты:**
- **Изумрудизация** (только `DIAMOND_ORE`): шанс зависит от уровня —
  ур.2: 2.1, ур.3: 3.3, ур.4: 4.5, ур.5: 5.7, ур.6: 6.0, ур.7: 8.0 «из 500»
  (`random.nextInt(500) < chance`). При срабатывании: событие отменяется, блок
  заменяется на AIR, игроку выдаются EMERALD и DIAMOND, action-bar
  `&aИзумрудизация! &bАлмаз &fпревратился в &aизумруд &f(…/mineskills&f)`.
- **Восстановление** (`repair` ур.1+): шанс `random.nextInt(1000) == 0`; восстанавливает
  1–3 % макс. прочности инструмента в основной руке (ур.1–2: 1 %, ур.3–4: 2 %, ур.5–7: 3 %);
  action-bar + звук `ENTITY_EXPERIENCE_ORB_PICKUP`.
- **Универсальный ключ** (ур.1+): абсолютный шанс 1·10⁻⁶ / 2·10⁻⁶ / 3·10⁻⁶ / 4·10⁻⁶
  (по уровню). При срабатывании консоль выполняет:
  - `kotikdang give unic <ник>`;
  - `litebans broadcast &x&F&F&7&0&0&0▶ &fИгрок <ник> получил &x&F&F&7&0&0&0Универсальный ключ&f, копаясь в автошахте!`.

**`BlockDropItemEvent` (HIGH, ignoreCancelled), только в регионе шахты:**
- Блоки `PURPUR_BLOCK`, `PURPUR_PILLAR`, `PURPUR_SLAB`, `PURPUR_STAIRS`,
  `PURPLE_GLAZED_TERRACOTTA` — дроп **полностью удаляется** (`event.getItems().clear()`).
- Прочие блоки, для каждого предмета дропа:
  - **Авто-плавка** (ур.1+): материал заменяется на переплавленный с сохранением количества:
    IRON_ORE→IRON_INGOT, GOLD_ORE→GOLD_INGOT, ANCIENT_DEBRIS→NETHERITE_SCRAP,
    NETHER_GOLD_ORE→GOLD_INGOT, COBBLESTONE→STONE, CLAY→TERRACOTTA, CACTUS→GREEN_DYE,
    WET_SPONGE→SPONGE;
  - **Удача**: шанс 5/10/13/15 % (ур.1/2/3/4+) — количество предмета ×2, action-bar
    `&b▶ &fСработал эффект &eУдача&f &f(квестовик: Автошахта)`;
  - **Ярость**: шанс 3 % (ур.1) / 6 % (ур.2+) — количество ×3, action-bar
    `&b▶ &fСработал эффект <Ярость> &f(квестовик: Автошахта)`;
  - **Магнит** (зачарование плагина HolyEnchantments): если на инструменте в главной руке
    есть PDC-ключ `holyenchantments:holyenchantments-magnet` (INTEGER) — все предметы дропа
    сразу кладутся в инвентарь игрока (не поместившееся — дроп на землю), дроп очищается.

**`PlayerMoveEvent` (MONITOR, ignoreCancelled) — Скорость:**
- Проверка не чаще раза в 500 мс на игрока (только при смене X/Z).
- Вне региона шахты (кэш 10 с): walkSpeed принудительно `0.2`.
- В регионе: множитель по уровню `speed`: ур.1: ×1.1, ур.2: ×1.2, ур.3: ×1.3,
  ур.4: ×1.35, ур.5: ×1.4, ур.6+: ×1.5; итог `0.2 * множитель`, применяется только если
  игрок на земле, не летит и не плывёт. Action-bar «Скорость увеличена до N %» — не чаще
  раза в 20 с.

### 4.4. Команда `/mineskills`
Право на команду: `holymineskills.use` (по умолчанию `true`; проверяется Bukkit по
`permission` в plugin.yml). Команда только для игроков.

| Подкоманда | Право | Описание |
|---|---|---|
| `/mineskills` (без аргументов) | `holymineskills.use` | Открыть меню бонусов |
| `/mineskills openmenu` | `holymineskills.admin`* | Открыть меню бонусов |
| `/mineskills add <игрок> [кол-во]` | `holymineskills.admin` | Добавить очки (по умолчанию 1; форматируются `%.1f`) |
| `/mineskills remove <игрок> [кол-во]` | `holymineskills.admin` | Убрать очки (реализовано как add с отрицательным значением) |
| `/mineskills get <игрок>` | `holymineskills.admin` | Показать очки игрока |
| `/mineskills reset <игрок>` | `holymineskills.admin` | Сбросить очки в 0 (списание текущего значения) |

\* Любые аргументы, кроме пустого списка, требуют `holymineskills.admin`; при `args.length < 2`
выводится справка. Цель ищется через `Bukkit.getOfflinePlayer` (должен быть известен серверу).

Сообщения (зашиты в код): «Эта команда только для игроков!», «Игрок <ник> не найден!»,
«Добавлено X очков игроку <ник>», «Убрано X очков у игрока <ник>», «У игрока <ник>: X очков»,
«Очки игрока <ник> сброшены!», справка `/mineskills …`.

TabComplet: аргумент 1 — `openmenu/add/remove/get/reset`; аргумент 2 (для
add/remove/get/reset) — имена онлайн-игроков; аргумент 3 (add/remove) — `1/10/100/1000`.
Права в TabCompleter не фильтруются.

### 4.5. Права
| Право | Default | Использование |
|---|---|---|
| `holymineskills.use` | `true` | Доступ к команде `/mineskills` (plugin.yml, permission-message «У вас нет прав для использования этой команды!») |
| `holymineskills.admin` | `op` | Админ-подкоманды add/remove/get/reset(+openmenu) — проверяется в коде |
| `holymineskills.reload` | `op` | Объявлено в plugin.yml, **в коде не используется** |

### 4.6. GUI-меню (MenuManager + MenuEventListener)
- Инвентарь 54 слота, держатель `MenuHolder`.
- Заголовок: страница 1 — `Квестовик (Автошахта)`; страница 2 (очки ≥ 8320) —
  `Квестовик (Автошахта) 2`. Страница выбирается по очкам, переключения кнопками нет.
- Слоты 0–8 и 45–53 — рамка из `ORANGE_STAINED_GLASS_PANE` (« »).
- Слот 45 — `BOOK` «Кто такой квестовик?» (статический лор-справка).
- Слот 53 — `LECTERN`: лор «Вы имеете N опыта» + список активных эффектов
  (`&5&o  &x&0&0&D&8&F&F• …`) либо «эффекты отсутствуют».
- Слоты 10–44 — предметы бонусов текущей страницы (по слоту из таблицы §4.2):
  `LIME_STAINED_GLASS_PANE` «✔ Бонус получен» / `GRAY_STAINED_GLASS_PANE`
  «✘ Бонус ещё не получен»; лор: имя эффекта (+значение %), длинное описание,
  «Прогресс получения: <текущие очки> / <порог>».
- Все клики в меню отменяются; клик по бонусу асинхронно читает очки (**блокирующе**,
  `CompletableFuture.get()`) и отправляет сообщение «Бонус … уже разблокирован!» либо
  «Бонус … требует N очков».

### 4.7. Голограмма топа (HologramManager)
- Создаётся при старте (`DecentHolograms`, имя `points_top`), существующая с таким именем
  предварительно удаляется.
- Локация: мир `spawn` (fallback — первый мир сервера), координаты `hologram.points.{x,y,z}`
  из конфига; `displayRange = 48`.
- Обновление: синхронная repeating-задача, старт через 60 тиков, период
  `updateInterval * 20` тиков (updateInterval по умолчанию 60 с).
- Содержимое (обновляется через `DHAPI.setHologramLines` в главном потоке):
  - заголовок: `&fТоп по заработанным`, `&fочкам квестовика (Автошахта) - …/mineskills`;
  - топ-10 из БД: `#N ┆ <ник> <очков>` (пустые места `--- ---`);
  - футер: `&fОбновление раз в ~1 минуту`.
- Формат очков: `<N> опыта` (<1000), `<X.X>к опыта` (<1 000 000), `<X.X>кк опыта`.

### 4.8. Плейсхолдеры (PlaceholderAPI, идентификатор `holymineskills`; persist=true)
| Плейсхолдер | Значение |
|---|---|
| `%holymineskills_points%` | очки игрока, формат `%.0f` |
| `%holymineskills_points_formatted%` | `N` / `X.Xк` / `X.Xкк` |
| `%holymineskills_rank%` | место в топе (выборка топ-1000; если игрока нет — позиция за концом списка) |
| `%holymineskills_total_blocks%` | всего добыто блоков |
| `%holymineskills_next_bonus%` | строка «Следующий бонус: <очки+100> очков» (заглушка, реальная сетка бонусов не учитывается) |
| `%holymineskills_active_bonuses%` | количество активных типов эффектов |

Значения кэшируются на 30 с; чтение из БД с таймаутом ожидания 5 с (rank — 10 с);
при ошибке — `0` / `?` / «Информация недоступна» / «Ошибка».

### 4.9. События, обрабатываемые плагином
| Событие | Приоритет | Класс | Назначение |
|---|---|---|---|
| `BlockBreakEvent` | MONITOR, ignoreCancelled | BlockBreakListener | начисление очков |
| `BlockBreakEvent` | HIGH, ignoreCancelled | BonusEffectsListener | изумрудизация, ремонт, универсальный ключ |
| `BlockDropItemEvent` | HIGH, ignoreCancelled | BonusEffectsListener | авто-плавка, удача, ярость, магнит, вырезание пурпура |
| `PlayerMoveEvent` | MONITOR, ignoreCancelled | BonusEffectsListener | скорость |
| `InventoryClickEvent` | HIGH | MenuEventListener | защита меню, клики по бонусам |

---

## 5. Конфигурация (`config.yml`)

| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `database.host` | string | `""` | Хост MySQL (геттер fallback `localhost`) |
| `database.port` | int | `3306` | Порт MySQL |
| `database.database` | string | `""` | Имя БД (fallback `minecraft`) |
| `database.username` | string | `""` | Пользователь (fallback `root`) |
| `database.password` | string | `""` | Пароль (fallback `password`) |
| `hologram.points.x/y/z` | double | 31000.5 / 5100.0 / 146.5 | Координаты голограммы топа (валидация: ключи обязательны) |
| `blocks.<MATERIAL>` | double | см. файл | Очки за блок; в конфиге 30+ материалов от STONE 0.01 до ANCIENT_DEBRIS 1.0 (значение ≤0 игнорируется) |
| `debug` | boolean | `false` | Читается `isDebugEnabled()`, в коде не используется |
| `performance.cache_duration` | long | `300000` | Читается геттером; фактический кэш очков — константа 300 000 мс |
| `performance.hologram_update_interval` | int | `60` | Читается геттером; фактический интервал — поле `60` в HologramManager |
| `performance.placeholder_cache_duration` | long | `30000` | Читается геттером; фактический кэш плейсхолдеров — константа 30 000 мс |
| `notifications.bonus_unlock` | boolean | `true` | Читается геттером; уведомления в коде отправляются всегда |
| `notifications.points_gain` | boolean | `false` | Читается геттером, нигде не применяется |
| `notifications.error_logging` | boolean | `true` | Читается геттером, нигде не применяется |
| `max-level` | int | `35` | Объявлен в конфиге, кодом не читается |

Валидация при загрузке: обязательны все ключи `database.*`, `hologram.points.*` и секция
`blocks` — иначе плагин отключается. `reloadConfig()` у ConfigManager существует, командой
перезагрузки не предусмотрено.

---

## 6. Хранение данных (только MySQL)

Пул HikariCP: maxPoolSize 5, minIdle 2, connectionTimeout 10 000 мс, idleTimeout 300 000 мс,
maxLifetime 600 000 мс, leakDetectionThreshold 30 000 мс, кэш prepared-статементов.
URL: `jdbc:mysql://<host>:<port>/<db>?useSSL=false&autoReconnect=true&characterEncoding=utf8&useUnicode=true&serverTimezone=UTC`.
Запросы выполняются в собственном пуле из 2 daemon-потоков (`HolyMineSkills-DB-*`).

| Таблица | Схема | Использование |
|---|---|---|
| `player_points` | `uuid VARCHAR(36) PK`, `username VARCHAR(16) NOT NULL`, `points DOUBLE DEFAULT 0.0`, `total_blocks BIGINT DEFAULT 0`, `last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`, индексы `idx_username`, `idx_points`; InnoDB/utf8mb4 | очки и счётчик блоков; запись `INSERT … ON DUPLICATE KEY UPDATE points = points ± ?` / `total_blocks = total_blocks + ?`; чтение SELECT по uuid; топ — `ORDER BY points DESC LIMIT ?` |
| `bonus_history` | `id INT AUTO_INCREMENT PK`, `uuid VARCHAR(36)`, `bonus_name VARCHAR(50)`, `bonus_level INT`, `required_points DOUBLE`, `unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`, индексы `idx_uuid`, `idx_bonus_name`, `idx_unlocked_at` | журнал разблокировок бонусов (только вставка) |

Кэш: `playerPointsCache` — очки игрока на 5 минут (обновляется и при записи).
При остановке: пул потоков закрывается с ожиданием до 10 с, dataSource закрывается.

---

## 7. Интеграции
- **PlaceholderAPI** — экспансия `holymineskills` (§4.8); при отсутствии плагина — warning.
- **WorldGuard + WorldEdit** — регионы `mine`/`mine2` мира `spawn`.
- **DecentHolograms** — голограмма топа.
- **HolyEnchantments** — чтение PDC-ключа `holyenchantments:holyenchantments-magnet` (магнит).
- **KotikDang** — консольная команда `kotikdang give unic <ник>`.
- **LiteBans** — консольная команда `litebans broadcast …`.
- Vault/PlayerPoints — **не используются**.

---

## 8. Сценарий работы (основной поток)
1. При старте плагин валидирует конфиг, поднимает пул MySQL, создаёт таблицы,
   регистрирует листенеры, команду и плейсхолдеры, создаёт голограмму топа.
2. Игрок добывает блок в шахте (`spawn`, регионы `mine`/`mine2`).
3. BlockBreakListener (MONITOR) начисляет очки с множителями эффектов, инкрементирует
   счётчик блоков, проверяет новые пороги → уведомление + запись в `bonus_history`.
4. BonusEffectsListener (HIGH) параллельно применяет изумрудизацию/ремонт/ключ, а в
   BlockDropItemEvent — плавку/удачу/ярость/магнит.
5. При перемещении по шахте игроку повышается walkSpeed согласно уровню «Скорости».
6. `/mineskills` открывает меню: список бонусов, прогресс, активные эффекты.
7. Голограмма раз в минуту показывает топ-10 по очкам.
8. Админ управляет очками через `/mineskills add|remove|get|reset`.

---

## 9. Особенности реализации и известные ограничения
- Все параметры бонусов (пороги, шансы, множители, тексты) **зашиты в код** BonusSystem и
  листенеров; конфиг задаёт только очки за блоки, БД, голограмму и (частично неиспользуемые)
  флаги производительности/уведомлений.
- Значения эффектов в меню — «проценты» из BonusEffect, но часть механик использует
  собственные hardcoded-шансы (изумрудизация «из 500», ремонт 1/1000, удача 5–15 %,
  ярость 3/6 %, универсальный ключ 1–4·10⁻⁶ — при заявленных 1–4·10⁻⁵ %).
- `isBlockInProtectedRegion` (BlockBreakListener) засчитывает **весь мир `spawn`** без
  проверки региона, тогда как BonusEffectsListener требует именно `mine`/`mine2` — зоны
  действия очков и эффектов могут расходоваться.
- `MenuEventListener.handleBonusClick` вызывает `CompletableFuture.get()` блокирующе в
  главном потоке — при медленной БД возможны фризы клика.
- `notifications.*`, `performance.*`, `debug`, `max-level` фактически не влияют на поведение.
- Право `holymineskills.reload` объявлено, но не реализовано; TabCompleter не фильтрует
  подкоманды по правам.
- Экземпляр `Random` в HolyMobMoney-стиле создаётся на каждый вызов в нескольких местах
  (BlockBreakListener — один на класс, CommandManager — не использует).
- В `plugin.yml` одни и те же плагины перечислены и в `depend`, и в `softdepend`.
- Пул БД — 2 потока; при большой шахте операции выстраиваются в очередь.

## 10. Сборка
```
mvn -pl modules/HolyMineSkills package    # из корня репозитория
# или: cd modules/HolyMineSkills && mvn package
```
Родительский `maven-shade-plugin` вшивает HikariCP в jar (релокации нет);
worldedit/worldguard/decentholograms/placeholderapi — provided (берутся с сервера).
