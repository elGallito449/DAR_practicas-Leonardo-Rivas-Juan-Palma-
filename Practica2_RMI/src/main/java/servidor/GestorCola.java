package servidor;

import comun.Tarea;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GestorCola extends Thread {
    private LinkedBlockingQueue<Tarea> cola;
    private ConcurrentHashMap<String, Tarea> registro;

    public GestorCola() {
        this.cola = new LinkedBlockingQueue<>();
        this.registro = new ConcurrentHashMap<>();
    }

    public void encolarTarea(Tarea t) {
        registro.put(t.getId(), t);
        cola.add(t);
    }

    public Tarea consultarTarea(String id) {
        return registro.get(id);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Tarea t = cola.take(); 
                t.setEstadoActual(Tarea.STATE_RUNNING);
                Thread.sleep(5000); // Pausa de 5seg para simular q le cuesta
                ejecutarLogica(t);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void ejecutarLogica(Tarea t) {
        try {
            if (t.getTipo().equals("FACTORIAL")) {
                long fact = 1;
                int n = Integer.parseInt(t.getParametros());
                for (int i = 1; i <= n; i++) fact *= i;
                t.setResultado(String.valueOf(fact));
            } else if (t.getTipo().equals("TEXT_UP")) {
                t.setResultado(t.getParametros().toUpperCase());
            } else {
                t.setResultado("ERROR_OP_DESCONOCIDA");
                t.setEstadoActual(Tarea.STATE_FAILED);
                return;
            }
            t.setEstadoActual(Tarea.STATE_FINISHED);
        } catch (Exception e) {
            t.setResultado("ERROR_LOGICA");
            t.setEstadoActual(Tarea.STATE_FAILED);
        }
    }
}