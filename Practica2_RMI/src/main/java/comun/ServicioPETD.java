package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServicioPETD extends Remote {
    boolean login(String usuario, String password) throws RemoteException;
    String pushTarea(String tipo, String parametros) throws RemoteException, IllegalArgumentException;
    Tarea pollTarea(String id) throws RemoteException;
}