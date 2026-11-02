# Eureka Server de LogiTrack

Servicio de registro y descubrimiento del Sprint 3. Su implementacion corresponde a [shipment-service #77](https://github.com/Logitrack-Sa/shipment-service/issues/77).

## Arranque

Java 25, Spring Boot 3.5.16 y Spring Cloud 2025.0.3, alineados con LogiTrack.
Config Server debe estar disponible primero. Los perfiles `dev` y `prod` importan
configuracion obligatoria; no se arranca silenciosamente con otro catalogo.

```powershell
$env:CONFIG_SERVER_URL = 'http://localhost:8888'
./mvnw.cmd spring-boot:run
```

Las propiedades de puerto 8065 y modo standalone viven en
`config-repo/eureka-server*.properties` (#79). El servidor no se registra ni
descarga su propio catalogo. En Compose se usa `http://eureka-server:8065/eureka/`.
Eureka no necesita base de datos: los clientes renuevan sus registros.

Dashboard local: `http://localhost:8065/`. Salud: `/actuator/health`.
El dashboard y la API de registro pertenecen a la red de infraestructura;
el acceso externo al negocio se realiza por el Gateway, no por Eureka.

## Verificacion

`./mvnw.cmd verify` prueba dashboard/salud, registro de una instancia por HTTP,
consulta, renovacion y baja contra el servidor real en un puerto aleatorio.
El perfil test deshabilita Config Client; la integracion con Config Server y
los clientes reales se comprueba desde Compose.

`docker build -t logitrack-eureka-server:local .` construye una imagen multietapa,
con usuario no root. `CONFIG_SERVER_URL` se proporciona en ejecucion.
