# TelcoNovaP7-Backend

Backend para el proyecto TelcoNovaP7, desarrollado en **Java 21** con **Spring Boot**.

## Tabla de Contenido

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Configuración y Despliegue](#configuración-y-despliegue)
- [Perfiles de Spring](#perfiles-de-spring)
- [Usuarios Iniciales (modo Dev)](#usuarios-iniciales-modo-dev)
- [Pruebas y Documentación Swagger](#pruebas-y-documentación-swagger)
- [Uso](#uso)
- [Autores](#autores)
- [Notas adicionales](#notas-adicionales)

---

## Descripción

Este proyecto provee el backend para la gestión de una empresa de telecomunicaciones ficticia. Permite administrar operaciones comunes como gestión de usuarios, técnicos y operarios, y soporta diferentes perfiles de acceso.

## Tecnologías

- **Java 21**
- **Spring Boot** (versión recomendada: 3.x)
- **Maven**
- **H2 Database** (modo desarrollo)
- **PostgreSQL** (modo producción)
- **pgAdmin** (opcional para administración de BD)
- **Spring Security** (gestión de autenticación y roles)
- **Swagger** (documentación y pruebas de la API)

## Requisitos

- Java JDK 21 o superior
- Maven 3.8+
- Git

## Configuración y Despliegue

### 1. Clonar el repositorio

```sh
git clone https://github.com/EV09-UDEA-20252/TelcoNovaP7-Backend.git
cd TelcoNovaP7-Backend
```

### 2. Compilar el proyecto

```sh
mvn clean package
```

### 3. Ejecutar en modo desarrollo (H2)

Por defecto el proyecto se ejecuta en modo **dev** usando una base de datos H2 en memoria y usuarios iniciales.

```sh
mvn spring-boot:run
```

O puedes ejecutar el JAR generado:

```sh
java -jar target/TelcoNovaP7-Backend-*.jar
```

### 4. Cambiar a modo producción (PostgreSQL)

Edita el archivo `src/main/resources/application.properties` y comenta/descomenta la línea correspondiente al perfil activo:

```properties
# Para modo dev (H2)
spring.profiles.active=dev

# Para modo producción (PostgreSQL), comenta la línea anterior y descomenta la siguiente:
# spring.profiles.active=prod
```

Configura las credenciales de tu base de datos PostgreSQL en los archivos de propiedades correspondientes.

### 5. Acceso a pgAdmin (opcional)

Puedes usar [pgAdmin](https://www.pgadmin.org/) para administrar tu base de datos PostgreSQL si trabajas en modo producción.

---

## Perfiles de Spring

- **dev:** Utiliza H2 en memoria, ideal para desarrollo y pruebas rápidas.
- **prod:** Utiliza PostgreSQL, recomendado para despliegue en servidores.

---

## Usuarios Iniciales (modo Dev)

Al iniciar el proyecto en **modo dev**, se crean automáticamente tres usuarios gracias a `DevSeedConfig.java`:

- **Admin**
  - Correo: `admin@acme.com`
  - Contraseña: `admin123`
- **Operario**
  - Correo: `operario@acme.com`
  - Contraseña: `operario123`
- **Técnico**
  - Correo: `tecnico@acme.com`
  - Contraseña: `tecnico123`

---

## Pruebas y Documentación Swagger

La principal herramienta utilizada para pruebas ha sido **Swagger**, la cual permite explorar, probar y validar todos los endpoints de la API de manera sencilla e interactiva.

- Al ejecutar el proyecto, accede a la interfaz Swagger en:  
  ```
  http://localhost:8080/swagger-ui/index.html
  ```
- Desde Swagger puedes probar todas las rutas, enviar peticiones y verificar las respuestas.
- Swagger también facilita la comprensión y documentación de la API para otros desarrolladores.

---

## Uso

La API expone endpoints REST para gestión de usuarios y operaciones del sistema. Puedes probarlos mediante herramientas como **Swagger**, **Postman** o **curl**.

- Revisa la carpeta `/src/main/java` para conocer los controladores y servicios disponibles.

---

## Autores

- Equipo EV09-UDEA-20252
- Mateo Vásquez García
- Juan David García García

---

## Notas adicionales

- Recuerda actualizar tus credenciales y variables de entorno antes de desplegar en producción.
- Para dudas o soporte, abre un issue en el repositorio.
