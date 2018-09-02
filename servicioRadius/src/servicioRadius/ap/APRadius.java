package servicioRadius.ap;

import java.rmi.Remote;
import java.rmi.*;

import servicioRadius.servidor.Session;

/**
 * Interfaz para el AP por el lado del servidor radius. 
 * @author Javier Ortiz Bonilla
 * @author Domingo Fernandez Piriz
 */

public interface APRadius extends Remote
{
	void deleteSessionByUser(String usuario, boolean tellRadiusServer) throws RemoteException;
	int getId() throws RemoteException;
	// void receiveNewSession(Session s) throws RemoteException;
}
