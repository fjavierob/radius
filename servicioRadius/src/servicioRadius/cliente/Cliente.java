/**
 * 
 */
package servicioRadius.cliente;

import java.net.InetAddress;
import java.rmi.*;
import java.util.Scanner;

import servicioRadius.ap.APCliente;
import servicioRadius.ap.Paquete;

/**
 * Clase que se utiliza para modelar el cliente. Tiene la interfaz del
 * access point en el lado cliente y se conecta a el a traves de rmi.
 * @author Domingo Fernandez Piriz
 * @author Javier Ortiz Bonilla
 *
 */
class Cliente
{
	static public void main(String args[])
	{
		if (args.length!=3)
		{
			System.err.println("Uso: Cliente direccionMAC usuario contraseña ");
			return;
		}

		String dirMac = args[0];
		String usuario = args[1];
		String pass = args[2];

		boolean siguiente=true;
		String ip = null;
		InetAddress address=null;
		InetAddress myIp=null;

		int port;
		APCliente ap=null;


		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());

		try 
		{
			Scanner scanner = new Scanner(System.in);

			// Menu para controlar el comportamiento del cliente
			 
			while (siguiente) {
				System.out.println("");
				System.out.println("Introduzca la opcion que quiera realizar:");
				System.out.println("1) Conectar con AP");
				System.out.println("2) Desplazarse a otro ap");
				System.out.println("3) Enviar paquete");
				System.out.println("4) Consultar bytes enviados");
				System.out.println("5) Desconectar");
				System.out.println("6) Salir");

				int opcion = scanner.nextInt();
				System.out.println("La opcion introducida ha sido "+opcion);
				scanner.nextLine();
				System.out.println("");

				switch (opcion) {
				// conectar por primera vez con un AP
				case 1:
					if(ap==null)
					{
						System.out.print("Introduzca IP del AP: ");
						ip=scanner.nextLine();

						try {
							// inetAddress utilizado para validar que la ip introducida es valida
							address=InetAddress.getByName(ip);
							System.out.print("Introduzca numero de puerto del AP: ");
							
							port=scanner.nextInt();
							scanner.nextLine();

							if ( !(0<= port && port <= 65535)) {
								System.out.println("El numero de puerto introducido no es valido");
							}
							else
							{
								System.out.println("Buscar "+"//"+address.getHostAddress()+":"+port+"/APC");

								// se localiza el objeto remoto del ap
								
								ap = (APCliente) Naming.lookup("//"+address.getHostAddress()+":"+port+"/APC");
								if (ap != null)
								{
									// conectar con el AP para poder entrar en la red
									String tmpIp = ap.conectar(usuario, pass, dirMac);
									
									if (tmpIp != null)
									{
										System.out.println("Enlazado al AP con IP " + tmpIp);
										myIp=InetAddress.getByName(tmpIp);
									}
									else 
									{
										System.out.println("Conexion rechazada");
										ap=null;
									}
								}
								else
									System.out.println("No ha sido posible conectar con el AP");
							}
						} catch (UnknownHostException e) {
							System.out.println("La IP introducida no es valida");
						}
					}
					else {
						System.out.println("Ya se esta conectado a un AP");
					}
					break;



				// Ejecuta la migracion de un ap a otro. si esta conectado a un
				// ap se desconecta de el y establece conexion con el nuevo
				case 2:
					if (myIp==null) {
						System.out.println("No se esta conectado a ningun AP");
					}
					else 
					{
						// lee la ip
						System.out.print("Introduzca IP del nuevo AP: ");
						ip=scanner.nextLine();

						try {
							address=InetAddress.getByName(ip);
							System.out.print("Introduzca numero de puerto del nuevo AP: ");
							port=scanner.nextInt();
							scanner.nextLine();

							//lee el puerto
							if ( !(0<= port && port <= 65535)) {
								System.out.println("El numero de puerto introducido no es valido.");
							}
							else
							{
								APCliente tmpap;
								tmpap=(APCliente) Naming.lookup("//"+address.getHostAddress()+":"+port+"/APC");
								if (tmpap != null)
								{
									// conectar con el nuevo AP para poder entrar en la red
									boolean tmp = tmpap.migrar(usuario);
									if (tmp)
									{
										System.out.println("Enlazado al nuevo AP.");
										ap=tmpap;
									}
									else 
									{
										System.out.println("Migracion fallida.");
									}
								}
								else
								{
									System.out.println("No ha sido posible conectar con el nuevo AP.");
								}
							}
						} catch (UnknownHostException e) {
							System.out.println("La IP introducida no es valida");
						}
					}

					break;


				// Envia un nuevo paquete al ap. Simula trafico normal entre el cliente
				// e internet
				case 3:
					System.out.print("Introduzca tamaño del paquete:");
					int size=scanner.nextInt();
					scanner.nextLine();
					Paquete pqt;
					
					// Cada paquete tiene un limite, para traficos mayores se envian
					// varios paquetes
					for (int i = 1; i <= size/Paquete.MAX_TAM_PAQUETE; i++) 
					{
						pqt = new Paquete(Paquete.MAX_TAM_PAQUETE, myIp.getHostAddress(), dirMac);
						if(ap.recibePaquete(pqt))
							System.out.println("Paquete de tamaño "+pqt.getSize()+" enviado");
						else System.out.println("No se pudo enviar el paquete.");
					}
					pqt = new Paquete(size%Paquete.MAX_TAM_PAQUETE, myIp.getHostAddress(), dirMac);
					if(ap.recibePaquete(pqt))
						System.out.println("Paquete de tamaño "+pqt.getSize()+" enviado");
					else System.out.println("No se pudo enviar el paquete.");

					break;

				// consultar el consumo de bytes hasta el momento
				case 4:
					try 
					{
						int sBytes = ap.getSessionBytesFromUser(usuario);
						int tBytes = ap.getTotalBytesFromUser(usuario);
						System.out.println("Bytes enviados en esta sesion: " + sBytes);
						System.out.println("Bytes enviados en total: " + tBytes);
					}
					catch (RemoteException e) 
					{
						System.out.println("No se pudieron consultar los bytes enviados");
					}

					break;

				// desconecta al cliente del ap en el que se encuentra
				case 5:
					if(ap!=null)
					{
						try {
							ap.desconectar(usuario);
						}
						catch (RemoteException e) {}
						ap = null;
						System.out.println("Desconexion realizada");
					}
					else System.out.println("No esta conectado a ningun AP");
					
					break;

				// finaliza la ejecucion del cliente
				case 6:
					siguiente=false;
					if(ap!=null)
					{
						try {
							ap.desconectar(usuario);
						}
						catch (RemoteException e) {}
						ap = null;
					}
					System.out.println("Saliendo...");
					break;
				
				// si se introduce un numero de opcion erroneo
				default:
					System.out.println("Numero de opcion \""+opcion+"\" no valido");
					break;
				}

			}
			// se cierra el descriptor de entrada
			scanner.close();


		}
		catch (RemoteException e)
		{
			System.err.println("Cliente: Error  de  comunicacion:  "  + e.toString());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.err.println("Excepcion en Cliente: "  + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
