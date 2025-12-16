# Курс подготовки к собеседованию: Middle Kotlin (под вакансию «Лемана Про»)

> Формат: 6 недель интенсивной подготовки (5 дней/нед., 2–4 ч/день) + опциональные недельные модули. Все задания приводят к одному консистентному pet‑project’у, закрывающему требования вакансии.

---

## 0) Цели и критерии готовности (DoD)
**Ты готов, если можешь:**
- Писать чистый Kotlin‑код с корутинами (structured concurrency, `suspend`, `Flow`, отмена, таймауты, исключения).
- Поднять REST‑сервис на Spring Boot (или Ktor): валидация DTO, глобальная обработка ошибок, документация OpenAPI, версионирование, идемпотентность.
- Уверенно работать с PostgreSQL (индексы, план `EXPLAIN ANALYZE`, транзакции, блокировки), понимать и решать N+1 в JPA/Hibernate; альтернативно владеть Exposed (для Ktor).
- Применять Redis для кеширования (`@Cacheable`, TTL, инвалидация, ключи), понимать подводные камни сериализации.
- Писать unit/integ‑тесты (JUnit5/Kotest/MockK), интеграцию через Testcontainers (Postgres/Redis/WireMock), прогонять локально в Docker Compose.
- Собрать Docker‑образ, описать минимальный CI (GitHub Actions/GitLab CI) и артефакты, показать логи/метрики/health.

**Артефакты к интервью:**
- Git‑репо с pet‑project’ом: README, Postman/HTTP‑файлы, OpenAPI, миграции, docker‑compose, CI pipeline.
- Короткий one‑pager по архитектуре и trade‑offs.
- Личный чеклист ответов (ниже) + тезисы на 2–3 мин для self‑intro.

---

# Ускоренный план (2–3 недели)
> Два режима: **Hard 14 дней** (2–4 ч/день) и **Balanced 21 день** (1.5–3 ч/день). Оба ведут к тому же артефакту: сервис «Deadline Manager» + вопросник + CI/Docker. Каждый день есть **Deliverables** и **DoD**.

### Общая ежедневная форма (≈120–180 мин)
- 25–35 мин теория (конспект 10–12 пунктов).
- 70–100 мин код pet‑project’а.
- 20–30 мин тесты/`EXPLAIN`/бенч.
- 10–15 мин фиксация находок (шпаргалка).

---

## Режим A: Hard 14 дней
**D1 — Bootstrap & каркас API**
- Spring Boot 3.x, Gradle, слои API/Service/Repo, DTO + `@field:` валидация.
- OpenAPI (springdoc), RFC‑7807 handler, версионирование `/v1`.
**Deliverables:** каркас, 2 ручки (`POST /items`, `GET /items/{id}`), OpenAPI виден.
**DoD:** `curl`/HTTP‑файлы проходят, 201/400/422/404 корректны.

**D2 — Модель данных + миграции**
- Flyway, сущности `Item`, `Tag` (M:N), констрейнты.
- Индексы: по `dueDate`, частичный по `status='OPEN'`.
**Deliverables:** миграции v1..vN, сид‑скрипт.
**DoD:** `EXPLAIN` показывает использование индексов.

**D3 — Репозитории, поиск, пагинация**
- JPA spec/Query methods, сортировка, фильтры, идемпотентность `POST` (Idempotency‑Key + таблица токенов).
**Deliverables:** `GET /items?status=&from=&to=&tag=` + пагинация.
**DoD:** 200/206, корректные `Link`/`X-Total` (или поля в body).

**D4 — N+1: воспроизведение и лечение**
- Пример N+1 на теги, фиксы: `JOIN FETCH`, `@EntityGraph`, `default_batch_fetch_size`.
**Deliverables:** 2 версии эндпоинта: медленная и быстрая.
**DoD:** разница по `EXPLAIN ANALYZE`/времени ≥ 3×.

**D5 — Redis кеш**
- Cache‑aside, TTL=5m, инвалидация `PUT/DELETE`.
**Deliverables:** кеш для `GET /items/{id}` + метрики hit/miss в логах.
**DoD:** под нагрузкой hit‑ratio ≥ 80% (скрипт k6/JMeter).

