import requests

BASE_URL = "http://127.0.0.1:8000"

def test_petd():
    # 1. Intentar PUSH sin login (Debe fallar con 403)
    print("\n[TEST 1] PUSH sin login:")
    r = requests.post(f"{BASE_URL}/tasks", json={"tipo": "TEXT_UP", "parametros": "hola"})
    print(f"Status: {r.status_code} | Msg: {r.text}")

    # 2. Login
    print("\n[TEST 2] Login:")
    r = requests.post(f"{BASE_URL}/login", json={"usuario": "admin", "password": "1234"})
    print(f"Status: {r.status_code} | Resp: {r.json()}")

    # 3. PUSH Tarea
    print("\n[TEST 3] PUSH Tarea FACTORIAL:")
    r = requests.post(f"{BASE_URL}/tasks", json={"tipo": "FACTORIAL", "parametros": "5"})
    t_id = r.json()["task_id"]
    print(f"Task ID creado: {t_id}")

    # 4. POLL Tarea (Estado PENDING/RUNNING)
    print("\n[TEST 4] POLL Tarea:")
    r = requests.get(f"{BASE_URL}/tasks/{t_id}")
    print(f"Resultado: {r.json()}")

if __name__ == "__main__":
    test_petd()