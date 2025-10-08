import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RPCClient {
    public static void main(String[] args) {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            URI uri = new URI("http://10.211.251.237:8080/");
            config.setServerURL(uri.toURL());

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            String nombreCliente = (String) client.execute("Contador.registrarCliente", new Object[]{});
            System.out.println("Cliente registrado: " + nombreCliente);

            int[] registroContador = new int[100];
            int indice = 0;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter = enviar petición | q = salir");

            while (true) {
                System.out.print("> ");
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.equalsIgnoreCase("q")) break;
                if (!line.isEmpty()) {
                    System.out.println("Presiona Enter sin texto para solicitar incremento.");
                    continue;
                }

                try {
                    Object[] params = new Object[]{nombreCliente};
                    Integer resultado = (Integer) client.execute("Contador.incrementar", params);

                    // Si el servidor ignora la petición (mismo turno)
                    if (resultado == -1) {
                        System.out.println("Servidor ignoró la petición por doble turno. Intenta nuevamente cuando otro cliente haya llamado.");
                        continue; // no guardes ni muestres el -1
                    }

                    if (indice < registroContador.length) registroContador[indice++] = resultado;
                    System.out.println(nombreCliente + " recibió contador: " + resultado);

                    if (resultado >= 100) {
                        System.out.println("Contador llegó a 100. Fin de comunicación.");
                        break;
                    }
                } catch (Exception e) {
                    String mensaje = e.getMessage();
                    if (mensaje != null && mensaje.contains("Petición ignorada")) {
                        System.out.println("Servidor ignoró la petición por doble turno. Intenta nuevamente cuando otro cliente haya llamado.");
                        continue;
                    }
                    if (mensaje != null && mensaje.contains("Servidor dejará de aceptar peticiones")) {
                        System.out.println("Servidor alcanzó 100. Fin de comunicación.");
                    } else {
                        System.out.println("Error al comunicarse con el servidor: " + mensaje);
                    }
                    break;
                }
            }

            System.out.println("\nValores recibidos por " + nombreCliente + ":");
            for (int i = 0; i < indice; i++) System.out.print(registroContador[i] + " ");
            System.out.println();
        } catch (Exception e) {
            System.err.println("Error cliente: " + e.getMessage());
        }
    }
}