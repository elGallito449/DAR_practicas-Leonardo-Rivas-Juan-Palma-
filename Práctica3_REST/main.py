from fastapi import FastAPI, HTTPException, BackgroundTasks, status, Request
from pydantic import BaseModel
from typing import Dict, List
import time

# Definición del modelo Tarea basado en Tarea.java
class Tarea(BaseModel):
    id: str
    tipo: str
    parametros: str
    estadoActual: str = "PENDING"
    resultado: str = "NULL"

app = FastAPI(
    title="PETD API REST",
    description="Sistema Distribuido de Tareas (Traducción de Java RMI)",
    version="1.0.0"
)

# Persistencia en memoria (Equivalente a GestorCola y ServidorRMI)
registro_tareas: Dict[str, Tarea] = {}
clientes_autenticados = set()
contador_ids = 1

def ejecutar_logica(tarea_id: str):
    """Lógica asíncrona que simula el hilo GestorCola.java"""
    tarea = registro_tareas[tarea_id]
    tarea.estadoActual = "RUNNING"
    time.sleep(5)  # Simulación de carga

    try:
        if tarea.tipo == "FACTORIAL":
            n = int(tarea.parametros)
            fact = 1
            for i in range(1, n + 1): fact *= i
            tarea.resultado = str(fact)
        elif tarea.tipo == "TEXT_UP":
            tarea.resultado = tarea.parametros.upper()
        else:
            tarea.resultado = "ERROR_OP_DESCONOCIDA"
            tarea.estadoActual = "FAILED"
            return
        tarea.estadoActual = "FINISHED"
    except Exception:
        tarea.resultado = "ERROR_LOGICA"
        tarea.estadoActual = "FAILED"

# --- ENDPOINTS ---

@app.post("/login", status_code=status.HTTP_200_OK)
async def login(request: Request, credenciales: dict):
    """Simula el login de ServidorRMI.java registrando la IP"""
    usuario = credenciales.get("usuario")
    password = credenciales.get("password")
    ip_cliente = request.client.host

    if usuario == "admin" and password == "1234":
        clientes_autenticados.add(ip_cliente)
        return {"auth": "OK", "session": ip_cliente}
    raise HTTPException(status_code=401, detail="AUTH_FAILED")

@app.post("/tasks", status_code=status.HTTP_201_CREATED)
async def create_task(request: Request, data: dict, bt: BackgroundTasks):
    """Transformación de pushTarea()"""
    ip_cliente = request.client.host
    if ip_cliente not in clientes_autenticados:
        raise HTTPException(status_code=403, detail="Acceso Denegado: Debes hacer LOGIN")

    tipo = data.get("tipo")
    parametros = data.get("parametros")

    if tipo not in ["FACTORIAL", "TEXT_UP"]:
        raise HTTPException(status_code=400, detail="Comando_Desconocido")

    global contador_ids
    t_id = f"{contador_ids:04d}"
    contador_ids += 1

    nueva_tarea = Tarea(id=t_id, tipo=tipo, parametros=parametros)
    registro_tareas[t_id] = nueva_tarea

    # Ejecución en segundo plano para no bloquear el API
    bt.add_task(ejecutar_logica, t_id)

    return {"task_id": t_id}

@app.get("/tasks/{task_id}")
async def get_task(request: Request, task_id: str):
    """Transformación de pollTarea()"""
    ip_cliente = request.client.host
    if ip_cliente not in clientes_autenticados:
        raise HTTPException(status_code=403, detail="Acceso Denegado")

    tarea = registro_tareas.get(task_id)
    if not tarea:
        raise HTTPException(status_code=404, detail="Tarea no encontrada")
    return tarea