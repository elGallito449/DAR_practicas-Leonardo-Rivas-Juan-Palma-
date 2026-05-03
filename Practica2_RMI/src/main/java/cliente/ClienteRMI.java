package cliente;

import comun.ServicioPETD;
import comun.Tarea;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Scanner;

public class ClienteRMI {
    public static void main(String[] args) {
        try {
            System.out.println("[INFO] Buscando servidor en RMI Registry...");
            // Localizamos el servicio de nombres en la IP servidor
            Registry reg = LocateRegistry.getRegistry("127.0.0.1", 1099); 
            // Hacemos el lookup del Stub
            ServicioPETD stub = (ServicioPETD) reg.lookup("ServicioPETD");
            System.out.println("[OK] Stub remoto obtenido. Listo para operar.");
            
            Scanner sc = new Scanner(System.in);
            
            System.out.println("\n--- PETD CLIENTE RMI ---");
            System.out.println("Comandos: LOGIN <usr> <pass> | PUSH <id_opc> <datos> | POLL <id> | EXIT");
            
            while (true) {
                System.out.print("\nPETD> ");
                String input = sc.nextLine().trim();
                
                if (input.isEmpty()) continue;
                
                String[] p = input.split("\\s+"); // Separa por uno o más espacios
                String cmd = p[0].toUpperCase();

                if (cmd.equals("EXIT")) {
                    System.out.println("Saliendo del cliente...");
                    break;
                }
                
                try {
                    // Ahora validamos los parámetros antes de invocar los métodos
                    switch (cmd) {
                        case "LOGIN":
                            if (p.length < 3) {
                                System.out.println("ERROR: Uso correcto -> LOGIN <usuario> <password>");
                                break;
                            }
                            boolean authExito = stub.login(p[1], p[2]);
                            System.out.println(authExito ? "AUTH_OK" : "AUTH_FAILED");
                            break;
                            
                        case "PUSH":
                            if (p.length < 3) {
                                System.out.println("ERROR: Uso correcto -> PUSH <id_opcional> <datos>");
                                break;
                            }
                            // Delegamos al servidor la responsabilidad de saber si estamos logueados
                            String id = stub.pushTarea(p[1], p[2]);
                            System.out.println("TASK_ID " + id);
                            break;
                            
                        case "POLL":
                            if (p.length < 2) {
                                System.out.println("ERROR: Uso correcto -> POLL <id_tarea>");
                                break;
                            }
                            Tarea t = stub.pollTarea(p[1]);
                            if (t == null) {
                                System.out.println("ERROR 404 (Tarea no encontrada)");
                            } else {
                                System.out.println("STAT " + t.getId() + " " + t.getEstadoActual());
                                if (t.getEstadoActual().equals(Tarea.STATE_FINISHED)) {
                                    System.out.println("RESULT " + t.getResultado());
                                }
                            }
                            break;
                            
                        default:
                            System.out.println("ERROR 400 (Comando no reconocido)");
                    }
                } catch (IllegalArgumentException e) {
                    // Capturamos el error si el servidor se queja de algo (ej un PUSH sin estar logueado)
                    System.out.println("ERROR DEL SERVIDOR: " + e.getMessage());
                } catch (RemoteException e) {
                    // Capturamos fallos de red en mitad de la ejecución 
                    System.out.println("CRÍTICO: Fallo de comunicación con el servidor. " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Este catch pilla el fallo inicial del Registry si el servidor está apagado 
            System.err.println("No se pudo iniciar el cliente. ¿Está el Servidor RMI encendido? Detalle: " + e.getMessage());
        }
    }
}