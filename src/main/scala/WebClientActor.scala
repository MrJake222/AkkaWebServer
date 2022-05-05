package httpqueens

import akka.actor.{Actor, ActorLogging}

import java.io.{FileInputStream, FileNotFoundException}
import java.net.Socket

case class Connection(cSock: Socket, staticPath: String, routes: Map[String, (ConnParams) => RouteData])
case class ConnParams(method: String, path: String, params: Map[String, String])

class WebClientActor extends Actor with ActorLogging {

  private val MIME_MAP: Map[String, String] = Map(
    "html" -> "text/html",
    "css" -> "text/css",
    "png" -> "image/png",
    "jpg" -> "image/jpeg",
    "jpeg" -> "image/jpeg",
    "js" -> "text/javascript"
  )

  override def receive: Actor.Receive = {
    case Connection(sock, staticPath, routes) =>
      log.debug("Got connection from " + sock.getInetAddress)

      val p = parseArgs(sock)

      log.info(p.method + " " +  p.path)
      log.debug(p.params.toString())

      // dump rest
      var line: String = ""
      do {
        line = readLine(sock)
      } while (line != "")

      if (routes.contains(p.path)) {
        val routeData = routes(p.path)(p)
        sendContent(sock, routeData.mime, routeData.body)
        closeConnection(sock)
      }
      else {
        val staticResourcePath = staticPath + p.path
        log.debug("reading: " + staticResourcePath)

        try {
          sendFile(sock, staticResourcePath)
        } catch {
          case _: FileNotFoundException =>
            log.warning("file not found: " + staticResourcePath + ", looking in: " + System.getProperty("user.dir"))
            sendFileNotFound(sock)
        } finally {
          closeConnection(sock)
        }
      }
  }

  private def sendContent(sock: Socket, mime: String, content: String): Unit = {
    write(sock, "HTTP/1.0 200 OK\r\n")
    write(sock, "Content-type: "+mime+"\r\n")
    write(sock, "\r\n")
    write(sock, content)
    write(sock, "\r\n")
    write(sock, "\r\n")
  }

  private def sendFileNotFound(sock: Socket): Unit = {
    write(sock, "HTTP/1.0 404 File Not Found\r\n")
    write(sock, "Content-type: text/plain\r\n")
    write(sock, "\r\n")
    write(sock, "file or route not found")
    write(sock, "\r\n")
    write(sock, "\r\n")
  }

  private def sendFile(sock: Socket, path: String): Unit = {

    var inFile: FileInputStream = null
    try {
      inFile = new FileInputStream(path)
      val extension = path.split("\\.").last
      val mime = if (MIME_MAP.contains(extension)) MIME_MAP(extension) else "text/plain"

      write(sock, "HTTP/1.0 200 OK\r\n")
      write(sock, "Content-type: "+mime+"\r\n")
      write(sock, "\r\n")

      var c = 0
      while ({c = inFile.read(); c != -1}) {
        sock.getOutputStream.write(c)
      }

      write(sock, "\r\n")
    }
    finally {
      if (inFile != null) inFile.close()
    }
  }

  private def parseArgs(sock: Socket): ConnParams = {
    val request = readLine(sock).split(" ")
    val method = request(0)

    val pathParams = request(1).split("\\?")
    val pathOrig = pathParams(0)

    val path = if (pathOrig == "/") "/index.html" else pathOrig
    val params = if (pathParams.length > 1) pathParams(1).split("&") else Array.empty[String]

    var pMap = Map.empty[String, String]
    for (param <- params) {
      val a = param.split("=")
      val key = a(0)
      val value = if (a.length > 1) a(1) else "1"
      pMap += (key -> value)
    }

    ConnParams(method, path, pMap)
  }

  private def readLine(sock: Socket): String = {
    var s: Int = 0
    var line: String = ""
    do {
      s = sock.getInputStream.read()
      if (s != '\r') line += s.toChar
    } while (s != '\r')
    sock.getInputStream.read() // read '\n'
    line
  }

  private def write(sock: Socket, str: String): Unit = {
    for (c <- str) {
      sock.getOutputStream.write(c.toInt)
    }
  }

  private def closeConnection(sock: Socket): Unit = {
    sock.close()
  }
}
