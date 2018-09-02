package servicioRadius.ap;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import servicioRadius.servidor.ServicioRadius;
import servicioRadius.servidor.Session;

/**
 * Clase para el AP por el lado del cliente.
 * @author Javier Ortiz Bonilla
 * @author Domingo Fernandez Piriz
 *
 */

class APRadiusImpl extends UnicastRemoteObject implements APRadius
{
	
	int myId;
	List<Session> mySessionList;
	ServicioRadius radius;
	AccessPoint ap;
	
	/**
	 * Constructor de la clase
	 * @param mPoint {@link AccessPoint}
	 * @param id int 
	 * @param rad {@link ServicioRadius} 
	 * @param sessionList {@link Session} 
	 * @throws RemoteException
	 */
	APRadiusImpl(AccessPoint mPoint,int id, ServicioRadius rad, List<Session> sessionList) throws RemoteException
	{
		myId = id;
		radius = rad; // Referencia del servidor radius
		mySessionList = sessionList;
		ap=mPoint; // Referencia del AP
	}

	/**
	 * Método que devuelve el id del AP.
	 * @return int
	 */
	public int getId() throws RemoteException
	{
		return myId;
	}

	/**
	 * Método que borra la sesión de un usuario. EL segundo parámetro indica si se quiere
	 * o no llamar al método remoto RemoteException del servidor radius para que él 
	 * también la borre.
	 * @param usuario {@link String}
	 * @param tellRadiusServer boolean
	 * @throws RemoteException
	 */
	public void deleteSessionByUser(String usuario, boolean tellRadiusServer) throws RemoteException
	{
		for (Session ss : mySessionList)
		{
			if (usuario.equals(ss.getUsuario()))
			{
				if (tellRadiusServer)
					radius.deleteSessionByUser(usuario, false);
				mySessionList.remove(ss);
				return;
			}
		}
	}

	/*
	public void receiveNewSession (Session s) throws RemoteException
	{
		mySessionList.add(s);
		System.out.println("Recibido nuevo usuario migrado: " + s.getUsuario());
	}
	*/
}
