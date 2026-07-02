# Deber: Microservicio por capas con autenticación (AWS Cognito)

## Objetivo

Construir un microservicio **igual en estructura** al proyecto de referencia `students`
(capas, mappers, excepciones, `GlobalExceptionHandler`, tests con **100% de cobertura** en
la capa de services) y **agregarle seguridad con AWS Cognito** como en el proyecto `demo_auth`:
**un endpoint público** (sin token) y **otro autenticado** (con token).

En este deber **NO se usan roles ni permisos**. Solo hay dos estados: *autenticado* o
*no autenticado*. Quien tenga un token válido de Cognito entra; quien no, recibe `401`.

---

## 1. El problema a resolver — Microservicio `events`

Vas a construir el backend de una **plataforma de reserva de entradas para eventos**
(conciertos, festivales, charlas). El sistema permite que **cualquiera vea la cartelera de
eventos** (público), pero **solo un usuario autenticado puede reservar o cancelar entradas**
(privado).

El microservicio se llama **`events`**, por lo tanto:

- **Paquete base:** `com.pucetec.events`
- **Nombre del repositorio:** `ae_puce_2026_[nrc]_[nombre]_[apellido]`
  (ejemplo: `ae_puce_2026_1473_juan_perez`)

### 1.1 Tablas (entities) — son 3

| Tabla          | Entidad        | Campos                                                                         |
|----------------|----------------|--------------------------------------------------------------------------------|
| `attendees`    | `Attendee`     | `id`, `name`, `email`                                                           |
| `events`       | `Event`        | `id`, `name`, `venue`, `totalTickets`, `availableTickets`                       |
| `reservations` | `Reservation`  | `id`, `attendee` *(ManyToOne)*, `event` *(ManyToOne)*, `status`, `createdAt`    |

`Reservation` es la tabla que **relaciona** a `Attendee` con `Event` (igual que `Enrollment`
relaciona `Student` con `Subject` en el proyecto de referencia). `status` solo puede ser
`ACTIVE` o `CANCELLED`.

### 1.2 Reglas de negocio (aquí está el razonamiento que se evalúa)

Estas validaciones viven en la **capa de services** y son las que debes cubrir al 100% con tests.

**Al crear un evento — `createEvent`:**
1. `name` o `venue` en blanco → `BlankFieldException` (HTTP `400`).
2. `totalTickets < 1` → `InvalidCapacityException` (HTTP `400`).
3. Si todo es válido: `availableTickets` inicia **igual a** `totalTickets`.

**Al crear un asistente — `createAttendee`:**
1. `name` o `email` en blanco → `BlankFieldException` (HTTP `400`).

**Al reservar una entrada — `createReservation(attendeeId, eventId)`:** *(la regla más importante)*
1. El asistente debe existir → si no, `AttendeeNotFoundException` (HTTP `404`).
2. El evento debe existir → si no, `EventNotFoundException` (HTTP `404`).
3. El evento debe tener `availableTickets > 0` → si no, `SoldOutException` (HTTP `409`).
4. El asistente no puede tener **más de 4 reservas `ACTIVE`** → si no, `ReservationLimitExceededException` (HTTP `409`).
5. Si todo es válido:
   - `status = ACTIVE`, `createdAt = ahora`,
   - se **decrementa** `availableTickets` del evento en 1,
   - se guarda la reserva.

**Al cancelar una reserva — `cancelReservation(reservationId)`:**
1. La reserva debe existir → si no, `ReservationNotFoundException` (HTTP `404`).
2. La reserva debe estar `ACTIVE` → si ya está `CANCELLED`, `ReservationAlreadyCancelledException` (HTTP `409`).
3. Si todo es válido: `status = CANCELLED` y se **incrementa** `availableTickets` del evento en 1.

> 💡 Cada `if`, `else`, `orElseThrow { }` y `when` de estos métodos es una **rama** que tu
> test debe ejercitar. Cuenta las ramas: ahí está tu objetivo de cobertura.

### 1.3 Endpoints

Debes exponer, **como mínimo**, un endpoint **público** y uno **privado**:

| Método | Ruta                            | Acceso        | Descripción                          |
|--------|---------------------------------|---------------|--------------------------------------|
| `GET`  | `/api/events`                   | 🔓 **Público** | Lista la cartelera de eventos        |
| `GET`  | `/api/events/{id}`              | 🔓 **Público** | Detalle de un evento                 |
| `POST` | `/api/events`                   | 🔒 Privado     | Crea un evento                       |
| `POST` | `/api/attendees`                | 🔒 Privado     | Crea un asistente                    |
| `POST` | `/api/reservations`             | 🔒 **Privado** | Reserva una entrada                  |
| `PUT`  | `/api/reservations/{id}/cancel` | 🔒 Privado     | Cancela una reserva                  |
| `GET`  | `/api/reservations`             | 🔒 Privado     | Lista las reservas                   |

**Regla de seguridad:** `GET /api/events/**` es público; **todo lo demás requiere token**.

---

## 2. Arquitectura obligatoria (idéntica a `students`)

El proyecto debe respetar **exactamente** la misma separación por capas del proyecto de
referencia. Estructura esperada:

```
src/main/kotlin/com/pucetec/events/
├── EventsApplication.kt
├── config/
│   └── SecurityConfig.kt              # seguridad (Cognito) — NUEVO respecto a students
├── controllers/
│   ├── EventController.kt
│   ├── AttendeeController.kt
│   └── ReservationController.kt
├── dto/
│   ├── EventDto.kt                    # EventRequest / EventResponse (data class)
│   ├── AttendeeDto.kt
│   └── ReservationDto.kt              # ReservationRequest / ReservationResponse
├── entities/
│   ├── Event.kt                       # @Entity @Table(name = "events")
│   ├── Attendee.kt
│   └── Reservation.kt                 # @ManyToOne a Event y Attendee
├── exceptions/
│   ├── BlankFieldException.kt
│   ├── InvalidCapacityException.kt
│   ├── AttendeeNotFoundException.kt
│   ├── EventNotFoundException.kt
│   ├── ReservationNotFoundException.kt
│   ├── SoldOutException.kt
│   ├── ReservationLimitExceededException.kt
│   ├── ReservationAlreadyCancelledException.kt
│   └── GlobalExceptionHandler.kt      # @RestControllerAdvice + ExceptionResponse
├── mappers/
│   ├── EventMapper.kt                 # toEntity() / toResponse() (extension functions)
│   ├── AttendeeMapper.kt
│   └── ReservationMapper.kt
├── repositories/
│   ├── EventRepository.kt             # : JpaRepository<Event, Long>
│   ├── AttendeeRepository.kt
│   └── ReservationRepository.kt
└── services/
    ├── EventService.kt                # lógica + validaciones
    ├── AttendeeService.kt
    └── ReservationService.kt
```

**Reglas de las capas (no romperlas):**
- Los **controllers** no tienen lógica: reciben el request, llaman al service y devuelven el response.
- Los **services** tienen la lógica de negocio y lanzan las excepciones de dominio.
- Los **mappers** son *extension functions* (`fun Event.toResponse() = ...`), nada de lógica.
- Las **entities** son JPA (`@Entity`, `@Id @GeneratedValue`, `@ManyToOne`). No se exponen directamente: siempre se devuelve un `...Response`.
- Las **excepciones** se traducen a HTTP en `GlobalExceptionHandler` con `ExceptionResponse(message, source)`.

---

## 3. Seguridad con Cognito (lo nuevo respecto a `students`)

Reutiliza el patrón del proyecto **`demo_auth`** (revisa su `README.md`). El servicio actúa
como **OAuth2 Resource Server**: valida el JWT de Cognito contra su JWKS usando `issuer-uri`.