**D6 — Коррутины в сервисе**
- Параллельная агрегация (3 внешних источника через WireMock), `withContext(Dispatchers.IO)`, `timeout`, отмена, `retryWhen`.
**Deliverables:** `GET /items/{id}/summary` (параллельные вызовы).
**DoD:** тест с `runTest` и фейковыми задержками проходит.

**D7 — Ошибки и устойчивость**
- Единый error catalog, маппинг доменных ошибок → HTTP.
- Идемпотентные ретраи (повтор `POST` не создаёт дубликат).
**Deliverables:** таблица ошибок в README, интеграционный сценарий.
**DoD:** негативные тесты покрывают ≥ 5 кейсов.

**D8 — Интеграционные тесты + Testcontainers**
- Поднять Postgres/Redis/WireMock контейнеры, `@ServiceConnection`.
**Deliverables:** e2e‑тест `create→get→update→get`.
**DoD:** `./gradlew test` зелёный локально и в CI.

**D9 — Наблюдаемость**
- Логи JSON с traceId (MDC), Micrometer + `/actuator`.
**Deliverables:** health/metrics, запросы видны в логах с корреляцией.
**DoD:** `curl /actuator/health` = `UP`, видны HTTP метрики.

**D10 — Dockerfile + Compose**
- Multi‑stage, slim JRE, `.dockerignore`, healthcheck.
**Deliverables:** образ < 200MB, compose: app+db+redis+adminer.
**DoD:** `docker compose up` поднимает весь стенд.

**D11 — CI (GitHub Actions или GitLab CI)**
- Кэш Gradle, тесты, сборка, buildx/push в registry.
**Deliverables:** pipeline на PR и `main`.
**DoD:** артефакт jar и image с тегом `sha-...` собираются.

**D12 — Ktor/Exposed (мини‑ветка)**
- Запуск альтернативы на Ktor + Exposed для 2–3 ручек.
**Deliverables:** отдельная ветка `ktor-exposed`.
**DoD:** базовые ручки и миграции работают, json = kotlinx.serialization.

**D13 — Полировка и документация**
- README, one‑pager архитектуры, коллекция Postman/HTTP.
**Deliverables:** репо «готов к ревью».
**DoD:** новый разработчик поднимает проект ≤ 10 минут.

**D14 — Mock‑интервью + регресс**
- Прогон вопросника, `EXPLAIN`, N+1, корутины, кеш.
**Deliverables:** список слабых мест + фиксы/заметки.
**DoD:** ответ по каждому блоку ≤ 90 секунд, с цифрами.

---

# Hard‑режим: Полная теория на D1–D2

## D1 — Bootstrap & каркас API: теория, которую нужно знать
**1) Архитектурные слои и договорённости**
- Слои: **API (web)** → **Service (domain/app)** → **Repo (data)**. Mapper между DTO и доменом (extension‑функции или MapStruct). Исключения домена не «протекают» наружу без маппинга.
- Конвенции пакетов: `api`, `service`, `domain`, `data(jpa)`, `config`, `common`.
- DTO не содержат бизнес‑логики; доменные модели — без аннотаций фреймворка (по возможности).

**2) Spring Boot 3.x essentials**
- Автоконфигурация и компонент‑сканирование (`@SpringBootApplication`). Стереотипы: `@Component`, `@Service`, `@Repository`, `@Configuration`.
- **ConfigurationProperties** для typed‑настроек: `@ConfigurationProperties(prefix="app")` + `@EnableConfigurationProperties`.
- Профили (`spring.profiles.active=dev,local`), `application.yml` vs `application-*.yml`.

**3) MVC‑конвейер**
- Filter → HandlerMapping → Interceptor → Controller → `@ResponseBody`/View → ExceptionResolver.
- Где логировать/ставить correlationId: фильтр/интерсептор.

**4) JSON в Kotlin**
- Jackson: подключить `jackson-module-kotlin`. По умолчанию учитывает nullability и дефолты Kotlin.
- Даты/время: добавить `jackson-datatype-jsr310` (входит в Boot). Настройка: `WRITE_DATES_AS_TIMESTAMPS=false`. Форматы ISO‑8601.
- `@JsonInclude(Include.NON_NULL)` на классе/глобально при необходимости.

