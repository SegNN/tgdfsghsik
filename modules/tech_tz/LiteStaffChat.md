# Техническое задание — модуль LiteStaffChat

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/LiteStaffChat`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | LiteStaffChat |
| Артефакт (Maven) | `LiteStaffChat` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `org.nezxenka.litestaffchat.LiteStaffChat` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | nezxenka, Dimasik201O (aka sh4dowgirl) |
| Зависимости (depend) | PlaceholderAPI (жёсткая) |

**Назначение:** чат для персонала (staff chat) сервера/сети. Сообщения отправляются
командой `/sc` в четырёх режимах адресации (LOCAL/GLOBAL/LITE/CLASSIC/CURRENT), собираются
в общий формат с плейсхолдерами и рассылаются игрокам с правом seeing, учитывающим
включённый ли у них приём. При наличии Redis сообщения транслируются между серверами сети
(pub/sub канал `StaffChat`), фильтруясь по имени/типу сервера.

---

## 2. Окружение и зависимости

### 2.1. Сервер
- Paper/Spigot API 1.16+. Java 21 (родительский pom).

### 2.2. Плагины
| Плагин | Обязательность | Область применения |
|---|---|---|
| PlaceholderAPI 2.12.3 | Обязательная (`depend`; в коде проверяется `isPluginEnabled`, при отсутствии форматы не обрабатываются) | Формат сообщений (`%chatbrackets_full%`, `%player_name%`, `%chatbrackets_color%` в дефолтных форматах) |

### 2.3. Библиотеки
- `redis.clients:jedis:4.4.6` (compile — зашивается в jar родительским maven-shade-plugin,
  релокации нет).

### 2.4. Внешние сервисы
- Redis (опционально, `redis.enabled: true`): pub/sub-канал `StaffChat`. При отключении —
  режим одного сервера.

---

## 3. Архитектура (структура пакетов)

```
org.nezxenka.litestaffchat
├── LiteStaffChat                    — главный класс: конфиги, Redis, команды, слушатели,
│                                      Set<UUID> включённых приёмников (ConcurrentHashMap keySet)
├── command
│   ├── StaffChatCommand             — /sc [режим] [сообщение]: только игроки, смена режимов
│   ├── StaffChatTabCompleter        — tab-подсказка режимов LOCAL/GLOBAL/LITE/CLASSIC/CURRENT
│   └── StaffChatToggleCommand       — /sct: включить/выключить личный приём сообщений
├── config
│   ├── ConfigManager                — кэш форматов chat-format.<mode> (EnumMap) + messages.*
│   │   └── enum ChatMode            — LOCAL, GLOBAL, CLASSIC, LITE
│   └── DatabaseConfig               — чтение database.yml: имя/режим сервера, параметры Redis
├── listener.PlayerListener          — PlayerJoinEvent/PlayerQuitEvent: автевключение приёма
│                                      по праву litestaffchat.see, очистка при выходе
├── redis
│   ├── RedisManager                 — JedisPool (maxTotal 10, maxIdle 5, minIdle 1, timeout 2000),
│   │                                  publish/subscribe, ping, shutdown
│   └── RedisSubscriber              — поток-демон «Redis-Subscriber»: подписка на канал StaffChat,
│                                      автопереподключение (пауза 5 с), разбор «server||mode||message»
└── service.ChatService              — форматирование (формат + PAPI + &-цвета), отправка в Redis
                                       или локальная, broadcastMessage с фильтром shouldReceive