> 🔑 **No crees tu propio User Pool.** En este deber te conectas al **User Pool de prueba ya
> configurado** (el mismo del `README.md` de `demo_auth`, sección 🔑). Todos los datos —issuer,
> dominio, client id/secret, endpoints— ya están dados; tú solo te **autorregistras** en su
> Hosted UI para obtener un usuario y un token. Datos del pool de prueba:
>
> | Dato | Valor |
> |------|-------|
> | Región | `us-east-1` |
> | User Pool ID | `us-east-1_yzwNALI2A` |
> | **`issuer-uri`** | `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_yzwNALI2A` |
> | Dominio Hosted UI | `https://us-east-1yzwnali2a.auth.us-east-1.amazoncognito.com` |
> | App client ID | `3gv2oqe4niko3s47srn1kitsk6` |
> | App client secret | `14qdd388f1j6fge52el3l5r2ouvcg5sperlno3701t2jj1chgeiu` |
> | Scopes | `email openid phone` |
> | Redirect URI registrado | `https://d84l1y8p4kdic.cloudfront.net` |

### 3.1 Dependencias (Spring Initializr)

Al proyecto de arquitectura por capas (Web + JPA + H2) **agrega**:

- **OAuth2 Resource Server** (`spring-boot-starter-oauth2-resource-server`) → valida el JWT de Cognito. (Trae Spring Security transitivamente.)

### 3.2 `SecurityConfig.kt`

```kotlin
package com.pucetec.events.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public: read-only access to the events catalog.
                    .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
                    // Everything else (creating events, attendees, reservations...) requires a token.
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 -> oauth2.jwt { } }
        return http.build()
    }
}
```

### 3.3 `application.yml`

Apunta el `issuer-uri` al **User Pool de prueba** (no a uno tuyo):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_yzwNALI2A
```

### 3.4 Obtener el token (flujo manual con el pool de prueba)

Como el `redirect_uri` registrado en el pool de prueba es la *managed login* de Cognito
(`https://d84l1y8p4kdic.cloudfront.net`) y **no** el callback de Postman, obtén el token con el
**flujo manual** descrito en el `README.md` de `demo_auth` (sección 🔑):

1. Abre la **Hosted UI** en el navegador y **autorregístrate** (*Sign up*):
   ```
   https://us-east-1yzwnali2a.auth.us-east-1.amazoncognito.com/login?client_id=3gv2oqe4niko3s47srn1kitsk6&response_type=code&scope=email+openid+phone&redirect_uri=https%3A%2F%2Fd84l1y8p4kdic.cloudfront.net
   ```
2. Tras el login, copia el `code` de la URL de redirección (`...cloudfront.net/?code=XXXX`). Es de un solo uso y dura pocos minutos.
3. Canjéalo por los tokens en el endpoint `/oauth2/token`:
   ```bash
   curl --location 'https://us-east-1yzwnali2a.auth.us-east-1.amazoncognito.com/oauth2/token' \
     --header 'Content-Type: application/x-www-form-urlencoded' \
     --data-urlencode 'grant_type=authorization_code' \
     --data-urlencode 'client_id=3gv2oqe4niko3s47srn1kitsk6' \
     --data-urlencode 'client_secret=14qdd388f1j6fge52el3l5r2ouvcg5sperlno3701t2jj1chgeiu' \
     --data-urlencode 'code=<PEGA_TU_CODE_AQUI>' \
     --data-urlencode 'redirect_uri=https://d84l1y8p4kdic.cloudfront.net'
   ```
4. Usa el `access_token` de la respuesta como `Authorization: Bearer <token>` en los endpoints
   privados (puedes pegarlo en Postman en *Authorization → Bearer Token*).

---

## 4. Tests — 100% de cobertura en `services`

Igual que en el deber anterior de cobertura, la **capa `services` debe quedar en 100% de
líneas y 100% de ramas** (demostrado con **Run with Coverage** de IntelliJ).

- Un archivo de test por service: `EventServiceTest.kt`, `AttendeeServiceTest.kt`,
  `ReservationServiceTest.kt`, dentro de `src/test/kotlin/com/pucetec/events/services/`.
- Usa Mockito: `@ExtendWith(MockitoExtension::class)`, `@Mock` para los repositorios,
  `@InjectMocks` para el service. Mismo patrón **Arrange / Act / Assert** del proyecto de referencia.
