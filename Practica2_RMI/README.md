# 🚀 Práctica 2: Sistema de Objetos Distribuidos con Java RMI

Este directorio contiene la evolución del sistema cliente-servidor de procesamiento de tareas (PETD) desarrollado en la Práctica 1. En esta iteración, se ha migrado la arquitectura de Sockets TCP y protocolos explícitos de texto a un modelo de **objetos distribuidos mediante Java RMI (Remote Method Invocation)**.

## 🏗️ Arquitectura del Sistema

El sistema utiliza el patrón **Singleton** para la exportación del objeto remoto. Las principales características técnicas son:
* **Invocación Implícita:** Los comandos del cliente se mapean directamente a llamadas a métodos nativos de Java (`login`, `pushTarea`, `pollTarea`).
* **Estado Compartido y Concurrencia:** Un único objeto en el servidor atiende a múltiples clientes simultáneamente de forma segura mediante el uso de `ConcurrentHashMap` y `LinkedBlockingQueue`.
* **Seguridad y Control de Sesión:** Dado que RMI es *stateless* (sin estado), el servidor rastrea las sesiones activas interceptando la IP del cliente mediante `RemoteServer.getClientHost()`, evitando que usuarios no autenticados envíen tareas.
* **Serialización:** Las tareas se envían a través de la red como objetos completos utilizando `java.io.Serializable`.


## ⚙️ Instrucciones Detalladas de Ejecución (Despliegue en 2 Máquinas)

Para probar el correcto funcionamiento del sistema y la comunicación en red, se requieren dos equipos (o Máquinas Virtuales) conectados a la misma red local. Ambos equipos deben tener instalado **Java (JDK 17+)** y **Maven**.

### Fase 1: Configuración y Arranque del Servidor (Máquina A)

1. Abre una terminal en la Máquina A y averigua su IP local en la red (usando `ip a` en Linux/Mac o `ipconfig` en Windows). Ejemplo: `192.168.56.103`.
2. Abre el archivo fuente del servidor: `src/main/java/servidor/ServidorRMI.java`.
3. Localiza la siguiente línea en el método principal y sustituye la IP de ejemplo por la IP real de la Máquina A:
   `System.setProperty("java.rmi.server.hostname", "192.168.56.103");`
4. Compila el proyecto con Maven:
   `mvn clean install`
5. Ejecuta la clase `ServidorRMI`. Verás en consola el mensaje: `>>> Servidor PETD RMI Iniciado (Puerto 1099) y esperando conexiones...`

### Fase 2: Configuración y Arranque del Cliente (Máquina B)

1. Ve a la Máquina B y abre el archivo fuente del cliente: `src/main/java/cliente/ClienteRMI.java`.
2. Localiza la línea donde se busca el *Registry* y sustituye la IP por la **IP de la Máquina A (Servidor)**:
   `Registry reg = LocateRegistry.getRegistry("192.168.56.103", 1099);`
3. Compila el proyecto con Maven:
   `mvn clean install`
4. Ejecuta la clase `ClienteRMI`. La consola mostrará el prompt `PETD> ` listo para recibir comandos.


## 💻 Uso del Cliente (Comandos Disponibles)

Una vez conectadas las máquinas, el usuario puede interactuar con la terminal del cliente. El servidor validará la sesión y los parámetros enviados.

1. **Iniciar Sesión:**
   * Sintaxis: `LOGIN <usuario> <contraseña>`
   * Ejemplo: `LOGIN admin 1234`
   * *Nota: Es obligatorio realizar este paso antes de enviar tareas.*

2. **Enviar Tareas (Push):**
   * Sintaxis: `PUSH <tipo_tarea> <parametros>`
   * Tipos soportados: `FACTORIAL` o `TEXT_UP`.
   * Ejemplos: 
     * `PUSH FACTORIAL 5`
     * `PUSH TEXT_UP hola_mundo`

3. **Consultar Estado de Tareas (Poll):**
   * Sintaxis: `POLL <id_tarea>`
   * Ejemplo: `POLL 0001`
   * El sistema devolverá el estado actual (`PENDING`, `RUNNING`, `FINISHED` o `FAILED`) y el resultado en caso de haber concluido.

4. **Salir:**
   * Sintaxis: `EXIT`


## 🦈 Análisis de Tráfico (Wireshark)
Si se desea auditar la conexión, se puede ejecutar Wireshark en cualquiera de las dos máquinas durante la sesión. Para limpiar el ruido de la red y visualizar únicamente la negociación y llamadas a métodos remotos, aplica el siguiente filtro:
`rmi || tcp.port == 1099`
