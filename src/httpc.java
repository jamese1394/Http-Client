import java.io.*;
import java.net.Socket;
import java.net.URI;
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
        PrintWriter clientOut;
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
            URI url = new URI(input[input.length-1]);
            String host = url.getHost();

            int port;
            if ((port = url.getPort()) == -1) {
                port = 80;
            }

            clientSocket = new Socket(host, port);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            clientOut.println("GET" + " "+ input[input.length-1] + " HTTP/1.1");
            clientOut.println("Host: " + host);
            clientOut.println("User-Agent: Concordia-HTTP/1.0");

            // Add other commands
            if(!hPairs.isEmpty()){
                for (Map.Entry<String, String> entry: hPairs.entrySet()){
                    clientOut.println(entry.getKey()+ ": " + entry.getValue());
                }
            }

            clientOut.println("");
            clientOut.flush();

            if (!toFile) {
	            boolean print = false;
	            // Print contents
	            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
	                if (line.isEmpty() || verbose) print = true;
	                if (print) System.out.println(line);
	            }
            }
            else {
            	PrintWriter printToFile = new PrintWriter(outputFile,"UTF-8");
            	
	            boolean print = false;
	            // Print contents
	            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
	                if (line.isEmpty() || verbose) print = true;
	                if (print) printToFile.println(line);
	            }
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
        PrintWriter clientOut;
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
            URI url = new URI(input[input.length-1]);
            String host = url.getHost();

            int port;
            if ((port = url.getPort()) == -1) {
                port = 80;
            }

            clientSocket = new Socket(host, port);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            clientOut.println("POST" + " "+ input[input.length-1] + " HTTP/1.1");
            clientOut.println("Host: " + host);
            clientOut.println("User-Agent: Concordia-HTTP/1.0");
            clientOut.println("Connection: close");

            // Add other commands
            if(!hPairs.isEmpty()){
                for (Map.Entry<String, String> entry: hPairs.entrySet()){
                    clientOut.println(entry.getKey()+ ": " + entry.getValue());
                }
            }
            clientOut.println("");

            // Print inline data
            if(hasInline){
                clientOut.println(inline);
            }

            // Print file content
            if(hasFile){
                Scanner sc = new Scanner(new FileInputStream(fileName));
                String fileContent = "";
                while(sc.hasNextLine()){
                    fileContent += sc.nextLine() + "\n";
                }
                clientOut.println(fileContent);
                sc.close();
            }

            clientOut.flush();
            
            if (!toFile) {
	            boolean print = false;
	            // Print contents
	            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
	                if (line.isEmpty() || verbose) print = true;
	                if (print) System.out.println(line);
	            }
            }
            else {
            	PrintWriter printToFile = new PrintWriter(outputFile,"UTF-8");
            	
	            boolean print = false;
	            // Print contents
	            for(String line = clientIn.readLine(); line != null; line = clientIn.readLine()){
	                if (line.isEmpty() || verbose) print = true;
	                if (print) printToFile.println(line);
	            }
	            
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