package ca.concordia.httpc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ForkJoinPool;

import static java.util.Arrays.asList;

public class httpcServer {

    private static final Logger logger = LoggerFactory.getLogger(ca.concordia.echo.BlockingEchoServer.class);

    private void readEchoAndRepeat(SocketChannel socket) {
        try (SocketChannel client = socket) {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            for (; ; ) {
                int nr = client.read(buf);

                // Convert byte array to string
                String requestString = new String(buf.array(), StandardCharsets.UTF_8);
                System.out.println("Client request: " + requestString);

                buf.clear();

                for (int i = 0; i < buf.array().length; i++)
                    buf.put((byte) 0);

                String responseString = parseCommandLine(requestString);

                if (nr == -1)
                    break;

                if (nr > 0) {

                    buf.clear();
                    buf.position(0);

                    // ByteBuffer is tricky, you have to flip when switch from read to write, or
                    // vice-versa
                    buf.flip();

                    client.write(ByteBuffer.wrap(responseString.getBytes(StandardCharsets.UTF_8)));
                    buf.clear();
                }
            }
        } catch (IOException e) {
            logger.error("Echo error {}", e);
        }
    }

    private String parseCommandLine(String commandLineString) {
        String[] commandLineStringArray = commandLineString.split(" ");

//        for (int i = 0; i < commandLineStringArray.length; i++)
//            System.out.println(i + ": " + commandLineStringArray[i]);

//		System.out.println(commandLineStringArray.length);

        if (commandLineStringArray.length > 0) {
            // Check the starting word, must start with httpc
            if (commandLineStringArray[0].compareTo("httpc") != 0) {
                return "Invalid syntax";
            } else {
                String urlString;

                if (commandLineStringArray.length == 2) {
                    if (compareStringsWithChar("help", commandLineStringArray[1]))
                        return "httpc is a curl-like application but supports HTTP protocol only.\nUsage:\n    httpc command [arguments]\nThe commands are:\n    get executes a HTTP GET request and prints the response.\n    post executes a HTTP POST request and prints the response.\n    help prints this screen.\n\n" +
                                "Use \"httpc help [command]\" for more information about a command.";
                } else if (commandLineStringArray.length > 2) {
                    if (compareStringsWithChar("help", commandLineStringArray[1])) {
                        if (compareStringsWithChar("get", commandLineStringArray[2]))
                            return "usage: httpc get [-v] [-h key:value] URL \n\n" + "Get executes a HTTP GET request for a given URL.\n\n   -v             Prints the detail of the response such as protocol, status, and headers.\n   -h key:value   Associates headers to HTTP Request with the format 'key:value'.";
                        else if (compareStringsWithChar("post", commandLineStringArray[2]))
                            return "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n\nPost executes a HTTP POST request for a given URL with inline data or from file.\n\n   -v             Prints the detail of the response such as protocol, status, and headers.\n   -h key:value   Associates headers to HTTP Request with the format 'key:value'.\n   -d string      Associates an inline data to the body HTTP POST request.\n   -f file        Associates the content of a file to the body HTTP POST request.\n\nEither [-d] or [-f] can be used but not both.";

                        return "Invalid syntax";
                    } else if (compareStringsWithChar("get", commandLineStringArray[1])) {
                        if (compareStringsWithChar("-v", commandLineStringArray[2])) {
                            // httpc get -v url

                            // Remove the apostrophes around the url
                            urlString = commandLineStringArray[3].replaceAll("'", "");

                            return getHeaderValueByKey(urlString, null) + "\nServer: " + getHeaderValueByKey(urlString, "Server") + "\nDate: " + getHeaderValueByKey(urlString, "Date") + "\nContent-Type: " + getHeaderValueByKey(urlString, "Content-Type") + "\nContent-Length: " + getHeaderValueByKey(urlString, "Content-Length") + "\nConnection: " + getHeaderValueByKey(urlString, "Connection") + "\nAccess-Control-Allow-Origin: " + getHeaderValueByKey(urlString, "Access-Control-Allow-Origin") + "\nAccess-Control-Allow-Credentials: " + getHeaderValueByKey(urlString, "Access-Control-Allow-Credentials") + "\n" + getHttpResponse(urlString);
                        } else if (compareStringsWithChar("-h", commandLineStringArray[2])) {
                            // httpc get -h key:value url

                            // Check if it contains the minimum number of terms
                            if (commandLineStringArray.length < 5)
                                return "Invalid syntax";

                            // Remove empty bytes from the string
                            commandLineStringArray[commandLineStringArray.length - 1] = commandLineStringArray[commandLineStringArray.length - 1].replaceAll("\u0000.*", "");

                            // Check the url format, it should wrapped by a pair of apostrophes
                            if (commandLineStringArray[commandLineStringArray.length - 1].charAt(0) == 39 & commandLineStringArray[commandLineStringArray.length - 1].charAt(commandLineStringArray[commandLineStringArray.length - 1].length() - 1) == 39) {
                                // Remove the apostrophes around the url
                                urlString = commandLineStringArray[commandLineStringArray.length - 1].replaceAll("'", "");
                            } else {
                                return "Invalid syntax";
                            }

                            String returnString = "";

                            // There could be multiple header parameters for httpc get -h
                            for (int index = 3; index < commandLineStringArray.length - 1; index++) {
                                boolean hasOneColon = false;

                                // Check if there is only one colon, otherwise it is invalid syntax
                                for (int characterIndex = 0; characterIndex < commandLineStringArray[index].length() - 1; characterIndex++) {
                                    if (commandLineStringArray[index].charAt(characterIndex) == ':') {
                                        if (!hasOneColon)
                                            hasOneColon = true;
                                        else
                                            return "Invalid syntax";
                                    }
                                }

                                String[] keyValueString = commandLineStringArray[index].split(":");

                                // Append each key search to the return string
                                returnString += keyValueString[0] + ": " + getHeaderValueByKey(urlString, keyValueString[0]);

                                // Append a new line except for the last line
                                if (index != commandLineStringArray.length - 2)
                                    returnString += "\n";
                            }

                            return returnString;
                        } else {
                            // httpc get url

                            // Ensure it is not an empty url
                            if (!compareStringsWithChar("", commandLineStringArray[2])) {
                                // Remove empty bytes from the string
                                commandLineStringArray[2] = commandLineStringArray[2].replaceAll("\u0000.*", "");

                                // Check the url format, it should wrapped by a pair of apostrophes
                                if (commandLineStringArray[2].charAt(0) == 39 & commandLineStringArray[2].charAt(commandLineStringArray[2].length() - 1) == 39) {
                                    // Remove the apostrophes around the url
                                    urlString = commandLineStringArray[2].replaceAll("'", "");

                                    return getHttpResponse(urlString);
                                } else {
                                    return "Invalid syntax";
                                }
                            }
                        }
                    }else if (compareStringsWithChar("post", commandLineStringArray[1])){
                        if (compareStringsWithChar("-v", commandLineStringArray[2])) {
                            // httpc post -v Content-Type:application urlParameters url
                        }
                        else if (compareStringsWithChar("-h", commandLineStringArray[2])) {
                            // httpc post -h key:value url
                            //to write...
                        }else if(compareStringsWithChar("-d", commandLineStringArray[3])){
                            //to write...
                        }
                        else {
                            // httpc post url

                          return "Please provide Data to be posted !!!";
                        }
                    } else if (compareStringsWithChar("-v", commandLineStringArray[1])) {
                        // httpc -v url -o file.txt

                        // Check if it contains the exact number of terms
                        if (commandLineStringArray.length != 5)
                            return "Invalid syntax";

                        // Remove empty bytes from the string
                        commandLineStringArray[2] = commandLineStringArray[2].replaceAll("\u0000.*", "");

                        // Check the url format, it should wrapped by a pair of apostrophes
                        if (commandLineStringArray[2].charAt(0) == 39 & commandLineStringArray[2].charAt(commandLineStringArray[2].length() - 1) == 39) {
                            // Remove the apostrophes around the url
                            urlString = commandLineStringArray[2].replaceAll("'", "");
                        } else {
                            return "Invalid syntax";
                        }

                        // The fourth term should be -o without any exception
                        if (!compareStringsWithChar("-o", commandLineStringArray[3]))
                            return "Invalid syntax";

                        // Remove empty bytes from the file name string
                        String fileName = commandLineStringArray[4].replaceAll("\u0000.*", "");

                        // Write to file
                        boolean result = writeToTextFile(fileName, getHttpResponse(urlString));

                        if (result)
                            return "Successfully wrote response to the file.";
                        else
                            return "Failed to write the file.";
                    }
                }
            }

            return "Invalid syntax";
        } else {
            return "Invalid syntax";
        }
    }

