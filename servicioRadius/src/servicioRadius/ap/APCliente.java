package servicioRadius.ap;

import java.rmi.Remote;
import java.rmi.*;

/**
 *  Interfaz para el AP por el lado del cliente. 
 *  @author Javier Ortiz Bonilla
 *  @author Domingo Fernandez Piriz
 */

public interface APCliente extends Remote 
{
	String conectar(String usuario, String pass, String dirMAC) throws RemoteException;

	void desconectar(String usuario) throws RemoteException;

	boolean migrar(String usuario) throws RemoteException;

	int getSessionBytesFromUser(String usuario) throws RemoteException;

	int getTotalBytesFromUser(String usuario) throws RemoteException;

	boolean recibePaquete(Paquete pqt) throws RemoteException;
}
