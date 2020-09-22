import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class HTTPClient {

	public String hostname = "localhost";
	public String port = "8080";
	
	//Starting the Client
	
	private void startClient(SocketAddress socketAddr) {
			
		try{
			SocketChannel channel = SocketChannel.open();
		    channel.connect(socketAddr);
		    
		    Scanner sc = new Scanner(System.in);
		    while(sc.hasNextLine()) {
		    	
		    	String line = sc.nextLine();
		    	String[] commands = line.split(" "); 
		    	
		    	if(line.equals("close")) {
		    		channel.close();
		    		return;
		    	}
		    	else if(commands[0].equals("httpc") && commands.length>1) {
		    		if(commands[1].equals("help")) {
		    			
		    			if(commands.length > 2) {
		    				//check help
		    				Help.help(commands[2]);
		    			}
		    			else{
		    				Help.help();
		    			}
		    		}else {
		    			//GET,POST Request
		    		}
		    		
		    	}else {
		    		System.out.println("Undefined Command !!!!");
		    	}
		    }
		    
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
