import org.apache.xmlrpc.client.XmlRpcClient;   // Importa la clase que permite crear un cliente XML-RPC para enviar peticiones al servidor
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl; // Importa la clase para configurar las opciones del cliente XML-RPC, como la URL del servidor

import java.net.URI;      // Importa la clase URI que representa un identificador uniforme de recursos, útil para definir la dirección del servidor
import java.net.URL;   // Importa la clase URL que representa la dirección a la cual el cliente se conectará
//import java.util.Arrays;    // Importa la clase Arrays para trabajar con arreglos, útil para pasar parámetros o manipular datos


public class RPCClient {
     public static void main(String[] args) {
        try {
            
            // Configurar la URL del servidor
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            URI uri = new URI("http://192.168.1.68:8080/");   // Valida la URI
            config.setServerURL(uri.toURL());              // Convierte a URL
            
            // Crear cliente XML-RPC
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // Registrar un nuevo cliente y obtener su nombre único
            String nombreCliente = (String) client.execute("Contador.registrarCliente", new Object[]{});
            System.out.println("Cliente registrado con nombre: " + nombreCliente);

            // Crear registro para guardar los valores del contador
            int[] registro_contador = new int[100];
            int indice = 0;

            while (true) {
                try {
                    Object[] params = new Object[]{nombreCliente};
                    Integer resultado = (Integer) client.execute("Contador.incrementar", params);

                    // Guardar valor en el registro
                    if (indice < registro_contador.length) {
                        registro_contador[indice++] = resultado;
                    }

                    System.out.println(nombreCliente + " recibió contador: " + resultado);

                    // Si el contador llegó a 100, detener el ciclo
                    if (resultado >= 100) {
                        System.out.println("Contador llegó a 100. Fin de comunicación.");
                        break;
                    }

                    Thread.sleep(7000); // Espera
                } catch (Exception e) {
                    // Capturamos excepciones lanzadas por el servidor
                    String mensaje = e.getMessage();
                    
                    if (mensaje != null && mensaje.contains("Petición ignorada")) {
                        // Solo ignorar la petición, esperar antes de intentar de nuevo
                        System.out.println("Servidor ignoró la petición por doble llamada consecutiva. Reintentando...");
                        Thread.sleep(2000); // Espera breve antes de reintentar
                        continue; // Saltar a la siguiente iteración
                    }

                    if (mensaje != null && mensaje.contains("Servidor dejará de aceptar peticiones")) {
                        System.out.println("Servidor alcanzó límite de 100. Fin de comunicación.");
                    } else {
                        System.out.println("Error al comunicarse con el servidor: " + mensaje);
                    }

                    break; // Salir del ciclo si hay error crítico
                }
            }

            // Imprimir los valores registrados al final
            System.out.println("\nValores recibidos por " + nombreCliente + ":");
            for (int i = 0; i < indice; i++) {
                System.out.print(registro_contador[i] + " ");
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error cliente: " + e.getMessage());
        }
    }
}
