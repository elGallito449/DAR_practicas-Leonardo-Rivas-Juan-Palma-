package servidorpetd;

// objeto que guarda info y viaja por la cola


public class Tarea {
 
    // Constantes de estado (Según ABNF)
    public static final String STATE_PENDING = "PENDING";
    public static final String STATE_RUNNING = "RUNNING";
    public static final String STATE_FINISHED = "FINISHED";
    public static final String STATE_FAILED = "FAILED";
    
    // Atributos de la tarea
    private String id;           // Identificador de 4 dígitos (ej: "0001") para respetar ABNF
    private String tipo;         // "FACTORIAL" o "TEXT_UP"
    private String parametros;   // El número o el texto a procesar
    private String estadoActual; // Estado en el que se encuentra
    private String resultado;    // El resultado final o "NULL" si aún no termina

    // Constructor
    public Tarea(String id, String tipo, String parametros) {
        this.id = id;
        this.tipo = tipo;
        this.parametros = parametros;
        this.estadoActual = STATE_PENDING; // Estado inicial por defecto según diagrama
        this.resultado = "NULL";
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getParametros() {
        return parametros;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
    
}
