# Servicio Radius con Java RMI

Sistemas distribuidos y servicios web.

Grado en Ingenier�a de las Tecnolog�as de Telecomunicaci�n.

Universidad de Sevilla.


## Resumen

La finalidad de este proyecto es implementar un servicio RADIUS de modo que un AP al que se conectan clientes pregunte a un servidor RADIUS, mediante invocaci�n de m�todos remotos, si el cliente puede entrar o no. As� mismo, el cliente debe implementar m�todos que puedan ser llamados de forma remota por el servidor RADIUS para que este pueda realizar operaciones como desconectar a un usuario.

Existen, pues, tres entidades fundamentales en este escenario:

1. Los usuarios.
2. Los puntos de acceso (AP).
3. El servidor RADIUS.

En la realidad un usuario se intenta conectar al AP que tenga m�s cerca y este le pide sus credenciales (usuario y contrase�a). El AP entonces manda estas credenciales al servidor RADIUS, y este, suponiendo que se est� usando el protocolo CHAP, env�a un desaf�o de vuelta. El desaf�o consiste en una cadena aleatoria a la que el AP debe responder haciendo un hash de una nueva cadena que consiste en el nombre de usuario, la contrase�a y el desaf�o concatenados. De esta forma la contrase�a no viaja en texto plano por la red ni su hash tampoco. Si la respuesta al desaf�o es correcta (lo ser� si la contrase�a lo es) el servidor RADIUS responde con un ACCEPT, y en caso contrario, con un REJECT.

## Implementaci�n

Consultar el fichero **memoria.pdf** 

## Autores

**Francisco Javier  Ortiz Bonilla** - [Pogorelich](https://github.com/pogorelich)
**Domingo Fern�ndez Piriz**