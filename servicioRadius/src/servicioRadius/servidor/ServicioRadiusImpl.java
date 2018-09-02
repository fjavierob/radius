package servicioRadius.servidor;

import servicioRadius.ap.APRadius;

import java.util.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.concurrent.*;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.*;
 
class ServicioRadiusImpl extends UnicastRemoteObject implements ServicioRadius
{
 
	private static final int CHALLENGE_LENGTH = 12;
	private static final String ALPHABET = "QWERTYUIOPASDFGHJKLÑZXCVBNMqwertyuiopasdfghjklñzxcvbnm0123456789";
	private static final String IP_NET = "10.1.0.";
	private static final int MAX_USERS = 240;
	private static final int MAX_APS = 10;
	private static final String DATABASE = "radius.db";

	List<APRadius> apList;
	List<Session> sessionList;
	Map<String, String> challengeMap;
	boolean[] assignedIps;

	Connection db;

	ServicioRadiusImpl() throws RemoteException
	{
		apList = new LinkedList<APRadius>();
		sessionList = new LinkedList<Session>();
		challengeMap = new HashMap<String, String>();
		assignedIps = new boolean[MAX_USERS]; // default false

		db = null;
		// Cargamos la clase para gestionar la conexión con la base de datos
		// y nos conectamos a esta.
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (Exception e)
		{
			System.err.println("Excepcion en ServidorRadius: ");
			e.printStackTrace();
			System.exit(1);
		}

		// Hilo que cada segundo ejecutará la siguiente función para desconectar a los clientes
		// que hayan excedido su límite de tiempo. Para ello compara la fecha y hora actual con
		// la fecha y hora límite de cada usuario, las cuales obtiene de la base de datos. 
		Runnable comprobarTiempo = new Runnable() {
		    public void run() 
		    {
		    	try 
		    	{
		    		Connection c = DriverManager.getConnection("jdbc:sqlite:"+DATABASE);
		    		c.setAutoCommit(false);
		    		Statement stmt = c.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT usuario,expira FROM usuarios;");
					Date actualDate = new Date();
					while (rs.next())
					{
						String usuario = rs.getString("usuario");
						String expiraString = rs.getString("expira");
						if (expiraString != null)
						{
							Date expira = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(expiraString);
							if (expira.before(actualDate))
							{
								System.out.println("Ha expirado el tiempo para la sesion de " + usuario);
								Statement s = c.createStatement();
								s.executeUpdate("UPDATE usuarios SET expira = null WHERE usuario = '" + usuario + "'");
								c.commit();
								s.close();
								deleteSessionByUser(usuario, true); // true: Decir al AP que borre la sesión también.
							}
						}
		        	}
		        	rs.close();
		        	stmt.close();
		        	c.close();
		    	}
		    	catch (SQLException e)
		    	{
		    		System.err.println("Excepcion SQL en ServicioRadiusImpl: comprobarTiempo: ");
					e.printStackTrace();
					return;
		    	}
		    	catch (Exception e)
		    	{
		    		System.err.println("Excepcion en ServicioRadiusImpl: comprobarTiempo: ");
					e.printStackTrace();
					return;
		    	}
		    }
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(comprobarTiempo, 0, 1000, TimeUnit.MILLISECONDS);
	}

	// Función por la que se da de alta un ap. El servicio radius recibe su referencia y la guarda
	// en la lista de los AP. Es llamada por un APRadius cuando éste quiere darse de alta.
	// Con ésta referencia luego el servicio radius podrá ejecutar métodos remotos de un AP en concreto.
	public boolean altaAp(APRadius nuevoAp) throws RemoteException
	{
		int idAp = nuevoAp.getId();
		if (apList.size() == MAX_APS)
		{
			System.out.println("No puede añadirse el AP " + idAp + ": capacidad llena.");
			return false;
		}
		if (findApById(idAp) != null)
		{
			System.err.println("Se intenta añadir un AP cuyo id ya existe: " + idAp);
			return false;
		}
		apList.add(nuevoAp);
		System.out.println("Añadido ap " + idAp);
		return true;	
	}

	// Función por la que se da de baja un ap. El servicio radius recibe su referencia y la
	// elimina de su lista de AP's (en caso de que existiera, claro).
	public void bajaAp(APRadius ap) throws RemoteException
	{
		removeAp(ap);
		removeApSessions(ap);
	}

	// Método por el cual se solicita el acceso de un nuevo usuario. El servicio radius comprueba
	// que el usuario existe en la base de datos. En caso de existir, se envía un desafío (tal y 
	// como ocure en CHAP) que consiste en una cadena aleatoria. En caso de no existir el usuario, se
	// devuelve null. Es llamado de forma remota por un AP cuando un usuario intenta conectarse a él.
	public String accessRequest(APRadius ap, String usuario) throws RemoteException
	{
		Statement stmt = null;
		try
		{
			db = DriverManager.getConnection("jdbc:sqlite:"+DATABASE);
			db.setAutoCommit(false);
			stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT password FROM usuarios WHERE usuario = '"+usuario+"';");

			if (rs.next()) //Existe el usuario en la base de datos.
			{
				String pass = rs.getString("password");
				rs.close();
				stmt.close();
				db.close();
				String challenge = generateChallenge();
				// Resultado del desafío = MD5 (usuari0 + pass + challenge)
				String result = usuario + pass + challenge;
				try
				{
					MessageDigest mDigest=MessageDigest.getInstance("MD5");
					mDigest.reset();
					mDigest.update(result.getBytes());
					BigInteger bigInt = new BigInteger(1,mDigest.digest());
					result = bigInt.toString(32);
					// Guardamos el resultado para comparar cuando llegue la respuesta.
					challengeMap.put(usuario, result);
					return challenge;
				}
				catch (NoSuchAlgorithmException e) 
				{
            		System.err.println("accessRequest: Error con MD5" + e.toString());
        		}
			}
			rs.close();
			stmt.close();
			db.close();
			System.out.println("Usuario '"+usuario+"' no encontrado");
			return null;
		}
		catch (Exception e)
		{
			System.err.println("Excepcion en ServicioRadiusImpl: accessRequest: ");
			e.printStackTrace();
			return null;
		}
	}

	// Método que sirve para recibir la respuesta a un desafío que se había enviado previamente hacia un AP. Es, por lo tanto,
	// llamado de forma remota desde un AP.
	// El servidor radius recibe la respuesta y la compara con el resultado esperado. Si coinciden, se acepta al usuario
	// y se crea un nuevo objeto Session que es retornado al AP.
	public Session challengeResponse(APRadius ap, String dirMac, String usuario, String response) throws RemoteException
	{
		String result = (String) challengeMap.get(usuario);
		if (result != null && result.equals(response))
		{
			String dirIp = getNewIp();
			try
			{
				db = DriverManager.getConnection("jdbc:sqlite:"+DATABASE);
				db.setAutoCommit(false);
			    Statement stmt = db.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT expira FROM usuarios WHERE usuario = '"+usuario+"';");
				if (rs.next()) //El usuario existe en la base de datos.
				{
					// Comprobar que al usuario no se le agotó el tiempo 
					String expira = rs.getString("expira");
					rs.close();
					stmt.close();
					db.close();
					if (expira != null)
					{
						// Comprobar si el usuario estaba ya conectado
						Session session = findSessionByUser(usuario);
						if (session != null)
						{
							deleteSessionByUser(usuario, true);
						}
						session = new Session(usuario, dirMac, dirIp, ap);
						sessionList.add(session);
						System.out.println("Aceptado usuario " + usuario);
						return session;
					}
					else 
					{
						System.out.println("Rechazado usuario " + usuario + ": limite de tiempo alcanzado.");
						return null;
					}
				}
				rs.close();
				stmt.close();
				db.close();
			}
			catch (SQLException e)
			{
				System.err.println("Excepcion SQL en challengeResponse.");
				e.printStackTrace();
			}
		}
		System.out.println("Desafío para usuario " + usuario + " fallido.");
		return null;
	}

	// Método que sirve para eliminar la sesión de un usuario. Puede ser llamado localmente
	// o de forma remota por un AP. El segundo parámetro indica si quiere llamarse a este mismo
	// método en el AP al que está conectado el usuario en cuestión para eliminar allí su sesión
	// también. Será verdadero cuando se llame de forma local y falso si es el AP el que lo llama
	// de forma remota.
	public void deleteSessionByUser(String usuario, boolean tellAp) throws RemoteException
	{
		Session s = findSessionByUser(usuario);
		if (s == null)
		{
			System.err.println("Error en deleteSessionByUser: Sesion de " + usuario + " no encontrada.");
		}
		else
		{
			if (tellAp)
			{
				APRadius ap = s.getAp();
				try 
				{
					ap.deleteSessionByUser(usuario, false); // false: No me invoques de vuelta, ya lo he borrado.	
				}	
				catch (RemoteException e)
				{
					// El ap está desconectado, así que lo borramos de la lista.
					removeAp(ap);
				}	
			}
			String ip = s.getIp();
			releaseIp(ip);
			sessionList.remove(s);
			System.out.println("Eliminada sesion de "+ usuario);			
		}
	}

	// Método por el cual un AP notifica al servicio radius una cantidad de bytes que ha
	// enviado un usuario. Los bytes se van acumulando en la columna 'bytes' del usuario 
	// en cuestión en la base de datos.
	public void notifyBytesFromUser(String usuario, int nBytes) throws RemoteException
	{
		Session s = findSessionByUser(usuario);
		s.addBytes(nBytes);
		try
		{
			db = DriverManager.getConnection("jdbc:sqlite:"+DATABASE);
			db.setAutoCommit(false);	
			Statement stmt = db.createStatement();
			stmt.executeUpdate("UPDATE usuarios SET bytes = bytes+"+nBytes+" WHERE usuario = '" + usuario + "'");
			db.commit();
			stmt.close();
			db.close();
		}
		catch (SQLException e)
		{
			System.err.println("Exception SQL en notifyBytesFromUser.");
			e.printStackTrace();
		}
	}

	// Método para obtener la cantidad de bytes TOTALES que ha transmitido un usuario.
	// Para obtenerla se lee de la base de datos.
	public int getTotalBytesFromUser(String usuario) throws RemoteException
	{
		int bytes = -1;
		try
		{
			db = DriverManager.getConnection("jdbc:sqlite:"+DATABASE);
			db.setAutoCommit(false);
			Statement stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT bytes FROM usuarios WHERE usuario = '"+usuario+"';");
			if (rs.next())
				bytes = rs.getInt("bytes");
			rs.close();
			stmt.close();
			db.close();
		}
		catch (SQLException e)
		{
			System.err.println("Exception SQL en getTotalBytesFromUser.");
			e.printStackTrace();
		}
		return bytes;
	}

	// Método para realizar una migración. Un usuario que estaba conectado a un AP decide
	// moverse a otro. El servicio radius actualiza esta información en la sesión del usuario
	// y el objeto Session al AP al que el usuario migra. 
	// Este método es llamado remotamente por el AP al que el usuario quiere migrar.
	public Session moveUser(String usuario, APRadius nuevoAp) throws RemoteException
	{
		System.out.println("Migrando a usuario "+usuario+"...");
		Session s = findSessionByUser(usuario);
		if (s == null)
			System.err.println("Error en la migracion de "+ usuario + ": sesion no encontrada.");
		else
		{
			APRadius oldAp = s.getAp();
			try 
			{
				// Borrar la sesión del usuario en el AP antiguo.
				oldAp.deleteSessionByUser(usuario, false);
			}
			catch (RemoteException e) 
			{
				// Borramos el AP antiguo ya que no está conectado.
				removeAp(oldAp);
			}
			s.setAp(nuevoAp);
		}
		try 
		{
			System.out.println("Usuario "+usuario+" migrado a AP "+nuevoAp.getId());
		}
		catch (RemoteException e) {}
		
		return s;	
	}

	//
    /* Métodos auxiliares */
   	//

    // Borra un AP de la lista.
    private void removeAp(APRadius ap)
    {
    	int index = apList.indexOf(ap);
		if (index == -1) System.err.println("Se intenta borrar un AP que no existe.");
		else 
		{
			int apId = -1;
			try {
				apId = ap.getId();
			}
			catch (RemoteException e) {}
			apList.remove(apList.indexOf(ap));
			System.out.println("Eliminado AP " + (apId == -1 ? "" : apId));
		}
    }

    private void removeApSessions(APRadius ap)
    {
    	for (Session ss : sessionList)
		{
			if (ap.equals(ss.getAp()))
			{
				sessionList.remove(ss);
				releaseIp(ss.getIp());
				System.out.println("Borrada sesion de "+ss.getUsuario());
			}
		}
    }

    // Genera una cadena aleatoria.
	private String generateChallenge()
	{
		Random rand = new Random();
		char[] challenge = new char[CHALLENGE_LENGTH];
		for (int i=0; i<CHALLENGE_LENGTH; i++)
			challenge[i] = ALPHABET.charAt(rand.nextInt(ALPHABET.length()));
		return new String(challenge);
	}

	// Obtiene una IP que esté disponible.
	// Devuelve null en caso de no haber IP disponibles.
	private String getNewIp()
	{
		for (int i=0; i<MAX_USERS; i++)
			if (!assignedIps[i])
			{
				assignedIps[i] = true;
				int ipHost = i+2+MAX_APS;
				String ip = IP_NET + ipHost;
				return ip;
			}
		return null;	
	}

	// Libera una IP que estaba siendo usada.
	private void releaseIp(String ip)
	{
		String[] parts = ip.split("[.]");
		if (parts.length < 4)
		{
			System.err.println("ServicioRadiusImpl: releaseIp: IP '" + ip + "' no validax. ");
			return;
		}
		int i = Integer.parseInt(parts[3])-2-MAX_APS;
		if (i < MAX_USERS || i >= 0)
			assignedIps[i] = false;
		else System.err.println("ServicioRadiusImpl: releaseIp: IP '" + ip + "' no valida.");
	}

	// Encuentra y devuelve la sesión del usuario que se pasa como parámetro.
	// Devuelve null en caso de no existir.
	private Session findSessionByUser(String usuario)
	{
		for (Session ss : sessionList)
			if (usuario.equals(ss.getUsuario()))
				return ss;
		return null;
	}

	// Encuentra y devuelve la referencia de un AP por su id.
	// Devuelve null en caso de no existir.
	private APRadius findApById(int id)
	{
		for (APRadius ap: apList)
		{
			try 
			{
				int apId = ap.getId();
				if (apId == id)
					return ap;
			}
			catch (RemoteException e)
			{
				// El ap está desconectado, así que lo borramos de la lista.
				removeAp(ap);
			}
		}
		return null;	
	}

}