import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import java.sql.*;
/**
 * Clase para facilitar la incluion y borrado de un usuario y su contrasena en
 * la base de datos.
 * @author Javier Ortiz Bonilla
 * @author Domingo Fernandez Piriz
 *
 */
class AnadirUsuarioBD
{
	public static void main(String[] args)
	{
		if (args.length!=3) 
		{
			System.err.println("Uso: AnadirUsuarioBd usuario contraseña tiempoEnSegundos");
			return;
		}

		try 
		{
			String usuario = args[0];
			String contraseña = args[1];
			int segundos = Integer.parseInt(args[2]);

			Calendar date = Calendar.getInstance();
			long t = date.getTimeInMillis();
			Date expira = new Date(t + (segundos * 1000));
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String expiraString = df.format(expira);

			Class.forName("org.sqlite.JDBC");
			Connection db = DriverManager.getConnection("jdbc:sqlite:radius.db");
			db.setAutoCommit(false);
			Statement stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios WHERE usuario = '" + usuario + "';");

			if (rs.next())
			{
				// Existe usuario
				stmt.executeUpdate(	"UPDATE usuarios " +
								  	"SET expira = '" + expiraString + "', " +
								  	"password = '" + contraseña + "' " +
								  	"WHERE usuario = '" + usuario + "';");
			}
			else
			{
				// Nuevo usuario
				stmt.executeUpdate(	"INSERT INTO usuarios (usuario, password, expira) VALUES " +
									"('" + usuario + "', '" + contraseña +"', '" + expiraString + "');");
			}
			db.commit();
			stmt.close();
			System.out.println("Añadido correctamente");

		}
		catch (NumberFormatException e)
		{
			System.err.println("Tiempo erroneo.");
			return;
		}
		catch (SQLException e)
		{
			System.err.println("Excepcion SQL: ");
			e.printStackTrace();
			return;
		}
		catch (Exception e)
		{
			System.err.println("Excepcion: ");
			e.printStackTrace();
			return;
		}
	}
}