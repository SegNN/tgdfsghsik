# Техническое задание — модуль HolyGoldSpawner

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/HolyGoldSpawner`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | HolyGoldSpawner |
| Артефакт (Maven) | `HolyGoldSpawner` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner` |
| API-версия Bukkit | 1.16 |
| Автор (plugin.yml) | lolodimo4ka |

**Назначение:** донат-предмет «Золотой Спавнер» — спавнер, который вместо мобов
приносит ближайшему игроку деньги. Пока рядом со спавнером стоит игрок, раз в
интервал проверки ему на баланс (Vault) начисляется случайная сумма монет.
Спавнер имеет прочность, которая тратится со временем (и может «сломаться»
раньше по вероятностной формуле), а плотная установка нескольких спавнеров
рядом снижает доход (штраф за соседей). Установка сопровождается глобальным
звуком и объявлением замаскированных координат; спавнер нельзя сломать обычным
способом (только «киркой Джейка» из HolyCustomEgg — на уровне сообщений; фактически
ломать запрещено полностью). Предмет выдаётся админ-командой или крафтом.

---

## 2. Окружение и зависимости

### 2.1. Сервер
- Paper/Spigot, API 1.16.

### 2.2. Плагины
| Плагин | Тип зависимости | Область применения |
|---|---|---|
| Vault (+ любой эконом-провайдер) | depend (жёсткая; без экономики плагин сам отключается) | Начисление монет (`Economy.depositPlayer`), чтение баланса |
| ProtectionStones | depend (объявлен; в коде напрямую не используется) | — |
| WorldEdit 7.3.16 | depend | Используется API адаптации (`BukkitAdapter`) |
| WorldGuard 7.0.13 | depend | Проверка права установки спавнера (флаг BUILD) |
| HolyCustomEgg (`local.holy:HolyCustomEgg:1.16.5`) | не объявлен в plugin.yml, подключается по имени в рантайме | «Кирка Джейка»: выдача, распознавание, крафт |
| HolySpawners | не объявлен, рефлексия в рантайме | Имя/лор обычного спавнера при создании предмета (см. §4.8) |

### 2.3. Библиотеки
- HikariCP 4.0.3 (compile — шейдится в jar).
- worldedit / worldguard / vault / HolyCustomEgg — provided.

---

## 3. Архитектура (структура пакетов)

```
ru.lolodimo4ka.holygoldspawner
├── HolyGoldSpawner        — главный класс: инициализация менеджеров, Vault-экономика,
│                            интеграция HolyCustomEgg, события установки/ломания/спавна,
│                            начисление монет,反射-интеграция HolySpawners
├── commands.SpawnerCommand — команда /holygoldspawner (give/reload + TabCompleter)
├── database.Database       — HikariCP + SQLite/MySQL, таблица golden_spawners
├── events.Config          — чтение config.yml (public-поля + геттеры), HEX-перевод цветов
├── events.SpawnerCraft    — регистрация крафтов (спавнер, обратный крафт, кирка Джейка),
│                            валидация матрицы в PrepareItemCraftEvent
├── managers.MessagesManager — чтение messages.yml, все тексты с HEX-цветами
├── managers.SpawnerManager  — реестр спавнеров в памяти, периодические задачи
│                              (доход, прочность, суточный рост, соседство), маскировка координат
├── protection.WorldGuardManager — проверка WorldGuard-регионов (BUILD/bypass)
└── utils.Item             — создание/распознавание предмета спавнера (PDC-метки)
└── utils.Sound            — звуки установки (ENTITY_WITHER_SPAWN)
```

---

## 4. Функциональные требования

### 4.1. Предмет «Золотой Спавнер»
- Материал — `settings.spawner.material` (default `SPAWNER`), имя и лор из конфига
  (HEX-цвета `&x&R&R&G&G&B&B` + `&`-коды).
- Лор-плейсхолдеры при создании: `%radius%`, `%min%`, `%max%`, `%durability%`,
  `%max_durability%`, `%break_chance%` (процент, формат `#0.0000`).
- Метки PersistentDataContainer (namespace плагина): `golden_spawner` (INTEGER, 1) —
  признак предмета; `durability` (INTEGER) — начальная прочность.

