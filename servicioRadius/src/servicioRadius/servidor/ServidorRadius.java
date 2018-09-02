package servicioRadius.servidor;
 
import java.rmi.*;
import java.rmi.server.*;

import java.sql.*;


/**
 *  Clase con el main que crea un servicio radius y lo hace públicamente accesible.
 * @author Domingo Fernandez Piriz
 * @author Javier Ortiz Bonilla 
 */
class ServidorRadius
{
	static public void main (String args[]) 
	{
		if (args.length!=1) 
		{
			System.err.println("Uso: ServidorRadius puertoRegistro");
			return;
		}
		if (System.getSecurityManager() == null) 
		{
			System.setSecurityManager(new RMISecurityManager());
		}
		try 
		{
			ServicioRadiusImpl srv = new ServicioRadiusImpl();
			Naming.rebind("rmi://localhost:"+args[0]+"/Radius", srv);
		}
		catch (RemoteException e) 
		{
			System.err.println("Error  de  comunicacion:  "  + e.toString());
			System.exit(1);
		}
		catch (Exception e) 
		{
			System.err.println("Excepcion en ServidorRadius: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
}