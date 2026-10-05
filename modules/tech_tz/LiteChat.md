# Техническое задание — модуль LiteChat

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/LiteChat`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | LiteChat |
| Артефакт (Maven) | `LiteChat` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0 |
| Главный класс | `ru.litechat.LiteChat` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | не указаны |
| Зависимости (depend) | PlaceholderAPI (жёсткая) |

**Назначение:** чат-плагин локального/глобального чата с антирекламой, антимальware-фильтром
рекламы, предупреждениями и авто-мутом, кулдауном и антифлудом, отдельным кулдауном для
новичков, поддержкой цветов по праву, HEX/градиентов и hover-подсказки со статистикой
на нике игрока. Формат сообщений собирается через плейсхолдеры PlaceholderAPI.

---

## 2. Окружение и зависимости

### 2.1. Сервер
- Paper/Spigot с поддержкой API 1.16 (используются Spigot-BungeeChatComponent API,
  `player.spigot().sendMessage(BaseComponent[])`, HoverEvent с `hover.content.Text`).
- Java 21 (родительский pom: `maven.compiler.release=21`).

### 2.2. Плагины
| Плагин | Обязательность | Область применения |
|---|---|---|
| PlaceholderAPI 2.12.3 | Обязательная (`depend`) | Формат чата, цвет игрока, hover, группа/префикс/суффикс LuckPerms |

Используемые чужие плейсхолдеры (в конфиге формата и в коде):
`%holystar_stars%`, `%chatbrackets_full%`, `%chatbrackets_color%`, `%player_displayname%`,
`%holycustom_name%` (ник для hover), `%luckperms_primary_group_name%`,
`%luckperms_prefix%`, `%luckperms_suffix%`.

### 2.3. Библиотеки
- Только `paper-api` (provided, из родительского pom) и `placeholderapi` (provided).
  Ничего не шейдится.

---

## 3. Архитектура (структура пакетов)

```
ru.litechat
├── LiteChat                  — главный класс: singleton, saveDefaultConfig, создание ChatFilter,
│                               регистрация BukkitListener
├── chat.filter.ChatFilter    — детектор рекламы: IP-адреса, URL-префиксы, домены из чёрного списка
│                               с обходом обфускации (омоглифы, leetspeak, «стайлд»-символы)
├── listeners.BukkitListener  — единственный слушатель AsyncPlayerChatEvent: вся логика чата
│                               (новички, фильтры, варны, флуд, кулдаун, цвета, локальный/глобальный чат, hover)
└── utils
    ├── ConsoleMSG            — цветной вывод в консоль (HexUtil.color + console sender)
    └── HexUtil               — цветовой движок: &#rrggbb, &{#rrggbb}, градиенты <#hex>…</#hex> и
                                <&c>…</&f>, legacy-коды &, stripColor, посимвольные списки
```

---

## 4. Функциональные требования

### 4.1. Команды
Плагин не регистрирует ни одной команды (plugin.yml без секции `commands`).
Единственная «команда» — `settings.mute-command`, выполняемая консолью при достижении
лимита предупреждений (см. 4.5).

### 4.2. Права (permissions)
Права не объявлены в plugin.yml, используются в коде (пути читаются из конфига,
значения по умолчанию — из кода):

| Право (default из config.yml) | Назначение |
|---|---|
| `litechat.bypass` (`settings.bypass-permissions`) | Полный обход: новичковый кулдаун, фильтры, флуд, кулдаун чата, кулдаун после сообщения |
| `litechat.color` (`settings.color-permissions`) | Разрешение писать цветом (`&`, `&#`, `<#`) |
| `litechat.alert` (`settings.alert-permissions`) | Получение алертов о флуде (`messages.flood-alert`) |
| `litechat.alert.admin` (жёстко в коде) | Получение алертов о заблокированных сообщениях (`admin.block-message-for-admin`) |
| `litechat.disable` (`settings.disablechat-permissions`) | Полное отключение своего чата: каждое сообщение отменяется |

### 4.3. Обрабатываемые события
| Событие | Обработка |
|---|---|
| `AsyncPlayerChatEvent` (`ignoreCancelled = true`) | Весь конвейер обработки сообщения (см. 4.4–4.8) |

