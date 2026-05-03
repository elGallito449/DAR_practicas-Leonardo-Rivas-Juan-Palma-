package servidor;

import comun.ServicioPETD;
import comun.Tarea;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.RemoteServer;
import java.util.HashSet;
import java.util.Set;

public class ServidorRMI extends UnicastRemoteObject implements ServicioPETD {
    private GestorCola gestorCola;
    private int contadorIds = 1;
    
    // Aquí guardamos las IPs de los clientes que han hecho LOGIN 
    private Set<String> clientesAutenticados;

    public ServidorRMI() throws RemoteException {
        super();
        this.gestorCola = new GestorCola();
        this.clientesAutenticados = new HashSet<>();
        this.gestorCola.start();
    }

    private synchronized String generarIdUnico() {
        return String.format("%04d", contadorIds++);
    }

    // Método para saber quién nos llama
    private String obtenerIpCliente() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            return "IP_DESCONOCIDA";
        }
    }

    @Override
    public boolean login(String usuario, String password) throws RemoteException {
        String ip = obtenerIpCliente();
        System.out.println("[RMI] Intento de login desde IP: " + ip + " - Usuario: " + usuario);
        
        if ("admin".equals(usuario) && "1234".equals(password)) {
            clientesAutenticados.add(ip); // Registramos la sesión de esta IP
            return true;
        }
        return false;
    }

    @Override
    public String pushTarea(String tipo, String parametros) throws RemoteException, IllegalArgumentException {
        String ip = obtenerIpCliente();
        // 1. VALIDACIÓN DE SEGURIDAD 
        if (!clientesAutenticados.contains(ip)) {
            throw new IllegalArgumentException("Acceso Denegado: Debes hacer LOGIN primero.");
        }
        
        // 2. VALIDACIÓN DE LÓGICA
        if (!tipo.equals("FACTORIAL") && !tipo.equals("TEXT_UP")) {
            throw new IllegalArgumentException("Comando_Desconocido: " + tipo);
        }
        
        String id = generarIdUnico();
        gestorCola.encolarTarea(new Tarea(id, tipo, parametros));
        System.out.println("[RMI] PUSH (" + ip + "): Tarea " + id);
        return id;
    }

    @Override
    public Tarea pollTarea(String id) throws RemoteException {
        String ip = obtenerIpCliente();
        if (!clientesAutenticados.contains(ip)) {
            throw new IllegalArgumentException("Acceso Denegado: Debes hacer LOGIN primero.");
        }
        return gestorCola.consultarTarea(id);
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.56.103"); // Para Wireshark local
            
            ServidorRMI servidor = new ServidorRMI();
            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("ServicioPETD", servidor);
            
            System.out.println(">>> Servidor PETD RMI Iniciado (Puerto 1099) y esperando conexiones...");
        } catch (Exception e) {
            System.err.println("Fallo al arrancar el servidor RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}