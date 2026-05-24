# Práctica 3: Migración a API REST (FastAPI)

**Asignatura:** Desarrollo de Aplicaciones en Red (DAR)  
**Autor:** Juan Palma Prieto  

## Descripción del Proyecto

Este repositorio contiene la implementación de la Práctica 3, donde se ha transformado el sistema distribuido original (basado en Java RMI y Sockets) en una arquitectura orientada a recursos mediante una API RESTful construida con **Python** y **FastAPI**.

El objetivo principal es mantener la lógica funcional del sistema de colas de tareas (PETD) aplicando los principios de servicios web, operando bajo el estándar HTTP y devolviendo la información en formato JSON.

## Arquitectura y Decisiones de Diseño

* **Framework:** Se utiliza FastAPI por su alto rendimiento y la autogeneración nativa de documentación OpenAPI.
* **Asincronía:** Se ha sustituido el clásico `Thread` de Java por `BackgroundTasks` de FastAPI. Esto permite simular la carga de trabajo pesada de las tareas en segundo plano sin bloquear el hilo principal del servidor web.
* **Gestión de Estado (Stateless):** Para respetar la restricción *stateless* de la arquitectura REST, pero manteniendo la lógica de autenticación exigida en la práctica original, el servidor captura y almacena dinámicamente en memoria la IP de los clientes logueados.

## Endpoints Disponibles

* `POST /login`: Autenticación del cliente en el sistema.
* `POST /tasks`: Creación de una nueva tarea en la cola (Requiere autenticación previa). Admite operaciones como `FACTORIAL` o `TEXT_UP`.
* `GET /tasks/{id}`: Consulta del estado en tiempo real (`PENDING`, `RUNNING`, `FINISHED`, `FAILED`) y el resultado de una tarea específica.

## Instrucciones de Ejecución

**1. Instalar dependencias**
Se requiere entorno de Python 3.x. Instalar las librerías necesarias ejecutando:
`pip install fastapi uvicorn requests`

**2. Lanzar el Servidor**
Para ejecutar el servidor en la red local y permitir conexiones externas (para la prueba de ejecución distribuida):
`uvicorn main:app --host 0.0.0.0 --port 8000`

**3. Lanzar el Cliente de Pruebas**
Asegurarse de configurar la variable `BASE_URL` en el archivo `cliente.py` con la IP asignada al servidor y ejecutar:
`python cliente.py`
