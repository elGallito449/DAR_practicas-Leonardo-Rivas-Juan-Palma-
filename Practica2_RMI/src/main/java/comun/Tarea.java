package comun;

import java.io.Serializable;

public class Tarea implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String STATE_PENDING = "PENDING";
    public static final String STATE_RUNNING = "RUNNING";
    public static final String STATE_FINISHED = "FINISHED";
    public static final String STATE_FAILED = "FAILED";
    
    private String id;
    private String tipo;
    private String parametros;
    private String estadoActual;
    private String resultado;

    public Tarea(String id, String tipo, String parametros) {
        this.id = id;
        this.tipo = tipo;
        this.parametros = parametros;
        this.estadoActual = STATE_PENDING;
        this.resultado = "NULL";
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public String getParametros() { return parametros; }
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
}