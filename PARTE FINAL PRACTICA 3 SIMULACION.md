# **PARTE FINAL PRACTICA 3 SIMULACION**



¡Vamos a por ese 10! Ahora que tienes el núcleo del código, el trabajo se divide en \*\*implementación\*\*, \*\*pruebas\*\* y \*\*documentación\*\*.



Sigue este orden lógico para completar la práctica:



\---



\## 1. Preparación del Entorno



Primero, asegúrate de tener las herramientas necesarias en tu máquina (o VM).



1\. \*\*Instala las dependencias:\*\*

```bash

pip install fastapi uvicorn requests



```





2\. \*\*Organiza tus archivos:\*\* Crea una carpeta nueva para la Práctica 3 y guarda el código del servidor como `main.py` y el del cliente como `client.py`.



\---



\## 2. Ejecución y Validación "Local"



Antes de pasar a lo distribuido, comprueba que la lógica que heredamos de Java funciona igual en Python:



1\. \*\*Lanza el servidor:\*\*

```bash

uvicorn main:app --reload



```





2\. \*\*Verifica la lógica:\*\* Ejecuta tu `client.py`. Deberías ver cómo las tareas pasan de `PENDING` a `RUNNING` y finalmente a `FINISHED` con el resultado correcto (el factorial o el texto en mayúsculas), respetando los 5 segundos de espera que tenía tu `GestorCola.java`.

3\. \*\*Prueba los errores:\*\* Intenta hacer un `POST` a `/tasks` sin estar logueado o con un tipo de tarea que no sea `FACTORIAL` para ver si el servidor responde con los códigos `403` o `400` que definimos siguiendo tu `ServidorRMI.java`.



\---



\## 3. Generación de Documentación (Punto 3 y 6 de la guía)



FastAPI hace el trabajo sucio por ti. Con el servidor corriendo:



1\. \*\*Swagger:\*\* Ve a `http://127.0.0.1:8000/docs`. Interactúa con la API desde ahí. Haz capturas de pantalla para tu memoria.

2\. \*\*Redoc (Para el PDF entregable):\*\* Ve a `http://127.0.0.1:8000/redoc`. Esta interfaz es más limpia. Usa la opción de \*\*"Imprimir" del navegador y selecciona "Guardar como PDF"\*\*. Este es uno de los archivos que debes entregar.



\---



\## 4. El "Boss Final": Ejecución Distribuida



La rúbrica pide que el servidor sea accesible desde otra máquina.



1\. \*\*Lanza el servidor en modo abierto:\*\*

```bash

uvicorn main:app --host 0.0.0.0 --port 8000



```





2\. \*\*Desde otra VM o PC:\*\* Cambia la `BASE\\\_URL` en tu `client.py` por la IP de la máquina servidor (ej. `http://192.168.1.50:8000`).

3\. \*\*Captura de tráfico:\*\* Abre \*\*Wireshark\*\* en la máquina cliente. Filtra por `http`. Realiza una petición y busca el paquete \*\*POST\*\* y el \*\*JSON\*\* de respuesta. \*\*Captura esto\*\*, es evidencia obligatoria.



\---



\## 5. Checklist de Entregables



Para que no se te escape nada antes de subir el ZIP:



\* \[ ] \*\*Código fuente:\*\* La carpeta con `main.py` y `client.py`.

\* \[ ] \*\*Tabla de Transformación:\*\* (La que hicimos arriba: `login` -> `POST /login`, etc.).

\* \[ ] \*\*PDF de la API:\*\* El generado con Redoc.

\* \[ ] \*\*Memoria (Máx 4 páginas):\*\*

\* Explica que cambiaste el `Thread` de Java por `BackgroundTasks` de FastAPI.

\* Discute que REST es \*stateless\* y por eso validamos la IP en cada petición en lugar de mantener una conexión abierta como en RMI.





\* \[ ] \*\*Evidencias:\*\* Capturas de Wireshark y de las pruebas con el cliente \*\*Bruno\*\* o \*\*curl\*\*.



\*\*¿Quieres que te redacte un párrafo técnico sobre la "Gestión de Estado" (Stateless) para que lo pegues en tu memoria?\*\* Es un punto donde los profesores suelen ponerse estrictos.

