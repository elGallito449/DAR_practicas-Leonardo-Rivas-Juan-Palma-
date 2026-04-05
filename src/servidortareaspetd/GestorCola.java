package servidorpetd;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * CONCEPTO: Thread (Hilo)
 * Al hacer que nuestra clase "extends Thread", la convertimos en un mini-programa 
 * que se ejecuta en paralelo al resto del servidor. 
 * ¿Por qué es vital aquí? Porque mientras este hilo está "atascado" calculando 
 * un factorial muy largo, el hilo principal del servidor sigue libre para 
 * escuchar a nuevos clientes o responder a un comando POLL.
 */
public class GestorCola extends Thread {

    private LinkedBlockingQueue<Tarea> cola;
    
    /*
     * CONCEPTO: ConcurrentHashMap
     * Un HashMap normal es como un diccionario (guardas una Tarea asociada a su ID).
     * El problema es que si el hilo del cliente intenta meter una tarea nueva 
     * justo en el mismo milisegundo en el que este hilo Worker intenta leer otra, 
     * el programa puede crashear (lo que en teoría llamamos "condición de carrera").
     * La versión "Concurrent" gestiona los semáforos internamente de forma automática 
     * para que sea 100% seguro leer y escribir desde varios hilos a la vez.
     */
    private ConcurrentHashMap<String, Tarea> registro;

    public GestorCola() {
        this.cola = new LinkedBlockingQueue<>();
        this.registro = new ConcurrentHashMap<>();
    }

    // --- MÉTODOS DEL CLIENTE ---

    public void encolarTarea(Tarea t) {
        registro.put(t.getId(), t); // Guardamos en el historial
        cola.add(t);                // Metemos a la cola de trabajo
    }

    public Tarea consultarTarea(String id) {
        return registro.get(id);    // Devuelve la tarea o null
    }

    // --- HILO PLANIFICADOR (WORKER) ---

    /*
     * CONCEPTO: @Override (Sobrescribir)
     * La clase padre "Thread" ya tiene un método run() vacío por defecto. 
     * Con @Override le decimos a Java: "Ignora el método original, quiero que 
     * uses mi propia versión". Todo el código que metamos dentro de este run() 
     * es lo que se ejecutará en paralelo cuando arranquemos el hilo.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // take() saca la tarea. Si no hay, el hilo se duerme solo.
                Tarea t = cola.take(); 
                
                t.setEstadoActual(Tarea.STATE_RUNNING);
                Thread.sleep(10000); // Pausa visual de 5 seg para la defensa
                
                ejecutarLogica(t);  // Hacemos el cálculo

            } catch (Exception e) { 
                break; // Si hay error grave, salimos del bucle
            }
        }
    }

    // --- LÓGICA DE LAS TAREAS ---

// --- LÓGICA DE LAS TAREAS ---

    private void ejecutarLogica(Tarea t) {
        try {
            if (t.getTipo().equals("FACTORIAL")) {
                long fact = 1;
                for (int i = 1; i <= Integer.parseInt(t.getParametros()); i++) {
                    fact *= i;
                }
                t.setResultado(String.valueOf(fact));
                
            } else if (t.getTipo().equals("TEXT_UP")) {
                t.setResultado(t.getParametros().toUpperCase());
                
            } else {
                // Operación no contemplada en nuestro servidor
                t.setResultado("ERROR_OPERACION_DESCONOCIDA");
                t.setEstadoActual(Tarea.STATE_FAILED);
                return; // Salimos antes
            }
            
            t.setEstadoActual(Tarea.STATE_FINISHED); // Si todo fue bien

        } catch (NumberFormatException e) {
            // Integer.parseInt() fallará si nos mandan "PUSH FACTORIAL letras"
            t.setResultado("ERROR_PARAMETROS_INVALIDOS");
            t.setEstadoActual(Tarea.STATE_FAILED); 
        } catch (Exception e) {
            // Cualquier otro fallo genérico
            t.setResultado("ERROR_INTERNO");
            t.setEstadoActual(Tarea.STATE_FAILED);
        }
    }
}