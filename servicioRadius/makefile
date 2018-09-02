

# make run FILE="Algo.csv" para sobre escribir el valor de FILE.

JFLAGS = -cp ./bin/servicioRadius -sourcepath ./src -d ./bin
JC = javac
RUN_FLAGS = -cp ./bin -Djava.security.policy=permisos
RUN_FLAGS_SERVER = -cp ./bin:./lib/sqlite-jdbc-3.16.1.jar -Djava.security.policy=permisos
JVM = java
FILE =

# Argumentos para main en clase servicioRadius.ap.MyAccessPoint
#	servicioRadius.ap.MyAccessPoint  puertoAPCliente hostRADIUS puertoRADIUS apID
ARGS_AP1 = 56544 localhost 54599 1
ARGS_AP2 = 56533 localhost 54599 2

# Argumentos para main en clase servicioRadius.servidor.ServidorRadius
# 	servicioRadius.servidor.ServidorRadius puertoRegistro
ARGS_SERVIDOR = 54599

# Argumentos para main en clase servicioRadius.cliente.Cliente
#	Cliente direccionMAC usuario contraseña
# Usuarios que pueden entrar
ARGS_CLIENTE1 = 0e:15:c2:e4:10:ce domin guero
ARGS_CLIENTE2 = 0e:15:c3:e5:11:ce javi 1234
# Usuario con límite de tiempo agotado
ARGS_CLIENTE3 = 0e:15:c4:e6:12:ce foo bar
# Usuario que no existe
ARGS_CLIENTE4 = 0e:15:c5:e7:13:ce no existe

# Usuario prueba
ARGS_CLIENTE5 = 0e:15:c5:e7:13:ce prueba 1234

# Añadir usuario en BD con 60 segundos
ARGS_ADD_USER = prueba 1234 60



#
# Here is our target entry for creating .class files from .java files
# This is a target entry that uses the suffix rule syntax:
#	DSTS:
#		rule
# DSTS (Dependency Suffix     Target Suffix)
# 'TS' is the suffix of the target file, 'DS' is the suffix of the dependency
#  file, and 'rule'  is the rule for building a target
# '$*' is a built-in macro that gets the basename of the current target
# Remember that there must be a < tab > before the command line ('rule')
#

default:
	cat README


#
# CLASSES is a macro consisting of N words (one for each java source file)
# When a single line is too long, use \<return> to split lines that then will be
# considered as a single line. For example:
# NAME = Camilo \
         Juan
# is understood as
# NAME = Camilo        Juan


bin/servicioRadius/ap/APCliente.class: src/servicioRadius/ap/APCliente.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/APCliente.java
bin/servicioRadius/ap/APRadius.class: src/servicioRadius/ap/APRadius.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/APRadius.java
bin/servicioRadius/ap/AccessPoint.class: src/servicioRadius/ap/AccessPoint.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/AccessPoint.java
bin/servicioRadius/ap/APClienteImpl.class: src/servicioRadius/ap/APClienteImpl.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/APClienteImpl.java
bin/servicioRadius/ap/APRadiusImpl.class: src/servicioRadius/ap/APRadiusImpl.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/APRadiusImpl.java
bin/servicioRadius/ap/MyAccessPoint.class: src/servicioRadius/ap/MyAccessPoint.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/MyAccessPoint.java
bin/servicioRadius/ap/Paquete.class: src/servicioRadius/ap/Paquete.java
	$(JC) $(JFLAGS) src/servicioRadius/ap/Paquete.java


bin/servicioRadius/cliente/Cliente.class: src/servicioRadius/cliente/Cliente.java
	$(JC) $(JFLAGS) src/servicioRadius/cliente/Cliente.java


bin/servicioRadius/servidor/ServicioRadius.class: src/servicioRadius/servidor/ServicioRadius.java
	 $(JC) -cp ./bin:./lib/sqlite-jdbc-3.16.1.jar -sourcepath ./src -d ./bin src/servicioRadius/servidor/ServicioRadius.java
bin/servicioRadius/servidor/ServicioRadiusImpl.class: src/servicioRadius/servidor/ServicioRadiusImpl.java
	 $(JC) -cp ./bin:./lib/sqlite-jdbc-3.16.1.jar -sourcepath ./src -d ./bin src/servicioRadius/servidor/ServicioRadiusImpl.java
