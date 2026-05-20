RESOLUCION PRACTICA 3 DAR





##### 2\. **Modelos de Datos (Pydantic)**



Tu clase Tarea.java se convierte en un modelo de Pydantic. Esto facilita que FastAPI genere el JSON automáticamente.



from pydantic import BaseModel

from typing import Optional



class Tarea(BaseModel):

&#x20;   id: str

&#x20;   tipo: str  # "FACTORIAL" o "TEXT\_UP"

&#x20;   parametros: str

&#x20;   estadoActual: str = "PENDING"

&#x20;   resultado: str = "NULL"





##### 3\. **Implementación de la Lógica (GestorCola → BackgroundTasks)**



En Java usabas un Thread y una LinkedBlockingQueue. En FastAPI, para que el servidor sea eficiente y no se bloquee mientras calcula el factorial, usaremos BackgroundTasks.

Estructura base del Servidor (main.py)





from fastapi import FastAPI, HTTPException, BackgroundTasks, status

import time



app = FastAPI(title="Servidor PETD REST", version="1.0.0")



\# Simulación de BD en memoria (tu ConcurrentHashMap)

registro\_tareas = {}

contador\_ids = 1



def ejecutar\_logica(tarea\_id: str):

&#x20;   """Equivalente a tu GestorCola.java"""

&#x20;   tarea = registro\_tareas\[tarea\_id]

&#x20;   tarea.estadoActual = "RUNNING"

&#x20;   time.sleep(5)  # Simula carga de trabajo

&#x20;

&#x20;   try:

&#x20;       if tarea.tipo == "FACTORIAL":

&#x20;           n = int(tarea.parametros)

&#x20;           fact = 1

&#x20;           for i in range(1, n + 1): fact \*= i

&#x20;           tarea.resultado = str(fact)

&#x20;       elif tarea.tipo == "TEXT\_UP":

&#x20;           tarea.resultado = tarea.parametros.upper()

&#x20;       tarea.estadoActual = "FINISHED"

&#x20;   except Exception:

&#x20;       tarea.estadoActual = "FAILED"

&#x20;       tarea.resultado = "ERROR\_LOGICA"



@app.post("/tasks", status\_code=status.HTTP\_201\_CREATED)

async def push\_tarea(tipo: str, parametros: str, bt: BackgroundTasks):

&#x20;   global contador\_ids

&#x20;   # Validaciones de tu Práctica 2

&#x20;   if tipo not in \["FACTORIAL", "TEXT\_UP"]:

&#x20;       raise HTTPException(status\_code=400, detail="Operación desconocida")

&#x20;

&#x20;   nuevo\_id = f"{contador\_ids:04d}"

&#x20;   contador\_ids += 1

&#x20;

&#x20;   nueva\_tarea = Tarea(id=nuevo\_id, tipo=tipo, parametros=parametros)

&#x20;   registro\_tareas\[nuevo\_id] = nueva\_tarea

&#x20;

&#x20;   # Lanzamos el proceso en segundo plano (asíncrono)

&#x20;   bt.add\_task(ejecutar\_logica, nuevo\_id)

&#x20;

&#x20;   return {"task\_id": nuevo\_id}



@app.get("/tasks/{id}")

async def poll\_tarea(id: str):

&#x20;   if id not in registro\_tareas:

&#x20;       raise HTTPException(status\_code=404, detail="Tarea no encontrada")

&#x20;   return registro\_tareas\[id]







##### **Servidor FastAPI (main.py)**



Este servidor traduce la lógica de ServidorRMI.java y GestorCola.java a una arquitectura REST.





from fastapi import FastAPI, HTTPException, BackgroundTasks, status, Request

from pydantic import BaseModel

from typing import Dict, List

import time



\# Definición del modelo Tarea basado en Tarea.java

class Tarea(BaseModel):

&#x20;   id: str

&#x20;   tipo: str

&#x20;   parametros: str

&#x20;   estadoActual: str = "PENDING"

&#x20;   resultado: str = "NULL"



app = FastAPI(

&#x20;   title="PETD API REST",

&#x20;   description="Sistema Distribuido de Tareas (Traducción de Java RMI)",

&#x20;   version="1.0.0"

)



\# Persistencia en memoria (Equivalente a GestorCola y ServidorRMI)

registro\_tareas: Dict\[str, Tarea] = {}

clientes\_autenticados = set()

contador\_ids = 1



def ejecutar\_logica(tarea\_id: str):

&#x20;   """Lógica asíncrona que simula el hilo GestorCola.java"""

&#x20;   tarea = registro\_tareas\[tarea\_id]

&#x20;   tarea.estadoActual = "RUNNING"

&#x20;   time.sleep(5)  # Simulación de carga

&#x20;

&#x20;   try:

&#x20;       if tarea.tipo == "FACTORIAL":

&#x20;           n = int(tarea.parametros)

&#x20;           fact = 1

