# Техническое задание — модуль HolyRTP

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/HolyRTP`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | HolyRTP |
| Артефакт (Maven) | `HolyRTP` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `ua.holymine.holyrtp.HolyRTP` |
| API-версия Bukkit | 1.16 |
| Авторы | Smail, HolyZako |
| Команда | `/rtp` (без алиасов) |
| Разработчик (баннер) | студия HolyDev (t.me/ExampleTG) |

**Назначение:** случайная телепортация (RTP) четырёх видов: обычная (с задержкой и
отменой при движении), дальняя, телепортация к случайному игроку и к случайной
«базе» (региону WorldGuard/ProtectionStones, принадлежащему игроку).每种 вид имеет
собственный кулдаун, права и параметры поиска безопасной точки.

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot, API 1.16 (сборка против paper-api 1.21.4 из родительского POM, Java 21).
- WorldBorder целевого мира учитывается при выборе точки.

### 2.2. Плагины (softdepend: WorldGuard, ProtectionStones)
| Плагин | Версия (pom) | Область применения | Обязательность |
|---|---|---|---|
| WorldGuard | 7.0.13 (worldedit 7.3.16, provided) | вид RTP `base`: поиск регионов игрока | Мягкая (проверка `getPlugin(...) != null` в рантайме) |
| ProtectionStones | — | вид RTP `base`: регионы с ID, начинающимся на `ps` | Мягкая |
- Доступ к регионам в обоих случаях идёт через WorldGuard API
  (`RegionContainer`/`RegionManager`); плагин ProtectionStones отдельно не используется API-но.
- Конфиг содержит комментарий к `base.use-worldguard: true` — «времено не роботает»
  (код при этом ветку WorldGuard выполняет).

### 2.3. Библиотеки
- worldedit / worldguard (provided, из локального репозитория `local.plugins`).

---

## 3. Архитектура (структура пакетов)

```
ua.holymine.holyrtp
├── HolyRTP                      — главный класс: синглтон, инициализация менеджеров,
│                                  регистрация команды /rtp и PlayerMoveListener
├── commands.RtpCommand          — CommandExecutor+TabCompleter: разбор вида RTP,
│                                  проверки прав/кулдауна, запуск телепортации
├── config.ConfigManager         — доступ ко всем ключам config.yml (типизированные геттеры)
├── listeners.PlayerMoveListener — отмена RTP-телепортации при движении игрока
├── managers
│   ├── CooldownManager          — in-memory кулдауны per-игрок per-вид (мс до истечения)
│   ├── RegionManager            — поиск регионов игрока (WorldGuard + ProtectionStones),
│   │                              случайный выбор центра региона
│   └── TeleportManager          — логика всех видов телепортации, отсчёт задержки
│                                  (ActionBar), отмена, расчёт кулдауна
└── utils
    ├── ActionBarUtils           — отправка/очистка ActionBar (bungee-chat)
    ├── ColorUtils               — цвета: &-коды, &#rrggbb, &x&r&g&b..., &rrggbb
    └── LocationUtils            — случайные координаты, проверка безопасности и границ мира
