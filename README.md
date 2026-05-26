# MiPlata — Cajero Bancario Digital

## Tabla de Contenidos

1. [Información General](#1-información-general)
2. [Estructura del Proyecto](#2-estructura-del-proyecto)
3. [Explicación del Código](#3-explicación-del-código)
4. [Base de Datos](#4-base-de-datos)
5. [Flujo del Sistema](#5-flujo-del-sistema)
6. [Configuración e Instalación](#6-configuración-e-instalación)
7. [Casos de Uso](#7-casos-de-uso)
8. [Decisiones Técnicas](#8-decisiones-técnicas)
9. [Posibles Mejoras](#9-posibles-mejoras)
10. [Conclusiones](#10-conclusiones)

---

## 1. Información General

### Nombre del Proyecto
**MiPlata** — Cajero Bancario Digital por Consola

### Objetivo Principal
Simular un sistema bancario completo operado desde la consola, donde los usuarios pueden registrarse, gestionar cuentas bancarias (ahorros y corriente), operar una tarjeta de crédito con cuotas, realizar transferencias entre usuarios y consultar su historial de movimientos, con persistencia real en una base de datos MySQL.

### Problema que Resuelve
La mayoría de ejercicios académicos de programación orientada a objetos trabajan con datos en memoria que desaparecen al cerrar el programa. MiPlata resuelve esto integrando persistencia real en MySQL, aplicando una arquitectura limpia (Ports & Adapters) que desacopla la lógica de negocio de la tecnología de almacenamiento.

### Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17+ | Lenguaje principal |
| MySQL | 8.x | Base de datos relacional |
| JDBC | Nativo | Conexión Java → MySQL |
| Maven | 3.x | Gestión de dependencias y build |
| MySQL Connector/J | 8.x | Driver JDBC para MySQL |

### Arquitectura Implementada
**Arquitectura Hexagonal (Ports & Adapters)**

El proyecto separa estrictamente tres capas:
- **Dominio** — reglas de negocio puras, sin dependencias externas.
- **Servicios** — orquestación de la lógica, depende solo de interfaces (puertos).
- **Infraestructura** — implementaciones concretas (MySQL, consola), conectadas a través de los puertos.

---

## 2. Estructura del Proyecto

```
miplata_java/
└── src/
    └── main/
        └── java/
            └── miplata/
                ├── Main.java
                ├── config/
                │   ├── Config.java
                │   └── DataBaseConnectionMySql.java
                ├── domain/
                │   ├── Cliente.java
                │   ├── Cuenta.java
                │   ├── CuentaAhorros.java
                │   ├── CuentaCorriente.java
                │   ├── TarjetaCredito.java
                │   ├── DeudaTC.java
                │   ├── Movimiento.java
                │   ├── OperacionResultado.java
                │   └── enums/
                │       ├── AccountState.java
                │       ├── AccountType.java
                │       └── CreditCardTier.java
                ├── persistence/
                │   ├── mapper/
                │   │   ├── RowMapper.java
                │   │   ├── ClienteRowMapper.java
                │   │   ├── CuentaAhorrosRowMapper.java
                │   │   ├── CuentaCorrienteRowMapper.java
                │   │   ├── TarjetaCreditoRowMapper.java
                │   │   ├── MovimientoRowMapper.java
                │   │   └── DeudaTCRowMapper.java
                │   └── repository/
                │       ├── ClienteRepositoryMySql.java
                │       ├── CuentaAhorrosRepositoryMySql.java
                │       ├── CuentaCorrienteRepositoryMySql.java
                │       ├── TarjetaCreditoRepositoryMySql.java
                │       ├── MovimientoRepositoryMySql.java
                │       └── DeudaTCRepositoryMySql.java
                ├── repository/
                │   └── ClienteRepository.java
                ├── services/
                │   ├── ClienteService.java
                │   ├── ClienteServiceImpl.java
                │   ├── CuentaService.java
                │   ├── CuentaServiceImpl.java
                │   ├── CreditCardTierSelector.java
                │   └── outputport/
                │       ├── ClientePersistencePort.java
                │       ├── CuentaAhorrosPersistencePort.java
                │       ├── CuentaCorrientePersistencePort.java
                │       ├── TarjetaCreditoPersistencePort.java
                │       ├── MovimientoPersistencePort.java
                │       └── DeudaTCPersistencePort.java
                ├── userinterface/
                │   └── MenuApp.java
                ├── utils/
                │   └── FormValidation.java
                └── view/
                    ├── ClienteView.java
                    └── CuentaView.java
```

### Descripción de Carpetas

| Carpeta | Responsabilidad | Interactúa con |
|---|---|---|
| `domain/` | Entidades del negocio con sus reglas y comportamientos. No depende de ninguna otra capa. | Nadie la llama a ella directamente excepto los servicios |
| `services/` | Orquesta los casos de uso. Depende solo de interfaces (puertos), nunca de clases concretas. | `domain/`, `outputport/` |
| `services/outputport/` | Interfaces que definen qué necesita el servicio de la capa de persistencia. | `services/`, `persistence/repository/` |
| `persistence/mapper/` | Convierte filas de `ResultSet` en objetos del dominio. | `persistence/repository/` |
| `persistence/repository/` | Implementaciones MySQL de los puertos de salida. Ejecutan SQL directo. | `services/outputport/`, `persistence/mapper/` |
| `repository/` | Repositorio en memoria para las cuentas activas durante la sesión. | `services/` |
| `config/` | Ensambla toda la aplicación mediante inyección manual de dependencias. | Todas las capas |
| `view/` | Captura la entrada del usuario y presenta resultados en consola. | `services/` |
| `userinterface/` | Controla los menús y el flujo de navegación de la aplicación. | `view/` |
| `utils/` | Herramientas de validación de entrada por consola. | `view/`, `services/` |

---

## 3. Explicación del Código

### 3.1 Punto de Entrada

#### `Main.java`
- **Responsabilidad:** Inicia la aplicación. Llama a `Config.createMenuApp()` y ejecuta el menú principal.
- **Flujo:** `main()` → `Config.createMenuApp()` → `menuApp.showMainMenu()`

---

### 3.2 Configuración

#### `Config.java`
- **Responsabilidad:** Fábrica de la aplicación. Ensambla todas las dependencias manualmente (inyección de dependencias sin framework).
- **Flujo de construcción:**
  1. Obtiene la conexión MySQL (Singleton).
  2. Crea los mappers.
  3. Crea los repositorios MySQL inyectando conexión y mapper.
  4. Crea el repositorio en memoria.
  5. Crea los servicios inyectando los puertos (interfaces).
  6. Crea las vistas inyectando los servicios.
  7. Crea y retorna el `MenuApp`.

#### `DataBaseConnectionMySql.java`
- **Responsabilidad:** Maneja la conexión a MySQL usando el patrón Singleton.
- **Métodos principales:**

| Método | Descripción | Retorna |
|---|---|---|
| `getInstance()` | Retorna la única instancia de la conexión (sincronizado). | `DataBaseConnectionMySql` |
| `getConnection()` | Retorna el objeto `Connection` JDBC activo. | `Connection` |

- **Parámetros de conexión:**
```
URL:      jdbc:mysql://localhost:3306/miplata
Usuario:  root
Password: (vacío por defecto)
```

---

### 3.3 Dominio

#### `Cliente.java`
- **Responsabilidad:** Representa al usuario del sistema con sus datos personales y credenciales.
- **Campos:** `usuario`, `password`, `nombre`, `identificacion`, `celular`
- **Métodos principales:**

| Método | Descripción | Retorna |
|---|---|---|
| `verificarPassword(String)` | Compara la contraseña ingresada con la almacenada. | `boolean` |

---

#### `Cuenta.java` (abstracta)
- **Responsabilidad:** Clase base para todas las cuentas. Define comportamiento común y métodos abstractos que cada tipo de cuenta implementa diferente (polimorfismo).
- **Campos:** `usuario`, `saldo`, `estado` (`AccountState`), `tipo` (`AccountType`), `movimientos`
- **Métodos principales:**

| Método | Descripción | Retorna |
|---|---|---|
| `depositar(double)` | Abstracto. Cada cuenta lo implementa. | `OperacionResultado` |
| `calcularRetiro(double)` | Abstracto. Cada cuenta define sus reglas de retiro. | `OperacionResultado` |
| `guardarMovimiento(String)` | Registra un evento con timestamp en la lista de movimientos en memoria. | `void` |
| `formatPesos(double)` | Formatea un número como pesos colombianos. | `String` |
| `isActiva()` | Verifica si la cuenta está activa. | `boolean` |

---

#### `CuentaAhorros.java`
- **Hereda de:** `Cuenta`
- **Reglas de negocio:**
  - Siempre se crea activa al registrar un cliente.
  - Tasa de rendimiento mensual: **1.5%**
  - Retiro máximo: el saldo disponible (sin sobregiro).
- **Métodos adicionales:**

| Método | Descripción | Retorna |
|---|---|---|
| `aplicarRendimiento()` | Calcula y aplica el 1.5% mensual sobre el saldo. | `OperacionResultado` |

---

#### `CuentaCorriente.java`
- **Hereda de:** `Cuenta`
- **Reglas de negocio:**
  - Se crea inactiva. Se activa trasladando saldo desde ahorros.
  - Permite **sobregiro del 20%** sobre el saldo disponible.
- **Métodos adicionales:**

| Método | Descripción | Retorna |
|---|---|---|
| `activar(double)` | Activa la cuenta con un saldo inicial. | `void` |

---

#### `TarjetaCredito.java`
- **Hereda de:** `Cuenta`
- **Reglas de negocio:**
  - Se crea inactiva. Se activa eligiendo un tier (`BASICO`, `INTERMEDIO`, `PREMIUM`).
  - Tasas de interés mensual:
    - 1-2 cuotas: **0%**
    - 3-6 cuotas: **1.9%**
    - 7+ cuotas: **2.3%**
  - Fórmula de cuota: amortización francesa.
- **Métodos principales:**

| Método | Parámetros | Descripción | Retorna |
|---|---|---|---|
| `activar(CreditCardTier)` | tier | Activa la tarjeta con el cupo del tier seleccionado. | `void` |
| `calcularCuota(double, int)` | capital, cuotas | Calcula la cuota mensual con la tasa correspondiente. | `double` |
| `comprar(double, int)` | monto, cuotas | Registra una compra y crea una `DeudaTC`. | `OperacionResultado` |
| `pagarCuota(int, Cuenta)` | index, cuentaOrigen | Paga una cuota de la deuda indicada. | `OperacionResultado` |
| `pagarTotal(int, Cuenta)` | index, cuentaOrigen | Paga el saldo pendiente completo de la deuda. | `OperacionResultado` |
| `getDeudasActivas()` | — | Filtra las deudas que aún no están completamente pagadas. | `List<DeudaTC>` |

---

#### `DeudaTC.java`
- **Responsabilidad:** Representa una compra realizada con la tarjeta de crédito a cuotas.
- **Campos:** `fecha`, `capital`, `cuotas`, `cuotasPagadas`, `cuotaMensual`, `totalAPagar`, `pagado`, `tasa`, `saldoPendiente`
- **Métodos clave:**

| Método | Descripción | Retorna |
|---|---|---|
| `isPagada()` | Retorna true si `cuotasPagadas >= cuotas`. | `boolean` |
| `getCuotasRestantes()` | Calcula las cuotas que faltan. | `int` |

---

#### `Movimiento.java`
- **Responsabilidad:** Registra cada operación bancaria con usuario, tipo de cuenta, tipo de operación, monto y fecha.
- **Tipos de operación registrados:** `DEPOSITO`, `RETIRO`, `TRANSFERENCIA`, `TRANSFERENCIA_RECIBIDA`, `TRASLADO`, `COMPRA`, `PAGO_CUOTA`, `PAGO_TOTAL`

---

#### `OperacionResultado.java`
- **Responsabilidad:** DTO que encapsula el resultado de cualquier operación bancaria.
- **Campos:** `ok` (boolean), `mensaje` (String)
- **Uso:** Todos los métodos de negocio retornan este objeto para comunicar éxito o error con un mensaje descriptivo.

---

#### Enums

| Enum | Valores | Uso |
|---|---|---|
| `AccountState` | `ACTIVA`, `INACTIVA` | Estado de una cuenta o tarjeta |
| `AccountType` | `AHORROS`, `CORRIENTE`, `TARJETA_CREDITO` | Tipo de cuenta |
| `CreditCardTier` | `NINGUNO`, `BASICO(1M)`, `INTERMEDIO(2M)`, `PREMIUM(3M)` | Nivel y cupo de la tarjeta de crédito |

---

### 3.4 Puertos de Salida (Output Ports)

Las interfaces en `services/outputport/` definen el contrato que la capa de servicios usa para acceder a la persistencia. Esto permite cambiar la implementación (MySQL, PostgreSQL, memoria) sin modificar los servicios.

| Interfaz | Métodos principales |
|---|---|
| `ClientePersistencePort` | `guardarCliente`, `findByUsuario`, `existeUsuario`, `findAllClientes`, `eliminarCliente`, `actualizarCliente` |
| `CuentaAhorrosPersistencePort` | `guardarCuenta`, `findByUsuario`, `actualizarSaldo` |
| `CuentaCorrientePersistencePort` | `guardarCuenta`, `findByUsuario`, `actualizarSaldo` |
| `TarjetaCreditoPersistencePort` | `guardarTarjeta`, `findByUsuario`, `actualizarTarjeta` |
| `MovimientoPersistencePort` | `guardarMovimiento`, `findByUsuario`, `findByUsuarioYTipoCuenta` |
| `DeudaTCPersistencePort` | `guardarDeuda`, `findByUsuario`, `actualizarDeuda` |

---

### 3.5 Persistencia

#### `RowMapper<T>` (interfaz genérica)
- **Responsabilidad:** Contrato para convertir una fila `ResultSet` de JDBC en un objeto del dominio.
- **Método:** `T mapRow(ResultSet rs) throws SQLException`
- Cada entidad tiene su mapper concreto: `ClienteRowMapper`, `CuentaAhorrosRowMapper`, etc.

#### Repositorios MySQL
Cada repositorio implementa su puerto de salida correspondiente y ejecuta SQL directo con `PreparedStatement`.

| Repositorio | Puerto que implementa |
|---|---|
| `ClienteRepositoryMySql` | `ClientePersistencePort` |
| `CuentaAhorrosRepositoryMySql` | `CuentaAhorrosPersistencePort` |
| `CuentaCorrienteRepositoryMySql` | `CuentaCorrientePersistencePort` |
| `TarjetaCreditoRepositoryMySql` | `TarjetaCreditoPersistencePort` |
| `MovimientoRepositoryMySql` | `MovimientoPersistencePort` |
| `DeudaTCRepositoryMySql` | `DeudaTCPersistencePort` |

---

### 3.6 Repositorio en Memoria

#### `ClienteRepository.java`
- **Responsabilidad:** Almacena las entidades cargadas durante la sesión activa (cliente, cuentas, tarjeta). Actúa como caché de sesión para evitar consultas repetidas a MySQL.
- **También implementa:** `ClientePersistencePort`, para que los servicios puedan tratar ambos repositorios con la misma interfaz.
- **Datos que almacena:** `Cliente`, `CuentaAhorros`, `CuentaCorriente`, `TarjetaCredito`

---

### 3.7 Servicios

#### `ClienteServiceImpl.java`
Implementa `ClienteService`. Orquesta el ciclo de vida del cliente.

| Método | Descripción |
|---|---|
| `registrarCliente(double)` | Valida usuario único, crea cliente, cuentas y tarjeta, persiste todo en BD. |
| `login(String, String)` | Autentica, carga cuentas y deudas TC desde BD a memoria. |
| `actualizarCliente(String)` | Muestra perfil actual y permite modificar nombre, identificación, celular o contraseña. |
| `eliminarCliente(String)` | Elimina el cliente de la BD. |
| `getAllClientes()` | Lista todos los clientes (función de administrador). |

---

#### `CuentaServiceImpl.java`
Implementa `CuentaService`. Orquesta todas las operaciones financieras.

| Método | Descripción |
|---|---|
| `depositar(String, String, double)` | Deposita en ahorros o corriente y actualiza BD. |
| `retirar(String, String, double)` | Retira de ahorros (sin sobregiro) o corriente (con sobregiro 20%) y actualiza BD. |
| `transferir(String, String, String, double)` | Transfiere de un usuario a otro, actualizando saldos de ambos en BD. |
| `trasladarInterno(String, String, String, double)` | Traslada entre las propias cuentas del usuario. |
| `activarCuentaCorriente(String, double)` | Activa la cuenta corriente trasladando saldo desde ahorros. |
| `activarTarjetaCredito(String, CreditCardTier)` | Activa la tarjeta de crédito con el tier seleccionado. |
| `realizarCompra(String, double, int)` | Registra una compra en la TC y persiste la deuda en BD. |
| `pagarCuota(String, int, String)` | Paga una cuota de una deuda, actualiza BD. |
| `pagarTotal(String, int, String)` | Paga el total pendiente de una deuda, actualiza BD. |
| `verMovimientos(String)` | Muestra movimientos agrupados por tipo de cuenta desde BD. |
| `verMovimientosTC(String)` | Muestra solo los movimientos de la tarjeta de crédito. |
| `verSaldoTC(String)` | Muestra cupo total, disponible, usado y deudas activas de la TC. |
| `tieneTarjetaActiva(String)` | Verifica si el usuario tiene TC activa. |

---

### 3.8 Interfaz de Usuario

#### `MenuApp.java`
- **Responsabilidad:** Controla el flujo de navegación entre menús (principal, cajero, tarjeta de crédito, administrador).
- **Seguridad:** El login permite máximo 3 intentos; al tercer fallo cierra la aplicación con `System.exit(0)`.

#### `ClienteView.java` y `CuentaView.java`
- **Responsabilidad:** Capturan entrada del usuario, invocan el servicio correspondiente y muestran el resultado.

#### `FormValidation.java`
- **Responsabilidad:** Centraliza la validación de entrada por consola.

| Método | Descripción |
|---|---|
| `validateString(String)` | Valida que el campo no esté vacío. |
| `validateInt(String)` | Valida número entero, rechaza texto y campo vacío. |
| `validateDouble(String)` | Valida número decimal, rechaza texto y campo vacío. |
| `validateNumerico(String)` | Valida que el valor contenga solo dígitos (para cédula y celular). |
| `pausar()` | Detiene la ejecución hasta que el usuario presione 1 para volver al menú. |

---

## 4. Base de Datos

### Motor y Nombre
- **Motor:** MySQL 8.x
- **Nombre de la base de datos:** `miplata`

### Tablas

#### `cliente`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `usuario` | VARCHAR(50) | PRIMARY KEY | Identificador único del usuario |
| `password` | VARCHAR(100) | NOT NULL | Contraseña del usuario |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre completo |
| `identificacion` | VARCHAR(20) | NOT NULL | Número de cédula (solo dígitos) |
| `celular` | VARCHAR(15) | NOT NULL | Número de celular (solo dígitos) |

#### `cuenta_ahorros`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `usuario` | VARCHAR(50) | PK, FK → cliente | Dueño de la cuenta |
| `saldo` | DOUBLE | NOT NULL | Saldo actual |
| `estado` | VARCHAR(10) | NOT NULL | `ACTIVA` o `INACTIVA` |

#### `cuenta_corriente`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `usuario` | VARCHAR(50) | PK, FK → cliente | Dueño de la cuenta |
| `saldo` | DOUBLE | NOT NULL | Saldo actual |
| `estado` | VARCHAR(10) | NOT NULL | `ACTIVA` o `INACTIVA` |

#### `tarjeta_credito`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `usuario` | VARCHAR(50) | PK, FK → cliente | Dueño de la tarjeta |
| `cupo` | DOUBLE | NOT NULL | Cupo total asignado |
| `usado` | DOUBLE | NOT NULL | Monto utilizado del cupo |
| `estado` | VARCHAR(10) | NOT NULL | `ACTIVA` o `INACTIVA` |
| `tier` | VARCHAR(20) | NOT NULL | `BASICO`, `INTERMEDIO` o `PREMIUM` |

#### `movimiento`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id_movimiento` | INT | PK, AUTO_INCREMENT | Identificador único |
| `usuario` | VARCHAR(50) | FK → cliente | Usuario que realizó la operación |
| `tipo_cuenta` | VARCHAR(20) | NOT NULL | `AHORROS`, `CORRIENTE`, `TARJETA_CREDITO` |
| `tipo_operacion` | VARCHAR(30) | NOT NULL | `DEPOSITO`, `RETIRO`, `TRANSFERENCIA`, etc. |
| `monto` | DOUBLE | NOT NULL | Valor de la operación |
| `fecha` | DATETIME | DEFAULT NOW() | Fecha y hora del movimiento |

#### `deuda_tc`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | INT | PK, AUTO_INCREMENT | Identificador único |
| `usuario` | VARCHAR(50) | FK → cliente | Titular de la deuda |
| `fecha` | VARCHAR(20) | NOT NULL | Fecha de la compra |
| `capital` | DOUBLE | NOT NULL | Monto original de la compra |
| `cuotas` | INT | NOT NULL | Total de cuotas pactadas |
| `cuotas_pagadas` | INT | DEFAULT 0 | Cuotas que ya se pagaron |
| `cuota_mensual` | DOUBLE | NOT NULL | Valor de cada cuota |
| `total_a_pagar` | DOUBLE | NOT NULL | Capital + intereses total |
| `pagado` | DOUBLE | DEFAULT 0 | Monto acumulado pagado |
| `tasa` | DOUBLE | NOT NULL | Tasa de interés mensual aplicada |
| `saldo_pendiente` | DOUBLE | NOT NULL | Lo que falta por pagar |

### Modelo de Relaciones

```
cliente (PK: usuario)
  ├── cuenta_ahorros     (FK: usuario)
  ├── cuenta_corriente   (FK: usuario)
  ├── tarjeta_credito    (FK: usuario)
  ├── movimiento         (FK: usuario)
  └── deuda_tc           (FK: usuario)
```

### Consultas SQL Utilizadas

#### SELECT — Buscar cliente por usuario
```sql
SELECT * FROM cliente WHERE usuario = ?;
```
Usado en: `ClienteRepositoryMySql.findByUsuario()` — login, validación de usuario existente, actualización de perfil.

#### SELECT — Verificar existencia de usuario
```sql
SELECT * FROM cliente WHERE usuario = ?;
```
Usado en: `ClienteRepositoryMySql.existeUsuario()` — al registrar un cliente nuevo o transferir a otro usuario.

#### SELECT — Listar todos los clientes
```sql
SELECT * FROM cliente;
```
Usado en: `ClienteRepositoryMySql.findAllClientes()` — panel de administrador.

#### SELECT — Buscar movimientos por usuario y tipo de cuenta
```sql
SELECT * FROM movimiento WHERE usuario = ? AND tipo_cuenta = ?;
```
Usado en: `MovimientoRepositoryMySql.findByUsuarioYTipoCuenta()` — ver movimientos filtrados por cuenta o TC.

#### SELECT — Buscar deudas TC de un usuario
```sql
SELECT * FROM deuda_tc WHERE usuario = ?;
```
Usado en: `DeudaTCRepositoryMySql.findByUsuario()` — al iniciar sesión para cargar deudas activas desde BD.

#### INSERT — Registrar cliente
```sql
INSERT INTO cliente (usuario, password, nombre, identificacion, celular) VALUES (?,?,?,?,?);
```
Usado en: `ClienteRepositoryMySql.guardarCliente()`.

#### INSERT — Registrar movimiento
```sql
INSERT INTO movimiento (usuario, tipo_cuenta, tipo_operacion, monto) VALUES (?,?,?,?);
```
Usado en: Todos los métodos de `CuentaServiceImpl` que realizan operaciones financieras.

#### INSERT — Registrar deuda TC
```sql
INSERT INTO deuda_tc (usuario, fecha, capital, cuotas, cuotas_pagadas, cuota_mensual, total_a_pagar, pagado, tasa, saldo_pendiente) VALUES (?,?,?,?,?,?,?,?,?,?);
```
Usado en: `DeudaTCRepositoryMySql.guardarDeuda()` — al realizar una compra con la TC.

#### UPDATE — Actualizar saldo de cuenta
```sql
UPDATE cuenta_ahorros SET saldo = ? WHERE usuario = ?;
UPDATE cuenta_corriente SET saldo = ? WHERE usuario = ?;
```
Usado en: depósitos, retiros, transferencias, traslados y pagos de cuotas TC.

#### UPDATE — Actualizar deuda TC
```sql
UPDATE deuda_tc SET cuotas_pagadas=?, pagado=?, saldo_pendiente=? WHERE usuario=? AND fecha=?;
```
Usado en: `DeudaTCRepositoryMySql.actualizarDeuda()` — al pagar cuota o pagar total.

#### UPDATE — Actualizar perfil de cliente
```sql
UPDATE cliente SET nombre=?, identificacion=?, celular=?, password=? WHERE usuario=?;
```
Usado en: `ClienteRepositoryMySql.actualizarCliente()`.

#### DELETE — Eliminar cliente
```sql
DELETE FROM cliente WHERE usuario = ?;
```
Usado en: `ClienteRepositoryMySql.eliminarCliente()` — panel de administrador.

---

## 5. Flujo del Sistema

### Flujo General
```
Usuario (consola)
    │
    ▼
MenuApp (navegación)
    │
    ▼
ClienteView / CuentaView (entrada/salida)
    │
    ▼
ClienteService / CuentaService (interfaces — puertos de entrada)
    │
    ▼
ClienteServiceImpl / CuentaServiceImpl (lógica de negocio)
    │
    ├── ClienteRepository (memoria — sesión activa)
    │
    └── OutputPorts (interfaces)
            │
            ▼
        Repositorios MySQL (JDBC + PreparedStatement)
            │
            ▼
        Base de Datos MySQL
```

### Flujo de Login
```
1. Usuario ingresa usuario y contraseña (hasta 3 intentos)
2. ClienteServiceImpl.login() consulta MySQL (ClientePersistencePort)
3. Si existe: carga CuentaAhorros, CuentaCorriente, TarjetaCredito desde MySQL
4. Carga deudas TC desde deuda_tc y las agrega a la TarjetaCredito en memoria
5. Recalcula cupo disponible de TC restando el capital de deudas activas
6. Almacena todo en ClienteRepository (memoria de sesión)
7. Retorna Optional<Cliente> → MenuApp muestra el panel cajero
```

### Flujo de Compra con Tarjeta de Crédito
```
1. Usuario ingresa monto y número de cuotas
2. TarjetaCredito.comprar() calcula cuota con fórmula francesa
3. CuentaServiceImpl persiste la DeudaTC en MySQL
4. Registra movimiento tipo COMPRA en tabla movimiento
5. Actualiza cupo disponible en memoria
```

### Flujo de Transferencia entre Usuarios
```
1. Usuario ingresa usuario destino y monto
2. Servicio verifica existencia del destinatario en MySQL
3. Descuenta saldo del emisor y actualiza su cuenta en MySQL
4. Busca cuenta ahorros del destinatario: primero en memoria, si no en MySQL
5. Suma el monto al saldo del destinatario y actualiza en MySQL
6. Registra dos movimientos: TRANSFERENCIA (emisor) y TRANSFERENCIA_RECIBIDA (destinatario)
```

---

## 6. Configuración e Instalación

### Requisitos
- Java 17 o superior
- MySQL 8.x
- Maven 3.x
- IDE recomendado: IntelliJ IDEA

### Paso 1 — Crear la base de datos en MySQL

```sql
CREATE DATABASE miplata;
USE miplata;

CREATE TABLE cliente (
    usuario        VARCHAR(50)  PRIMARY KEY,
    password       VARCHAR(100) NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    identificacion VARCHAR(20)  NOT NULL,
    celular        VARCHAR(15)  NOT NULL
);

CREATE TABLE cuenta_ahorros (
    usuario VARCHAR(50) PRIMARY KEY,
    saldo   DOUBLE      NOT NULL,
    estado  VARCHAR(10) NOT NULL,
    FOREIGN KEY (usuario) REFERENCES cliente(usuario)
);

CREATE TABLE cuenta_corriente (
    usuario VARCHAR(50) PRIMARY KEY,
    saldo   DOUBLE      NOT NULL,
    estado  VARCHAR(10) NOT NULL,
    FOREIGN KEY (usuario) REFERENCES cliente(usuario)
);

CREATE TABLE tarjeta_credito (
    usuario VARCHAR(50) PRIMARY KEY,
    cupo    DOUBLE      NOT NULL,
    usado   DOUBLE      NOT NULL,
    estado  VARCHAR(10) NOT NULL,
    tier    VARCHAR(20) NOT NULL,
    FOREIGN KEY (usuario) REFERENCES cliente(usuario)
);

CREATE TABLE movimiento (
    id_movimiento  INT AUTO_INCREMENT PRIMARY KEY,
    usuario        VARCHAR(50)  NOT NULL,
    tipo_cuenta    VARCHAR(20)  NOT NULL,
    tipo_operacion VARCHAR(30)  NOT NULL,
    monto          DOUBLE       NOT NULL,
    fecha          DATETIME     DEFAULT NOW(),
    FOREIGN KEY (usuario) REFERENCES cliente(usuario)
);

CREATE TABLE deuda_tc (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    usuario        VARCHAR(50) NOT NULL,
    fecha          VARCHAR(20) NOT NULL,
    capital        DOUBLE      NOT NULL,
    cuotas         INT         NOT NULL,
    cuotas_pagadas INT         NOT NULL DEFAULT 0,
    cuota_mensual  DOUBLE      NOT NULL,
    total_a_pagar  DOUBLE      NOT NULL,
    pagado         DOUBLE      NOT NULL DEFAULT 0,
    tasa           DOUBLE      NOT NULL,
    saldo_pendiente DOUBLE     NOT NULL,
    FOREIGN KEY (usuario) REFERENCES cliente(usuario)
);
```

### Paso 2 — Configurar la conexión

En `DataBaseConnectionMySql.java` ajusta las credenciales si es necesario:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/miplata";
private static final String USERNAME = "root";
private static final String PASSWORD = "tu_password";
```

### Paso 3 — Ejecutar el proyecto

Desde IntelliJ: clic derecho sobre `Main.java` → **Run 'Main.main()'**

Desde terminal:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="miplata.Main"
```

---

## 7. Casos de Uso

### CU-01: Registrar Cliente

| | |
|---|---|
| **Actor** | Usuario nuevo |
| **Entrada** | Usuario (único), contraseña, nombre, identificación (solo números), celular (solo números), saldo inicial |
| **Validaciones** | Usuario no existente, campos no vacíos, identificación y celular solo numéricos, saldo > 0 |
| **Salida** | Cliente creado con cuenta de ahorros activa, cuenta corriente inactiva y tarjeta de crédito inactiva |
| **Persistencia** | Inserta en `cliente`, `cuenta_ahorros`, `cuenta_corriente`, `tarjeta_credito` |

### CU-02: Iniciar Sesión

| | |
|---|---|
| **Actor** | Usuario registrado |
| **Entrada** | Usuario y contraseña |
| **Validaciones** | Máximo 3 intentos; al tercer fallo cierra la app |
| **Salida** | Sesión activa con cuentas y deudas cargadas en memoria |

### CU-03: Depositar

| | |
|---|---|
| **Entrada** | Tipo de cuenta (ahorros/corriente), monto |
| **Validaciones** | Monto > 0, cuenta activa |
| **Salida** | Saldo actualizado en memoria y BD, movimiento registrado |

### CU-04: Retirar

| | |
|---|---|
| **Entrada** | Tipo de cuenta, monto |
| **Validaciones** | Ahorros: monto ≤ saldo; Corriente: monto ≤ saldo + 20% sobregiro |
| **Salida** | Saldo actualizado en BD, movimiento registrado |

### CU-05: Transferir a Otro Usuario

| | |
|---|---|
| **Entrada** | Usuario destino, monto |
| **Validaciones** | Destino existe en BD, no es el mismo usuario, saldo suficiente |
| **Salida** | Saldo de emisor reducido, saldo de destinatario aumentado, ambos actualizados en BD |

### CU-06: Realizar Compra con Tarjeta de Crédito

| | |
|---|---|
| **Entrada** | Monto, número de cuotas |
| **Validaciones** | TC activa, cupo disponible suficiente |
| **Salida** | Deuda creada en BD, cupo disponible actualizado, movimiento registrado |
| **Tasas** | 0% (1-2 cuotas), 1.9% (3-6 cuotas), 2.3% (7+ cuotas) |

### CU-07: Pagar Cuota / Total de TC

| | |
|---|---|
| **Entrada** | Número de deuda, cuenta origen (ahorros/corriente), tipo de pago |
| **Validaciones** | Saldo suficiente en cuenta origen, deuda no pagada |
| **Salida** | Saldo de cuenta reducido, deuda actualizada en BD, movimiento registrado |

### CU-08: Ver Movimientos

| | |
|---|---|
| **Salida** | Movimientos agrupados: Cuenta Ahorros / Cuenta Corriente / Tarjeta de Crédito |

---

## 8. Decisiones Técnicas

### Arquitectura Hexagonal (Ports & Adapters)
Se implementó para desacoplar la lógica de negocio de la tecnología de persistencia. Los servicios dependen únicamente de interfaces (`outputport/`), no de clases concretas MySQL. Esto permite:
- Cambiar de MySQL a otra BD sin tocar los servicios.
- Testear los servicios con repositorios en memoria.

### Doble Repositorio (MySQL + Memoria)
Al iniciar sesión se cargan las cuentas del usuario desde MySQL al `ClienteRepository` en memoria. Las operaciones durante la sesión se aplican primero en el objeto en memoria y luego se persisten en MySQL. Esto evita consultas repetidas a la BD por cada operación.

### Singleton para la Conexión
`DataBaseConnectionMySql` usa el patrón Singleton sincronizado para garantizar una sola conexión compartida durante toda la ejecución.

### RowMapper genérico
La interfaz `RowMapper<T>` estandariza la conversión de `ResultSet` a objetos del dominio, separando la responsabilidad de mapeo de la lógica de consulta SQL.

### OperacionResultado como DTO
En lugar de lanzar excepciones para errores de negocio (saldo insuficiente, cupo agotado), los métodos retornan un `OperacionResultado` con un flag `ok` y un mensaje descriptivo. Esto hace el flujo más predecible y fácil de mostrar en consola.

### Polimorfismo en las Cuentas
`CuentaAhorros` y `CuentaCorriente` heredan de `Cuenta` e implementan `calcularRetiro()` diferente. Esto permite tratar ambas con el mismo tipo `Cuenta` en los servicios y que cada una aplique sus propias reglas.

### FormValidation centralizado
Toda la entrada por consola pasa por `FormValidation`, que valida tipos, vacíos y formatos. Esto evita excepciones no controladas por entrada inválida del usuario.

---

## 9. Posibles Mejoras

| Mejora | Descripción |
|---|---|
| **Encriptación de contraseñas** | Aplicar hashing con BCrypt antes de almacenar en BD. |
| **Interfaz gráfica** | Migrar de consola a JavaFX o una API REST con frontend web. |
| **Rendimiento mensual automatizado** | Implementar un job que aplique el 1.5% mensual automáticamente en la fecha correspondiente. |
| **Historial de deudas pagadas** | Mostrar al usuario el resumen de compras ya liquidadas. |
| **Paginación de movimientos** | Cuando el historial es extenso, paginar los resultados en consola. |
| **Inyección de dependencias con framework** | Usar Spring o Guice en lugar de la inyección manual en `Config.java`. |
| **Tests unitarios** | Agregar pruebas con JUnit para los servicios y el dominio, usando repositorios en memoria. |
| **Múltiples cuentas ahorros** | Permitir que un usuario tenga más de una cuenta de ahorros. |
| **Notificaciones** | Enviar email o SMS al realizar operaciones (integración con servicios externos). |

---

## 10. Conclusiones

MiPlata es un sistema bancario completo que va más allá de un ejercicio académico típico. Implementa persistencia real en MySQL, una arquitectura limpia que separa las responsabilidades en capas bien definidas, y aplica los principios fundamentales de la programación orientada a objetos: herencia (`Cuenta` → `CuentaAhorros`, `CuentaCorriente`, `TarjetaCredito`), polimorfismo (cada cuenta define sus propias reglas de retiro), encapsulamiento (los datos del dominio solo se modifican a través de métodos propios) e interfaces para el desacoplamiento.

La arquitectura Ports & Adapters garantiza que el día de mañana se pueda cambiar MySQL por cualquier otro motor de base de datos modificando únicamente los repositorios, sin tocar ni una línea de la lógica de negocio. Esto demuestra que el diseño está pensado para crecer y mantenerse a largo plazo.
