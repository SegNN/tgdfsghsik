# Техническое задание — модуль CheatReport

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/CheatReport`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | CheatReport |
| Артефакт (Maven) | `CheatReport` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0-SNAPSHOT |
| Главный класс | `org.dimasik.cheatreport.CheatReport` |
| API-версия Bukkit | 1.16 |
| Авторы | в plugin.yml не указаны |

**Назначение:** система жалоб на читеров для сети серверов (общая MySQL-база).
Игроки отправляют репорты командой `/cheatreport`; модераторы через GUI
просматривают очередь жалоб (сгруппированных по нарушителю), берут «дела» на
рассмотрение, принимают/отклоняют их. Ведётся статистика «репутации» заявителей
(доля подтверждённых/отклонённых жалоб) и нарушителей (число подтверждённых/отклонённых
репортов). Поддержан кросс-серверный контекст: метка сервера в репорте, таблица
онлайн-статусов игроков сети, рассылка результатов заявителям через SyncService,
бродкасты в LiteBans. Отдельный режим входа в БД — от «System» (античит шлёт
репорты без кулдауна, с собственным форматом бродкаста).

---

## 2. Требования к окружению и зависимости

### 2.1. Сервер
- Paper/Spigot с поддержкой API 1.16 (сборка под ветку mc-1.21.4, Java 21).

### 2.2. Плагины
| Плагин | Область применения | Обязательность |
|---|---|---|
| LiteBans | Бродкаст о новых жалобах и фейлах античита: консольная команда `litebans broadcast broadcast-type:report ...` | Мягкая (при отсутствии команда просто не выполнится) |
| SyncService (`ru.nezxenka.syncservice.api.SyncAPI`) | Доставка сообщений/звуков заявителю о результате жалобы на другом сервере сети (`SyncAPI.addAction`) | Мягкая (в сборке — compile-stub из `_api-stubs`; при отсутствии класса на сервере класс репорт-менеджера не загрузится — фактически требуется) |

### 2.3. Библиотеки
| Библиотека | Версия | Scope | Назначение |
|---|---|---|---|
| HikariCP | 4.0.3 | compile (зашивается shade в jar) | пул соединений MySQL |
| `_api-stubs` | 1.0.0 | provided | compile-time заглушки SyncAPI и др. |

### 2.4. База данных
- MySQL/MariaDB, параметры в `config.yml` (`mysql.*`). Обязательна: без неё пул
  не создастся (HikariCP бросит исключение при инициализации в конструкторе
  `DatabaseManager`, что уронит `onEnable`).

---

## 3. Архитектура (структура пакетов)

```
org.dimasik.cheatreport
├── CheatReport        — главный класс: saveDefaultConfig, создание менеджеров, регистрация команды и GUI-листенера
├── DatabaseManager    — HikariCP-пул + автосоздание 4 таблиц и миграция недостающих колонок reputation
├── ReportManager      — CommandExecutor /cheatreport: отправка репорта, кулдаун, взятие/закрытие дел, выборки
├── ReportGUI          — Listener + GUI: два инвентаря (общий список, репорты на игрока), пагинация, сортировка, фильтр онлайн, клики
├── ReputationManager  — статистика репутации игроков (4 счётчика), sortType, playerExists
├── BatchDataHelper    — пакетные SELECT ... IN (...) для репутации/онлайна/счётчика репортов (+ record PlayerReputationData)
├── OnlinePlayerManager — онлайн-статусы: таблица online_players, флаг onlyOnline, локальный Set онлайн-игроков
├── Report             — POJO-строка таблицы reports (id, sender, target, date, reason, type, admin, mode)
├── SortType           — enum сортировок GUI (SQL-ORDER + русское имя)
└── Pair<F, S>         — универсальная пара
```

---

## 4. Функциональные требования

### 4.1. Команда `/cheatreport` (алиасы: `/cr`, `/report`)
plugin.yml: `usage: /cheatreport [игрок] [причина]`.

| Форма | Отправитель | Поведение |
|---|---|---|
| `/cheatreport` (без аргументов) | любой | сообщение «Использование: /cheatreport [игрок] [причина]» |
| `/cheatreport <игрок> <причина...>` | игрок | отправка репорта (см. 4.2) |
| `/cheatreport <игрок> <причина...>` | консоль | репорт от имени `System`, без кулдауна (`checkCd=false`) — путь для античита |
| `/cheatreport list` | игрок | общий список жалоб (GUI), право `cheatreport.reports`; из консоли — отказ |
| `/cheatreport list <игрок>` | игрок | репорты на конкретного игрока (GUI), право `cheatreport.reports` |

Проверки при отправке игроком:
- нельзя пожаловаться на себя;
- целевой игрок должен существовать (`reputation.playerExists` — строка в таблице
  `reputation`; создаётся при входе игрока на любой сервер сети);
- кулдаун: репорт на того же игрока не чаще 1 раза в 30 минут
  (`report_cooldowns.cooldown_end = now + 1 800 000 мс`), иначе сообщение о 30-минутной паузе;
- при успехе — «Репорт успешно отправлен» (отправляется сразу, не дожидаясь БД).

Отправка репорта (`sendReport`) асинхронна:
- `INSERT INTO reports (sender, target, reason, type='EXPECT', mode=<mysql.server>)`;
- затем в главном потоке — бродкаст LiteBans:
  - обычный репорт (`checkCd=true`): «Новая жалоба на <игрок>» (+ счётчик, если уже
    были репорты) «Режим: <сервер> | /report list»;
  - системный (`checkCd=false`, античит): `[HAC](<игрок> <пинг> ⇆) провалил проверку
    античита () (<N>VL) [<сервер>]` — пинг берётся из `Player.getPing()`, при оффлайне
    «[неизвестно]»;
- для обычного репорта — `INSERT ... ON DUPLICATE KEY UPDATE` кулдауна.

### 4.2. Права (permissions)
| Право | Где проверяется | Назначение |
|---|---|---|
| `cheatreport.reports` | `/cheatreport list [игрок]` | доступ к GUI очереди жалоб |

В plugin.yml права не объявлены. Отправка репорта доступна всем игрокам без права.

### 4.3. События
| Событие | Обработчик | Действие |
|---|---|---|
| `PlayerJoinEvent` | `ReportGUI.onPlayerJoin` | `OnlinePlayerManager.addPlayer`: асинхронно `INSERT INTO online_players (nickname, server) ON DUPLICATE KEY UPDATE` + `INSERT IGNORE INTO reputation (player)` (создание «паспорта» игрока) |
| `PlayerQuitEvent` | `ReportGUI.onPlayerLeave` | асинхронно `DELETE FROM online_players WHERE nickname = ?` |
| `InventoryClickEvent` | `ReportGUI.onInventoryClick` | вся логика GUI (см. 4.4) |

### 4.4. GUI модератора
Два меню, оба 54 слота (6 строк, `Bukkit.createInventory(null, 54, title)`),
идентифицируются по заголовку окна (`title.startsWith("&0Список жалоб")` /
`startsWith("Репорты на")`).

**Меню «Список жалоб»** — открывается сразу с «заглушками» (серые панели
«Загрузка...», нижняя строка — голубое стекло), данные догружаются асинхронно,
после чего инвентарь пересоздаётся с итоговым заголовком
`&0Список жалоб (<уникальных целей> шт.) [<стр>/<всего>]`. Содержимое — жалобы,
**сгруппированные по нарушителю** (`GROUP BY target`): один предмет = один нарушитель,
количество в стаке = число жалоб, лор — из первого попавшегося репорта +
`(и ещё N записей)` при N ≥ 2. 45 предметов на страницу.

Клик по предмету-жалобе (`ZOMBIE_HEAD`, для отправителя `System` — `SKELETON_SKULL`;
`reportId` хранится в PersistentDataContainer):
- **ПКМ** — открыть меню всех репортов на этого игрока;
- **ЛКМ** — «взять дело»: если у модератора уже есть активное дело — отказ; если
  репорты на цель уже взяты другим (`admin IS NOT NULL`) — отказ; иначе
  `UPDATE reports SET admin = <модератор> WHERE target = ? AND type='EXPECT' AND admin IS NULL`.

**Меню «Репорты на <игрок>»** — плоский список репортов (`type='EXPECT' AND admin IS NULL`,
`ORDER BY date DESC`, пагинация 45/стр), кнопка «[⟲] Вернуться..» (`TIPPED_ARROW`,
слот 49) — назад в общий список.

Служебные слоты (значения слотов в конфиге фактически игнорируются, номера захардкожены):

| Слот | Предмет | Действие |
|---|---|---|
| 45 / 47 | `ARROW` | предыдущая / следующая страница (сравнение по displayName из конфига; < 1 — игнор) |
| 46 | `ZOMBIE_HEAD` активное дело / `BARRIER` «нет активных дел» | ЛКМ — принять все свои активные дела, ПКМ — отклонить (звук `BLOCK_NOTE_BLOCK_PLING`) |
| 49 | `NETHER_STAR` «Обновить» (общий список) / `TIPPED_ARROW` «Вернуться» (меню игрока) | реопен текущей страницы / страницы 1 |
| 52 | `HOPPER` «Сортировка» | ЛКМ — следующая сортировка, ПКМ — предыдущая (циклично); сохраняется в `reputation.sortType` |
| 53 | `ENDER_PEARL` «Только игроки онлайн» | переключение флага `onlyOnline` (зачарование-подсветка когда вкл.); фильтр убирает оффлайн-цели из общего списка |

Текущая страница извлекается из заголовка (парсинг `[n/m]`, при ошибке — 1).

**Сортировки** (`SortType`, SQL-ORDER подставляется в запрос напрямую):
`NEWEST_FIRST` «Сначала новые» (`max_date DESC`), `OLDEST_FIRST` «Сначала старые»
(`max_date ASC`), `MOST_REPORTS` «Сначала большое количество» (`report_count DESC`),
`LEAST_REPORTS` «Сначала маленькое количество» (`report_count ASC`),
`ALPHABETICAL` «Сначала по алфавиту» (`target ASC`), `REVERSE_ALPHABETICAL`
«Сначала не по алфавиту» (`target DESC`).

**Закрытие дела** (`resolveAllActiveReports(player, accepted)`):
- асинхронно выбираются все `reports WHERE admin = ? AND type='EXPECT'`;
- `UPDATE reports SET type = 'ACCEPTED' | 'REJECTED'`;
- по каждому репорту: `ReputationManager.updateReputation(report, accepted, 1)` —
  заявителю идут счётчики `totalSuccessCheck`/`totalFailureCheck` (принятые/отклонённые
  его жалобы), цели — `totalSuccessReports`/`totalFailureReports`;
- заявителю (кроме `System`) через `SyncAPI.addAction` в главном потоке уходит
  сообщение («жалоба принята, репутация увеличена...» / «жалоба отклонена, при
  продолжении ложных жалоб — блокировка...») и звук `ENTITY_PLAYER_LEVELUP 2 1`
  (звук в обеих ветках);
- модератору — «Репорт успешно принят/отклонен», меню переоткрывается.

### 4.5. Плейсхолдеры GUI (форматирование предметов жалоб)
Подставляются в `gui.reports_format.*` и `gui.buttons.activeReport.*`:

| Плейсхолдер | Значение |
|---|---|
| `{sender}` | ник заявителя |
| `{target}` | ник нарушителя |
| `{date}` | дата жалобы, формат `HH:mm dd.MM.yyyy` |
| `{reason}` | причина (+ `&7&o(и ещё N записей)` при группировке) |
| `{mode}` | сервер, на котором подана жалоба |
| `{online}` | `Онлайн | <сервер>` либо `Оффлайн` (из `online_players`) |
| `{amount}` | всего жалоб на нарушителя |
| `{sender_statsSuccess}` / `{sender_statsFailure}` | принятые / отклонённые жалобы заявителя (`totalSuccessReports`-семантика, см. примечание) |
| `{target_statsSuccess}` / `{target_statsFailure}` | принятые / отклонённые репорты цели |
| `{sender_statsSuccessCheck}` / `{sender_statsFailureCheck}` | успехи/провалы проверок заявителя (`totalSuccessCheck` / `totalFailureCheck`) |
| `{target_statsSuccessCheck}` / `{target_statsFailureCheck}` | то же для цели |

Цвета: `&x&R&R&G&G&B&B`-HEX и обычные `&`-коды через `ChatColor.translateAlternateColorCodes`.

Звуки GUI: `BLOCK_NOTE_BLOCK_PLING` (активное дело), `ENTITY_ENDER_EYE_LAUNCH`
(обновить), `ENTITY_ENDERMAN_TELEPORT` (назад), `BLOCK_NOTE_BLOCK_BELL`
(переключение onlyOnline), `ENTITY_CHICKEN_EGG` (сортировка), `UI_BUTTON_CLICK`
(клик по жалобе).

### 4.6. Сообщения
Сообщения команды захардкожены в коде (`ChatColor.translate...` со строками-литералами):
использование, «нет прав», «игрок не существует», «нельзя на себя», «кулдаун 30 минут»,
«репорт отправлен», «берём дело...», «взято репортов...», «не удалось взять»,
«нет активных дел», «репорт принят/отклонен», «ошибка при взятии/обработке».
Секция `messages` в config.yml (error, cheatreportusage, noPlayer, cheatreportsend)
кодом **не читается** — фактически мёртвая.

---

## 5. Конфигурация (`config.yml`)

### 5.1. Подключение к БД
| Параметр | Тип | Default (в конфиге / в коде) | Описание |
|---|---|---|---|
| `mysql.host` | string | `"localhost"` / `"localhost"` | хост MySQL |
| `mysql.port` | int | `3306` / `3306` | порт |
| `mysql.database` | string | `"reports"` / `"cheatreport"` | имя БД |
| `mysql.user` | string | `"root"` / `"root"` | пользователь |
| `mysql.password` | string | `"[пароль]"` / `""` | пароль |
| `mysql.server` | string | `"Анархия #1"` / `"Анархия #1"` | метка этого сервера (пишется в `reports.mode` и `online_players.server`, подставляется в бродкасты) |

JDBC-URL: `jdbc:mysql://<host>:<port>/<db>?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8`.
Пул HikariCP: maxPoolSize 10, minIdle 2, connectionTimeout 30 000, idleTimeout 600 000,
maxLifetime 1 800 000, leakDetectionThreshold 5 000, имя пула `CheatReport-Hikari`.

