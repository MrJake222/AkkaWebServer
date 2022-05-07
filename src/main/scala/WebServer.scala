package httpqueens

import akka.actor.{ActorSystem, Props}

import java.net.{ServerSocket, Socket}

case class RouteData(mime: String, body: String)

class WebServer(val port: Int, val system: ActorSystem, val staticPath: String) {

  private var routes: Map[String, (ConnParams) => RouteData] = Map.empty[String, (ConnParams) => RouteData]

  def listen(): Unit = {
    val sock: ServerSocket = new ServerSocket(port)
    println("Listening on port " + port)

    while (true) {
      val cSock: Socket = sock.accept()
      val actorName = getActorName(cSock)

      val clientActor = system.actorOf(Props[WebClientActor], name = actorName)
      clientActor ! Connection(cSock, staticPath, routes)
    }
  }

  def addRoute(route: String, value: (ConnParams) => RouteData): Unit = {
    routes += (route -> value)
  }

  private def getActorName(sock: Socket): String = {
    val addr = sock.getInetAddress.getHostAddress.replace(".", "_")
    val port = sock.getPort
    addr + "_" + port
  }
}
