# Products API — Resumen del aplicativo (reactivo)

Este repositorio contiene una **API HTTP reactiva** para registrar y consultar productos.

## Stack y estilo

- **Java 21**
- **Spring Boot 4.x**
- **Spring WebFlux + Reactor** (programación no bloqueante con `Mono`/`Flux`)
- **Functional Endpoints**: routers (`RouterFunction`) y handlers (`Handler`), sin controladores anotados.
- **SpringDoc OpenAPI** para documentar endpoints (Swagger UI).
- **Arquitectura:** se organiza por módulos/capas (modelo de dominio + casos de uso + infraestructura + aplicación), manteniendo dependencias en dirección al dominio.

## Contrato transversal (headers y errores)

### Headers requeridos
En todos los endpoints se manejan estos headers (UUID):

- `message-id`: correlación lógica de la transacción.
- `x-request-id`: correlación técnica del request (se propaga de vuelta en la respuesta).

### Respuesta de error unificada
Los errores se responden en JSON con una estructura consistente (ejemplo conceptual):

- `meta.executionDate`: fecha/hora formateada del servidor.
- `meta.messageId`: el `message-id` (o uno generado si falta/está inválido).
- `error.code`: código estándar (p. ej. `ER400`, `ER409`, `ER500`).
- `error.message`: mensaje funcional.
- `error.details`: lista (opcional, según configuración).

El mapeo de excepciones se concentra en el **`GlobalExceptionHandler`**.

## Endpoints

### 1) POST `/addProducts`
Registra una lista de productos.

**Request (JSON)**
```json
{
  "data": {
    "products": [
      { "name": "Laptop", "type": "Tecnología", "quantity": 1, "price": 100, "currency": "COP" }
    ]
  }
}
```

**Respuestas esperadas**
- **201 Created**: la petición se guardó correctamente.
- **400 Bad Request**: faltan parámetros, valores inválidos, o hay duplicados dentro de la misma lista.
- **409 Conflict**: el producto ya existe (según la llave de negocio).
- **500 Internal Server Error**: error desconocido.

### 2) GET `/getProducts`
Consulta productos, con filtro opcional.

**Query params (opcionales)**
- `any_filter`: nombre del campo a filtrar.
- `any_value`: valor del filtro.

**Ejemplos**
- Sin filtro: `GET /getProducts`
- Con filtro: `GET /getProducts?any_filter=tipo&any_value=tecnologia`

**Respuestas esperadas**
- **200 OK**: retorna lista (puede venir vacía según la regla del caso de uso).
- **400 Bad Request**: filtro/valor inválido o incompleto.
- **409 Conflict**: regla de negocio “no encontrado” (si el caso de uso decide tratar “vacío” como conflicto).
- **500 Internal Server Error**: error desconocido.

## Persistencia actual

Para facilitar pruebas y ejecución local, se utiliza una implementación **in-memory** (store concurrente) a través de un adapter en infraestructura. Esto permite probar el flujo end-to-end sin base de datos.
