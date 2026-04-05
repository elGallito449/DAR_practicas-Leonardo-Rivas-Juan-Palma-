package servidorpetd;

// objeto que guarda info y viaja por la cola

/* PQ string??
--> Para el id ("0001"): Si usáramos un int, el número 0001 se
guardaría simplemente como 1, perdiendo los ceros a la izquierda.
Como el ABNF nos obliga a que sean 4 dígitos exactos, guardarlo 
como texto (String) es lo más seguro

--> Para los parametros ("10"): Cuando leemos datos de un enchufe
de red (socket), lo que nos llega es texto puro. Es mucho más 
seguro guardarlo en la tarea tal cual llega como texto (String). 
Más adelante, justo en el momento de calcular el factorial, ya 
lo transformaremos a número entero.

*/

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