### 5.2. GUI (что реально используется)
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `gui.rows` | int | 6 | **не используется** (размер захардкожен: 54) |
| `gui.title` | string | `Список жалоб ({amount} шт.) [{page}/{total_pages}]` | **не используется** (заголовок формируется в коде) |
| `gui.buttons.pagesNavs.material` | material | ARROW | **не используется** (материал захардкожен) |
| `gui.buttons.nextPage.displayName` | string | `&fСледующая страница &6▶` | используется (слот в конфиге 45 игнорируется, фактически 47) |
| `gui.buttons.prevPage.displayName` | string | `&6◀ &fПредыдущая страница` | используется (слот 47 игнорируется, фактически 45) |
| `gui.buttons.exit.*` | — | ARROW, слот 53 | **не используется** (кнопка выхода не создаётся) |
| `gui.buttons.noActiveReport.displayName` | string | `&x&D&5&2&1&2&3У вас нет активных дел` | используется (material BARRIER и слот захардкожены) |
| `gui.buttons.activeReport.displayName` / `.lore` | string / list | `❐ Дело игрока {target} ❐` + лор со статами | используется (плейсхолдеры §4.5; material ZOMBIE_HEAD и слот захардкожены) |
| `gui.reports_format.material` | material | ZOMBIE_HEAD | **не используется** (ZOMBIE_HEAD/SKELETON_SKULL захардкожены) |
| `gui.reports_format.displayName` / `.lore` | string / list | см. config.yml | используется: имя и лор предмета жалобы |

