# Техническое задание — модуль Shame

> Документ описывает фактическую реализацию модуля на основе анализа исходного кода
> (`modules/Shame`, ветка `mc-1.21.4`). Служит спецификацией для восстановления,
> доработки и поддержки плагина.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Название плагина | Shame |
| Артефакт (Maven) | `Shame` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0-SNAPSHOT |
| Главный класс | `org.dimasik.shame.Shame` |
| API-версия Bukkit | 1.16 |
| Авторы (plugin.yml) | не указаны |

**Назначение:** админско-античит-инструмент «наказаний» (shame = позор): баны с
театральной анимацией (левитация, разброс предметов, частицы, звуки) перед
наказанием, «отложенный бан» (игрок банится при первом ударе по другому игроку),
краш-клиента целевого игрока спамом частиц, множитель входящего урона и «откат»
перемещения игрока. Баны выполняются консольными командами LiteBans.

---

## 2. Окружение и зависимости

### 2.1. Сервер и Java
- Paper/Spigot, API 1.16+ (сборка против `paper-api 1.21.4-R0.1-SNAPSHOT`, provided).
- Java 21.

### 2.2. Плагины
| Плагин | Область применения | Обязательность |
|---|---|---|
| LiteBans | Команды `ban <ник> <причина>`, `banip <ник> <причина>`, `litebans broadcast <сообщение>` от имени консоли | Обязателен для фактического бана/броадкаста |
| AntiCheatStatus | Подкоманда `get`: очки античита (`databaseManager.getPlayerPoints`), порог `configManager.getInt("ban.value", 600)` | Мягкая (нет — `get` упадёт при запросе очков) |

### 2.3. Библиотеки
- HikariCP 4.0.3 (compile, shade в jar) — пул MySQL.
- `_api-stubs` 1.0.0 (provided) — заглушки AntiCheatStatus для компиляции.
- Lombok 1.18.32, jetbrains annotations 24.1.0 (provided).

### 2.4. База данных
Только MySQL (конфиг `mysql.*`); SQLite не поддерживается.

---

## 3. Архитектура (структура пакетов)

```
org.dimasik.shame
├── Shame                        — главный класс: конфиг, команда /shame (+TabCompleter),
│                                  регистрация подкоманд и модулей, init DatabaseManager
├── command.CommandExecutor      — TabExecutor: роутинг подкоманд, автодополнение (подкоманды/ники онлайн)
├── command.SubCommand           — абстракция подкоманды (execute, getTabCompletes, getRequiredArgs,
│                                  getUsage, register, хелперы сообщений leaveUsage/leavePNF)
├── command.impl.Add             — внести/убрать игрока в «отложенный бан» (таблица nicknames)
├── command.impl.Ban             — бан с анимацией немедленно (флаги no/new/old/-n)
├── command.impl.Crash           — назначить краш-клиент
├── command.impl.Damage          — задать множитель урона игрока
├── command.impl.Get             — «статус» игрока (в базе = 100%, иначе % очков античита)
├── command.impl.Rollback        — запустить откат перемещения
├── config.Config                — статическая обёртка FileConfiguration + утилита replace(строка, Pair...)
├── database.DatabaseManager     — HikariCP-пул MySQL; CRUD таблицы nicknames (async CompletableFuture)
├── modules.Module               — базовый Listener c registerListener()
├── modules.impl.AddModule       — EntityDamageByEntityEvent: игрок из базы → отложенный бан срабатывает
├── modules.impl.BanModule       — вся логика анимации бана (новая/старая), заморозка, финализация
├── modules.impl.CrashModule     — краш-клиента спамом Particle.ASH
├── modules.impl.DamageModule    — EntityDamageEvent: множитель урона по карте editedDamages
├── modules.impl.RollbackModule  — запись траектории 30 позиций и откат телепортами
└── utils
   ├── Pair                      — generic-пара (used в replace)
   ├── Parser                    — цвета: HEX `&#rrggbb` + `&x` через ChatColor bungee
   └── ParticleDataStorage       — сгенерированный массив кадров (tick, частицы x/y/z/RGB, звуки)
                                  для «старой» анимации бана (~3000 строк данных)
