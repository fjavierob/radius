# Servicio Radius con Java RMI

Sistemas distribuidos y servicios web.

Grado en Ingeniería de las Tecnologías de Telecomunicación.

Universidad de Sevilla.


## Resumen

La finalidad de este proyecto es implementar un servicio RADIUS de modo que un AP al que se conectan clientes pregunte a un servidor RADIUS, mediante invocación de métodos remotos, si el cliente puede entrar o no. Así mismo, el cliente debe implementar métodos que puedan ser llamados de forma remota por el servidor RADIUS para que este pueda realizar operaciones como desconectar a un usuario.

Existen, pues, tres entidades fundamentales en este escenario:

1. Los usuarios.
2. Los puntos de acceso (AP).
3. El servidor RADIUS.

En la realidad un usuario se intenta conectar al AP que tenga más cerca y este le pide sus credenciales (usuario y contraseña). El AP entonces manda estas credenciales al servidor RADIUS, y este, suponiendo que se está usando el protocolo CHAP, envía un desafío de vuelta. El desafío consiste en una cadena aleatoria a la que el AP debe responder haciendo un hash de una nueva cadena que consiste en el nombre de usuario, la contraseña y el desafío concatenados. De esta forma la contraseña no viaja en texto plano por la red ni su hash tampoco. Si la respuesta al desafío es correcta (lo será si la contraseña lo es) el servidor RADIUS responde con un ACCEPT, y en caso contrario, con un REJECT.

## Implementación

Consultar el fichero **memoria.pdf** 

## Autores

* **Francisco Javier  Ortiz Bonilla** - [Pogorelich](https://github.com/pogorelich)
* **Domingo Fernández Piriz**