### 5.3. `messages.*`
| Параметр | Тип | Default | Статус |
|---|---|---|---|
| `messages.error` | string | `&cПроизошла внутреняя ошибка...` | не читается |
| `messages.cheatreportusage` | list | «Использование: /cheatreport [игрок] [причина]» | не читается (аналог захардкожен) |
| `messages.noPlayer` | string | «Игрок не найден» | не читается |
| `messages.cheatreportsend` | list | «Репорт успешно отправлен.» | не читается (аналог захардкожен) |

---

## 6. Хранение данных (MySQL)

Таблицы создаются при старте (`CREATE TABLE IF NOT EXISTS`), плюс миграция
недостающих колонок `reputation` через `information_schema` (типы: INT / VARCHAR(50)
`NEWEST_FIRST` / BOOLEAN FALSE).

| Таблица | Схема | Назначение |
|---|---|---|
| `reports` | `id INT AUTO_INCREMENT PK, sender VARCHAR(255) NOT NULL, target VARCHAR(255) NOT NULL, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, reason TEXT, type VARCHAR(50) DEFAULT 'EXPECT', admin VARCHAR(255), mode VARCHAR(255)` + индексы `idx_target, idx_type, idx_admin, idx_date` | репорты. `type`: `EXPECT` (в очереди) → `ACCEPTED`/`REJECTED`; `admin` — ник взявшего модератора; `mode` — сервер подачи |
| `reputation` | `player VARCHAR(255) PK, totalSuccessCheck INT, totalFailureCheck INT, totalSuccessReports INT, totalFailureReports INT, sortType VARCHAR(50) DEFAULT 'NEWEST_FIRST', onlyOnline BOOLEAN DEFAULT FALSE` | «паспорт» игрока: 4 счётчика статистики + настройки GUI (сортировка, фильтр онлайн). Строка создаётся при входе (`INSERT IGNORE`) |
| `online_players` | `nickname VARCHAR(255) PK, server VARCHAR(255)` | текущий онлайн сети: кто и на каком сервере (для `{online}` и фильтра) |
| `report_cooldowns` | `sender VARCHAR(255), target VARCHAR(255), cooldown_end TIMESTAMP, PK(sender, target), INDEX idx_cooldown` | 30-минутный кулдаун репортов на одну цель |

