package servidorpetd;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Este hilo atiende a un único cliente de forma exclusiva.
 * Implementa la Máquina de Estados Finita (FSM) del protocolo PETD.
 */
public class ManejadorCliente extends Thread {

    // --- LA MÁQUINA DE ESTADOS EXPLÍCITA ---
    private static final int STATE_WAIT_LOGIN = 1;
    private static final int STATE_AUTHENTICATED = 2;
    private static final int STATE_CLOSING = 3;

    // Generador de IDs únicos (static para que se comparta entre todos los hilos)
    private static int contadorIds = 1;

    private Socket socket;
    private GestorCola gestorCola;
    private int estadoActual; // Nuestra variable de estado

    // Constructor
    public ManejadorCliente(Socket socket, GestorCola gestorCola) {
        this.socket = socket;
        this.gestorCola = gestorCola;
        this.estadoActual = STATE_WAIT_LOGIN; // Estado inicial según nuestro diagrama
    }

    // Método sincronizado para generar el ID (ej: "0001", "0002") sin colisiones
    private synchronized String generarIdUnico() {
        String id = String.format("%04d", contadorIds);
        contadorIds++;
        return id;
    }

    @Override
    public void run() {
        try {
            // HERRAMIENTAS DE RED (Teoría de la asignatura)
            // BufferedReader: Lee texto línea a línea hasta encontrar el \r\n
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            // PrintWriter: Escribe texto. El "true" es el auto-flush (envía el mensaje instantáneamente)
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            System.out.println("[RED] Nuevo cliente conectado. IP: " + socket.getInetAddress());

            // --- MOTOR DE LA MÁQUINA DE ESTADOS ---
            while (estadoActual != STATE_CLOSING) {
                
                String mensaje = in.readLine(); // Se queda bloqueado esperando el mensaje (\r\n)
                
                // Si el mensaje es null, el cliente tiró del cable repentinamente
                if (mensaje == null) {
                    System.out.println("[RED] Cliente desconectado abruptamente.");
                    break;
                }

                String[] partes = mensaje.split(" "); // Troceamos el comando ABNF
                String comando = partes[0].toUpperCase();

                try {
                    switch (estadoActual) {
                        
                        // ==========================================
                        // ESTADO 1: ESPERANDO LOGIN
                        // ==========================================
                        case STATE_WAIT_LOGIN:
                            if (comando.equals("LOGIN")) {
                                String user = partes[1];
                                String pass = partes[2];
                                
                                // Credenciales hardcodeadas (nivel estudiante)
                                if (user.equals("admin") && pass.equals("1234")) {
                                    out.println("AUTH_OK");
                                    estadoActual = STATE_AUTHENTICATED; // ¡Transición de estado!
                                    System.out.println("[SESIÓN] Cliente autenticado con éxito.");
                                } else {
                                    out.println("ERROR 401 Credenciales_Invalidas");
                                }
                            } else if (comando.equals("EXIT")) {
                                out.println("BYE");
                                estadoActual = STATE_CLOSING;
                            } else {
                                // Si intenta hacer PUSH o POLL sin login
                                out.println("ERROR 403 Forbidden_Inicie_Sesion_Primero");
                            }
                            break;

                        // ==========================================
                        // ESTADO 2: AUTENTICADO (IDLE)
                        // ==========================================
                        case STATE_AUTHENTICATED:
                            if (comando.equals("PUSH")) {
                                String tipo = partes[1].toUpperCase();
                                String params = partes[2];
                                
                                String nuevoId = generarIdUnico();
                                Tarea nuevaTarea = new Tarea(nuevoId, tipo, params);
                                
                                gestorCola.encolarTarea(nuevaTarea); // Se la damos al Worker
                                
                                out.println("TASK_ID " + nuevoId + " STATE=PENDING");

                            } else if (comando.equals("POLL")) {
                                String idConsulta = partes[1];
                                Tarea tareaConsultada = gestorCola.consultarTarea(idConsulta);
                                
                                if (tareaConsultada == null) {
                                    out.println("ERROR 404 ID_No_Encontrado");
                                } else {
                                    String estado = tareaConsultada.getEstadoActual();
                                    // Si ha terminado o fallado, adjuntamos el RESULT
                                    if (estado.equals(Tarea.STATE_FINISHED) || estado.equals(Tarea.STATE_FAILED)) {
                                        out.println("STAT " + idConsulta + " " + estado + " RESULT=" + tareaConsultada.getResultado());
                                    } else {
                                        // Si está PENDING o RUNNING, no hay resultado aún
                                        out.println("STAT " + idConsulta + " " + estado);
                                    }
                                }

                            } else if (comando.equals("EXIT")) {
                                out.println("BYE");
                                estadoActual = STATE_CLOSING; // ¡Transición de estado!
                            } else {
                                // Comando desconocido
                                out.println("ERROR 400 Comando_Desconocido");
                            }
                            break;
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    // Si el cliente manda "PUSH" a secas (sin parámetros), el split() falla.
                    // Capturamos el error para que el servidor no crashee y respondemos con error de sintaxis.
                    out.println("ERROR 400 Formato_ABNF_Incorrecto");
                }
            }

            // --- FASE DE CIERRE ORDENADO ---
            in.close();
            out.close();
            socket.close();
            System.out.println("[RED] Conexión cerrada limpiamente.");

        } catch (Exception e) {
            System.out.println("[RED] Error en la comunicación con el cliente: " + e.getMessage());
        }
    }
}