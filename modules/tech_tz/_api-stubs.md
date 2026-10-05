# Техническое задание — модуль _api-stubs

> Документ описывает фактическое содержимое модуля (`modules/_api-stubs`, ветка `mc-1.21.4`).
> Это НЕ Bukkit-плагин, а библиотека заглушек для компиляции.

---

## 1. Общие сведения

| Параметр | Значение |
|---|---|
| Артефакт (Maven) | `_api-stubs` (родитель `world.example:exampleworld-parent:1.0.0`) |
| Версия | 1.0.0 |
| Имя (pom) | «API stubs for plugins absent from this build» |
| plugin.yml / главный класс | Отсутствуют — артефакт не является плагином и на сервер не ставится |

**Назначение:** локальные компилируемые заглушки (compile-time stubs) публичных
API внешних серверных плагинов, которые не входят в эту сборку репозитория.
Позволяют модулям ссылаться на классы этих API при компиляции; в рантайме на
сервере вместо заглушек должны стоять реальные плагины (заглушки подключаются
со scope `provided` и в jar модулей не попадают).

---

## 2. Окружение и зависимости
- Общий родительский pom (`paper-api 1.21.4-R0.1-SNAPSHOT`, Java 21).
- Собственных зависимостей нет; используется только `org.bukkit.entity.Player`
  из paper-api (в заглушке `PremiumAPI`).

---

## 3. Состав (заглушенные API)

| Пакет / класс | Заглушка плагина | Методы | Поведение заглушки |
|---|---|---|---|
| `org.dimasik.anticheatstatus.AntiCheatStatus` | AntiCheatStatus | `public static AntiCheatStatus getInstance()` | бросает `UnsupportedOperationException("AntiCheatStatus is not installed")` |
| `org.dimasik.anticheatstatus.manager.ConfigManager` | — | `public int getInt(String path, int def)` | возвращает `def` |
| `org.dimasik.anticheatstatus.manager.DatabaseManager` | — | `public CompletableFuture getPlayerPoints(String player)` (raw type) | бросает `UnsupportedOperationException` |
| `ru.holydev.holyeco.HolyEcoAPI` | HolyEco (экономика) | `double getBalance(String player)`, `void addBalance(String player, double amount)`, `void subtractBalance(String player, double amount)` | все бросают `UnsupportedOperationException("HolyEco is not installed")` |
| `ru.holydev.holypremium.PremiumAPI` | HolyPremium (премиум-статус) | `boolean checkPremium(Player player)` | всегда `false` |
| `ru.nezxenka.syncservice.api.SyncAPI` | SyncService (кросс-серверная синхронизация) | `static void addAction(String type, String target, String action, String value)` | пустой метод (no-op) |

Каждый класс снабжён javadoc-комментарием
«Compile-time stub for the <плагин> plugin, which is not shipped in this build».

---

## 4. Потребители (модули, зависящие от `_api-stubs`, scope `provided`)

| Модуль | Что реально вызывает |
|---|---|
| `CheatReport` | `ReportManager` — классы AntiCheatStatus |
| `LiteAuction` | `economy/impl/HolyEco.java` — `HolyEcoAPI` (провайдер экономики аукциона) |
| `Shame` | `command/impl/Get.java` — API из заглушек (см. ТЗ Shame) |
| `HolyCustom` | `listeners/traps/TrapsListener.java` |

---

## 5. Хранение данных / конфигурация / события
Не применимо — модуль не содержит логики и ресурсов.

---

## 6. Сценарий использования
1. При сборке Maven сначала собирается `_api-stubs` (первый в списке `<modules>`
   родительского pom), затем модули-потребители компилируются против его классов.
2. `provided`-scope гарантирует, что классы заглушек не будут зашиты в jar'ы
   плагинов (иначе в рантайме они «перекрыли» бы реальные классы плагинов).
3. На сервере должны быть установлены реальные плагины AntiCheatStatus, HolyEco,
   HolyPremium, SyncService; их отсутствие приведёт к `NoClassDefFoundError`
   в местах вызова (или к молчаливому no-op для `SyncAPI`-заглушки, если класс
   всё же попал в jar).

---

## 7. Сборка
```
mvn -pl modules/_api-stubs package    # артефакт: target/_api-stubs-1.0.0.jar
```
Ставится в локальный репозиторий (`mvn install`) для сборки зависимых модулей.