**5) Валидация DTO (Jakarta Validation)**
- В Kotlin аннотации ставятся на **поле**: `@field:NotBlank`, `@field:Size`. Иначе валидатор может не сработать.
- Валидация query/path параметров: на уровне метода + `@Validated` на контроллере/классе.
- Кастомные валидаторы: `@Constraint(validatedBy=...)`, `ConstraintValidator<A, T>`.
- Группы валидации (создание/обновление) — по необходимости.

**6) Обработка ошибок (RFC‑7807 / ProblemDetail)**
- Spring 6+ имеет `ProblemDetail`. Возвращаем `application/problem+json`.
- Карта: бизнес‑исключения → 409/422; валидация → 400; не найдено → 404; неавторизован → 401; запрет → 403.
- Единая форма ответа: `type`, `title`, `status`, `detail`, `instance`, + `extensions` (id ошибки, поля).

**7) REST‑дизайн и статусы**
- Ресурсы во мн. числе: `/items`. Идемпотентность: `GET`, `PUT`, `DELETE` — да; `POST` — нет (кроме идемпотентного ключа — позже).
- 201 + `Location: /v1/items/{id}` при создании; 204 на `DELETE`; 200/206 для коллекций.
- Пагинация: `limit`/`offset` или курсоры. Отдавать `total` отдельно, если дешево.

**8) Версионирование API**
- Простой и понятный подход: **path‑версия** `/v1`. Меняем мажор при breaking changes; поддерживаем `v1` до sunset.
- Документация должна иметь версию; депрекейт метки в OpenAPI.

**9) OpenAPI (springdoc)**
- Библиотека: `org.springdoc:springdoc-openapi-starter-webmvc-ui`.
- Конфиг через `application.yml` (title, version). UI по `/swagger-ui.html`.
- Схемы DTO, коды ответов, content‑type `application/problem+json` для ошибок.

**10) Контроллеры: минимальные правила**
- Не лить доменные сущности наружу, только DTO.
- Валидировать вход (`@Valid`) и возвращать корректные статусы.
- Не трогать транзакции в web‑слое; сервис управляет границами.

**11) Маппинг DTO ↔ домен**
- Для скорости — extension‑функции `fun ItemEntity.toDto()` / `fun ItemCreateRq.toDomain()`.
- MapStruct актуален, если маппингов много и нужны compile‑time проверки.

**12) Логирование и корреляция**
- Формат JSON; `traceId`/`spanId` в MDC; логировать: метод, путь, статус, latency, userId (если есть).

**13) Тесты web‑слоя (минимум на D1)**
- Slice‑тесты `@WebMvcTest` для контроллера, мок сервиса (Mockk), проверка кодов/валидаций.

---

## D2 — Модель данных и миграции (PostgreSQL + JPA): теория
**1) Проектирование схемы под кейс**
- **Сущности**: `Item` (id, title, due_date, status, assignee_id, created_at, updated_at), `Tag` (id, name, created_at), `ItemTag` (item_id, tag_id, created_at).
- **Ключи**: предпочесть `BIGINT` + sequence (`GENERATED BY DEFAULT AS IDENTITY`) или `UUID` (дороже по индексации, но удобно для публичных ссылок). Для Postgres/Hibernate 6 хорошо работает `SEQUENCE`.
- **Время**: для «моментов» — `timestamptz`; для «дедлайна‑дата» — `date`. Если нужен часовой пояс/время — берём `timestamptz`.

**2) Нормализация и инварианты**
- `Tag.name` — уникален (case‑insensitive): `citext` или нормализуем в lowercase + `UNIQUE`.
- `Item.status` — `CHECK` ограничение (например, `OPEN|DONE|CANCELLED`) или enum‑таблица.
- Доп. инварианты через `CHECK` (например, `due_date >= current_date`).

**3) Индексы**
- Поиск по сроку: B‑Tree `idx_item_due_date`.
- Фильтр по статусу: **частичный индекс**: `CREATE INDEX ... ON item(due_date) WHERE status='OPEN';` — ускоряет только активные.
- M:N: `UNIQUE(item_id, tag_id)` в `item_tag`.
- Покрывающий индекс: `CREATE INDEX ... ON item(status, due_date) INCLUDE (title);` если часто выбираем эти поля.
- Порядок столбцов в составном индексе — по селективности и фильтрации.

