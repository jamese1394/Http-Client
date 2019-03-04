import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class server extends Thread{
	private ServerSocketChannel serverSocket;
    private boolean debug;
    private String path;
    private int port;
    private Map <String, String> query = new HashMap <String, String>();
	
    // Initialize
    public server(ServerSocketChannel serverSocketChannel) {
        this.serverSocket = serverSocketChannel;
        this.debug = false;
        this.path = null;
        this.port = 8080;
    }
    
    // On use
    public void run() {
    	// Server now has information to run
    	System.out.println("Server Online!");
        
        // Open
        try {
            SocketAddress sAddress = new InetSocketAddress(port);
            serverSocket.bind(sAddress);
            while(true) {
                SocketChannel client = serverSocket.accept();
                handleRequest(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    // Setters
    public void setDebug(boolean debug){ this.debug = debug; }
    public void setPort(int port){ this.port = port; }
    public void setPath(String path){ this.path = path; }
    
    // When request is received do
    private void handleRequest(SocketChannel socketChannel) throws IOException {
    	// Set buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(10000);
        int buffer = socketChannel.read(byteBuffer);
        // Hold request information
        String header = null;
        String body = null;
        
        
        // When buffer is not done
        if(buffer > 0) {
            byteBuffer.flip();
            String request = StandardCharsets.UTF_8.decode(byteBuffer).toString();
            
            // Print debug message
            if(debug) {
                System.out.println("Debug messages:");
                System.out.println(request);
                System.out.println();
            }
            
            byteBuffer.clear();
            
            // Request information
            header = request;
            if (header.contains("POST")) {
                body = request.split("\r\n\r\n")[1];
            }
        }
        // All input received
        socketChannel.socket().shutdownInput();

        // Send response
        OutputStream os = socketChannel.socket().getOutputStream();
        PrintWriter printWriter = new PrintWriter(os);
        printWriter.write(response(header, body));
        printWriter.flush();
        printWriter.close();
        os.close();
    }
    
    private String response(String header,String body) throws IOException {
    	// build response string
        StringBuilder sb = new StringBuilder();
        
        // header
        	// first line
        sb.append(status(header)+"\r\n");

        	// connection header if action failed
        if(!(status(header).contains("200") && status(header).contains("OK")))
        	sb.append(header.split("\r\n")[1]+"\r\n");
        
        	// pairs
        for (int i = 2; i < header.split("\r\n").length; i++)
            sb.append(header.split("\r\n")[i]+"\r\n");
        
        	// content type / content-disposition
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String pathURL = firstLine[1];
        
        sb.append("\r\n");
        
        // Content
        if(status(header).contains("200") && status(header).contains("OK"))
            sb.append(locateFiles(header, body)+"\r\n");
        
        if(pathURL.contains("get") || pathURL.contains("post"))
            sb.append(output(header, body));
        
        return sb.toString();
    }
    
    private String status(String header) throws IOException {
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String pathURL = firstLine[1];

        if(pathURL.contains("get") || pathURL.contains("post"))
            return "HTTP/1.0 200 OK";
        
        URL url = new URL(pathURL);
        String fileName = url.getFile();
        File file = new File(this.path+"\\"+fileName);
        if((file.exists() || fileName.equals("") && header.contains("GET")) || header.contains("POST"))
            return "HTTP/1.0 200 OK";
        else
            return "HTTP/1.0 ERROR 404";
    }
    
    private synchronized String locateFiles(String header, String body) throws IOException {
        StringBuilder sb = new StringBuilder();
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String pathURL = firstLine[1];
        if(pathURL.contains("get") || pathURL.contains("post"))
            return "";
        URL url = new URL(pathURL);
        String fileName = url.getFile();
        File file = new File(path + "\\" + fileName);
        if(header.contains("GET")){
            if(fileName.equals("")){
                File[]fileList = file.listFiles((dir, name) -> name.charAt(0) != '.');
                sb.append("Files in directory:\r\n");
                for (File f:fileList) {
                    sb.append(f.getName()+"\r\n");
                }
            }else {
                sb.append(readFile(this.path + "\\" + fileName));
            }
        }else if(header.contains("POST")){
            writeFile(body, this.path + "\\" + fileName);
        }
        return sb.toString();
    }
    
    private String readFile(String fPath) throws IOException {
        File fFile = new File(fPath);
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fFile));
        String nextLine = bufferedReader.readLine();
        while(nextLine != null) {
        	sb.append(nextLine);
            nextLine = bufferedReader.readLine();
        };
        bufferedReader.close();
        return sb.toString();
    }
    
    private void writeFile(String body, String fPath){
        File file = new File(fPath);
        BufferedWriter writer = null;
        try {
        	FileWriter fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            writer.write(body);
            writer.close();
            fw.close();
            
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    
    private String output(String header,String body) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String pathURL = firstLine[1];
        URL url = new URL(pathURL);
        if(pathURL.contains("?"))
            queryParameters(url);
        
        sb.append("{\r\n");
        sb.append("  \"args\": {\r\n");
        for (String key:query.keySet())
            sb.append("      ").append("\""+key+"\"").append("\""+query.get(key)+"\"").append(",\r\n");
        sb.append("  },\r\n");
        if(header.contains("POST"))
            sb.append("  \"data\": ").append("\""+body+"\"\r\n");
        sb.append("  \"headers\": {\r\n");
        for (int i = 2; i < header.split("\r\n").length; i++)
            sb.append("      ").append(header.split("\r\n")[i]+"\r\n");
        sb.append("  },\r\n");
        sb.append("  \"url\": ").append("\""+url+"\"\r\n");
        sb.append("}\r\n");
        query.clear();
        return sb.toString();
    }
    
    private void queryParameters(URL u){
        String queryLine = u.getQuery();
        String [] pair = queryLine.split("&");
        for (String s:pair) {
            String [] rest = s.split("=");
            query.put(rest[0],rest[1]);
        }
    }
}
