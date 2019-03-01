import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class httpfs{

	public static void main(String[] args) throws IOException
	{
		// open server socket channel
        ServerSocketChannel server = ServerSocketChannel.open();
        // run server
        server socketThread = new server(server);
        
        // read input
        if (args.length > 0) {
	        for(int i = 0; i < args.length - 1; i++){
	            switch (args[i]){
	                case "-v":
	                	socketThread.setDebug(true);
	                    break;
	                case "-p":
	                	i++;
	                	if (i >= args.length) {
	                		System.out.println("Invalid command");
	                        System.exit(0);
	                	}
	                    socketThread.setPort(Integer.valueOf(args[i]));
	                    break;
	                case "-d":
	                	i++;
	                	if (i >= args.length) {
	                		System.out.println("Invalid command");
	                        System.exit(0);
	                	}
	                	socketThread.setPath(args[i]);
	                	break;
	            }
	        }
        }
        else {
            System.out.println("Invalid command");
            System.exit(0);
        }
        
        // Start server
        socketThread.start();
	}
}