Состояния `type`: `EXPECT` → `ACCEPTED` (жалоба подтверждена) | `REJECTED` (отклонена).

Примечание по семантике счётчиков: `updateReputation` пишет заявителю в
`*Check`-колонки, цели — в `*Reports`-колонки; в GUI `{sender_statsSuccess}` и др.
без суффикса `Check` показывают при этом поля `successReports/failureReports`
заявителя (см. §9).

---

## 7. Интеграции
| Интеграция | Что реально используется |
|---|---|
| LiteBans | только исходящие консольные команды `litebans broadcast broadcast-type:report ...` (2 формата: новая жалоба / фейл античита) |
| SyncService (`ru.nezxenka.syncservice.api.SyncAPI`) | `SyncAPI.addAction("player", <ник>, "message", <текст>)` и `(..., "sound", "ENTITY_PLAYER_LEVELUP 2 1")` — доставка результата заявителю; в сборке используется заглушка `_api-stubs` (реальный класс должен быть на сервере) |
| Bukkit Scheduler | все запросы к БД — `runTaskAsynchronously`, возврат в UI — `runTask` |

PlaceholderAPI, Vault, LuckPerms и прочее — не используются.

---

## 8. Сценарий работы
1. Игрок заходит на любой сервер сети → `online_players` (ник + сервер),
   `INSERT IGNORE` строки `reputation`.
