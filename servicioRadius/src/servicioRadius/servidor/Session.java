package servicioRadius.servidor;

import servicioRadius.ap.APRadius;

import java.io.*;
import java.util.Date;


/**
 * Almacena la información relativa a la sesión de un cliente conectado.
 * @author Domingo Fernandez Piriz
 * @author Javier Ortiz Bonilla
 *
 */

public class Session implements Serializable 
{
	private String usuario;
	private String dirMAC;
	private String dirIp;
	private APRadius ap;
	private int bytesTx;

	/**
	 * Constructor de la clase
	 * @param u {@link String}
	 * @param dMAC {@link String}
	 * @param dIP {@link String}
	 * @param ap {@link APRadius}
	 */
	Session(String u, String dMAC, String dIP, APRadius ap)
	{ 
		usuario = u;
		dirMAC = dMAC;
		dirIp = dIP;
		this.ap = ap;
		bytesTx = 0;
	}

	/**
	 * Devuelve el usuario de la sesion
	 * @return {@link String}
	 */
	public String getUsuario() 
	{
		return usuario;
	}

	/**
	 * Metodo para llevar la contabilidad de cosumo de datos
	 * @param b int
	 */
	public void addBytes(int b)
	{
		bytesTx+=b;
	}

	/**
	 * Devuelve la MAC del dispositivo asociado a esta sesion
	 * @return {@link String}
	 */
	public String getMAC()
	{
		return dirMAC;
	}

	/**
	 * Devuelve la IP del dispositivo asociado a esta sesion
	 * @return {@link String}
	 */
	public String getIp()
	{
		return dirIp;
	}

	/**
	 * Devuelve el AP en el que esta vinculado el dispositivo de la sesion
	 * @return {@link String}
	 */
	public APRadius getAp()
	{
		return ap;
	}

	/**
	 * Devuelve la cantidad de bytes consumidos durante la sesion
	 * @return int
	 */
	public int getBytes()
	{
		return bytesTx;
	}

	/**
	 * Establece el AP en el que esta vinculado el dispositivo de la sesion.
	 * @param ap {@link APRadius}
	 */
	public void setAp(APRadius ap)
	{
		this.ap = ap;
	}
}