### 4.2. Установка спавнера (`BlockPlaceEvent`, priority HIGHEST)
1. Если ставится золотой спавнер (по PDC-метке):
   - мир должен называться `world` — иначе отмена + сообщение `player.wrong-world`;
   - `isSpawnerPlacementEnabled()` — всегда `true` в текущем коде (ветка
     `placement-disabled` фактически недостижима);
   - проверка WorldGuard `canPlaceSpawner` (флаг BUILD / bypass) — иначе тихая отмена;
   - регистрация в `SpawnerManager.addSpawner` (мир/владелец/прочность из предмета) и в БД.
2. Через 1 тик, если блок на месте: всем игрокам онлайн — звук
   `ENTITY_WITHER_SPAWN` (дважды: playPlacementSound и playWitherSound) и,
   если включена «шифровка координат» (в коде всегда `true`) и формат не пуст, —
   broadcast сообщения `coords.message-format` с `%x_mask%`, `%x_full%`, `%y_mask%`,
   `%y_full%`, `%z_mask%`, `%z_full%`.
3. При регистрации спавнер настраивается: тип сущности `PIG` (`getDisplayEntity()`
   возвращает константу; при невалидном значении — fallback PIG), `delay = 32767`
   (мобы не спавнятся).

### 4.3. Маскировка координат (`SpawnerManager.maskCoordinates/applyMasking`)
- Y (высота) не маскируется. X и Z маскируются, если `coordsEncryptionEnabled`
  (в коде всегда `true`).
- Шаблон по числу цифр модуля координаты (константы в Config, НЕ читаются из yml):
  1 цифра — `*`, 2 — `•*`, 3 — `*•*`, 4+ — `•*•*`. Символ `•` (U+2022) заменяется
  на очередную цифру координаты, `*` остаётся как есть. Знак «минус» сохраняется.
  Пример: X=1234 → «1•2•»-подобная строка по шаблону `•*•*` → `•1•2` (первая цифра
  скрыта, видна часть цифр).

### 4.4. Доход (`startSpawnerCheckTask`, каждые `check-interval` секунд)
- Для каждого зарегистрированного спавнера (блок на месте, иначе удаление из реестра):
  ищется ближайший игрок в радиусе `spawner-radius` (тот же мир, без кулдауна
  `playersWithCooldown`).
- Если игрок найден: случайная сумма `min-coins..max-coins` (текущие значения,
  см. суточный рост), округление до 2 знаков (`Math.floor(x*100)/100`), затем
  применяется штраф соседей: `coins *= (1 - penalty)`, снова округление.
- Штраф соседей (`neighbor-penalty`): если включён — считается число других золотых
  спавнеров в радиусе `radius` в том же мире; `penalty = min(n * penalty-per-spawner,
  max-penalty) / 100` (в долях).
- Начисление в main-потоке: `economy.depositPlayer(player, coins)`; при
  `showMessages` (всегда true) — сообщение `player.coin-received` с `%coins%`
  (формат `#0.00`) и `%balance%` (текущий баланс).
- Задача плотности `checkSpawnerDensity()` — пустая (заглушка).

### 4.5. Суточный рост дохода (`daily-increase`, если включён)
- Каждые `interval` секунд (default 86400 = сутки): `currentMinCoins += min-increase`,
  `currentMaxCoins += max-increase`.
- `resetMoneyUp()` сбрасывает текущие границы к базовым (вызывается извне/зарезервировано).

### 4.6. Прочность (`startDurabilityCheckTask`, каждые `durability-check-interval` секунд)
- Для каждого спавнера: если блока нет — удаление из реестра и БД.
- Прочность уменьшается на `durability-loss-per-check`, но только если спавнер
  активен (рядом стоял игрок) либо `only-when-active` = false (в конфиге-ресурсе
  ключа нет, default true).
- Обновление пишется в БД (`updateDurability`).
- Разрушение: прочность достигла 0 **или** сработала вероятность
  `checkSpawnerBreak`: `breakChance = (max-durability - durability) * break-chance-multiplier`;
  `Math.random() < breakChance` → спавнер удаляется, блок заменяется на AIR.

### 4.7. Защита спавнера
- `BlockBreakEvent` (HIGHEST): если локация в реестре — отмена. Сломать золотой
  спавнер нельзя вообще (сообщение `must-use-jackhammer` и механика кирки Джейка
  в коде загружаются, но ветка ломания отсутствует).