- Por **cada** excepción de la sección 1.2 debe existir un test con `assertThrows<...> { ... }`,
  y por cada camino feliz un test con `assertEquals`.

Cuenta mínima de tests del `ReservationService` (a modo de guía): camino feliz de
`createReservation`, asistente inexistente, evento inexistente, evento agotado (sold out),
límite de reservas superado, camino feliz de `cancelReservation`, reserva inexistente y reserva
ya cancelada. **Cada rama, un test.**

---

## 5. Entregables

Se entrega el **enlace al repositorio de GitHub** llamado
`ae_puce_2026_[nrc]_[nombre]_[apellido]`, que debe contener:

1. **El código completo** del microservicio `events` (todas las capas) compilando.
2. **Los tests** con cobertura **100%** en `services`.
3. **La colección de Postman** (`*.postman_collection.json`) con los requests al servicio:
   los públicos sin token y los privados con el header `Authorization: Bearer <access_token>`.
4. Una carpeta **`evidencias/`** con **capturas de pantalla** que demuestren:

   | # | Captura requerida |
   |---|-------------------|
   | 1 | `GET /api/events` (público) respondiendo **200 sin token**. |
   | 2 | `POST /api/reservations` (privado) **sin token** respondiendo **401 Unauthorized**. |
   | 3 | `POST /api/reservations` (privado) **con token** válido respondiendo **200/201** (reserva creada). |
   | 4 | La obtención del **token desde el pool de prueba**: el login/registro en la Hosted UI y/o la respuesta del `/oauth2/token` con el `access_token`. |
   | 5 | El **reporte de cobertura** de IntelliJ mostrando `services` en **Line 100% / Branch 100%**. |

> Sin el **link del repositorio** la tarea no se evalúa. Sin las **capturas** de los tres
> escenarios de autenticación (público OK, privado sin token = 401, privado con token = OK)
> no se evalúa la parte de seguridad.

---

## 6. Rúbrica (100 puntos)

| Criterio | Puntos |
|---|---|
| Estructura por capas correcta (entities, dto, mappers, repositories, services, controllers, exceptions) | 15 |
| Las 3 tablas con sus relaciones (`Reservation` → `Attendee` y `Event`) modeladas con JPA | 10 |
| Validaciones de negocio completas (todas las excepciones de la sección 1.2) | 15 |
| `GlobalExceptionHandler` traduce cada excepción a su HTTP correcto | 10 |
| Endpoint **público** funcionando sin token | 5 |
| Endpoint **privado** protegido: 401 sin token, 200 con token (Cognito) | 15 |
| **100% de cobertura** (líneas y ramas) en `services` | 15 |
| Colección de Postman incluida y funcional | 5 |
| Carpeta `evidencias/` con las 5 capturas | 5 |
| Repo y paquete con el nombre correcto (`ae_puce_2026_...` / `com.pucetec.events`) | 5 |

---

## 7. Lista de verificación antes de entregar

- [ ] El repo se llama `ae_puce_2026_[nrc]_[nombre]_[apellido]`.
- [ ] El paquete base es `com.pucetec.events`.
- [ ] Existen las 3 entities (`Attendee`, `Event`, `Reservation`) con sus relaciones.
- [ ] Cada capa está separada (no hay lógica en controllers ni en mappers).
- [ ] Todas las excepciones de la sección 1.2 existen y están registradas en `GlobalExceptionHandler`.
- [ ] `GET /api/events` responde **sin** token.
- [ ] `POST /api/reservations` responde **401 sin** token y **200/201 con** token.
- [ ] `application.yml` apunta al `issuer-uri` del **User Pool de prueba** (`us-east-1_yzwNALI2A`), no a uno propio.
- [ ] Me **autorregistré** en la Hosted UI del pool de prueba y obtuve un `access_token`.
- [ ] Cobertura de `services` = **100% líneas y 100% ramas** (con captura).
- [ ] Subí la **colección de Postman** y la carpeta **`evidencias/`** con las 5 capturas.
- [ ] Pegué el **link del repositorio** en la entrega.
```