### 4.4. Конвейер обработки сообщения (BukkitListener.onChat)
1. Есть право `litechat.disable` → сообщение молча отменяется.
2. Нет права `litechat.bypass` и игрок «новичок» → отмена (см. 4.6).
3. Заблокированные слова/реклама → предупреждение, при лимите — мут (см. 4.5).
4. Антифлуд: ≥3 сообщений за < 2000 мс → отмена + `cooldown-for-chat` (жёстко «2» сек) + алерт (см. 4.7).
5. Кулдаун чата: между сообщениями `settings.cooldown` секунд (статическое поле `time`,
   default в коде 2, в конфиге 1) → отмена + сообщение с остатком секунд `{sec}` + алерт.
6. Обычное сообщение: цвета (4.8), сборка формата (4.9).

### 4.5. Фильтр запрещённого и система предупреждений (containsBlockedWords)
- Список запрещённых фраз: конфиг-ключ `blocks-text` (список строк; в дефолтном config.yml
  отсутствует — пуст → работает только авто-детектор рекламы). Сравнение в нижнем регистре,
  подстрокой. Тип блокировки — `block-types.swear.message` (default «запрещённое слово»).
- Антиреклама `ChatFilter.isMessageBlocked`:
  - IP-адрес: `цифра.цифра.цифра.цифра` с разделителями `[.,\-_*\s]`;
  - префиксы `http(s)://` и `www.` (с пробелами между символами);
  - доменные зоны из зашитого списка: su, mc, gomc, mclan, ru, com, net, org, xyz, site,
    online, fun, gift, pro, top, info, gg, me, io, cc, pw, tk, ml, ga, cf, gq, ws, club,
    shop, store, space, live, tv, vip, best — каждая буква шаблона допускает латиницу +
    кириллицу-омоглиф + цифры/символы (a/а/4/@, o/о/0, s/с/5/$, e/е/3 …), между буквами —
    мусорные разделители; срабатывание дополнительно проверяется эвристикой «похоже на домен»;
  - обфускация снимается нормализацией: fullwidth-символы, математические буквы
    (ᴀ-ᴢ и ᴬ-ᶻ Unicode-диапазоны), bold/italic mathematical alphanumeric symbols (суррогатные
    пары U+1D400–U+1D7FF), невидимые символы (zero-width, BOM, direction overrides), NFKC;
  - сообщение, состоящее из «стайлд»-символов при длине латиницы > 5, блокируется целиком.
  Тип блокировки — `block-types.ads.message` (default «реклама»).
- При блокировке игроку отправляются `messages.block-message-for-player` и
  `messages.warning-notify` (плейсхолдеры `{warning_count}`, `{max_warnings}`); всем с правом
  `litechat.alert.admin` — `admin.block-message-for-admin` (`{player}`, `{message}`, `{block_type}`).
- Счётчик предупреждений — в памяти (Map по UUID). При `warnings >= settings.max-warnings`
  (default 3): в главном потоке консолью выполняется `settings.mute-command`
  (default `mute {player} 5m Автоматический мут за нарушения чата -s`, `{player}` — ник),
  игроку — `messages.mute-message`, счётчик сбрасывается. Счётчик не сбрасывается при
  перезаходе и не сохраняется на диск (теряется при рестарте).
- Ключ `messages.already-muted` есть в config.yml, но кодом не используется (остаток).

### 4.6. Кулдаун новичков (isNewbie)
- Игрок с `firstPlayed` младше `settings.newbie-cooldown-minutes` минут (default 5) не может
  писать: отмена + `settings.newbie-cooldown-message` c `{time_played}` («N мин. M сек.»),
  лог в консоль `[Чат] Новичок … пытался писать в чат до истечения 5 минут`.

### 4.7. Антифлуд (handleFloodCheck)
- Если игрок написал ≥ 3 сообщений, где каждое следующее позже предыдущего менее чем на 2000 мс —
  отмена, `cooldown-for-chat` с `{sec}` = 2, отправка `sendAlertFlood` всем с правом
  `litechat.alert`: `messages.flood-alert` с `{playerName}`, `{msg}`.

### 4.8. Цвета в чате (processRegularMessage)
- Если сообщение содержит `&`/`&#`/`<#`, а права `litechat.color` нет: сообщение
  «&cУ вас нет разрешения для использования цветов в чате!», лог в консоль, из сообщения
  вырезаются все цвета (`HexUtil.removeColors`).
