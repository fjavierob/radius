package servicioRadius.ap;
 
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
 
import servicioRadius.servidor.*;

/* 
 * Clase que contiene el main y crea un nuevo objeto AccessPoint.
 * Hace públicamente accesible el lado del cliente para que un cliente 
 * pueda conectarse a él. No es necesario esto para el lado del servidor radius
 * ya que este obtiene previamente su referencia al interactuar el AP con él 
 * para darse de alta.
 *
 */

class MyAccessPoint
{
	static int myId;
	static List<Session> mySessionList;
	static ServicioRadius radius;

	static public void main (String args[])
	{

		if (args.length!=4)
		{
			System.err.println("Uso: MyAccessPoint puertoAPCliente hostRADIUS puertoRADIUS apID");
			return;
		}

		myId = Integer.parseInt(args[3]);

		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());

		try 
		{
			/* Localizar servicio radius */
			radius = (ServicioRadius) Naming.lookup("//"+args[1]+":"+args[2]+"/Radius");
			if (radius != null)
			{
				AccessPoint ap=new AccessPoint(radius, myId);
				
				System.out.println("Alta AP");
				if(!radius.altaAp(ap.getApRadius()))
					System.err.println("Error en el alta");
				else
				{
					/* Rebind servicio AP Cliente */
					System.out.println("Rebind MyAccessPoint lado cliente: " + "rmi://localhost:"+args[0]+"/APC");
					Naming.rebind("rmi://localhost:"+args[0]+"/APC", ap.getApCliente());
					
					/* Darse de baja y salir */
					Thread.sleep(90*1000);
					System.out.println("Baja AP");

					radius.bajaAp(ap.getApRadius());
				}
				System.exit(0);
			}
		}
		catch (RemoteException e)
		{
			System.err.println("MyAccessPoint: Error  de  comunicacion:  "  + e.toString());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.err.println("Excepcion en MyAccessPoint: "  + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
}