```

---

## 4. Функциональные требования

### 4.1. Команда `/shame` (алиасы нет)
Единственное право — `shame.use` (на всю команду и все подкоманды, из plugin.yml;
отдельных прав на подкоманды нет).

Общий формат: `/shame <подкоманда> [игрок] ...`; при неверном числе аргументов —
сообщение `&b&l▶ &fИспользование: &6/shame <подкоманда> [игрок] <usage>`;
игрок не найден — `&b&l▶ &fИгрок &6не найден&f.`.
Tab-complete: 1-й аргумент — подкоманды, 2-й — ники онлайн, далее — `getTabCompletes`.

| Подкоманда | Синтаксис | Поведение |
|---|---|---|
| `add` | `/shame add <ник>` | Если ник есть в таблице `nicknames` — он удаляется из базы и игрок немедленно банится с анимацией (сообщение «Игрок был уже в базе и он забанен!»). Если нет — добавляется в базу, «отложенный бан»: сообщение «Вы добавили игрока в бан, как только он ударит что либо то он будет забанен!». Хардкод: ник `kotikstasika` — всегда сообщение «Игрок был уже в базе и он забанен!» (защита от наказания конкретного игрока) |
| `ban` | `/shame ban <ник> [no] [new\|old] [-n]` | Бан прямо сейчас. Флаг `no` — анимация без фактического бана; `new`/`old` — тип анимации (взаимоисключающие, конфликт = молчаливый выход); `-n` — мгновенная финализация (`instant=true`); без флага тип анимации случайный (`Random.nextBoolean()`). Если игрок офлайн — консоль: `ban <ник> <причина>` + broadcast |
| `crash` | `/shame crash <ник>` | Краш-клиент: асинхронно 1000 раз `spawnParticle(Particle.ASH, loc, 10000)`. Сообщение «Игроку <ник> назначен краш.». Ник `kotikstasika` игнорируется |
| `damage` | `/shame damage <ник> <множитель>` | Множитель входящего урона (double) для игрока; `0` — удалить из памяти («Переменная удалена с памяти.»); успех — «Переменная задана.»; не число — «Множитель должен быть числом двойной точности.». Ник `kotikstasika` игнорируется (проверка до null-check — NPE при офлайн-игроке) |
| `get` | `/shame get <ник>` | Если ник в базе: «Статус игрока: 100%, он будет забанен как ударит кого либо». Иначе: `round(очки_античита / порог * 100)%`, где очки = `AntiCheatStatus...getPlayerPoints(ник)`, порог = `getInt("ban.value", 600)` (конфиг AntiCheatStatus) |
| `rollback` | `/shame rollback <ник>` | «Откат начат.» — воспроизведение перемещения игрока назад (см. 4.3) |

### 4.2. Анимация бана (`BanModule.startBanAnimation(player, reason, ban, newAnim)`)
Игрок «замораживается» (`frozenPlayers` по UUID): отменяются движение (по X),
команды, телепорты, подбор предметов; при `ban=true` каждые тик отключаются
полёт/полёт-режим/скольжение/плавание.

**Новая анимация (`new`):**
- Эффект `LEVITATION` 130 тиков, усилитель 6.
- Таймер каждый тик (до ~60–70 тиков): разброс инвентаря — по 1+ предмету за тик
  выбрасывается (`dropItemNaturally` на высоте +1, звук `ENTITY_ITEM_PICKUP`,
  2.0F/0.8F), слоты идут по кругу; вокруг ног частицы `WITCH` (3 точки × 4 частицы).
- Финализация: звук `ENTITY_WITHER_SPAWN` (3.0F/3.0F), случайное сообщение из
  `banacmsg` через `litebans broadcast`; при `ban=true` — `ban <ник> <причина>`
  и `player.setHealth(0)`.

**Старая анимация (`old`):**
- Эффект `LEVITATION` 140 тиков, усилитель 1.
- Таймер до ~105–110 тиков: покадровое воспроизведение `ParticleDataStorage.startAnimation`
  относительно стартовой позиции: частицы `DUST` (RGB из кадра, size 1.0) или
  `FIREWORK` (color == −1), звуки по кадрам (`Sound.valueOf`, ошибки глушатся).
- Финализация: телепорт на +4 по Y, облако частиц `CLOUD` (5 сверху/5 снизу);
  при `ban=true` — `ban <ник> <причина>` + broadcast `ban.broadcast`.

**Общие правила финализации:** досрочно — при погружении в жидкость, значении
`instant=true` (флаг `-n`) или по истечении таймера; при выходе игрока с сервера —
`finishBanAnimation` (старый вариант, гарантия бана).

### 4.3. Прочие модули
- **AddModule** (`EntityDamageByEntityEvent`, оба участника — игроки, событие не
  отменено): асинхронная проверка ника атакующего в `nicknames`; если есть — ник
  удаляется, в главном потоке запускается анимация бана (`ban=true`, случайный тип,
  `instant=false`).
- **DamageModule** (`EntityDamageEvent`): если для игрока задан множитель —
  `damage *= множитель`.
- **RollbackModule**: 30 тиков (шаг 2) пишет позиции игрока в deque, затем с 60-го
  тика каждый тик телепортирует игрока в позиции с конца (воспроизведение пути назад).
- **CrashModule**: асинхронный спам частиц (1000 итераций × 10000 частиц ASH).

### 4.4. Формат сообщений и подстановки
- Цвета: `&#rrggbb` (HEX) и `&x`-коды (`Parser.color`).
- `%player%` — ник игрока (в `ban.broadcast` и `banacmsg`).
- Плейсхолдеров PlaceholderAPI нет.