```

---

## 4. Функциональные требования

### 4.1. Команды

| Команда | Алиасы | Право | Поведение |
|---|---|---|---|
| `/sc [LOCAL\|GLOBAL\|LITE\|CLASSIC\|CURRENT] [сообщение]` | `/staffchat` | `litestaffchat.use` (из plugin.yml) | Отправка сообщения в staff-чат. Если первый аргумент — режим (regex `(?i)LOCAL\|GLOBAL\|LITE\|CLASSIC\|CURRENT`), он определяет адресацию и вырезается из текста; иначе режим = GLOBAL. `CURRENT` заменяется на режим сервера из database.yml. Пустое сообщение → подсказка `messages.0args` с `{команда}`. Приём выключен → `messages.senddisabled` |
| `/sct` | `/staffchattoggle` | `litestaffchat.use` | Переключение личного приёма сообщений: сообщения `messages.toggle.on`/`toggle.off` |

Обе команды только для игроков: консоль получает `messages.onlyplayer`.
Tab-дополнение `/sc`: на первом аргументе — режимы LOCAL, GLOBAL, LITE, CLASSIC, CURRENT.

### 4.2. Права (permissions)
| Право | Где объявлено | Назначение |
|---|---|---|
| `litestaffchat.use` | plugin.yml (на обеих командах) | Доступ к командам /sc и /sct |
| `litestaffchat.see` | только код | Право видеть staff-чат; выдаётся автоприём при входе (PlayerJoinEvent) |

### 4.3. Обрабатываемые события
| Событие | Приоритет | Обработка |
|---|---|---|
| `PlayerJoinEvent` | LOW | если есть `litestaffchat.see` — UUID добавляется в набор приёма |
| `PlayerQuitEvent` | LOW | UUID удаляется из набора приёма |

При старте плагина все онлайн-игроки с `litestaffchat.see` тоже добавляются в набор.
`/sct` убирает/возвращает UUID в набор (т.е. право видимости остаётся, приём отключается).

### 4.4. Режимы адресации (shouldReceive)
Формат строки в Redis: `serverName||MODE||formattedMessage`.
| Режим | Кто получает |
|---|---|
| `LOCAL` | только сервер с именем = имени сервера-отправителя (`server.name` из database.yml) |
| `GLOBAL` | все серверы сети (default) |
| `LITE` | только серверы с `server.mode: lite` |
| `CLASSIC` | только серверы с `server.mode: classic` |
| `CURRENT` | на этапе команды заменяется на `server.mode` сервера-отправителя (фактически превращается в `LITE` или `CLASSIC` от типа сервера, см. ограничение 9.3) |

Получатель видит сообщение, если одновременно: есть право `litestaffchat.see`,
UUID в наборе включённых, `shouldReceive` пропустил пару (сервер-адресат/режим).

### 4.5. Формат сообщений
- Формат берётся из `chat-format.<режим в нижнем регистре>`; при отсутствии ключа —
  fallback на формат GLOBAL.
- Подстановки до PAPI: `{сервер}` → имя сервера-отправителя, `{сообщение}` → текст.
- Затем (если PlaceholderAPI доступен) — `PlaceholderAPI.setPlaceholders` по автору
  (в дефолтных форматах: `%chatbrackets_full%`, `%player_name%`, `%chatbrackets_color%`).
- Затем `&`-коды цветов (translateAlternateColorCodes).
- Форматирование выполняется на сервере-отправителе; по сети летит уже готовая строка.

### 4.6. Redis
- Если `redis.enabled: false` (default) — публикация не выполняется, сообщение
  рассылается только локально («single-server mode», лог при старте).
- Иначе `RedisManager.publish("StaffChat", serverName||mode||message)`;
  подписчик-демон читает канал, парсит по `||` (3 части) и в главном потоке вызывает
  `ChatService.broadcastMessage`.
- Переподключение: при обрыве подписки — warning в лог и повтор через 5 секунд.
- Аутентификация по паролю, если `redis.password` непуст.

---

## 5. Конфигурация

### 5.1. config.yml
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `chat-format.local` | string | `&b&n&lSC&7 │ &cS &7│ &b&n&l{сервер}&7 │ &r%chatbrackets_full%%player_name% &b▶ %chatbrackets_color%{сообщение}` | Формат LOCAL (метка S) |
| `chat-format.global` | string | (аналогично, метка G) | Формат GLOBAL |
| `chat-format.classic` | string | (аналогично, метка C) | Формат CLASSIC |
| `chat-format.lite` | string | (аналогично, метка L) | Формат LITE |
| `messages.onlyplayer` | string | `…Только игрок может использовать эту команду.` | Ответ не-игроку |
| `messages.0args` | string | `…Использование: /{команда} [LOCAL/GLOBAL/LITE/CLASSIC/CURRENT] [сообщение]` | Подсказка; `{команда}` |
| `messages.senddisabled` | string | `…У вас выключено получение сообщений.` | Отправка при выключенном приёме |
| `messages.toggle.on` | string | `…Получение сообщений включено.` | /sct включение |
| `messages.toggle.off` | string | `…Получение сообщений выключено.` | /sct выключение |

### 5.2. database.yml (создаётся saveResource при отсутствии)
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `server.name` | string | `lite-1` | Имя этого сервера (для LOCAL-адресации и `{сервер}`) |
| `server.mode` | string | `lite` | Тип сервера: lite/classic (для LITE/CLASSIC/CURRENT) |
| `redis.enabled` | boolean | `false` | Включение сетевого режима |
| `redis.host` | string | `localhost` | Адрес Redis |
| `redis.port` | int | `6379` | Порт Redis |
| `redis.password` | string | `""` | Пароль (пусто — без аутентификации) |

Методы `reloadConfig()` у менеджеров существуют (ConfigManager перечитывает форматы;
DatabaseConfig — файл заново), но ни одна команда reload не зарегистрирована.

---

## 6. Хранение данных
- Постоянного хранения нет: состояние «приём включён» — in-memory `Set<UUID>`
  (сбрасывается при рестарте, автоматически восстанавливается по праву при входе).
- Файлы: `config.yml`, `database.yml` — только настройки.

---

## 7. Интеграции
- **PlaceholderAPI**: форматы сообщений (см. 4.5). Собственной экспансии нет.
- **Redis (Jedis 4.4.6)**: межсерверный транспорт, канал `StaffChat`.
- Через PAPI-плейсхолдеры дефолтных форматов косвенно зависит от плагина ChatBrackets.

---

## 8. Сценарий работы (сетевой режим)
1. На всех серверах сети в database.yml заданы уникальные `server.name` и общий `server.mode`
   (`lite` или `classic`), `redis.enabled: true`.
2. Член персонала заходит → PlayerListener добавляет его UUID в набор приёма (право
   `litestaffchat.see`).
3. Игрок пишет `/sc LOCAL сервер упал?` → StaffChatCommand выбирает режим LOCAL.
4. ChatService.formatMessage подставляет `{сервер}` и `{сообщение}`, прогоняет PAPI и &-цвета.
5. Строка `lite-1||LOCAL||<formatted>` публикуется в канал Redis `StaffChat`.
6. RedisSubscriber каждого сервера принимает её и в главном потоке зовёт broadcastMessage.
7. На каждом сервере сообщение получают игроки с правом see, включённым приёмом,
   прошедшие фильтр (для LOCAL — только сервер `lite-1`; для LITE — все «lite»-серверы).
8. Игрок пишет `/sc` без аргументов → подсказка 0args; `/sct` — выключает личный приём.
9. При остановке сервера подписчик и пул Redis закрываются (onDisable).

---

## 9. Особенности реализации и известные ограничения
- Переключатель приёма `/sct` не персистится: после перезахода приём снова включён
  (по праву), состояние до рестарта не хранится.
- `ChatMode.valueOf(mode)` в `sendStaffChat`: если в Redis прилетит некорректный режим,
  будет IllegalArgumentException; при обычном использовании режимы валидированы regex'ом команды.
- `CURRENT` на этапе команды превращается в `server.mode`, т.е. фактические значения —
  `LITE`/`CLASSIC` в верхнем регистре — проходит `ChatMode.valueOf` корректно,
  но семантика «режим сервера» и enum-режим смешаны.
- `ChatService.broadcastMessage` рассылает готовую строку всем подходящим игрокам
  (без разбора по мирам — «локальность» только на уровне серверов сети).
- PlaceholderAPI в depend, но код использует его мягко (`isPlaceholderAPIAvailable`).
- Jedis зашивается shade-плагином без релокации — возможен конфликт версий с другими
  плагинами, тоже шейдящими jedis.
- Сообщения в Redis передаются в открытом виде (без префикса плагина/версии протокола).

---

## 10. Сборка
```
mvn -pl LiteStaffChat package     # из каталога modules (или mvn package в LiteStaffChat)
```
Jedis 4.4.6 (compile) попадает в итоговый jar через maven-shade-plugin родительского pom
(фильтры META-INF-подписей, dependency-reduced pom не создаётся; релокаций нет).