**4) JPA‑моделирование связей**
- Прямая `@ManyToMany` удобна, но хрупка для продакшена (сложно расширять, нет дополнительных полей, каскады). **Рекомендуется** явная сущность `ItemTagEntity` с `@ManyToOne` на обе стороны + `UNIQUE(item, tag)`.
- Ленивая загрузка **по умолчанию**; никогда не делай `EAGER` без нужды.
- Транзакционные границы в сервисе (`@Transactional` на методах сервиса), не в контроллере.

**5) Типы и маппинг**
- Kotlin `LocalDate` → `date`; `Instant/OffsetDateTime` → `timestamptz`.
- Enum: хранить как `VARCHAR` (`@Enumerated(EnumType.STRING)`) — легче мигрировать.

**6) Flyway**
- Именование: `V001__init.sql`, `V002__add_item_tag.sql`. **Не переименовывать** существующие миграции.
- Повторяемые миграции для справочников/функций: `R__seed.sql`.
- Скрипты должны быть **детерминированы** и идемпотентны при повторном запуске dev.
- Разделять DDL/данные; не смешивать много изменений в один файл.

**7) Zero‑downtime паттерны миграций**
- Добавить колонку → задеплоить код, который пишет в обе → бэкфилл → сделать NOT NULL/удалить старое поле.
- Изменение типа: новую колонку + бэкфилл + свитч.

**8) Конвенции SQL**
- snake_case, ясные имена констрейнтов: `pk_item`, `fk_item_tag_item`, `uq_tag_name`.
- `created_at/updated_at` с `DEFAULT now()` + `trigger` (или в приложении) на обновление.

**9) Пример DDL (сущности и связи)**
```sql
-- V001__init.sql
CREATE TABLE item (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  title TEXT NOT NULL,
  due_date DATE NOT NULL,
  status TEXT NOT NULL DEFAULT 'OPEN',
  assignee_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (status IN ('OPEN','DONE','CANCELLED')),
  CHECK (char_length(title) BETWEEN 1 AND 255)
);

CREATE TABLE tag (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name CITEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE item_tag (
  item_id BIGINT NOT NULL REFERENCES item(id) ON DELETE CASCADE,
  tag_id  BIGINT NOT NULL REFERENCES tag(id)  ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_item_tag UNIQUE (item_id, tag_id)
);

CREATE INDEX idx_item_due_date ON item(due_date);
CREATE INDEX idx_item_open_due ON item(due_date) WHERE status='OPEN';
```

**10) `EXPLAIN (ANALYZE, BUFFERS)` базово**
- Смотри на: **Rows**, **Loops**, **Actual time**, **Filter**.
- Seq Scan → индекс не используется (возможно, малый селективность/не тот порядок колонок).
- `Bitmap Index Scan` часто для неполных покрытий.

**11) Возврат 201 и заголовок Location**
- После `POST` возвращай 201 и `Location: /v1/items/{id}` — это помогает клиентам и соответствует REST‑практике.

**12) Тестовые данные**
- Для dev — `R__seed.sql` (немного данных). Для тестов — билдеры/фикстуры в коде; **не** делать зависимость unit‑тестов от SQL‑сидов.

**13) Анти‑паттерны**
- `EAGER` по умолчанию; хранить даты как `TEXT`; `timestamp without time zone` для бизнес‑моментов; мягкие удаления без индекса на `is_deleted`.

---

# Hard‑режим: Полная теория на D3–D6

## D3 — Репозитории, поиск, пагинация, идемпотентность POST
**1) Подходы к запросам в Spring Data JPA**
- Derived queries (`findByStatusAndDueDateBetween`), `@Query` JPQL/SQL, `Specification` (динамические фильтры), Projections (DTO/интерфейсы) для выборки только нужных полей.
- Когда что: простые фильтры → derived; сложная динамика → `Specification`; узкие ответы → DTO‑projection.

**2) Пагинация и сортировка**
- **Offset‑pagination**: `Pageable`/`Page`. Просто, но медленно на больших offset (скан).
- **Keyset/seek**: `WHERE (due_date,id) > (:lastDue,:lastId) ORDER BY due_date,id LIMIT :limit`. Быстро, требует стабильного композитного сорт‑ключа и индекса `(due_date, id)`.
- Возвращай `nextCursor` (последние `dueDate`+`id`) и/или `Link: rel=next`.
- Для больших коллекций лучше `Slice` (не считает total).