---

## 5. Конфигурация (`config.yml`)

| Параметр | Тип | Default (в ресурсе) | Описание |
|---|---|---|---|
| `mysql.host` | string | `localhost` | Хост MySQL |
| `mysql.port` | int | `3306` | Порт MySQL |
| `mysql.user` | string | `root` | Пользователь |
| `mysql.password` | string | `"сайнес гпт кодер"` | Пароль (дефолт в коде совпадает) |
| `mysql.database` | string | `lite_shame` | Имя базы |
| `ban.reason` | string | `30d &cОбнаружено сторонние ПО. … --sender=&c&lАнтиЧит` | Причина бана (первое слово — срок LiteBans) |
| `ban.broadcast` | string | `litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом.` | Консольная команда broadcast при бане (`%player%`) |
| `banacmsg` | list<string> | 11 шутливых строк «Античит решил, что %player% слишком хорош…» | Случайное сообщение в broadcast при финале новой анимации |

---

## 6. Хранение данных

MySQL, база `lite_shame` (создаётся `CREATE DATABASE IF NOT EXISTS … utf8mb4`),
таблица:
```
nicknames (nickname VARCHAR(16) PRIMARY KEY NOT NULL,
           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```
- API `DatabaseManager`: `addNickname` (true/`false` при дубликате, ошибка 1062),
  `nicknameExists`, `deleteNickname`, `getNicknameCount` — все `CompletableFuture`
  на общем ForkJoinPool; nick тримится и обрезается до 16 символов.
- Пул HikariCP: max 10, min idle 2, connectionTimeout 30 c, idleTimeout 10 мин,
  maxLifetime 30 мин, плюс набор оптимизаций prepared-statement cache.
- `onDisable` в главном классе пул не закрывает (закрытие только через
  `shutdown()`/`finalize()` DatabaseManager).

---

## 7. Интеграции
- **LiteBans** — фактический исполнитель банов и broadcast'ов (консольные команды
  `ban`, `banip`, `litebans broadcast`).
- **AntiCheatStatus** — чтение очков и порога для `/shame get` (через реальные
  классы плагина; компиляция против заглушек `_api-stubs`).
- PlaceholderAPI / Vault — не используются.

---

## 8. Сценарий работы
1. Античит/админ выявляет нарушителя → `/shame add <ник>` (отложенный бан)
   или `/shame ban <ник>` (немедленный).
2. Отложенный: игрок в базе; при первом ударе по игроку AddModule запускает анимацию.
3. Анимация: заморозка (движение/команды/телепорт/подбор отменяются), левитация,
   разброс инвентаря (новая) или частицы/звуки по кадрам (старая).
4. Финал: бан консолью LiteBans с причиной `ban.reason`, broadcast; в новой
   анимации — шутливое сообщение из `banacmsg` и смерть игрока.
5. Отдельные инструменты: `/shame crash` (краш клиента), `/shame damage` (множитель
   урона), `/shame rollback` (откат перемещения), `/shame get` (процент статуса).

---

## 9. Особенности реализации и известные ограничения
- Хардкод-защита ника `kotikstasika` от наказаний (в add/crash/damage).
- В `onMove` сравнивается `getTo().getX() != getFrom().getX()` дважды (Z не
  проверяется — фактически заморозка только по оси X).
- `Damage` вызывает `target.getName()` до null-проверки — NPE для офлайн-игрока.
- `CrashModule` и `RollbackModule` не регистрируют листенеры (конструктор без
  `registerListener()`), `CrashModule.startCrash` спавнит частицы из асинхронного
  потока — потенциально небезопасно для Paper.
- Все запросы к БД в `add`/`get` — `.get()` (блокирующее ожидание в главном потоке).
- Причина бана начинается с длительности LiteBans (`30d …`) — форматLiteBans-специфичный.
- `Add`: если игрок в момент команды онлайн — анимация; иначе `banip <ник>` + broadcast.
- Команда не имеет вложенных прав: доступ к `crash`/`damage`/`ban` определяется
  одним `shame.use`.

---

## 10. Сборка
```
mvn -pl modules/_api-stubs install   # сначала заглушки (если ещё не установлены)
mvn -pl modules/Shame package
```
Maven Shade зашивает HikariCP 4.0.3; `_api-stubs`, lombok, annotations — provided.
Артефакт — `target/Shame-1.0-SNAPSHOT.jar`.