- `PlayerInteractEvent` (HIGHEST): ПКМ по золотому спавнеру любым спавн-яйцом
  (материал заканчивается на `_spawn_egg`) — отмена (нельзя подменить моба).
- `SpawnerSpawnEvent`: для золотых спавнеров спавн мобов подавляется
  (`preventMobSpawning` — в коде всегда false, т.е. событие не отменяется),
  но таймер спавнера сбрасывается на 32767 (`resetSpawnerTimer`).

### 4.8. Интеграция HolySpawners (рефлексия)
`createSpawnerWithHolySpawnerData(EntityType)` — если на сервере есть плагин
`HolySpawners`, через рефлексию (`getInstance` → `getConfigManager` →
`getSpawnerName(String)`, `getSpawnerLore(String)`) создаётся обычный спавнер
с именем/лором из HolySpawners. Иначе — спавнер с именем `§f<ENTITY_TYPE>`.
В текущем модуле метод externally не вызывается (точка расширения).

### 4.9. Команда `/holygoldspawner` (алиасы `hgs`, `goldspawner`)
| Подкоманда | Право | Описание |
|---|---|---|
| (без аргументов) / `help` | `holygoldspawner.admin` (право команды из plugin.yml) | Справка |
| `give spawner <игрок> [прочность] [количество]` | `holygoldspawner.give` | Выдать предмет спавнера; прочность 1..max-durability (default max), количество (default 1), стопки по 64, при полном инвентаре — дроп под ноги |
| `give pickaxe <игрок> [количество]` | `holygoldspawner.give` | Выдать «кирку Джейка» из HolyCustomEgg (если плагин подключен; молча ничего не выдаёт иначе) |
| `reload` | `holygoldspawner.reload` | Перечитать config.yml, messages.yml, сбросить границы дохода, перенастроить все спавнеры, перезагрузить WorldGuard-интеграцию |

TabComplete: подкоманды `give`/`reload`; для `give` — `spawner`/`pickaxe`; ники
онлайн-игроков; подсказки `<прочность>`/`<количество>`.

### 4.10. Крафты (SpawnerCraft)
| Ключ рецепта | Схема | Ингредиенты | Результат |
|---|---|---|---|
| `gold_spawner_craft` | `SSS / SNS / SSS` | S = SPAWNER, N = NETHER_STAR | 1 золотой спавнер с максимальной прочностью |
| `gold_spawner_reverse` | `G` | G = SPAWNER (только НЕ золотый, проверяется в PrepareItemCraft) | 8 обычных SPAWNER |
| `jake_pickaxe_craft` | `ABA / BCB / ABA` | A = REDSTONE_BLOCK, B = TNT (особый, см. ниже), C = GOLDEN_PICKAXE | Кирка Джейка (HolyCustomEgg) |

Валидация в `PrepareItemCraftEvent`:
- для обратного крафта: если в матрице есть золотой спавнер — результат обнуляется
  (золотой нельзя разобрать в 8 обычных);
- для крафта кирки Джейка: все 4 предмета в слотах TNT обязаны иметь PDC-метку
  `holytnt:tnt` (string) со значением `stealer` (особое ТрТ из плагина HolyTnT),
  иначе результат обнуляется.

---

## 5. Конфигурация

