# RecruitmentCentre

Spring Boot foundation for the recruitment system in `sql/database.sql`.

## Packages

- `com.example.tuyendung.config`
- `com.example.tuyendung.controller`
- `com.example.tuyendung.dto.request`
- `com.example.tuyendung.dto.response`
- `com.example.tuyendung.entity`
- `com.example.tuyendung.exception`
- `com.example.tuyendung.repository`
- `com.example.tuyendung.service`
- `com.example.tuyendung.service.impl`
- `com.example.tuyendung.util`

## Run

1. Update DB credentials in `src/main/resources/application.properties`.
2. Run SQL scripts in this order:
   - `sql/database.sql`
   - `sql/mock_data.sql` (optional)
   - `sql/extra.sql` (optional views/procedures/triggers)
3. Start app:

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