```

---

## 4. Функциональные требования

### 4.1. Команда `/rtp [вид]`
| Форма | Право | Поведение |
|---|---|---|
| `/rtp` | `rtp.use` | RTP вида `default` |
| `/rtp default` | `rtp.use` | обычная телепортация с задержкой |
| `/rtp long` | `rtp.use` + `rtp.long` | дальняя телепортация |
| `/rtp player` | `rtp.use` + `rtp.player` | телепортация к случайному игроку |
| `/rtp base` | `rtp.use` + `rtp.base` | телепортация к своей базе |
- Неизвестный/отсутствующий аргумент → вид `default`.
- Консоль: сообщение «это консоль».
- Мир отправителя должен входить в `settings.allowed-worlds`, иначе `messages.world-not-allowed`.
- Все виды, кроме default, при отключённом флаге `<вид>.enabled` — hardcoded-сообщение
  `&c<Вид> RTP is disabled.`.

### 4.2. Права (plugin.yml)
| Право | Default | Назначение |
|---|---|---|
| `rtp.use` | true | базовый доступ к `/rtp` |
| `rtp.long` | op | вид `long` |
| `rtp.player` | op | вид `player` |
| `rtp.base` | op | вид `base` |
| `rtp.bypass.delay` | op | обход задержки перед телепортацией |
| `rtp.bypass.cooldown` | op | обход кулдаунов всех видов |

### 4.3. Кулдауны
- In-memory `Map<UUID, Map<вид, Long>>` (время истечения, мс); сбрасываются при
  перезапуске сервера. Проверка перед запуском; остаток в секундах подставляется в
  `%time%` сообщений `cooldown-default` / `cooldown-custom`.
- Обход — право `rtp.bypass.cooldown`.
- Кулдаун устанавливается **после** успешной телепортации (для `default` — по завершении
  отсчёта).

### 4.4. Вид `default`
1. Если действует `rtp.bypass.delay` либо игрок находится в мире `settings.spawn-world`
   и включён `default.bypass-delay-in-spawn` — телепортация сразу.
2. Иначе — задача с периодом 20 тиков на `default.teleport-delay` секунд: ActionBar
   `messages.teleporting` (с `%time%`); запоминается позиция на старте.
3. Движение (смещение `distanceSquared > 0.01`) → отмена: сообщение
   `messages.teleport-cancelled`, ActionBar очищается. Выход игрока — отмена задачи.
4. Телепортация: случайная точка радиуса `default.min-radius..max-radius`
   (квадратное распределение: |x| и |z| независимо равномерны в [min, max),
   знак случайный), затем `messages.teleport-success` (`%x% %y% %z%`).
5. Кулдаун `default.cooldown`.

### 4.5. Вид `long`
Без задержки: случайная точка в мире `settings.target-world`, радиус
`long.min-radius..max-radius`; кулдаун `long.cooldown`.

### 4.6. Вид `player`
1. Сбор валидных целей среди онлайн-игроков: не сам игрок, не в мирах
   `player.excluded-worlds`; при `ignore-vanished-players: true` — без метадаты
   `vanished` и видимых игроку (`canSee`); при `ignore-invisible-players: true` — без
   эффекта невидимости.
2. Если целей меньше `player.min-players-online` (или 0) — `messages.not-enough-players`.
3. Случайная цель; точка вокруг неё радиусом 10..50 (`getRandomLocationAroundPoint`);
   кулдаун `player.cooldown`.

### 4.7. Вид `base`
1. Список центров регионов игрока (миры, кроме `base.excluded-worlds`):
   - WorldGuard-регионы, где игрок owner или member (при `base.use-worldguard`);
   - регионы ProtectionStones (ID начинается с `ps`, owner/member) при
     `base.use-protectionstones`.
   Центр = (min+max)/2, Y = highest block.
2. Пустой список → `messages.no-bases-found`.
3. Случайный регион; точка вокруг центра радиусом `base.min-radius-from-region..max-radius-from-region`;
   кулдаун `base.cooldown`.

### 4.8. Выбор безопасной точки (LocationUtils)
- `getRandomLocation`: до 50 попыток; координата должна быть внутри WorldBorder мира;
  Y = `getHighestBlockYAt + 1`; проверка безопасности; иначе возврат точки спавна мира.
- `getRandomLocationAroundPoint`: до 30 попыток; случайный угол + радиус; внутри
  WorldBorder + безопасность; иначе возвращается сам центр.
- Безопасность (`isSafeLocation`): блок в ногах и над головой — AIR; блок под ногами —
  твёрдый; сам блок/под/над и все 8 соседних по горизонтали не входят в список
  `unsafe-blocks` (LAVA, FIRE, CACTUS, MAGMA_BLOCK, SWEET_BERRY_BUSH, CAMPFIRE,
  SOUL_CAMPFIRE — по конфигу).

### 4.9. Сообщения (config.yml, плейсхолдеры)
| Ключ | Плейсхолдеры |
|---|---|
| `teleporting` | `%time%` (ActionBar) |
| `teleport-cancelled` | — |
| `teleport-success` | `%x%`, `%y%`, `%z%` |
| `cooldown-default` / `cooldown-custom` | `%time%` (сек) |
| `world-not-allowed`, `not-enough-players`, `no-permission`, `no-bases-found` | — |

### 4.10. TabComplete
- Первый аргумент: `default` (без проверки прав), `long`/`player`/`base` — только при
  соответствующих правах; фильтрация по префиксу.

---

## 5. Конфигурация (config.yml)

| Параметр | Тип | Default (файл) | Default (код, если ключа нет) | Описание |
|---|---|---|---|---|
| `settings.allowed-worlds` | list | `[world, spawn]` | — | миры, откуда разрешён `/rtp` |
| `settings.target-world` | string | `world` | `world` | мир назначения default/long |
| `settings.spawn-world` | string | `spawn` | `spawn` | мир спавна (bypass задержки) |
| `default.enabled` | bool | `true` | `true` | включён ли вид default |
| `default.teleport-delay` | int | `3` | `3` | задержка, сек |
| `default.cooldown` | int | `10` | `10` | кулдаун, сек |
| `default.min-radius` / `max-radius` | int | `100` / `1000` | `1000` / `10000` | радиусы поиска точки |
| `default.bypass-delay-in-spawn` | bool | `true` | `true` | без задержки из мира спавна |
| `long.enabled` | bool | `true` | `true` | |
| `long.cooldown` | int | `60` | `45` | |
| `long.min-radius` / `max-radius` | int | `1000` / `3000` | `15000` / `30000` | |
| `player.enabled` | bool | `true` | `true` | |
| `player.cooldown` | int | `1200` | `60` | |
| `player.min-players-online` | int | `5` | `5` | минимум валидных целей |
| `player.ignore-vanished-players` | bool | `true` | `true` | пропуск скрытых |
| `player.ignore-invisible-players` | bool | `false` | `true` | пропуск невидимых |
| `player.excluded-worlds` | list | `[spawn, world_the_end]` | — | миры-исключения целей |
| `base.enabled` | bool | `true` | `true` | |
| `base.cooldown` | int | `1200` | `65` | |
| `base.min-radius-from-region` / `max-radius-from-region` | int | `250` / `300` | `50` / `300` | удаление от центра региона |
| `base.use-protectionstones` | bool | `true` | `true` | учитывать ps-регионы |
| `base.use-worldguard` | bool | `true` (комментарий: «времено не роботает») | `true` | учитывать обычные WG-регионы |
| `base.excluded-worlds` | list | `[spawn, world_the_end]` | — | |
| `unsafe-blocks` | list | 7 материалов | — | небезопасные блоки (см. §4.8) |
| `messages.*` | string | — | `""` | см. §4.9 |

---

## 6. Хранение данных
- Постоянное хранение отсутствует: состояние только в памяти
  (кулдауны `CooldownManager`, активные задачи/позиции `TeleportManager`).
- Конфигурация — единственный файл `plugins/HolyRTP/config.yml`.

---

## 7. Интеграции
- **WorldGuard 7** — чтение регионов (owner/member) для вида `base`.
- **ProtectionStones** — те же регионы через WG API (ID с префиксом `ps`).
- PlaceholderAPI / Vault — не используются.

---

## 8. Сценарий работы (основной поток)
1. Игрок в разрешённом мире вводит `/rtp` → проверка `rtp.use` → выбор вида.
2. Для long/player/base: проверка права вида → проверка кулдауна (`rtp.bypass.cooldown`).
3. default: ActionBar-отсчёт; движение отменяет телепортацию.
4. Подбор случайной безопасной точки (внутри WorldBorder, не на опасных блоках).
5. `player.teleport`, очистка ActionBar, сообщение об успехе с координатами.
6. Установка кулдауна вида.

---

## 9. Особенности реализации и известные ограничения
- Кулдауны не персистентны — сбрасываются рестартом.
- `ignore-invisible-players` в коде по умолчанию `true`, в конфиге `false` — поведение
  определяется фактическим значением в файле.
- `performDefaultTeleport` содержит бессмысленный тернарник
  (`type.equals("default") ? getDefaultCooldown() : getDefaultCooldown()`) — мёртвая ветка.
- Для `player` сравнивается число **валидных целей** (исключая самого игрока) с
  `min-players-online` — фактически требуется на одного игрока больше, чем «онлайн».
- Вызов `getHighestBlockYAt` для дальних координат может загружать чанки —
  потенциальные лаги при max-radius 30000.
- Геттеры ConfigManager кэшируют значения только на время чтения — конфиг применяется
  после `reloadConfig`/рестарта; команды перезагрузки нет.
- Цвета: три формата HEX (`&#rrggbb`, `&x&...&`-последовательность, `&rrggbb`) поверх `&`-кодов.

## 10. Сборка
```
mvn -pl modules/HolyRTP package
```
Особенностей нет (worldedit/worldguard — provided, shade пустой).
