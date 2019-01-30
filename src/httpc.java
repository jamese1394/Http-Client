import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class httpc {
	
	// get function, receive string [argument]* URL
	public static void Get(String[] input) {
		// Function values
		boolean verbose = false;
		Socket clientSocket;
		PrintWriter clientOut;
		BufferedReader clientIn;
		String outputString = "";
		String inputLine;
		
		// Check argument (TODO)
		for(int i = 0; i < input.length - 1; i++){	
			switch (input[i]){
				case "-v":																		// verbose
					verbose = true;
					outputString += "-v ";
					break;
				case "-h":																		// header
					break;
			}
		}
		
		// Process (TODO)
		try {
			clientSocket = new Socket(input[input.length - 1], 80);
			clientOut = new PrintWriter(clientSocket.getOutputStream());
			clientOut.println("GET " + outputString + " " + "HTTP/1.0\r\n" + "Host:" + input[input.length - 1] + "\r\n\r\n");
			clientOut.flush();
			
			clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			
			if (verbose) {
				while ((inputLine = clientIn.readLine()) != null) {
					if (inputLine.equals("{")) {
						System.exit(0);
					}
					System.out.println(inputLine);
				}
			}
			else {
				while ((inputLine = clientIn.readLine()) != null) {
					System.out.println(inputLine);
				}
			}
			
			clientIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// from function, receive string [argument]* URL
	public static void Post(String[] input) {
		// Function values
		boolean verbose = false;
		
		// Check arguments (TODO)
		for(int i = 0;i < input.length - 1; i++){	
			switch (input[i]){
				case "-v":																		// verbose
					verbose = true;
					break;
				case "-h":																		// header
					break;
				case "-d":																		// inlineData
					break;
				case "-f":																		// post file
					break;
			}
		}
		
		// Process (TODO)
		try {
			
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
		
		for (String arg: args){
			arg = arg.toLowerCase();
		}
		
		// Help output
		if(args[0].contentEquals("help")) {
			if (args.length >= 2) {
				switch(args[1]) {
				case "get":
					System.out.println("Use:");
					System.out.println("get [-v] [-h key:value] URL");
					System.out.println("-v, prints the details of the response such as protocol, status and headers");
					System.out.println("-h key:value, associates headers to HTTP request with the format \'key:value\'");
				break;
				case "post":
					System.out.println("Use:");
					System.out.println("post [-v] [-h key:value] [-d inline-data] [-f file] URL");
					System.out.println("-v, prints the details of the response such as protocol, status and headers");
					System.out.println("-h key:value, associates headers to HTTP request with the format \'key:value\'");
					System.out.println("-d string, associates an inline data to the body HTTP POST request");
					System.out.println("-f file, associates the content of a file to the body HTTP");
				break;
				default:
					System.out.print("Invalid argument");
				}
			}
			else {
				System.out.println("httpc is a simple http client library that aims to be curl-like");
				System.out.println("Use:");
				System.out.println("java httpc.java command [arguments]");
				System.out.println("Commands:");
				System.out.println("get -> HTTP GET request and prints its response");
				System.out.println("post -> HTTP POST request and prints its response");
				System.out.println("help -> prints this screen, use help [command] for more information on that command");
			}
			System.exit(0);
		}
		
		// commands
		if (args.length >= 2) {
			// get
			if (args[0].contentEquals("get")) {
				Get(Arrays.copyOfRange(args, 1, args.length));
			}
			// from
			else if (args[0].contentEquals("post")) {
				Post(Arrays.copyOfRange(args, 1, args.length));
			}
			else {
				System.out.println("Invalid command, refer to help");
				System.exit(0);
			}
		}
	}
}