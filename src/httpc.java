import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class httpc {

    // get function, receive string [argument]* URL
    public static void Get(String[] input) {
        // Function values
        boolean verbose = false;
        boolean toFile = false;
        String outputFile = "";
        Socket clientSocket;
        SocketAddress Address;
        BufferedWriter clientOut;
        BufferedReader clientIn;
        Map <String, String> hPairs = new HashMap <String, String>();

        // Check argument
        for(int i = 0; i < input.length - 1; i++){
            switch (input[i]){
                case "-v":
                    verbose = true;
                    break;
                case "-h":
                    String pairs = input[i+1];
                    int separation = pairs.indexOf(':');
                    hPairs.put(pairs.substring(0, separation), pairs.substring(separation+1));
                    break;
                case "-o":
                	toFile = true;
                	outputFile = input[i+1];
                	break;
            }
        }

        // Process
        try {
            // Register socket and info
            String url = input[input.length - 1];
            URL u = new URL(url);
            String host = u.getHost();
            int port;
            
            if(host.equals("localhost")){
                port = 8080;
            }else {
                port = u.getDefaultPort();
            }
            
            // Open socket
            clientSocket = new Socket();
            Address = new InetSocketAddress(host, port);
            clientSocket.connect(Address);
            
            clientOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("GET" + " "+ url + " HTTP/1.1\r\n");
            sb.append("Host: " + host + "\r\n");
            sb.append("Connection: close\r\n");
            sb.append("User-Agent: COMP445\r\n");
            for (String keys:hPairs.keySet()) {
                sb.append(keys).append(": ").append(hPairs.get(keys)).append("\r\n");
            }
            sb.append("\r\n");
            clientOut.write(sb.toString());
            clientOut.flush();
         
            sb = new StringBuilder();
            boolean print = false;
            // Print contents
            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
                if (line.isEmpty() || verbose) print = true;
                if (print) sb.append(line + "\r\n");
            }
            
            //Check if needs redirection
            if(sb.toString().contains("HTTP/1.0") || sb.toString().contains("HTTP/1.1") || sb.toString().contains("HTTP/2.0")){
                if(Redirection(sb.toString()))
                {
                    // Open socket
                	clientSocket.close();
                    clientSocket = new Socket();
                    Address = new InetSocketAddress(host, port);
                    clientSocket.connect(Address);
                    
                    clientOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    sb = new StringBuilder();
                    sb.append("GET" + " "+ url + " HTTP/1.1\r\n");
                    sb.append("Host: " + host + "\r\n");
                    sb.append("Connection: close\r\n");
                    sb.append("User-Agent: COMP445\r\n");
                    for (String keys:hPairs.keySet()) {
                        sb.append(keys).append(": ").append(hPairs.get(keys)).append("\r\n");
                    }
                    sb.append("\r\n");
                    clientOut.write(sb.toString());
                    clientOut.flush();
                    
                    sb = new StringBuilder();
                    print = false;
                    // Print contents
                    for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
                        if (line.isEmpty() || verbose) print = true;
                        if (print) sb.append(line + "\r\n");
                    }
                }
            }
            
            if (!toFile) {
	            // Print contents
            	System.out.println(sb);
            }
            else {
            	PrintWriter printToFile = new PrintWriter(outputFile,"UTF-8");
	            // Print contents
            	printToFile.println(sb);
	            printToFile.close();
            }
            
            // Close streams
            clientIn.close();
            clientOut.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // post function, receive string [argument]* URL
    public static void Post(String[] input) {
        // Function values
        boolean verbose = false;
        boolean hasInline = false;
        String inline = "";
        String contentLength = "";
        boolean hasFile = false;
        String fileName = "";
        boolean toFile = false;
        String outputFile = "";
        Socket clientSocket;
        SocketAddress Address;
        BufferedWriter clientOut;
        BufferedReader clientIn;
        Map <String, String> hPairs = new HashMap <String, String>();

        // Check arguments
        for(int i = 0;i < input.length - 1; i++){
            switch (input[i]){
                case "-v":
                    verbose = true;
                    break;
                case "-h":
                    String pairs = input[i+1];
                    int separation = pairs.indexOf(':');
                    hPairs.put(pairs.substring(0, separation), pairs.substring(separation+1));
                    break;
                case "-d":
                    hasInline = true;
                    if(hasFile && hasInline){
                        System.out.println("Either [-d] or [-f] can be used but not both.");
                        System.exit(0);
                    }
                    inline += input[i + 1];
                    while(!input[i + 1].endsWith("'")){
                        inline += " " + input[++i + 1];
                    }
                    inline = inline.substring(1, inline.length()-1);
                    
                    int tempIndex;
                    tempIndex = inline.indexOf("{");
                    inline = inline.substring(0, tempIndex + 1) + "\"" + inline.substring(tempIndex + 1);
                    tempIndex = inline.indexOf(":");
                    inline = inline.substring(0, tempIndex) + "\"" + inline.substring(tempIndex);
                    contentLength = "";
                    contentLength += inline.length();
                    hPairs.put("Content-Length", contentLength);
                    break;
                case "-f":
                    hasFile = true;
                    if(hasFile && hasInline){
                        System.out.println("Either [-d] or [-f] can be used but not both.");
                        System.exit(0);
                    }
                    fileName = input[i+1];
                    File fileContent = new File (fileName);
                    contentLength += fileContent.length();
                    hPairs.put("Content-Length", contentLength);
                    break;
                case "-o":
                	toFile = true;
                	outputFile = input[i + 1];
                	break;
            }
        }

        // Process
        try {
            // Register socket and info
            String url = input[input.length - 1];
            URL u = new URL(url);
            String host = u.getHost();
            int port;
            
            if(host.equals("localhost")){
                port = 8080;
            }else {
                port = u.getDefaultPort();
            }

            // Open socket
            clientSocket = new Socket();
            Address = new InetSocketAddress(host, port);
            clientSocket.connect(Address);
            
            clientOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("POST" + " "+ url + " HTTP/1.1\r\n");
            sb.append("Host: " + host + "\r\n");
            sb.append("Connection: close\r\n");
            sb.append("User-Agent: COMP445\r\n");
            for (String keys:hPairs.keySet()) {
                sb.append(keys).append(": ").append(hPairs.get(keys)).append("\r\n");
            }
            sb.append("\r\n");

            // Print inline data
            if(hasInline){
                sb.append(inline + "\r\n");
            }

            // Print file content
            if(hasFile){
                Scanner sc = new Scanner(new FileInputStream(fileName));
                String fileContent = "";
                String line = "";
                while(sc.hasNextLine()){
                    line += sc.nextLine() + "\n";
                    fileContent = line.replace("'", "");
                }
                sb.append(fileContent + "\r\n");
                sc.close();
            }

            clientOut.write(sb.toString());
            clientOut.flush();
            
            sb = new StringBuilder();
            boolean print = false;
            // Print contents
            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
                if (line.isEmpty() || verbose) print = true;
                if (print) sb.append(line + "\r\n");
            }
            
            //Check if needs redirection
            if(sb.toString().contains("HTTP/1.0") || sb.toString().contains("HTTP/1.1") || sb.toString().contains("HTTP/2.0")){
                if(Redirection(sb.toString()))
                {
                    // Open socket
                	clientSocket.close();
                    clientSocket = new Socket();
                    Address = new InetSocketAddress(host, port);
                    clientSocket.connect(Address);
                    
                    clientOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    sb = new StringBuilder();
                    sb.append("POST" + " "+ url + " HTTP/1.1\r\n");
                    sb.append("Host: " + host + "\r\n");
                    sb.append("Connection: close\r\n");
                    sb.append("User-Agent: COMP445\r\n");
                    for (String keys:hPairs.keySet()) {
                        sb.append(keys).append(": ").append(hPairs.get(keys)).append("\r\n");
                    }
                    sb.append("\r\n");

                    // Print inline data
                    if(hasInline){
                        sb.append(inline + "\r\n");
                    }

                    // Print file content
                    if(hasFile){
                        Scanner sc = new Scanner(new FileInputStream(fileName));
                        String fileContent = "";
                        String line = "";
                        while(sc.hasNextLine()){
                            line += sc.nextLine() + "\n";
                            fileContent = line.replace("'", "");
                        }
                        sb.append(fileContent + "\r\n");
                        sc.close();
                    }

                    clientOut.write(sb.toString());
                    clientOut.flush();
                    
                    sb = new StringBuilder();
                    print = false;
                    // Print contents
                    for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
                        if (line.isEmpty() || verbose) print = true;
                        if (print) sb.append(line + "\r\n");
                    }
                }
            }
            
            if (!toFile) {
	            // Print contents
            	System.out.println(sb);
            }
            else {
            	PrintWriter printToFile = new PrintWriter(outputFile,"UTF-8");
	            // Print contents
            	printToFile.println(sb);
	            printToFile.close();
            }

            // Close streams
            clientIn.close();
            clientOut.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean Redirection(String data) {
    	String[] dataLines = data.split("\r\n");
        if(dataLines[0].contains("300") || dataLines[0].contains("301") || dataLines[0].contains("302") || dataLines[0].contains("304")){
            return true;
        }
        return false;
    }

    public static void main (String [] args){
        //No arguments
        if (args.length <= 0) {
            System.out.println("Argument required, refer to help.");
            System.exit(0);
        }

        args[args.length - 1] = args[args.length - 1].replace("'", "");
        
        // Help output
        if(args[0].contentEquals("help")) {
            if (args.length >= 2) {
                switch(args[1]) {
                    case "get":
                        System.out.println("Usage: httpc get [-v] [-h key:value] URL");
                        System.out.println("Get executes a HTTP GET request for a given URL.");
                        System.out.println("-v \t Prints the detail of the response such as protocol, status, and headers.");
                        System.out.println("-h \t Associates headers to HTTP Request with the format \'key:value\'.");
                        break;
                    case "post":
                        System.out.println("Usage:");
                        System.out.println("post [-v] [-h key:value] [-d inline-data] [-f file] URL");
                        System.out.println("-v \t Prints the detail of the response such as protocol, status, and headers.");
                        System.out.println("-h \t Associates headers to HTTP Request with the format \'key:value\'.");
                        System.out.println("-d \t string Associates an inline data to the body HTTP POST request.");
                        System.out.println("-f \t Associates the content of a file to the body HTTP POST request.");
                        System.out.println("Either [-d] or [-f] can be used but not both.");    // Edit from James
                        break;
                    default:
                        System.out.print("Invalid argument");
                }
            }
            else {
                System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
                System.out.println("Usage:");
                System.out.println("httpc command [arguments]");
                System.out.println("The commands are:");
                System.out.println("get \t executes a HTTP GET request and prints the response.");
                System.out.println("post \t executes a HTTP POST request and prints the response.");
                System.out.println("");
                System.out.println("Use \"httpc help [command]\" for more information about a command.");
            }
            System.exit(0);
        }
        
        // commands
        if (args.length >= 2) {
            // get
            if (args[0].contentEquals("get") || args[0].contentEquals("GET")) {
                Get(Arrays.copyOfRange(args, 1, args.length));
            }
            // post
            else if (args[0].contentEquals("post") || args[0].contentEquals("POST")) {
                Post(Arrays.copyOfRange(args, 1, args.length));
            }
            else {
                System.out.println("Invalid command, refer to help");
            }
        }
        
        System.exit(0);
    }
}