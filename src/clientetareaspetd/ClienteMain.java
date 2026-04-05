package clientepetd;

/* Codigo que  simplemente implemente una consola para hacer peticiones en crudo
una simple terminal (consola) donde tú escribirás los comandos (LOGIN admin 1234,
PUSH FACTORIAL 5, POLL 0001) y verás la respuesta cruda del servidor 
(AUTH_OK, TASK_ID...).
*/


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClienteMain {

    public static void main(String[] args) {
        
        // Configuración de red (Apunta a la misma máquina por ahora)
        String ipServidor = "127.0.0.1"; // Cuando uses las dos VMs, pondrás aquí la IP de la VM Servidor
        int puerto = 5000;

        System.out.println("=========================================");
        System.out.println("   CLIENTE PETD (Consola ABNF)           ");
        System.out.println("=========================================");
        System.out.println("Comandos soportados:");
        System.out.println(" - LOGIN <usuario> <pass>");
        System.out.println(" - PUSH <tipo> <parametros>");
        System.out.println(" - POLL <id>");
        System.out.println(" - EXIT");
        System.out.println("=========================================\n");

        /*
         * CONCEPTO: try-with-resources
         * Al poner los Sockets y Buffers entre paréntesis después del "try", 
         * Java se encarga de cerrarlos automáticamente al final, aunque haya errores.
         */
        try (
            // 1. Conectamos la "tubería" al servidor
            Socket socket = new Socket(ipServidor, puerto);
            
            // 2. Herramientas para hablar con el SERVIDOR
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            // 3. Herramienta para leer tu TECLADO
            Scanner teclado = new Scanner(System.in)
        ) {
            System.out.println("[SISTEMA] Conectado al servidor " + ipServidor + ":" + puerto);
            String comandoTeclado;

            // Bucle principal de interacción
            while (true) {
                System.out.print("\nPETD> ");
                comandoTeclado = teclado.nextLine(); // Leemos lo que has escrito
                
                if (comandoTeclado.trim().isEmpty()) {
                    continue; // Evitamos mandar líneas en blanco
                }

                // 1. Enviamos el mensaje al servidor (le añade el \r\n internamente)
                out.println(comandoTeclado);

                // 2. Esperamos y leemos la respuesta del servidor
                String respuestaServidor = in.readLine();
                
                // Si el servidor nos devuelve null, es que nos ha cerrado la conexión
                if (respuestaServidor == null) {
                    System.out.println("[SISTEMA] El servidor ha cerrado la conexión.");
                    break;
                }

                System.out.println("Servidor responde: " + respuestaServidor);

                // 3. Si mandamos EXIT y el servidor responde BYE, salimos del bucle
                if (comandoTeclado.toUpperCase().equals("EXIT") && respuestaServidor.equals("BYE")) {
                    System.out.println("[SISTEMA] Cerrando cliente ordenadamente...");
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("[SISTEMA] Error de conexión: " + e.getMessage());
            System.out.println("Asegúrate de que el servidor está encendido primero.");
        }
    }
}