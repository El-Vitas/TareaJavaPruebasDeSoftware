**Proyecto**: Calculadora de Tarifas de Estacionamiento

**Descripción**
Calculadora de Tarifas de Estacionamiento: aplicación de línea de comandos que registra entradas y salidas y calcula cobros según reglas de negocio.

**Cómo compilar**

Requiere: Java 21 (u OpenJDK 21) y Maven.

Desde la raíz del proyecto:

```
mvn clean package
```

Esto compila el proyecto y genera `target/calculadora-parking.jar`.

**Cómo ejecutar (CLI)**

Después de compilar:

```
java -jar target/calculadora-parking.jar
```

**Cómo ejecutar tests**

Los tests unitarios usan JUnit 5. Ejecuta:

```
mvn test
```

Ejemplo de salida de tests (console):

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.estacionamiento.ParkingServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.109 s - in com.estacionamiento.ParkingServiceTest
[INFO]
[INFO] Results:
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
```

**Licencia**
- Este proyecto se publica bajo la licencia MIT (ver `LICENSE`).

**Qué se midió**
- Se midió principalmente el cumplimiento de los requisitos principales: registro de entrada/salida, cálculo por bloques de 30 minutos, tarifas por tipo de vehículo, tope diario, descuento de fin de semana y consultas (listar tickets y total recaudado).

**Por qué se midió**
- Para asegurar que las funcionalidades principales del sistema funcionen según lo esperado y que las lógica del negocio estén cubiertas por los tests.
