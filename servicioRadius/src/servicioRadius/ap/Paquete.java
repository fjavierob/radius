/**
 * 
 */
package servicioRadius.ap;

import java.io.Serializable;

/**
 * Clase que representa un paquete de tamano 1500 octetos, o menor.
 * Tiene un UID unico
 *  @author Javier Ortiz Bonilla
 *  @author Domingo Fernandez Piriz
 *
 */
public class Paquete implements Serializable 
{

	private int size;
	private String ip;
	private String mac;

	private static final long serialVersionUID = -6488762938404668022L;
	
	public final static int MAX_TAM_PAQUETE = 1500;
	
	/**
	 * Constructor de la clase
	 * @param  tam int
	 * @param ip {@link String}
	 * @param mac {@link String}
	 */
	public Paquete(int tam, String ip, String mac)
	{
		size = tam;
		this.ip = ip;
		this.mac = mac;
	}

	/**
	 * Devuelve el tamano del paquete
	 * @return int
	 */
	public synchronized int getSize() {
		return size;
	}

	/**
	 * Devuelve la IP del paquete
	 * @return {@link String}
	 */
	public synchronized String getIp() {
		return ip;
	}

	/**
	 * Devuelve la MAC del paquete
	 * @return {@link String}
	 */
	public synchronized String getMAC() {
		return mac;
	}
	
}
