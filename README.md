# Cineplanet 2026 — Backend API

API REST para la gestión de un sistema de reservas de cine. Soporta catálogo de películas, funciones (showtimes), reservas de tickets y autenticación JWT basada en roles (`ADMIN` / `CUSTOMER`).

## Stack

| Tecnología | Versión |
|------------|---------|
| Java | 21 (LTS) |
| Spring Boot | 4.0.7 |
| MySQL | 8.0 |
| Gradle | 9.5.1 |
| Docker & Docker Compose | latest |

Para la justificación técnica detallada (patrón de arquitectura, diagrama de componentes y decisiones de diseño), consulta **[ARCHITECTURE.md](ARCHITECTURE.md)**.

---

## Ejecución local con Docker Compose

### Requisitos previos

- Docker Engine y Docker Compose instalados.
- Puerto **3308** y **8090** disponibles en tu máquina (puedes cambiarlos vía `.env`).

### Paso 1: Crear archivo `.env`

Crea un archivo `.env` en la raíz del repositorio con el siguiente contenido:

```bash
DB_HOST=localhost
DB_PORT=3308
DB_NAME=cinetest_db
DB_USERNAME=root
DB_PASSWORD=root
SERVER_PORT=8090
JWT_SECRET=cambia-esto-en-produccion
JWT_EXPIRATION_HOURS=24
```

> El archivo `.env` es leído automáticamente por `docker-compose.yml`.

### Paso 2: Levantar servicios

```bash
docker-compose up -d
```

Esto levantará:
- **MySQL** en `localhost:3308`
- **API Spring Boot** en `http://localhost:8090`

### Paso 3: Verificar que está corriendo

```bash
curl http://localhost:8090/api/health
```

Respuesta esperada:
```json
{ ... , "status": "UP" }
```

### Detener servicios

```bash
docker-compose down
```

Para eliminar también el volumen de datos de MySQL:

```bash
docker-compose down -v
```

---

## Variables de entorno

| Variable | Requerida | Valor por defecto | Descripción |
|----------|-----------|-------------------|-------------|
| `DB_HOST` | Sí | `localhost` | Host de la base de datos MySQL. En Docker Compose interno usa `db`. |
| `DB_PORT` | Sí | `3308` | Puerto expuesto de MySQL en el host local. |
| `DB_NAME` | Sí | `cinetest_db` | Nombre de la base de datos. |
| `DB_USERNAME` | Sí | `root` | Usuario de la base de datos. |
| `DB_PASSWORD` | Sí | `root` | Contraseña del usuario de la base de datos. |
| `SERVER_PORT` | Sí | `8090` | Puerto en el que escucha la aplicación Spring Boot. |
| `JWT_SECRET` | Sí | `change-me-in-production` | Clave secreta para firmar tokens JWT. **Cámbiala en producción.** |
| `JWT_EXPIRATION_HOURS` | No | `24` | Tiempo de expiración de los tokens JWT en horas. |

---

## Despliegue público

- **API desplegada:** `https://TBD-COMPLETAR-URL`  *(pendiente de configurar)*
- **Swagger UI (documentación interactiva):** [`https://TBD-COMPLETAR-URL/api/docs`](https://TBD-COMPLETAR-URL/api/docs) *(pendiente de configurar)*

> Actualiza las URLs anteriores con los enlaces de tu plataforma de despliegue (Render, Railway, AWS, etc.).

---

## Colección Postman

Los archivos de Postman para testing de la API están incluidos en este repositorio:

| Archivo | Descripción |
|---------|-------------|
| [`postman/cinetest-collection.json`](postman/cinetest-collection.json) | Colección con todos los endpoints organizados por funcionalidad (Auth, Movies, Showtimes, Bookings). |
| [`postman/cinetest-environment.json`](postman/cinetest-environment.json) | Environment con variables `baseUrl`, `jwtToken`, `movieId`, etc. Soporta captura automática de token tras login. |

### Cómo usar

1. Importa ambos archivos en Postman (`File → Import`).
2. Selecciona el environment **Cinetest Environment** en el selector superior derecho.
3. Ejecuta primero **Register** o **Login** en la carpeta **Auth**. El token JWT se guardará automáticamente en la variable `jwtToken`.
4. Los demás requests usarán `{{jwtToken}}` automáticamente en el header `Authorization`.

---

## Documentación adicional

- **[ARCHITECTURE.md](ARCHITECTURE.md)** — Patrón de arquitectura, diagrama de componentes y decisiones técnicas justificadas.

---

## Respuesta de Escenario de Escalabilidad

#### 1. ¿Cómo garantizarías que no se vendan más asientos de los disponibles bajo esta concurrencia?
La garantía de no overbooking se gestionaría en una capa de memoria distribuida utilizando **Redis** como la fuente de la verdad del inventario en tiempo real.

* **Contadores Atómicos:** Cada función tendrá una llave numérica en Redis que representará sus asientos disponibles (`availableSeats`) con una key del estilo `showtime::\<showtime_id>::available-seats`.
* **Reducción en Memoria:** Al recibir un intento de reserva, la aplicación ejecutará el comando atómico **`DECRBY llave_showtime cantidad_asientos`** directamente en Redis. Si el resultado del comando es menor a 0, la solicitud se rechaza inmediatamente arrojando un error `409 Conflict`. Si es mayor o igual a 0, la capacidad queda reservada de forma segura a nivel lógico.

---

#### 2. ¿Qué cambios harías a tu arquitectura actual para soportar esta carga sin caerse?
Para evitar el colapso del servidor por retención de hilos y agotamiento del pool de conexiones, se implementarán los siguientes cambios estructurales:

* **Asincronía en la Persistencia:** El endpoint principal `POST /api/bookings` dejará de escribir directamente en la base de datos relacional. Tras la validación en Redis, responderá inmediatamente al cliente con un código `202 Accepted` y un ID de seguimiento.
* **Desacoplamiento:** Se extraerá la lógica de persistencia del monolito hacia un microservicio independiente (*Booking Processor*). Este componente se configurará en la nube con **Auto-scaling horizontal** automático para instanciar réplicas bajo demanda, procesando las transacciones aprobadas a un ritmo seguro para el almacenamiento final.

---

#### 3. ¿Qué tecnologías o patrones adicionales introducirías?
Para sostener la infraestructura distribuida y el flujo asíncrono, se integrarán los siguientes componentes:

* **Apache Kafka (Patrón de Mensajería Asíncrona):** Se utilizará como un buffer de absorción de tráfico masivo. El API Gateway publicará los eventos en el tópico `reserva-solicitada` y Kafka garantizará el almacenamiento temporal y el orden estricto de llegada por partición para que los Workers los consuman de forma controlada.
* **Redis:** Base de datos en memoria para la gestión de contadores atómicos de asientos disponibles y para la implementación del patrón **Fail-Fast**, aprovechable tambien para definir un rate limiting si se escala en microservicios la aplicacion.
* **Patrón Fail-Fast:** Implementado directamente en el API perimetral utilizando el contador de Redis. Toda solicitud que llegue cuando el contador sea menor a 0 será bloqueada y rebotada en milisegundos, impidiendo que el tráfico excedente ingrese a la red interna o sature la cola de Kafka.
