//RPCServer.java
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import java.util.*;

public class RPCServer {
    public static void main(String[] args) {
        try {
            int puerto = 8080;
            WebServer webServer = new WebServer(puerto);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("Contador", Contador.class);
            xmlRpcServer.setHandlerMapping(phm);

            webServer.start();
            System.out.println("Servidor XML-RPC en " + puerto);

            while (true) {
                if (Contador.debeDetenerServidor()) {
                    // Imprime resumen completo al finalizar, incluso si hubo desconexiones
                    System.out.println("\n=== Registro global (orden cronológico) ===");
                    for (String s : Contador.snapshotRegistroGlobal()) System.out.println(s);

                    System.out.println("\n=== Historial por cliente ===");
                    for (Map.Entry<String, List<Integer>> e : Contador.snapshotHistorialPorCliente().entrySet()) {
                        System.out.println(e.getKey() + ": " + e.getValue());
                    }

                    webServer.shutdown();
                    System.out.println("\nServidor detenido (máximo alcanzado).");
                    break;
                }
                Thread.sleep(300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
import java.util.List;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class RPCServer {
    public static void main(String[] args) {
        try {
            int puerto = 8080;
            WebServer webServer = new WebServer(puerto);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("Contador", Contador.class);
            xmlRpcServer.setHandlerMapping(phm);
            webServer.start();
            System.out.println("Servidor XML-RPC en " + puerto);

            while (true) {
                if (new Contador().debeDetenerServidor()) {
                    List<String> reg = new Contador().obtenerRegistroGlobal();
                    System.out.println("\nRegistro:");
                    for (String s : reg) System.out.println(s);
                    webServer.shutdown();
                    System.out.println("Servidor detenido al llegar a 100.");
                    break;
                }
                Thread.sleep(500);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}


import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico

public class RPCServer {

    public static void main(String[] args) {
        
        try {
            int puerto = 8080;
            System.out.println("Iniciando servidor en el puerto " + puerto + "...");

            WebServer webServer = new WebServer(puerto); // Crear una instancia del servidor web en el puerto especificado
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer(); // Obtener el servidor XML-RPC del servidor web
            
            PropertyHandlerMapping phm = new PropertyHandlerMapping(); // Crear un mapeo de propiedades para registrar los manejadores de servicios
            phm.addHandler("Contador", Contador.class); // Registrar la clase Contador con el nombre "Contador"
            xmlRpcServer.setHandlerMapping(phm); // Asignar el mapeo de propiedades al servidor XML-RPC
            
            webServer.start(); // Iniciar el servidor web para que escuche peticiones
            
            System.out.println("Servidor XML-RPC escuchando en el puerto" + puerto +"...");

            // Monitorear para detener el servidor
            while (true) {
                if (Contador.debeDetenerServidor()) {
                    // Imprimir registro de todas las llamadas antes de detener
                    List<String> registro = Contador.obtenerRegistro();
                    System.out.println("\nRegistro de llamadas");
                    for (String linea : registro) {
                        System.out.println(linea);
                    }
                    System.out.println("\n");

                    webServer.shutdown(); // Detener el servidor web
                    System.out.println("Servidor detenido automáticamente al llegar a 100.");
                    break;
                }
                Thread.sleep(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
*/