bin/servicioRadius/servidor/ServidorRadius.class: src/servicioRadius/servidor/ServidorRadius.java
	$(JC) -cp ./bin:./lib/sqlite-jdbc-3.16.1.jar -sourcepath ./src -d ./bin src/servicioRadius/servidor/ServidorRadius.java
bin/servicioRadius/servidor/Session.class: src/servicioRadius/servidor/Session.java
	 $(JC) $(JFLAGS) src/servicioRadius/servidor/Session.java

bin/AnadirUsuarioBD.class: src/AnadirUsuarioBD.java
	$(JC) -cp ./lib/sqlite-jdbc-3.16.1.jar -sourcepath ./src -d ./bin src/AnadirUsuarioBD.java

CLASSES_AP = \
		bin/servicioRadius/ap/APCliente.class \
		bin/servicioRadius/ap/APRadius.class \
		bin/servicioRadius/ap/AccessPoint.class \
		bin/servicioRadius/ap/APClienteImpl.class \
		bin/servicioRadius/ap/APRadiusImpl.class \
		bin/servicioRadius/ap/MyAccessPoint.class \
		bin/servicioRadius/ap/Paquete.class


CLASSES_CLIENTE = bin/servicioRadius/cliente/Cliente.class

CLASSES_SERVIDOR = \
		bin/servicioRadius/servidor/ServicioRadius.class \
		bin/servicioRadius/servidor/ServicioRadiusImpl.class \
		bin/servicioRadius/servidor/ServidorRadius.class \
		bin/servicioRadius/servidor/Session.class

#
# MAIN is a variable with the name of the file containing the main method
#

MAIN_CLIENTE = servicioRadius/cliente/Cliente
MAIN_AP = servicioRadius/ap/MyAccessPoint
MAIN_SERVIDOR = servicioRadius/servidor/ServidorRadius

#
# the default make target entry
# for this example it is the target classes

compile: classes_ap classes_cliente classes_servidor class_addUser


# Next line is a target dependency line
# This target entry uses Suffix Replacement within a macro:
# $(macroname:string1=string2)
# In the words in the macro named 'macroname' replace 'string1' with 'string2'
# Below we are replacing the suffix .java of all words in the macro CLASSES
# with the .class suffix
#

classes_ap: $(CLASSES_AP)

classes_cliente: $(CLASSES_CLIENTE)

classes_servidor: $(CLASSES_SERVIDOR)

class_addUser: ./bin/AnadirUsuarioBD.class

# Next two lines contain a target for running the program
# Remember the tab in the second line.
# $(JMV) y $(MAIN) are replaced by their values

# Añadir usuario prueba con 60s de tiempo

add_prueba: bin/AnadirUsuarioBD.class
	$(JVM) -cp ./bin:./lib/sqlite-jdbc-3.16.1.jar AnadirUsuarioBD $(ARGS_ADD_USER)


# Run servidor
run_servidor: classes_servidor
	$(JVM) $(RUN_FLAGS_SERVER) $(MAIN_SERVIDOR) $(ARGS_SERVIDOR)

# Run aps
run_ap1: classes_ap
	$(JVM) $(RUN_FLAGS) $(MAIN_AP) $(ARGS_AP1)

run_ap2: classes_ap
	$(JVM) $(RUN_FLAGS) $(MAIN_AP) $(ARGS_AP2)

# Run clientes
run_cliente1: classes_cliente
	$(JVM) $(RUN_FLAGS) $(MAIN_CLIENTE) $(ARGS_CLIENTE1)

run_cliente2: classes_cliente
	$(JVM) $(RUN_FLAGS) $(MAIN_CLIENTE) $(ARGS_CLIENTE2)

run_cliente3: classes_cliente
	$(JVM) $(RUN_FLAGS) $(MAIN_CLIENTE) $(ARGS_CLIENTE3)

run_cliente4: classes_cliente
	$(JVM) $(RUN_FLAGS) $(MAIN_CLIENTE) $(ARGS_CLIENTE4)

run_cliente5: classes_cliente
	$(JVM) $(RUN_FLAGS) $(MAIN_CLIENTE) $(ARGS_CLIENTE5)

# this line is to remove all unneeded files from
# the directory when we are finished executing(saves space)
# and "cleans up" the directory of unneeded .class files
# RM is a predefined macro in make (RM = rm -f)
#

clean:
	$(RM) -r ./bin/*


