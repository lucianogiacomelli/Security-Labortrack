# LaborTrack - Security & Authentication Service 🔐

Servicio centralizado de autenticación, autorización y gestión de usuarios para la plataforma **LaborTrack**, desarrollado con **Java 21** y **Spring Boot**. Incorpora seguridad stateless mediante **JWT** (JSON Web Tokens) e integración nativa con **Google OAuth2** para inicio de sesión único (SSO).

---

## 🚀 Tecnologías Utilizadas

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.x
* **Seguridad:** Spring Security (Stateless, Method Security)
* **Autenticación Externa:** Google OAuth2 API (Google Identity Services)
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL
* **Token Standard:** JWT (JJWT)
* **Build Tool:** Gradle

---

## ✨ Características Principales

* **Autenticación Dual:** Soporte para inicio de sesión tradicional (Email/Password con contraseñas encriptadas mediante `BCrypt`) y login social con **Google OAuth2** (`idToken`).
* **Control de Acceso Basado en Roles (RBAC):** Gestión de permisos jerárquicos a nivel de endpoint mediante anotaciones `@PreAuthorize`.
  * `ROLE_ADMIN`
  * `ROLE_RRHH`
  * `ROLE_OPERARIO`
* **Arquitectura Unificada de Usuarios:** Vinculación automática entre cuentas de Google (`google_id`) y registros locales de la base de datos PostgreSQL.
* **Seguridad por Variables de Entorno:** Aislamiento total de credenciales y secretos mediante *fallbacks* para desarrollo local.

---

## 🛠️ Configuración del Entorno

El proyecto lee las configuraciones desde variables de entorno. Puedes definirlas en tu sistema operativo, IDE o en un archivo `.env`:

| Variable | Descripción | Valor por defecto (Local) |
| :--- | :--- | :--- |
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/nombre` |
| `DB_USERNAME` | Usuario de PostgreSQL | `usuario` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `contraseña` |
| `PRIVATE_KEY` | Clave secreta para firma de JWT | *Clave de desarrollo* |
| `EXPIRATION_TIME` | Tiempo de expiración del JWT (ms) | `1800000` (30 mins) |
| `GOOGLE_ID` | Client ID de Google OAuth 2.0 | `tu_client_id.apps.googleusercontent.com` |

---

## 📌 Principales Endpoints de la API

### Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Acceso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Login tradicional con Email y Password. | `Público` |
| `POST` | `/api/auth/google` | Login mediante Google ID Token (`JWT`). | `Público` |

#### Ejemplo de Payload: `POST /api/auth/google`

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs..."
}
