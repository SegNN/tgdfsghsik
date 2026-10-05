# Техническое задание — модуль PlayTimeAPI

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/PlayTimeAPI`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | PlayTimeAPI |
| Артефакт (Maven) | `PlayTimeAPI` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `org.dimasik.playtimeapi.Main` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | nezxenka, Dimasik201O (aka sh4dowgirl) |

**Назначение:** плагин учёта времени игры. Для каждого игрока ведёт общее время
в игре, «активное» время (когда игрок совершал действия в последние 5 секунд),
время бездействия (idle), время текущей сессии, текущий сервер сети и метки
последнего входа/активности. Данные хранятся в SQLite или MySQL и доступны
другим плагинам через класс-фасад `org.dimasik.playtimeapi.api.PlayTimeAPI`
(используется, например, для сбора статистики и проверки активности игроков).

---

## 2. Окружение и зависимости

### 2.1. Сервер и Java
- Paper/Spigot, API 1.16+ (сборка проекта — против `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT`, provided, из родительского pom).
- Java 21 (`maven.compiler.release=21` в родительском pom).

### 2.2. Библиотеки
- HikariCP 4.0.3 (scope `compile`, зашивается shade-плагином в jar).
- JDBC-драйверы SQLite/MySQL в pom не объявлены и в jar не попадают —
  рассчитано на драйверы, поставляемые самим сервером (Paper бандлит sqlite-jdbc и mysql-connector).

### 2.3. Зависимости от других плагинов
Нет (depend/softdepend в plugin.yml отсутствуют).

---

## 3. Архитектура (структура пакетов)

```
org.dimasik.playtimeapi
├── Main                       — главный класс: init БД, команды, листенеры, две периодические задачи,
│                                хранение activePlayers и lastActivity (ConcurrentHashMap), join/quit
├── api.PlayTimeAPI            — публичный фасад для других плагинов (геттеры времён, текущий сервер,
│                                обновление активности игрока)
├── commands.PlayTimeCommand   — исполнитель команды /playtime
├── database.DatabaseManager   — HikariCP-пул, схема playtime_data, async чтение/запись PlayerData
├── listeners.ActivityListener — 7 событий, обновляющих метку «последняя активность»
├── models.PlayerData          — модель данных игрока (total/session времена, сервер, метки времени)
└── utils.TimeFormatter        — форматирование длительностей «Nd., Nh., Nm., Ns.» и «N назад»
```

---

## 4. Функциональные требования

### 4.1. Учёт времени (главный класс `Main`)
- При входе игрока (или на старте для уже онлайн) асинхронно загружается `PlayerData`
  (или создаётся новая), проставляются `currentServer` = `server.name` из конфига,
  `lastLogin` = `lastActivity` = текущее время; запись сразу сохраняется в БД.
- **Задача подсчёта** — синхронный таймер каждые **20 тиков (1 секунда)**:
  - каждый живой (`!isDead()`), онлайн-игрок: `totalPlayTime += 1000` и `sessionPlayTime += 1000`;
  - если с последней активности прошло **≤ 5000 мс** — также `totalActiveTime += 1000`
    и `sessionActiveTime += 1000` (и обновляется `lastActivity` модели);
  - idle-времена не хранятся, а вычисляются: `total idle = totalPlayTime − totalActiveTime`,
    `session idle = sessionPlayTime − sessionActiveTime`.
- **Задача автосохранения** — асинхронный таймер каждые **100 тиков (5 секунд)**:
  все `activePlayers` сохраняются в БД (`savePlayerDataAsync`).
- При выходе игрока: `currentServer` = `"Оффлайн"`, финальное сохранение, удаление из кэшей.
- При `onDisable`: принудительное сохранение всех игроков, закрытие пула БД.

### 4.2. Определение активности (`ActivityListener`)
События, обновляющие метку `lastActivity` (в `Main.lastActivity`):
`PlayerMoveEvent`, `PlayerInteractEvent`, `InventoryClickEvent`,
`PlayerCommandPreprocessEvent`, `PlayerChatEvent` (deprecated API),
`PlayerTeleportEvent`, `PlayerToggleSneakEvent`.

### 4.3. Команда `/playtime`
| Элемент | Значение |
|---|---|
| Команда | `playtime` (алиасы не заданы) |
| Право | `playtimeapi.command` (проверяется Bukkit по `permission` в plugin.yml; сообщения о отсутствии права — стандартное серверное) |
| Использование | `/playtime [игрок]` |

Поведение:
- Без аргументов — статистика самого отправителя (только для игроков; консоли —
  сообщение «Использование /playtime [игрок]»).
- С аргументом — статистика `Bukkit.getOfflinePlayer(args[0])`.
- Выводится блок сообщения (жёстко зашитые строки с HEX-кодами `§x§e§7§e§7§e§7`):
  «Активность <ник>», общее время в игре, общее время активности, текущая сессия
  (в скобках — имя сервера или «Оффлайн»), время сессии, активное время сессии,
  время бездействия, «Последняя активность: … назад», «Последний вход на анархию: … назад».
- Формат длительности: `Xд., Xч., Xм., Xс.` (дни выводятся только если > 0).

### 4.4. Публичный API (`api.PlayTimeAPI`, для вызова из других плагинов)
| Метод | Возвращает |
|---|---|
| `getTotalPlayTime(UUID)` | общее время в игре, мс |
| `getTotalActiveTime(UUID)` | общее активное время, мс |
| `getSessionPlayTime(UUID)` | время текущей сессии, мс |
| `getSessionActiveTime(UUID)` | активное время сессии, мс |
| `getIdleTime(UUID)` | общее время бездействия, мс |
| `getLastActivityTime(UUID)` | метка последней активности, мс epoch |
| `getCurrentServer(UUID)` | имя текущего сервера либо «Оффлайн» |
| `updatePlayerActivity(Player)` | обновить lastActivity модели и сохранить в БД |

Все геттеры при отсутствии данных возвращают 0 / «Оффлайн».
`getPlayerData` сначала смотрит кэш `activePlayers`, при промахе — блокирующее
чтение из БД (`.join()`).

---

## 5. Конфигурация (`config.yml`)

| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `database.type` | string | `SQLITE` | Тип БД: `SQLITE` или `MYSQL` (регистр не важен) |
| `database.file` | string | `playtime.db` | Имя файла SQLite (в папке плагина) |
| `database.host` | string | `localhost` | Хост MySQL |
| `database.port` | int | `3306` | Порт MySQL |
| `database.database` | string | `playtimeapi` | Имя базы MySQL |
| `database.user` | string | `root` | Пользователь MySQL |
| `database.password` | string | `""` | Пароль MySQL |
| `server.name` | string | `Lite-1` | Имя текущего сервера (пишется в `current_server`) |

---

## 6. Хранение данных

Таблица `playtime_data` (создаётся при старте `CREATE TABLE IF NOT EXISTS`):

| Поле | Тип | Описание |
|---|---|---|
| `uuid` | VARCHAR(36), PK | UUID игрока |
| `total_playtime` | BIGINT | Общее время в игре, мс |
| `total_active_time` | BIGINT | Общее активное время, мс |
| `total_idle_time` | BIGINT | Хранится, но фактически не читается/не считается (idle вычисляется) |
| `current_server` | VARCHAR(32) | Имя сервера или «Оффлайн» |
| `last_login` | BIGINT | Метка последнего входа |
| `last_activity` | BIGINT | Метка последней активности |
| `last_update` | BIGINT | Метка последнего сохранения |

- MySQL: `INSERT ... ON DUPLICATE KEY UPDATE ...` (полная перезапись полей);
  URL `jdbc:mysql://host:port/db?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true`,
  драйвер `com.mysql.cj.jdbc.Driver`, пул до 10 соединений.
