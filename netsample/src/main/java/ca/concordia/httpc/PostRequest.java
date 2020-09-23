import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import clientlibrary.PostRequest;

public class PostRequest {

	public String HTTPMethods = "POST";
	
	public static PostRequest requestBuilder(String[] args) {
		
		PostRequest request = new PostRequest();
		//finding URL and hostname
		for (int i = 1; i < args.length; i++) {
            if (args[i].contains("/")) {
                request.setServerInfo(args[i]);
            }
        }
		
		if (request.URL == null) {
            System.out.println("URL NOT FOUND !!!");
            return null;
        }
		// checking "-h" 
		for (int i = 2; i < args.length; i++) {
            if (args[i].equals("-h")) {
                String pair = args[i + 1].replaceAll("\'", "");
                if (!pair.contains(":")) {
                    System.out.println("Invalid Format");
                    return null;
                }
                String[] param = pair.split(":", 2);
                request.HeaderMap.put(param[0], param[1]);
            }
		 // checking "-d" 
        if (args[i].equals("-d")) {
            String inline = args[i + 1].replaceAll("\'", "");
            request.setBody(inline);
        }
         // Either "-d" or "-f", not both
           else if (args[i].equals("-f")) {
            String filePath = args[i + 1].replaceAll("\'", "");
            String content = "";
            try {
                File file = new File("src/" + filePath);
                if (file.exists()) {
                    BufferedReader in = new BufferedReader(new FileReader("src/" + filePath));
                    String string;
                    while ((string = in.readLine()) != null) {
                        content += string;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            request.setBody(content);
          }
        }
    return request;
	}
}
