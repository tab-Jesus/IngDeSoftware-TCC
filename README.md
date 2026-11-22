# turnos-api

API REST (Java 17, Spring Boot 3.3, PostgreSQL) que asigna sesiones de una sala comunitaria a períodos y equipos
mediante coloración de grafos y valida seis reglas de consistencia. Proyecto del TCC de Ingeniería de Software,
Universidad de Cartagena (CFVD).

## Ejecutar con Docker
```
docker compose up --build
```
- API: http://localhost:8080/api/v1/sesiones
- Swagger UI: http://localhost:8080/swagger-ui.html
- Salud: http://localhost:8080/actuator/health

Con `APP_SEED_ENABLED=true` (activo en docker-compose) se carga la instancia del TCC: 6 usuarios, 7 sesiones A-G, 3 períodos y 3 equipos.

## Probar
```
mvn test          # 47 pruebas: dominio, servicios (Mockito), controlador (MockMvc), integración (H2)
```
Colección de Postman: `postman/turnos-api.postman_collection.json` (ejecutar las carpetas en orden sobre una base recién iniciada).

## Endpoints principales (prefijo /api/v1)
| Recurso | Operaciones |
|---|---|
| /usuarios, /equipos, /periodos, /sesiones | CRUD (+ PUT /usuarios/{id}/disponibilidad) |
| /reservas | GET, POST (validada, 201/404/409/422), DELETE |
| POST /planificacion/generar?persistir=true | Agenda con el mínimo de períodos (campo `optimo`) |
| GET /planificacion/validar | Lista de violaciones (vacía = consistente) |
| GET /planificacion/grafo | Grafo de conflictos |

## Estructura
`domain` (sin Spring: grafo, planificador, validador) · `service` · `controller` · `repository` · `model` · `dto` · `exception` · `config`.
Diagramas en `docs/diagramas` (fuentes `.dot` de Graphviz).

## Variables de entorno
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DDL_AUTO` (por defecto `update`), `APP_SEED_ENABLED`.
