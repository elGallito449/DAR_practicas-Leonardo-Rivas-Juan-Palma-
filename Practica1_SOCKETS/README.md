## ⚠️ IMPORTANTE
En este repositorio existen dos tipos de archivos:

- 🔹 **Archivos definitivos**: contienen la versión final/correcta del proyecto --> ubicados en las carpetas "src"/"docs".
- 🔸 **Archivos "en sucio"**: contienen pruebas, versiones intermedias o material de trabajo.

👉 Para la evaluación, tener en cuenta únicamente los archivos definitivos por favor.




# Servidor Concurrente TCP (Protocolo PETD)

Este repositorio contiene la implementación completa de un sistema Cliente-Servidor multihilo basado en Sockets TCP, desarrollado en Java. Implementa un protocolo de aplicación propio basado en texto (sintaxis ABNF) para la gestión remota de tareas matemáticas y de texto.

**Autores:** Juan Palma Prieto, Leo Rivas Martin.
**Asignatura:** Desarrollo de Aplicaciones en Red (Ingeniería de Telecomunicaciones)


## 🏗️ Arquitectura del Sistema

El servidor está diseñado bajo un modelo concurrente que separa la atención de red del procesamiento intensivo, garantizando que la aplicación no se bloquee.

1. **ManejadorCliente (FSM):** Un hilo independiente por cada conexión TCP. Implementa una Máquina de Estados Finita (WAIT_LOGIN -> AUTHENTICATED -> CLOSING) para validar la sintaxis ABNF y aplicar seguridad.
2. **GestorCola (Worker + FIFO):** Un hilo en segundo plano que extrae tareas de una "LinkedBlockingQueue" (Thread-Safe) y las procesa asíncronamente simulando carga de trabajo.
3. **Registro Concurrente:** Uso de "ConcurrentHashMap" para almacenar el historial de tareas y permitir consultas ("POLL") rápidas y seguras desde múltiples hilos.


## 📜 Protocolo Soportado (Comandos)

El servidor entiende los siguientes comandos en texto plano (terminados en "\r\n"):

* LOGIN <usuario> <contraseña>: Autenticación inicial en el sistema.
* PUSH <tipo_tarea> <parametros>: Envía una tarea a la cola (ej. "PUSH FACTORIAL 5"). Devuelve un "TASK_ID".
* POLL <id_tarea>: Consulta el estado asíncrono de una tarea (PENDING, RUNNING, FINISHED, FAILED)
* EXIT: Cierra la conexión de forma ordenada.

*El servidor incluye manejo de excepciones nativo para capturar comandos mal formateados y responder con códigos de error HTTP-like (400, 401, 403, 404).*


## ⚙️ Cómo ejecutar el proyecto

Este proyecto está configurado para ejecutarse fácilmente desde cualquier IDE (como NetBeans, Eclipse o IntelliJ) o desde consola.

### 1. Iniciar el Servidor
Ejecuta la clase "ServidorMain.java". El servidor quedará a la escucha en el puerto "5000" (configurable en el código).

### 2. Iniciar el Cliente
Ejecuta la clase "ClienteMain.java". 
*Nota: Si ejecutas el cliente en una máquina diferente (o Máquina Virtual), asegúrate de cambiar la variable "ipServidor" dentro del código del cliente para que apunte a la IP de la máquina host del servidor.*


## 📊 Documentación Formal y Diagramas de Diseño

La documentación formal se encuentra en la carpeta "docs/":
* [Ver Especificación Formal ABNF](docs/ABNF.txt)
* [Ver Diagrama de Estados](docs/DIAGRAMA%20ESTADOS%20SERVIDOR%20DEFINITIVO.pdf)
* [Ver Diagrama de Secuencia de interacciones](docs/DIAGRAMAS%20DE%20SECUENCIAS%20DEFINITIVO.pdf)

## 🦈 Validación en Red (Captura Wireshark)

Se incluye en el repositorio el archivo "captura_petd_trafico.pcap" con la captura de tráfico real.

**Descripción del escenario probado:** Comunicación en red aislada entre dos Máquinas Virtuales Ubuntu. La captura refleja el establecimiento de la conexión TCP, el intercambio completo de comandos del protocolo de aplicación (incluyendo autenticación, envío de las tareas matemáticas/texto, y sondeo de resultados) y el cierre final de la comunicación.