**3) Консистентная сортировка**
- Всегда добавляй tiebreaker: `ORDER BY due_date ASC, id ASC`. Иначе дубликаты/пропуски между страницами.

**4) Мэппинг фильтров**
- Request → `SearchFilter(status?, fromDate?, toDate?, tag?)` → `Specification` через `and/or`. Не подставляй условия с `null`.

**5) Идемпотентность `POST` (Idempotency‑Key)**
- Клиент шлёт заголовок `Idempotency-Key`. Сервис хранит запись: `key` (UNIQUE), `req_hash`, `resource_id`, `status`, `response_checksum`, `created_at`.
- Повтор с тем же `key`: если `req_hash` совпадает → вернуть прежний результат/статус; иначе 409 (конфликт ключа и тела запроса).
- Реализация: вставка «замка», затем выполнение операции и фиксация результата.

**6) Надёжность и гонки**
- В БД: `INSERT ... ON CONFLICT DO NOTHING` (или Hibernate «поймать» `ConstraintViolationException`). Если конфликт — читаем существующую запись и возвращаем её результат.
- Очистка старых ключей по TTL (job). Не ставь слишком короткий TTL (<24h) — ретраи могут опоздать.
- Redis‑подход (`SET key uuid NX PX ttl`) хорош как лок, но хранить итог лучше в БД.

**7) Валидация пагинации**
- Ограничь `limit` (напр. 100). Отдавай 400 при `limit<=0` или неверных курсорах.

**8) Пример схемы и потока идемпотентности**
```sql
CREATE TABLE idempotency (
  key TEXT PRIMARY KEY,
  req_hash TEXT NOT NULL,
  resource_id BIGINT,
  status TEXT NOT NULL DEFAULT 'LOCKED',
  response_checksum TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_idemp_created ON idempotency(created_at);
```
```kotlin
@Transactional
fun create(cmd: CreateItemCmd, key: String): ItemRs {
  val hash = hash(cmd)
  val inserted = tryInsert(key, hash) // INSERT; return true/false
  if (!inserted) return replayOrConflict(key, hash)
  val saved = repo.save(cmd.toEntity())
  completeKey(key, saved.id!!, checksum(saved))
  return saved.toDto()
}
```

---

## D4 — N+1: выявление и устранение
**1) Что такое N+1**
- 1 запрос за N сущностями, затем N запросов за их связями (например, теги для каждого item). Симптомы: много коротких одинаковых SELECT по одному id.

**2) Диагностика**
- SQL‑лог (p6spy), Hibernate‑статистика, `EXPLAIN` по «медленным» местам. Увидишь многократные `select ... from tag where id=?`.

**3) Способы исправления**
- **JOIN FETCH**: подгрузить коллекции/отношения в одном запросе. Ограничения: пагинация по коллекциям ломает count/дубли; используем `DISTINCT` и/или «двухшаговый» паттерн (см. ниже).
- **EntityGraph**: декларативно указать пути загрузки (`@EntityGraph(attributePaths=["tags"])`). Удобно для `findById*`.
- **Batch fetch**: `spring.jpa.properties.hibernate.default_batch_fetch_size=100` или `@BatchSize`. Hibernate сгруппирует `IN (...)` для ленивых прокси.
- **DTO‑проекции**: запросить только необходимые поля (убежать от графа сущностей).

**4) Двухшаговая пагинация**
- Шаг 1: получить `ids` страницей (`SELECT i.id FROM item i ORDER BY ... LIMIT ... OFFSET ...`).
- Шаг 2: `SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.tags WHERE i.id IN :ids ORDER BY i.id`.
- Плюс: корректная пагинация; минус: два запроса.

**5) M:N и дубликаты**
- При `JOIN FETCH` коллекции будет декартово умножение → дубликаты строк. Используй `DISTINCT` в JPQL + обработку на уровне Hibernate, или собирай в DTO.

**6) Выбор стратегии**
- Деталка по одному ресурсy → `EntityGraph`/`JOIN FETCH`.
- Списки с пагинацией → двухшаговый или batch fetch.
- Тяжёлые графы → DTO‑проекции.