### 5.1. `config.yml`
| Параметр | Тип | Default (код) | Описание |
|---|---|---|---|
| `database.type` | string | `""` (пусто → ветка sqlite) | `mysql` или sqlite (всё остальное = sqlite) |
| `database.host` | string | `localhost` | Хост MySQL |
| `database.port` | int | `3306` | Порт MySQL |
| `database.database` | string | `minecraft` | Имя БД |
| `database.user` | string | `root` | Пользователь |
| `database.password` | string | `""` | Пароль |
| `settings.check-interval` | int | `5` | Период начисления монет, сек |
| `settings.spawner-radius` | int | `6` | Радиус действия спавнера, блоки |
| `settings.min-coins` / `max-coins` | double | `5900.0` / `6300.99` | Границы случайной суммы за интервал |
| `settings.spawner-placement.enabled` | boolean | `true` | (читается только при загрузке; геттер всегда возвращает true) |
| `settings.spawner-placement.sound` | string | `ENTITY.WITHER.SPAWN` | (не используется — звук захардкожен) |
| `settings.spawner.material` | string | `SPAWNER` | Материал предмета |
| `settings.spawner.name` | string | HEX-строка «Золотой Спавнер» | Имя предмета |
| `settings.spawner.lore` | list[string] | многострочный лор с `%durability%` | Лор предмета |
| `settings.durability.max-durability` | int | `5000` | Максимальная прочность |
| `settings.durability.durability-check-interval` | int | `600` (в ресурсе yml — 1000) | Период списания прочности, сек |
| `settings.durability.durability-loss-per-check` | int | `1` | Списание за проверку |
| `settings.durability.break-chance-multiplier` | double | `0.0001` | Множитель вероятности поломки |
| `settings.neighbor-penalty.enabled` | boolean | `true` | Штраф за соседние спавнеры |
| `settings.neighbor-penalty.radius` | int | `10` | Радиус учёта соседей |
| `settings.neighbor-penalty.penalty-per-spawner` | double | `3.0` (в yml — 1) | Проценты штрафа за каждого соседа |
| `settings.neighbor-penalty.max-penalty` | double | `60.0` (в yml — 50) | Потолок штрафа, % |
| `settings.neighbor-penalty.check-interval` | int | `1` | Период задачи соседства (сама проверка выполняется при каждом начислении) |
| (код) `settings.durability.only-when-active` | boolean | `true` | Прочность тратится только у активных спавнеров |
| (код) `settings.daily-increase.enabled/min-increase/max-increase/interval` | bool/double/double/int | `true` / `100.0` / `200.0` / `86400` | Суточный рост границ дохода |

Часть геттеров Config (`isShowMessages`, `isPreventMobSpawning`, `isSpawnerPlacementEnabled`,
`isCoordsEncryptionEnabled`, `getCustomDigits`, `getPattern*`, `getDisplayEntity`,
`getPlacementSound`) возвращают константы и конфиг не читают (см. §9).

### 5.2. `messages.yml` (ключ — плейсхолдеры)
| Ключ | Плейсхолдеры | Назначение |
|---|---|---|
| `player.wrong-world` | — | Установка не в мире `world` |
| `player.economy-disabled` | — | (загружается, не используется) |
| `player.must-use-jackhammer` | — | (загружается, не используется) |
| `player.placement-disabled` | — | Запрет установки (ветка недостижима) |
| `player.coin-received` | `%coins%`, `%balance%` | Начисление монет |
| `player.protection-near-spawner`, `player.spawner-protected` | — | (загружаются, не используются) |
| `player.inventory-full` | — | (загружается, не используется) |
| `player.no-permission`, `player.not-found`, `player.reload-success` | — | Стандартные ответы команды |
| `command.usage-give`, `command.usage-give-spawner`, `command.usage-give-pickaxe`, `command.unknown-item` | `%item%` | Использование команды |
| `command.help-header`, `help-give-spawner`, `help-give-pickaxe`, `help-reload`, `help-footer` | — | Справка (help-reload из default-строки кода) |
| `give.spawner-given`, `give.jackhammer-given` | `%player%`, `%amount%` | Успешная выдача |
| `give.durability-number-error`, `give.amount-number-error` | — | Ошибки разбора чисел |
| `coords.message-format` | `%x_mask%`, `%x_full%`, `%y_mask%`, `%y_full%`, `%z_mask%`, `%z_full%` | Broadcast при установке спавнера |

Цвета: `translateHexColors` — `&x`-последовательности и `&`-коды в `§`.

---

## 6. Хранение данных

### 6.1. База данных (`Database`, HikariCP)
- Тип: `database.type` = `mysql` → MySQL (пул 10 соединений, оптимизации prep-stmt);
  иначе SQLite-файл `<dataFolder>/spawners.db` (WAL, NORMAL).
- Таблица (создаётся автоматически):
```sql
CREATE TABLE IF NOT EXISTS golden_spawners (
  id ..., world <VARCHAR/TEXT>, x INT, y INT, z INT,
  owner_uuid <VARCHAR(36)/TEXT>, durability INT, placement_time BIGINT,
  UNIQUE (world, x, y, z))
```
- Операции: `addSpawner` (INSERT, async), `removeSpawner` (DELETE по world+x+y+z, async),
  `updateDurability` (UPDATE, async), `loadSpawners` (SELECT *, построчный runTask в
  main-поток через `addSpawnerToMemory`), `saveSpawners(Sync)` (транзакция: DELETE всех +
  батч-INSERT по 100, вызывается при отключении плагина), `closeConnection`.
