# Matchmaking Service

## Контекст

Ты разрабатываешь систему подбора матчей для MOBA-игры 5v5 (типа League of Legends / Dota).

---

## Игрок

| Поле | Описание |
|------|----------|
| id | Уникальный идентификатор |
| mmr | Matchmaking Rating (0–3000) |
| primaryRole | Основная роль |
| secondaryRole | Запасная роль |

**Роли:** `TOP`, `JUNGLE`, `MID`, `ADC`, `SUPPORT`

---

## Требования

### Базовые (MVP)

1. Игрок встаёт в очередь
2. Система формирует матч из 10 игроков (2 команды по 5)
3. Каждый игрок в команде занимает уникальную роль
4. Игрок получает свою primary или secondary роль
5. Разница среднего MMR команд ≤ 50 (идеал)

```java
void enqueue(Player player)
Optional<Match> tryCreateMatch()
void cancelQueue(String playerId)
```

---

### Расширенные

**Autofill:**
- Если невозможно собрать матч из-за нехватки ролей — разрешить autofill
- Autofill = игрок получает роль, которую не выбирал
- Не более 1 autofill на команду
- Autofill защита: если игрок был autofill в прошлом матче, он не может быть autofill снова

**Время ожидания:**
- Чем дольше игрок в очереди, тем шире допустимый диапазон MMR
- После 5 минут — гарантированный матч (качество вторично)

**Приоритеты:**
1. Primary role > Secondary role > Autofill
2. Качество матча (близкий MMR) > Скорость
3. Игроки с долгим ожиданием — приоритет

---

## Формат результата

```java
record MatchResult(
    boolean found,
    Match match,
    String reason          // Если не найден — почему
)

record Match(
    String matchId,
    Team team1,
    Team team2,
    int mmrDifference
)

record Team(
    Map<Role, PlayerAssignment> roster  // Role -> кто играет
)

record PlayerAssignment(
    Player player,
    Role assignedRole,
    AssignmentType type    // PRIMARY, SECONDARY, AUTOFILL
)
```

---

## Примеры

### Пример 1: Идеальный матч

```
Очередь (10 игроков):
  Player(mmr=1500, primary=TOP, secondary=MID)
  Player(mmr=1510, primary=JUNGLE, secondary=TOP)
  Player(mmr=1490, primary=MID, secondary=SUPPORT)
  Player(mmr=1505, primary=ADC, secondary=MID)
  Player(mmr=1495, primary=SUPPORT, secondary=ADC)
  Player(mmr=1520, primary=TOP, secondary=JUNGLE)
  Player(mmr=1480, primary=JUNGLE, secondary=MID)
  Player(mmr=1515, primary=MID, secondary=TOP)
  Player(mmr=1485, primary=ADC, secondary=SUPPORT)
  Player(mmr=1500, primary=SUPPORT, secondary=ADC)

Результат:
  Team1 (avg 1500): TOP=1500, JG=1510, MID=1490, ADC=1505, SUP=1495
  Team2 (avg 1500): TOP=1520, JG=1480, MID=1515, ADC=1485, SUP=1500
  MMR diff: 0 ✓
  Все на primary ролях ✓
```

### Пример 2: Secondary role

```
Очередь (10 игроков, 3 MID-мейна):
  Player(mmr=1500, primary=MID, secondary=TOP)    ← получит TOP
  Player(mmr=1500, primary=MID, secondary=SUPPORT)
  Player(mmr=1500, primary=MID, secondary=ADC)
  ... остальные

Результат:
  Один MID получает secondary роль (TOP)
```

### Пример 3: Autofill

```
Очередь (10 игроков, 0 SUPPORT-мейнов):
  Все: primary ≠ SUPPORT, secondary ≠ SUPPORT

Результат:
  2 игрока получают SUPPORT через autofill (по одному на команду)
```

### Пример 4: Невозможно собрать

```
Очередь (8 игроков)

Результат:
  MatchResult(found=false, reason="not_enough_players")
```

---

## Edge Cases

- [ ] Меньше 10 игроков
- [ ] Все хотят одну роль (5 MID-мейнов)
- [ ] Невозможно распределить роли даже с autofill
- [ ] Огромный разброс MMR (500 vs 2500)
- [ ] Игрок вышел из очереди во время формирования матча
- [ ] Дубликат — игрок уже в очереди
- [ ] primary == secondary (невалидно?)

---

## Метрики качества матча

| Метрика | Идеал | Допустимо | Плохо |
|---------|-------|-----------|-------|
| MMR diff | 0–20 | 21–50 | >50 |
| Primary role % | 100% | ≥80% | <60% |
| Autofill на команду | 0 | 1 | >1 |
| Время ожидания | <2 мин | 2–5 мин | >5 мин |

---

## Подсказки (минимальные)

**Структуры данных:**
- Подумай как эффективно искать игроков по MMR-диапазону
- Подумай как группировать игроков по ролям

**Распределение ролей:**
- Это задача о назначениях (assignment problem)
- Можно решать жадно или перебором (10 игроков — не много)

**Баланс команд:**
- После выбора 10 игроков — как разделить на 2 команды с минимальной разницей MMR?
- Это вариация задачи о разбиении множества (partition problem)

---

## Бонусные фичи (если останется время)

1. **Duo Queue** — два игрока в очереди вместе (должны попасть в одну команду)
2. **Role priority queue** — отдельные очереди по ролям
3. **MMR decay** — снижение допустимого качества со временем
4. **Match history** — не ставить в одну команду игроков, которые недавно репортили друг друга
5. **Ranked tiers** — Bronze не может играть с Diamond даже при близком MMR

---

## Что оценивается

- Корректность распределения ролей
- Качество баланса команд
- Обработка edge cases
- Чистота кода и структура классов
- Умение объяснить tradeoffs
