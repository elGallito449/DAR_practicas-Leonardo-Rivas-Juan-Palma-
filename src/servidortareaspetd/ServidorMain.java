package servidorpetd;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase principal del Servidor PETD.
 * Se encarga de arrancar la cola, abrir el puerto y aceptar clientes.
 */
public class ServidorMain {

    public static void main(String[] args) {
        
        int puerto = 5000; // Puedes cambiarlo si el profesor te pide otro
        
        System.out.println("=========================================");
        System.out.println("   SERVIDOR PETD INICIANDO...            ");
        System.out.println("=========================================");

        // 1. Instanciamos el almacén y arrancamos el hilo Worker
        GestorCola gestorCola = new GestorCola();
        gestorCola.start(); // Esto hace que el método run() del GestorCola empiece a ejecutarse en paralelo

        // 2. Abrimos el ServerSocket (Modo pasivo según la teoría)
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            
            System.out.println("[SERVIDOR] Escuchando en el puerto " + puerto + "...");
            System.out.println("[SERVIDOR] Esperando conexiones de clientes...\n");

            // 3. Bucle infinito para aceptar múltiples clientes
            while (true) {
                // El programa se bloquea en esta línea hasta que un cliente se conecta
                Socket socketCliente = serverSocket.accept(); 
                
                System.out.println("[SERVIDOR] ¡Nuevo cliente aceptado!");

                // 4. Creamos un "camarero" exclusivo para este cliente y le damos el socket y la cola
                ManejadorCliente manejador = new ManejadorCliente(socketCliente, gestorCola);
                
                // 5. Arrancamos el hilo del cliente
                manejador.start(); 
            }

        } catch (Exception e) {
            System.out.println("[SERVIDOR] Error crítico al abrir el puerto " + puerto);
            System.out.println("Detalle: " + e.getMessage());
        }
    }
}
