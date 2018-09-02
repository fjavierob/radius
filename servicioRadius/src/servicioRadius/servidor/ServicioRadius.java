package servicioRadius.servidor;
 
import java.rmi.*;
import servicioRadius.ap.APRadius;

 
/**
 * Interfaz del servidor
 * @author Domingo Fernandez Piriz
 * @author Javier Ortiz Bonilla
 */
public interface ServicioRadius extends Remote
{
    boolean altaAp(APRadius ap) throws RemoteException;

    void bajaAp(APRadius ap) throws RemoteException;

    String accessRequest(APRadius ap, String usuario) throws RemoteException;

    Session challengeResponse(APRadius ap, String dirMac, String usuario, String response) throws RemoteException;

    void deleteSessionByUser(String usuario, boolean tellAp) throws RemoteException;

    void notifyBytesFromUser(String usuario, int nBytes) throws RemoteException;

    int getTotalBytesFromUser(String usuario) throws RemoteException;

    Session moveUser(String usuario, APRadius nuevoAp) throws RemoteException;
}