- При наличии права сообщение прогоняется через `HexUtil.color` (полная обработка HEX и градиентов).

### 4.9. Формат чата и радиус
- Группа/префикс/суффикс игрока берутся плейсхолдерами LuckPerms через PAPI:
  `%luckperms_primary_group_name%`, `%luckperms_prefix%`, `%luckperms_suffix%`.
- Цвет ника: `color.<группа_luckperms>` → fallback `color.global`/`color.local` (по префиксу
  «!») → fallback `&f`. Ключи `color.*` в дефолтном config.yml отсутствуют (необязательные).
- **Глобальный чат** — сообщение начинается с `!`: префикс «!» вырезается, формат
  `format.global`, рассылка всем онлайн через `spigot().sendMessage`, лог `[Глобальный чат]`.
- **Локальный чат** — все остальные: формат `format.local`, получатели — сам игрок + все игроки
  того же мира в радиусе `format.radius` блоков (default 100; считается
  `p.getLocation().distance(...)`, т.е. DifferentWorld → исключён ещё условием мира).
  В обоих случаях исходное событие отменяется и рассылка идёт вручную.
- Встроенные подстановки формата (заменяются после PAPI): `{player}` (окрашенный ник),
  `{prefix}`, `{suffix}`, `{color}`, `{message}`.
- **Hover на нике** (метод `withHover`): если в конфиге задан список `hover`, по отрисованному
  нику (значение `%holycustom_name%`, fallback — реальный ник) вешается HoverEvent SHOW_TEXT
  из строк `hover` с PAPI-плейсхолдерами и `{playtime}` = наигранное время из статистики
  `PLAY_ONE_MINUTE` в виде «Nч., Mм.». Хвост после ника наследует цвет (ChatColor.getLastColors).

### 4.10. Кулдаун после успешного сообщения
- После отправки обычного сообщения (без `litechat.bypass`) время последнего сообщения
  записывается в Map `cooldown` (см. 4.4 п.5).

---

## 5. Конфигурация (config.yml)

### 5.1. messages.*
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `messages.already-muted` | string | `&cВы замучены…` | Не используется кодом (остаток) |
| `messages.block-message-for-player` | string | `▶ Сообщение не было отправлено…` | Игроку при блокировке; `{warning_count}`, `{max_warnings}` |
| `messages.warning-notify` | string | `&6▶ Нарушение #…` | Счётчик предупреждений; `{warning_count}`, `{max_warnings}` |
| `messages.mute-message` | string | `&cВы получили мут на 5 минут…` | Игроку при авто-муте |
| `messages.cooldown-for-chat` | string | `&6▶ Чат будет доступен через {sec}` | Кулдаун/флуд; `{sec}` |
| `messages.flood-alert` | string | `▶ Игрок … пытается флудить: '{msg}'` | Алерт для `litechat.alert`; `{playerName}`, `{msg}` |

### 5.2. settings.*
| Параметр | Тип | Default (config / код) | Описание |
|---|---|---|---|
| `settings.bypass-permissions` | string | `litechat.bypass` | Право обхода всех ограничений |
| `settings.color-permissions` | string | `litechat.color` | Право на цвета |
| `settings.alert-permissions` | string | `litechat.alert` | Право получения алертов о флуде |
| `settings.disablechat-permissions` | string | `litechat.disable` | Право полного отключения своего чата |
| `settings.max-warnings` | int | 3 | Предупреждений до авто-мута |
| `settings.mute-command` | string | `mute {player} 5m Автоматический мут… -s` | Консольная команда мута; `{player}` |
| `settings.cooldown` | int | 1 / 2 (в коде) | Кулдаун между сообщениями, сек |
| `settings.newbie-cooldown-minutes` | int | 5 | Сколько минут новичок не может писать |
| `settings.newbie-cooldown-message` | string | `▶ Нужно отыграть 5м…` | Сообщение новичку; `{time_played}` |

### 5.3. admin.*
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `admin.block-message-for-admin` | string | `▶ Игрок {player} написал: '{message}' (Тип: {block_type})` | Алерт администраторам с правом `litechat.alert.admin` |