2. Игрок пишет `/cheatreport Steve использует флай` → проверки (себя/существует/кулдаун) →
   асинхронный INSERT `reports(type='EXPECT', mode=<сервер>)` + запись кулдауна.
3. LiteBans-бродкаст «Новая жалоба...» уходит модерации сети.
4. Модератор на любом сервере вводит `/cheatreport list` (право `cheatreport.reports`) →
   GUI с группировкой по нарушителям, статами заявителей и онлайн-статусами
   (данные — пакетными запросами `IN (...)`).
5. ЛКМ по делу → все ожидающие репорты на игрока закрепляются за модератором
   (`admin = ник`); ПКМ → просмотр всех репортов на игрока.
6. Слот 46 → своё активное дело: ЛКМ «принять», ПКМ «отклонить».
7. Асинхронно: `type = ACCEPTED/REJECTED`, обновление 4 счётчиков репутации
   (заявитель + цель) на каждый репорт.
8. Заявителю через SyncService уходит личное сообщение и звук о результате.
9. Модератору — подтверждение, меню переоткрывается с актуальной очередью.
10. Параллельно античит может слать `/cheatreport <ник> <причина>` из консоли —
    репорт от `System` без кулдауна, с бродкастом «провалил проверку античита».

---

## 9. Особенности реализации и известные ограничения
- **Блокирующие SQL в главном потоке**: `hasCooldown`, `playerExists` внутри
  `onCommand`, `getReportById`/`hasActiveReport`/`getActiveReports` в обработчике
  клика GUI, `getSortType`/`isOnlyOnline`/`setSortType`/`toggleOnlyOnline` —
  выполняются синхронно и могут фризить тик при медленной БД.