&#x20;           for i in range(1, n + 1): fact \*= i

&#x20;           tarea.resultado = str(fact)

&#x20;       elif tarea.tipo == "TEXT\_UP":

&#x20;           tarea.resultado = tarea.parametros.upper()

&#x20;       else:

&#x20;           tarea.resultado = "ERROR\_OP\_DESCONOCIDA"

&#x20;           tarea.estadoActual = "FAILED"

&#x20;           return

&#x20;       tarea.estadoActual = "FINISHED"

&#x20;   except Exception:

&#x20;       tarea.resultado = "ERROR\_LOGICA"

&#x20;       tarea.estadoActual = "FAILED"



\# --- ENDPOINTS ---



@app.post("/login", status\_code=status.HTTP\_200\_OK)

async def login(request: Request, credenciales: dict):

&#x20;   """Simula el login de ServidorRMI.java registrando la IP"""

&#x20;   usuario = credenciales.get("usuario")

&#x20;   password = credenciales.get("password")

&#x20;   ip\_cliente = request.client.host

&#x20;

&#x20;   if usuario == "admin" and password == "1234":

&#x20;       clientes\_autenticados.add(ip\_cliente)

&#x20;       return {"auth": "OK", "session": ip\_cliente}

&#x20;   raise HTTPException(status\_code=401, detail="AUTH\_FAILED")



@app.post("/tasks", status\_code=status.HTTP\_201\_CREATED)

async def create\_task(request: Request, data: dict, bt: BackgroundTasks):

&#x20;   """Transformación de pushTarea()"""

&#x20;   ip\_cliente = request.client.host

&#x20;   if ip\_cliente not in clientes\_autenticados:

&#x20;       raise HTTPException(status\_code=403, detail="Acceso Denegado: Debes hacer LOGIN")

&#x20;

&#x20;   tipo = data.get("tipo")

&#x20;   parametros = data.get("parametros")

&#x20;

&#x20;   if tipo not in \["FACTORIAL", "TEXT\_UP"]:

&#x20;       raise HTTPException(status\_code=400, detail="Comando\_Desconocido")

&#x20;

&#x20;   global contador\_ids

&#x20;   t\_id = f"{contador\_ids:04d}"

&#x20;   contador\_ids += 1

&#x20;

&#x20;   nueva\_tarea = Tarea(id=t\_id, tipo=tipo, parametros=parametros)

&#x20;   registro\_tareas\[t\_id] = nueva\_tarea

&#x20;

&#x20;   # Ejecución en segundo plano para no bloquear el API

&#x20;   bt.add\_task(ejecutar\_logica, t\_id)

&#x20;

&#x20;   return {"task\_id": t\_id}



@app.get("/tasks/{task\_id}")

async def get\_task(request: Request, task\_id: str):

&#x20;   """Transformación de pollTarea()"""

&#x20;   ip\_cliente = request.client.host

&#x20;   if ip\_cliente not in clientes\_autenticados:

&#x20;       raise HTTPException(status\_code=403, detail="Acceso Denegado")

&#x20;

&#x20;   tarea = registro\_tareas.get(task\_id)

&#x20;   if not tarea:

&#x20;       raise HTTPException(status\_code=404, detail="Tarea no encontrada")

&#x20;   return tarea







##### **Cliente de Prueba (client.py)**



Sustituye a ClienteRMI.java usando la librería requests.





import requests



BASE\_URL = "http://127.0.0.1:8000"



def test\_petd():

&#x20;   # 1. Intentar PUSH sin login (Debe fallar con 403)

&#x20;   print("\\n\[TEST 1] PUSH sin login:")

&#x20;   r = requests.post(f"{BASE\_URL}/tasks", json={"tipo": "TEXT\_UP", "parametros": "hola"})

&#x20;   print(f"Status: {r.status\_code} | Msg: {r.text}")



&#x20;   # 2. Login

&#x20;   print("\\n\[TEST 2] Login:")

&#x20;   r = requests.post(f"{BASE\_URL}/login", json={"usuario": "admin", "password": "1234"})

&#x20;   print(f"Status: {r.status\_code} | Resp: {r.json()}")



&#x20;   # 3. PUSH Tarea

&#x20;   print("\\n\[TEST 3] PUSH Tarea FACTORIAL:")

&#x20;   r = requests.post(f"{BASE\_URL}/tasks", json={"tipo": "FACTORIAL", "parametros": "5"})

&#x20;   t\_id = r.json()\["task\_id"]

&#x20;   print(f"Task ID creado: {t\_id}")



&#x20;   # 4. POLL Tarea (Estado PENDING/RUNNING)

&#x20;   print("\\n\[TEST 4] POLL Tarea:")

&#x20;   r = requests.get(f"{BASE\_URL}/tasks/{t\_id}")

&#x20;   print(f"Resultado: {r.json()}")



if \_\_name\_\_ == "\_\_main\_\_":

&#x20;   test\_petd()

