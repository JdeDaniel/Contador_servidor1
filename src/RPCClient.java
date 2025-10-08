import org.apache.xmlrpc.client.XmlRpcClient; 
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl; 
import java.net.URI;
import java.util.List;
import java.io.*; 
public class RPCClient { 
    private static final String ID_FILE = "idCliente.txt"; 
    public static void main(String[] args) { 
        try { 
            // Configurar conexión 
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl(); 
            URI uri = new URI("http://localhost:8080/"); 
            config.setServerURL(uri.toURL()); 
            XmlRpcClient client = new XmlRpcClient(); 
            client.setConfig(config); 

            // Cargar o generar ID único del cliente 
            String idCliente = cargarIdCliente(); 
            System.out.println("ID local del cliente: " + idCliente); 

            // Registrar cliente (nuevo o reconectado) 
            Object[] paramsRegistro = new Object[]{idCliente}; 
            String nombreCliente = (String) client.execute("Contador.registrarCliente", paramsRegistro); 
            System.out.println("Cliente registrado/reconectado en servidor como: " + nombreCliente); 
            
            // Preparar para registrar valores recibidos 
            int[] registroContador = new int[10]; 
            int indice = 0; 
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
            System.out.println("Enter = enviar petición | q = salir"); 
            while (true) { 
                System.out.print("> "); String line = br.readLine(); 
                if (line == null) break; line = line.trim(); 
                if (line.equalsIgnoreCase("q")) break; 
                if (!line.isEmpty()) { 
                    System.out.println("Presiona Enter sin texto para solicitar incremento."); 
                    continue; 
                } 
                try { 
                    Object[] params = new Object[]{idCliente}; 
                    Integer resultado = (Integer) client.execute("Contador.incrementar", params); 
                    
                    // Si el servidor ignora la petición (mismo turno) 
                    if (resultado == -1) { 
                        System.out.println("Servidor ignoró la petición por doble turno. Intenta nuevamente cuando otro cliente haya llamado."); 
                        continue; 
                    } 
                        
                    if (indice < registroContador.length) registroContador[indice++] = resultado; 
                    System.out.println(nombreCliente + " recibió contador: " + resultado); 
                    if (resultado >= 20) { 
                        // Ajusta este valor al límite que definas en el servidor 
                        System.out.println("Contador llegó al límite. Fin de comunicación."); 
                        break; } 
                    } catch (Exception e) { String mensaje = e.getMessage(); 
                        if (mensaje != null && mensaje.contains("Petición ignorada")) { 
                            System.out.println("Servidor ignoró la petición por doble turno. Intenta nuevamente cuando otro cliente haya llamado."); 
                            continue; 
                        } 
                        if (mensaje != null && mensaje.contains("Servidor dejará de aceptar peticiones")) { 
                            System.out.println("Servidor alcanzó el límite. Fin de comunicación."); 
                        } else { 
                            System.out.println("Error al comunicarse con el servidor: " + mensaje); 
                        } break; } 
                    } 
                    
                    // Mostrar todos los valores históricos del cliente (de todas las sesiones)
                    Object[] paramsHistorial = new Object[]{idCliente};
                    List<Integer> historial = (List<Integer>) client.execute("Contador.obtenerValoresCliente", paramsHistorial);

                    System.out.println("\nHistorial total de valores recibidos por " + nombreCliente + ":");
                    for (int val : historial) {
                        System.out.print(val + " ");
                    }
                    System.out.println();
                    
                    // Consultar cuántos incrementos ha hecho este cliente en el servidor 
                    Object[] paramsConsulta = new Object[]{idCliente}; 
                    Integer misIncrementos = (Integer) client.execute("Contador.obtenerContadorCliente", paramsConsulta); 
                    System.out.println("\nTotal de incrementos hechos por este cliente: " + misIncrementos); 
                } catch (Exception e) { System.err.println("Error cliente: " + e.getMessage()); } 
            } 
            
            // Cargar o crear un ID único para el cliente (persistente) 
            private static String cargarIdCliente() { 
                File archivo = new File(ID_FILE); 
                try { 
                    if (archivo.exists()) { BufferedReader br = new BufferedReader(new FileReader(archivo)); 
                        String id = br.readLine().trim();
                         br.close();
                          return id;
                    } else { 
                        // Crear nuevo ID único 
                        String nuevoId = "Cliente_" + System.currentTimeMillis(); 
                        BufferedWriter bw = new BufferedWriter(new FileWriter(archivo)); 
                        bw.write(nuevoId); 
                        bw.close(); 
                        return nuevoId; 
                    } 
                } catch (IOException e) { 
                    throw new RuntimeException("Error al manejar archivo de ID del cliente: " + e.getMessage()); 
                } 
            } 
        }