### 5.4. format.*
| Параметр | Тип | Default | Описание |
|---|---|---|---|
| `format.local` | string | (см. config.yml, c `%holystar_stars%`, `%chatbrackets_full%`, `%player_displayname%`, `{suffix}`, `%chatbrackets_color%`, `{message}`) | Формат локального сообщения |
| `format.global` | string | (см. config.yml) | Формат глобального сообщения (префикс `!`) |
| `format.radius` | int | 100 | Радиус локального чата, блоки |

### 5.5. Ключи, читаемые кодом, но отсутствующие в дефолтном config.yml
| Параметр | Тип | Default в коде | Описание |
|---|---|---|---|
| `hover` | list | пусто (hover отключён) | Строки подсказки на нике; поддерживает `{playtime}` и PAPI |
| `blocks-text` | list | пусто | Запрещённые фразы (подстрока, lower case) |
| `block-types.swear.message` | string | «запрещённое слово» | Тип блокировки для админ-алерта |
| `block-types.ads.message` | string | «реклама» | Тип блокировки для админ-алерта |
| `color.<группа>` / `color.global` / `color.local` | string | `&f` | Цвет ника игрока |

---

## 6. Хранение данных
- Файлов хранения нет. В runtime — четыре HashMap по UUID в `BukkitListener`
  (`cooldown`, `messageCount`, `lastMessageTime`, `warningCount`); очищаются при рестарте.
- Конфиг: единственный `config.yml` (saveDefaultConfig).

---

## 7. Интеграции
- **PlaceholderAPI** (жёсткая зависимость): применяются чужие экспансии HolyStar,
  ChatBrackets, HolyCustom, LuckPerms (перечень плейсхолдеров в §2.2). Собственной
  экспансии плагин не регистрирует.
- Косвенно зависит от плагинов ChatBrackets / HolyStar / HolyCustom / LuckPerms
  (их плейсхолдеры вшиты в формат чата) и от команды `mute` стороннего плагина.

---

## 8. Сценарий работы
1. Игрок (первый вход < 5 минут назад) пишет сообщение → отмена, сообщение о кулдауне новичка.
2. Обычный игрок пишет «купите на example dot ru» → ChatFilter распознаёт домен → отмена,
   +1 предупреждение, алерт администраторам.
3. Третье нарушение подряд → консоль выполняет `mute <ник> 5m … -s`, игроку — сообщение о муте.
4. Игрок шлёт 3 сообщения за 2 секунды → антифлуд отменяет, алерт `litechat.alert`.
5. Игрок пишет «привет» → проходит кулдаун (1 сек.), собирается формат `format.local`.
6. LuckPerms-плейсхолдеры подставляют префикс/суффикс, `color.<группа>` задаёт цвет ника.
7. Событие отменяется, получатели — игроки в радиусе 100 блоков того же мира, рассылка
   через Bungee-компоненты.
8. Ник оборачивается HoverEvent'ом со статистикой (наигранные часы/минуты).
9. Игрок пишет «!привет» → формат `format.global`, рассылка всем игрокам сервера.
10. Игрок с `litechat.color` пишет `&#FF0000 красный` → цвет обрабатывается HexUtil.

---

## 9. Особенности реализации и известные ограничения
- Оба формата отправки (локальный/глобальный) отменяют событие и рассылают сообщение сами —
  другие чат-плагины (ролевые, локальные каналы) видеть сообщения не будут.
- `settings.cooldown` читается в статическом инициализаторе при первом создании слушателя —
  `/reload` конфига без пересоздания класса на значение не влияет (поле `time` final-static).
- Счётчики предупреждений/кулдаунов живут только в памяти: рестарт сервера обнуляет их.
- Кулдаун-таймер пишется и после обычного сообщения, и при его обработке — фактически
  ограничение «1 сообщение в N секунд» для всех без bypass.
- Математические/стайлд-символы блокируются как обфускация даже без признаков рекламы,
  если в сообщении > 5 «нормальных» латинских символов.
- `ChatFilter.logCheck` — отладочный метод, в продакшн-пути не вызывается.
- Права не объявлены в plugin.yml — их нужно выдавать плагином прав (LuckPerms и т.п.).

---

## 10. Сборка
```
mvn -pl LiteChat package     # из каталога modules (или mvn package в LiteChat)
```
Особенностей нет: paper-api и placeholderapi — provided, шейд-плагин родительского pom
ничего не добавляет.