- **Идентификация GUI по заголовку** (`title.startsWith`) вместо
  `InventoryHolder`/PDC инвентаря — хрупко (переименование заголовка ломает клики;
  любой инвентарь с таким же началом заголовка перехватит клики; у игрока могут
  «пересекаться» меню, открытые асинхронной догрузкой — она делает `player.openInventory`
  нового инвентаря, даже если игрок уже закрыл/открыл другое).
- **Расхождение семантики счётчиков**: `updateReputation` инкрементирует заявителю
  `totalSuccessCheck/totalFailureCheck`, а GUI-плейсхолдеры `{sender_statsSuccess}`
  и `{sender_statsFailure}` читают `successReports/failureReports` (т.е. «репорты
  против игрока»), а не Check-поля — статистика заявителя в лоре фактически
  показывает чужие репорты на него.
- **Мёртвый конфиг**: `gui.rows`, `gui.title`, материалы кнопок, слоты, кнопка exit
  и вся секция `messages` кодом не используются.
- `SQLException` в `DatabaseManager.createTables` проглатывается молча (пустой catch) —
  ошибка схемы не остановит плагин.
- `SortType.getSqlOrder()` подставляется в SQL напрямую — безопасно (enum), но
  паттерн «строка в SQL» стоит сохранять при доработках.
- Никнеймы — ключи БД (не UUID): смена ника отрывает статистику и репорты.
- Отправка сообщения «Репорт успешно отправлен» происходит до фактической записи
  (успех INSERT не проверяется).
- Звук результата заявителю одинаковый (LEVELUP) и для принятой, и для отклонённой жалобы.
- `createGroupedReportItem`/`replacePlaceholders` (без кэша) — мёртвый код:
  используются только `*WithCache`-варианты.
- Проверка `report.sender.equals(translate("System"))` — фактическое сравнение
  с литералом `System` (цвет-кодов нет).
- Пересоздание инвентаря при догрузке теряет незаполненные слоты > числа предметов
  (addItem кладёт в первые свободные — ок), но кнопки нижней строки копируются из
  старого инвентаря по одному (`for i in 45..53`).

---

## 10. Сборка
```
mvn -pl modules/CheatReport package     # из корня репозитория
```
- maven-shade-plugin (из родительского POM) зашивает HikariCP 4.0.3 в jar
  (scope compile), **без релокации** пакета `com.zaxxer.hikari`.
- `_api-stubs` — provided (класс SyncAPI в jar не попадает; на сервере ожидается
  реальный SyncService).
- Java: `--release 21`; target-артефакт `CheatReport-1.0-SNAPSHOT.jar`.