**7) Типичные ошибки**
- `EAGER` по умолчанию.
- Пытаться пагинировать `JOIN FETCH` коллекции одним запросом.
- Бесконтрольный размер графа (лавина по памяти).

**8) Минимальный конфиг**
```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=100
spring.jpa.properties.hibernate.generate_statistics=true
```

---

## D5 — Redis кеш: паттерны и подводные камни
**1) Что кешируем**
- Горячие `GET /items/{id}`. Не кешировать сложные поиски (высокая кардинальность). Не кешировать нестабильные данные без TTL.

**2) Паттерны**
- **Cache‑aside**: читаем → если miss → грузим из БД → кладём в Redis с TTL. При изменении → сначала БД, затем инвалидация/обновление кеша **после коммита**.
- **Write‑through/behind** — реже в приложениях на Spring.

**3) Ключи и TTL**
- Префиксы: `item::${id}`. Не смешивай через общий namespace.
- TTL с джиттером (±10–20%) для борьбы с синхронным истечением.

**4) Инвалидация**
- На `PUT/DELETE` → `@CacheEvict(cacheNames=["item"], key="#id")`.
- Гарантировать «после коммита»: `TransactionSynchronizationManager` или `@TransactionalEventListener(AFTER_COMMIT)`.

**5) Spring Cache нюансы**
- `@Cacheable(sync = true)` уменьшает stampede на один инстанс. Для кластера — распределённый лок.
- Условие `unless`/`condition` для пропуска больших/нестабильных объектов.
- Кешировать `null`? Можно с sentinel‑значением, но аккуратно с TTL.

**6) Сериализация**
- `GenericJackson2JsonRedisSerializer` или `KotlinModule` + `ObjectMapper`. Избегать JDK‑serialization.

**7) Мониторинг и память**
- Политика eviction (например, `volatile-lru`). Следить за hit/miss, memory usage. Не забывать `.flushAll` в dev.

**8) Мини‑конфиг**
```kotlin
@Bean
fun cacheManager(cf: RedisConnectionFactory) = RedisCacheManager.builder(cf)
  .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
  .withCacheConfiguration("item", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
  .build()
```

---

## D6 — Коррутины в сервисе: параллелизм, таймауты, отмена
**1) Где уместны корутины**
- Параллельные запросы к внешним сервисам, CPU‑лёгкая агрегация. При JDBC/JPA всё равно блокируем тред → оборачивай в `withContext(Dispatchers.IO)`.
- В контроллерах можно использовать `suspend` (Spring 6+ поддерживает). На Servlet‑стеке это не даёт неблокирующий I/O, но позволяет удобную модель конкурентности.

**2) Structured concurrency**
- `coroutineScope { val a = async { ... }; val b = async { ... }; awaitAll(a,b) }` — дети отменяются вместе при ошибке.
- Изолировать сбои: `supervisorScope` для независимых подпотоков.

**3) Таймауты и отмена**
- `withTimeout(500)`/`withTimeoutOrNull`. Отмену пробрасывать наверх; убеждаться, что блокирующие вызовы в `IO` dispatcher.
- Сопоставление таймаутов HTTP‑клиента (например, WebClient/OkHttp) и сервиса.

**4) Повтор и backoff**
- `retryWhen { cause, attempt -> delay(exp(attempt)); }`. Останавливай ретраи на 4xx.

**5) Ограничение параллелизма**
- `Semaphore`/`Channel` для лимита внешних вызовов.

**6) Тестирование корутин**
- `kotlinx.coroutines.test.runTest { ... }` + `advanceTimeBy`/`TestScope`.
- Фейки задержек/WireMock для внешних.

**7) Пример агрегатора**
```kotlin
suspend fun summary(id: Long): Summary = coroutineScope {
  val a = async(Dispatchers.IO) { extA(id) }
  val b = async(Dispatchers.IO) { extB(id) }
  val c = async(Dispatchers.IO) { extC(id) }
  withTimeout(800) { Summary(a.await(), b.await(), c.await()) }
}
```

**8) Транзакции и `suspend`**
- `@Transactional` на `suspend` методах поддерживается в Spring 6+. Граница транзакции охватывает приостановки, но избегай долгих внешних вызовов внутри транзакции.

---

