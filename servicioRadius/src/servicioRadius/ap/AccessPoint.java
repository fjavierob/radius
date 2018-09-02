package servicioRadius.ap;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import servicioRadius.servidor.ServicioRadius;
import servicioRadius.servidor.Session;


/**
 * Clase AccessPoint. 
 * Modela un AP que tiene una lado hacia los usuarios (objeto APCliente)
 *	y otro lado hacia el servidor Radius (objeto APRadius).
 * @author Javier Ortiz Bonilla
 * @author Domingo Fernandez Piriz
 */
public class AccessPoint 
{
	
	private APCliente apCliente;
	private APRadius apRadius;
	private ServicioRadius servicioRadius;
	private List<Session> mySessionList;
	private int id;
	
	public AccessPoint(ServicioRadius servicio, int id) throws RemoteException 
	{
		mySessionList = new LinkedList<Session>();

		this.servicioRadius =  servicio;
		this.id = id;
		apCliente = new APClienteImpl(this, this.id, servicioRadius, mySessionList);
		apRadius = new APRadiusImpl(this, this.id, servicioRadius, mySessionList);
	}	
	
	/**
	 * Devuelve la referencia del objeto APCliente.
	 * @return APCliente
	 */
	public synchronized APCliente getApCliente() {
		return apCliente;
	}


	/** Devuelve la referencia del objeto APRadius.
	 * 
	 * @return APRadius
	 */
	public synchronized APRadius getApRadius() {
		return apRadius;
	}
}