- SQLite: `INSERT OR REPLACE ...`, файл `<dataFolder>/playtime.db`, пул из 1 соединения.
- Пул HikariCP: `minimumIdle=2`, `idleTimeout=30000`, `maxLifetime=600000`,
  `connectionTimeout=5000`, имя пула `PlayTimeAPI-Pool`.
- Чтение/запись — асинхронные (`CompletableFuture` на общем ForkJoinPool).

---

## 7. Интеграции
Нет внешних интеграций (PlaceholderAPI/Vault отсутствуют). Интеграция с другими
плагинами — прямое использование класса `org.dimasik.playtimeapi.api.PlayTimeAPI`
как библиотеки (плагин должен быть загружен раньше вызывающего).

---

## 8. Сценарий работы
1. Старт: создание/чтение конфига, инициализация пула и таблицы, регистрация
   команды `/playtime`, листенеров; загрузка данных всех онлайн-игроков.
2. Игрок входит → загрузка `PlayerData`, `currentServer` = имя сервера из конфига.
3. Каждую секунду таймер начисляет +1000 мс игрового времени; при активности
   в последние 5 с — также +1000 мс активного времени.
4. Действия игрока (движение, клики, чат, команды и т.д.) обновляют метку активности.
5. Каждые 5 с — асинхронное автосохранение всех активных игроков в БД.
6. Выход игрока → `currentServer` = «Оффлайн», финальное сохранение.
7. `/playtime [ник]` → форматированный блок статистики.

---

## 9. Особенности реализации и известные ограничения
- Формат вывода команды и сообщения об ошибках жёстко зашиты в код (не конфигурируются).
- `setIdleTime` в `PlayerData` — пустой метод; колонка `total_idle_time` всегда
  пишется нулём при создании и перезаписывается значением `getIdleTime()` (вычисляемым) при сохранении.
- `getPlayerData` для офлайн-игрока выполняет блокирующий `.join()` в главном потоке —
  возможна задержка при обращении к БД.
- `PlayerChatEvent` — deprecated API (работает, но генерирует предупреждения на новых версиях).
- Пороги активности (5 с) и интервалы задач (1 с / 5 с) не конфигурируются.
- Смена `server.name` в конфиге требует перезапуска (перечитывается только на onEnable).

---

## 10. Сборка
```
mvn -pl modules/PlayTimeAPI package    # из корня репозитория
```
Maven Shade (общий для родителя) зашивает HikariCP 4.0.3; JDBC-драйверы
в jar не включаются (предоставляются сервером). Готовый артефакт — `target/PlayTimeAPI-1.0.jar`.
