import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.ServerSocket;
import java.net.Socket;

public class ServB {
    public static void main(String[] args) throws Exception {
        // Creating a server socket to listen for incoming connections on port 8081
        ServerSocket servidor = new ServerSocket(8081);

        // Infinite loop to continuously accept incoming connections
        for (;;) {
            Socket conexion = servidor.accept(); // Accepting incoming connection
            new Worker(conexion).start(); // Creating a new Worker thread to handle the connection
        }
    }

    // Function to request the partial sum of Ramanujan's formula from three servers
    public static BigDecimal GetPi(){

        MathContext context = new MathContext(100); // Setting precision to 100 decimal places

        // Calling the user function to request partial sums from the servers
        BigDecimal Sum1 = RespServA("localhost", "/0,333", 8080);
        BigDecimal Sum2 = RespServA("localhost", "/334,666", 8080);
        BigDecimal Sum3 = RespServA("localhost", "/667,1000", 8080);

        // Calculating Pi using the partial sums obtained
        BigDecimal Pi3 = calculatePiWithRamanujanBigDecimal(Sum1, Sum2, Sum3, context);

        return Pi3;
    }

    // Worker class to handle incoming client requests
    static class Worker extends Thread {
        Socket conexion;

        Worker(Socket conexion) {
            this.conexion = conexion;
        }

        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                PrintWriter salida = new PrintWriter(conexion.getOutputStream());

                String req = entrada.readLine(); // Reading the request line
                System.out.println(req); // Logging the request

                // Reading request headers
                for (;;) {
                    String encabezado = entrada.readLine();
                    System.out.println(encabezado);
                    if (encabezado.equals("")) break;
                }

                if (req.startsWith("GET /PI ")) {
                    // If the request is for calculating Pi, call the GetPi function
                    BigDecimal PIRes = GetPi();

                    // Sending HTTP response with Pi value
                    salida.println("HTTP/1.1 200 OK");
                    salida.println("Content-type: text/html; charset=utf-8");
                    salida.println("Content-length: "+PIRes.toString().length());
	                salida.println("Connection: close");
                    salida.println();
                    salida.println(PIRes); // Sending the Pi value in the response body
                    salida.flush();
                }
                else {
                    // If the request is for an unknown resource, send a 404 error
                    salida.println("HTTP/1.1 404 File Not Found");
                    salida.flush();
                }
                
            } catch (Exception e) {
                System.err.println(e.getMessage()); // Handling exceptions
            } finally {
                try {
                    conexion.close(); // Closing the connection
                } catch (Exception e) {
                    System.err.println(e.getMessage()); // Handling exceptions
                }
            }
        }
    }

    // Function to request partial sums of Ramanujan's formula from a server
    public static BigDecimal RespServA(String IP, String Range, int Port){        
        try {
            Socket socket = new Socket(IP, Port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Sending the request to the server
            out.println("GET "+ Range + " HTTP");
            out.println("Host: " + IP);
            out.println();

            // Reading the response from the server
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }

            // Closing resources
            out.close();
            in.close();
            socket.close();

            // Parsing the response to extract the result
            String response = responseBuilder.toString();
            int startIndex = response.indexOf("\n\n") + 2; // Skip headers
            String responseBody = response.substring(startIndex).trim(); // Trim extra spaces and newlines

            // Parsing the BigDecimal from the response body
            BigDecimal result = new BigDecimal(responseBody);

            return result;
            
        } catch (Exception e) {
            e.printStackTrace(); // Handling exceptions
            return BigDecimal.valueOf(0); // Returning default value in case of error
        }
    }

    // Function to calculate Pi using Ramanujan's formula
    public static BigDecimal calculatePiWithRamanujanBigDecimal(BigDecimal Sum1, BigDecimal Sum2, BigDecimal Sum3, MathContext context) {
        // Calculating Pi using Ramanujan's formula
        BigDecimal constant = new BigDecimal(Math.sqrt(2)).divide(new BigDecimal(9801), context);
        BigDecimal Pi = Sum1.add(Sum2).add(Sum3);

        return constant.multiply(Pi).pow(-1, context).divide(new BigDecimal(2), context);
    }
}
