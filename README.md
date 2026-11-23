# RecetApp

Aplicación web de recetas hecha con **Spring Boot** y **Thymeleaf** como proyecto académico del ciclo de Desarrollo de Aplicaciones Web (DAW).

> Proyecto de aprendizaje: los datos se guardan en ficheros de texto y las contraseñas no están cifradas. No está pensado para producción.

## Qué permite hacer

- Registro e inicio de sesión de usuarios.
- Explorar recetas (desayunos, platos principales y postres) y ver el detalle de cada una.
- Crear recetas propias con ingredientes, pasos y foto, y borrarlas.
- Recetas del mundo por país y botón "Sorpréndeme".
- Planificador semanal de comidas (colocar, rellenar al azar, vaciar y guardar).
- Lista de la compra, que se puede generar a partir del plan.

## Tecnologías

Java 17 · Spring Boot · Thymeleaf · HTML5 · CSS3 · Maven · Lombok · almacenamiento en ficheros de texto

## Cómo ejecutarlo

Necesitas Java 17 o superior.

```bash
./mvnw spring-boot:run
```

Después abre http://localhost:8080

## Estructura

- `src/main/java/clases`: modelo (Receta, Desayuno, PlatoPrincipal, Postre, Ingrediente, Usuario).
- `src/main/java/com/example/demo`: controladores, gestor de ficheros y excepción propia.
- `src/main/resources/templates`: vistas Thymeleaf.
- `src/main/resources/static`: CSS e imágenes.
- `datos`: datos de ejemplo en texto plano.

## Qué mejoraría

- Guardar los datos en una base de datos (MySQL) con Spring Data JPA.
- Cifrar contraseñas y añadir Spring Security.
- Validar los formularios en servidor.
- Añadir tests.

## Autor

Daniel Benítez Araújo