    private String getHttpResponse(String urlString) {
        StringBuilder stringBuilder;

        try {
            stringBuilder = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line + "\n");

            bufferedReader.close();
        } catch (Exception e) {
            return "Get Http response error";
        }

        return stringBuilder.toString();
    }

    private String getHeaderValueByKey(String urlString, String keyString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            String headerValueString = connection.getHeaderField(keyString);

            if (headerValueString == null)
                System.out.println("Key '" + keyString + "' not found");
            else
                return headerValueString;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Not found";
    }

    private boolean writeToTextFile(String fileNameString, String contentString) {
        try {
            FileWriter myWriter = new FileWriter(fileNameString);
            myWriter.write(contentString);
            myWriter.close();
            System.out.println("Successfully wrote response to the file.");
        } catch (IOException e) {
            System.out.println("Failed to write the file.");
            e.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean compareStringsWithChar(String string1, String string2) {
        // Remove empty bytes from the string
        string1 = string1.replaceAll("\u0000.*", "");
        string2 = string2.replaceAll("\u0000.*", "");

        if (string1.length() != string2.length())
            return false;

        for (int index = 0; index < string1.length(); index++)
            if (Character.compare(string1.charAt(index), string2.charAt(index)) != 0)
                return false;

        return true;
    }

    private void listenAndServe(int port) throws IOException {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(port));
            logger.info("EchoServer is listening at {}", server.getLocalAddress());
            for (; ; ) {
                SocketChannel client = server.accept();
                logger.info("New client from {}", client.getRemoteAddress());
                // We may use a custom Executor instead of ForkJoinPool in a real-world
                // application
                ForkJoinPool.commonPool().submit(() -> readEchoAndRepeat(client));
            }
        }
    }

    private String postHttpResponse(String urlString) {
        StringBuilder stringBuilder;
        String urlParameters = null;    // I need to get the parameters here ???
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        try {
            stringBuilder = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent","Java client");

            try ( var wr= new DataOutputStream(connection.getOutputStream())){
             //   wr.write(postData);   //this shows error ??
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            System.out.println(stringBuilder.toString()); //Displays output
            bufferedReader.close();
        } catch (Exception e) {
            return "Post Http response error";
        }

        return stringBuilder.toString();
        //to write functionality
         return urlString;
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("port", "p"), "Listening port").withOptionalArg().defaultsTo("8007");

        OptionSet opts = parser.parse(args);
        int port = Integer.parseInt((String) opts.valueOf("port"));
        httpcServer server = new httpcServer();
        server.listenAndServe(port);
    }
}