- Загрузка данных отложена на 60 тиков после старта (`runTaskLater`).

### 6.2. Память (`SpawnerManager`)
`ConcurrentHashMap`-коллекции: `goldenSpawners` (Set<Location>), `spawnerDurability`,
`spawnerOwners` (UUID владельца), `spawnerActivity` (был ли игрок рядом),
`spawnerPlacementTime` (epoch millis), `playersWithCooldown` (Set<UUID> — API для
внешнего исключения игрока из дохода).

---

## 7. Интеграции
- **Vault** — экономика: `depositPlayer`, `getBalance`. Отсутствие Vault/провайдера
  отключает плагин на старте.
- **WorldGuard/WorldEdit** — проверка права установки спавнера (BUILD-флаг или bypass).
- **HolyCustomEgg** — прямая зависимость по классам (provided): `getJakePickaxe()`,
  `isJakePickaxe(ItemStack)`.
- **HolyTnT** — не зависимость, а PDC-метка `holytnt:tnt = stealer` на предметах TNT
  в крафте кирки.
- **HolySpawners** — рефлексивная интеграция (имя/лор спавнеров).
- PlaceholderAPI — не используется.

---

## 8. Сценарий работы
1. Админ выдаёт предмет `/hgs give spawner <игрок> [прочность] [кол-во]` (или игрок
   крафтит 8 спавнеров + незер-звезда).
2. Игрок ставит спавнер в мире `world` на незаприваченной чужими регионами территории.
3. Всем онлайн проигрывается звук визера, в чат уходит объявление с частично
   скрытыми координатами.
4. Пока игрок стоит в радиусе 6 блоков, каждые 5 сек ему начисляется случайная сумма
   (5900–6300.99 базово, растёт каждые сутки на 100/200, минус штраф за соседние
   спавнеры в радиусе 10 блоков — до 50–60%).
5. Каждые 1000 сек у активного спавнера списывается 1 прочность; при 0 или по
   формуле вероятности спавнер самоликвидируется (блок → AIR, записи чистятся).
6. Спавнер нельзя сломать, нельзя подменить яйцом; мобы из него не появляются
   (delay 32767 + сброс таймера).
7. При выключении сервера все спавнеры синхронно сохраняются в БД; при старте —
   загружаются через 3 секунды.

---

## 9. Особенности реализации и известные ограничения
- Много «замороженных» настроек: геттеры Config для сообщений, запрета мобов,
  шифровки координат, паттернов маскировки, звука и сущности возвращают константы —
  соответствующие секции yml (`spawner-placement.sound`, `prevent-mob-spawning`,
  паттерны) на поведение не влияют.
- Часть сообщений (`must-use-jackhammer`, `economy-disabled`, `protection-near-spawner`,
  `spawner-protected`, `inventory-full`) загружаются из messages.yml, но нигде не
  отправляются.
- Задача `checkSpawnerDensity` пустая — отдельный интервал соседства ни на что не влияет
  (штраф считается при каждом начислении).
- Мир установки захардкожен: `world` (сообщение говорит «в мире РТП»).
- Регистр спавнеров хранит `Location` как ключ: загрузка из БД создаёт новые объекты
  `Location` (поequals — совпадение по миру/координатам работает, но `hasSpawnerAtExactLocation`
  продублирован отдельным ручным сравнением).
- Хранение полного снимка таблицы через DELETE+INSERT при каждом сохранении —
  при большом числе спавнеров возможны потери между автосейвами (сохранение только
  на onDisable; инкрементальные UPDATE идут только по прочности).
- Права: `holygoldspawner.admin` (вход в команду, plugin.yml), `holygoldspawner.give`,
  `holygoldspawner.reload` (в коде).

## 10. Сборка
```
mvn -pl HolyGoldSpawner package     # из каталога modules
```
HikariCP 4.0.3 компилируется в jar (shade); worldedit/worldguard/vault/HolyCustomEgg —
provided, должны присутствовать на сервере или в локальном репозитории `local.plugins`/`local.holy`.
