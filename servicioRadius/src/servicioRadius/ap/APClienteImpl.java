package servicioRadius.ap;
 
import java.util.*;
import java.math.BigInteger;
import java.rmi.*;
import java.rmi.server.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import servicioRadius.servidor.Session;
import servicioRadius.servidor.ServicioRadius;

/**
 * Clase para el AP por el lado del cliente. 
 * @author Javier Ortiz Bonilla
 * @author Domingo Fernandez Piriz
 */

class APClienteImpl extends UnicastRemoteObject implements APCliente
{
	int myId;
	List<Session> mySessionList;
	ServicioRadius radius;
	AccessPoint ap;

	/**
	 * Constructor de la clase
	 * @param mPoint
	 * @param id
	 * @param serv
	 * @param sessionList
	 * @throws RemoteException
	 */
	APClienteImpl(AccessPoint mPoint,int id, ServicioRadius serv, 
			List<Session> sessionList) throws RemoteException
	{
		myId = id;
		radius = serv; // Referencia del servidor radius.
		mySessionList = sessionList;
		ap=mPoint; // Referencia del AP
	}

	 /**
	 * Método por el cual un usuario procede a conectarse al AP. Se llama por el usuario en cuestión
	 * de forma remota. 
	 * 1. El AP ejecuta en el servidor radius el accessRequest para solicitar el acceso del cliente.
	 * 2. El AP recibe (si existía el usuario en la base de datos del radius) un desafío que es una
	 *  cadena aleatoria.
	 *  3. En caso de recibir el desafío, responde a este llamando al método remoto challengeResponse
	 *  	del servidor radius. La respuesta es el MD5 de las cadenas usuario, ontraseña y desafío
	 *  concatenadas.
	 *  4. Si la contraseña era correcta, el desafío debió superarse con éxito y el método 
	 *  challengeResponse habrá devuelto un objeto Session, con los parámetros de red (la IP) para
	 *  el usuario, que el AP almacenará. En caso de no haber superado el desafío, el método habrá 
	 *  retornado null y el cliente no se habrá conectado.
	 *  5. Se devuelve la IP de cliente o null dependiendo del resultado anterior.
	 *  @param usuario {@link String}  
	 *  @param pass {@link String}  
	 *  @param dirMAC {@link String}  
	 *  @throws RemoteException
	 *  @return {@link String}
	 */
	
	public String conectar(String usuario, String pass, String dirMAC) throws RemoteException
	{
		try 
		{
			MessageDigest mDigest=MessageDigest.getInstance("MD5");
			mDigest.reset();
			String challenge = radius.accessRequest(ap.getApRadius(), usuario);
			if (challenge != null)
			{
				// Respuesta al desafío = MD5(usuario + pass + challenge)
				String response = usuario + pass + challenge;
				mDigest.update(response.getBytes());
				BigInteger bigInt = new BigInteger(1,mDigest.digest());
				response = bigInt.toString(32);
				// Responder al desafío			
				Session s = radius.challengeResponse(ap.getApRadius(), dirMAC, usuario,  response);
				
				if (s != null)
				{
					mySessionList.add(s);
					System.out.println("Usuario " + usuario + " conectado con IP " + s.getIp());
					return s.getIp();
				}
			}
		}
		catch (RemoteException e)
		{
			System.err.println("AccessPointImpl: Error  de  comunicacion:  "  + e.toString());
		} catch (NoSuchAlgorithmException e) 
		{
//			e.printStackTrace();
			System.err.println("AccessPointImpl: Error  en la eleccion de "+
					"funcion hash:  "  + e.toString());
		}
		return null;
	}
	
	/**
	 *  Método para desconectar a un usuario. Es llamado remotamente por el usuario que
	 *  quiere desconectarse. Se llama al método deleteSessionByUser de APRadius con el segundo 
	 *  parámetro = true para borrar la sesión del usuario en el AP y en el servidor radius.
	 *  @param usuario {@link String} 
	 *  @throws RemoteException
	 */
	public void desconectar(String usuario) throws RemoteException
	{
		System.out.println("Desconectar usuario "+usuario);
		ap.getApRadius().deleteSessionByUser(usuario, true);
		/*Session s = findSessionByUser(usuario);
		if (s != null)
		{
			mySessionList.remove(s);
	    	ap.getApRadius().deleteSessionByUser(usuario, true);
	    	System.out.println("Usuario "+usuario+" desconectado");
	    }
	    else System.out.println("Sesión de "+usuario+" no encontrada");
		*/
	}
	
	/** Método ejecutado de forma remota por un cliente para migrarse de un AP <i>a este AP</i>
	 * Se llama al método moveUser del servidor radius para notificar que un usuario quiere 
	 * moverse hacia este AP y este le retorna el objeto sesión del usuario.
	 * @param usuario {@link String} 
	 * @return {@link Boolean} Si se ha realizado la migracion correctamente
	 */
	public boolean migrar(String usuario) throws RemoteException 
	{
		Session s = radius.moveUser(usuario, ap.getApRadius());
		if (s != null)
		{
			System.out.println("Recibo usuario migrado: " + usuario);
			mySessionList.add(s);
			return true;
		}
		System.out.println("Migracion de usuario " + usuario +" fallida");
		return false;
	}

	
	/**
	 * 	Método que devuelve la cantidad de bytes enviados por un usuario durante su sesión actual.
	 * @param usuario {@link String} 
	 * @throws RemoteException
	 * @return int
	 */
	public int getSessionBytesFromUser(String usuario) throws RemoteException
	{
		Session s = findSessionByUser(usuario);
		return (s == null) ? -1 : s.getBytes();
	}

	/**
	 * Método que devuelve la cantidad de bytes enviados por un usuario en toda su historia.
	 * Para obtenerla llama al método del servidor radius getTotalBytesFromUser.
	 * @param usuario {@link String} 
	 * @throws RemoteException
	 * @return int
	 */
	public int getTotalBytesFromUser(String usuario) throws RemoteException
	{
		return radius.getTotalBytesFromUser(usuario);
	}
	
	/**
	 * Método que invoca de forma remota un usuario para enviar tráfico.
	 * Se hace accounting del tráfico que envía anotándolo en el objeto de su
	 * sesión y notificando al servidor radius.
	 * @param pqt {@link Paquete} 
	 * @throws RemoteException
	 */
	public boolean recibePaquete(Paquete pqt) throws RemoteException
	{
		Session s = findSessionByIp(pqt.getIp());

		if (s != null)
		{
			if (pqt.getMAC().equals(s.getMAC()))
			{
				s.addBytes(pqt.getSize());
				radius.notifyBytesFromUser(s.getUsuario(), pqt.getSize());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Devuelve el objeto Session de un usuario.	
	 * @param usuario {@link String} 
	 * @return {@link Session}
	 */
	private Session findSessionByUser(String usuario)
	{
		for (Session ss : mySessionList)
			if (usuario.equals(ss.getUsuario()))
				return ss;
		return null;
	}

	/**
	 * Devuelve el objeto Session de una IP.	
	 * @param ip {@link String} 
	 * @return {@link Session}
	 */
	private Session findSessionByIp(String ip)
	{
		for (Session ss : mySessionList)
			if (ip.equals(ss.getIp()))
				return ss;
		return null;
	}
}
