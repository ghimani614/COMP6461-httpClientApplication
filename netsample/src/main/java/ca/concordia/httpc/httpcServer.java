package ca.concordia.httpc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import static java.util.Arrays.asList;

public class httpcServer {

	private static final Logger logger = LoggerFactory.getLogger(ca.concordia.echo.BlockingEchoServer.class);

	private void readEchoAndRepeat(SocketChannel socket) {
		try (SocketChannel client = socket) {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			for (;;) {
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
				if (commandLineStringArray.length == 2) {
					if (compareStringsWithChar("help", commandLineStringArray[1]))
						return "httpc is a curl-like application but supports HTTP protocol only.";
				} else if (commandLineStringArray.length > 2) {
					if (compareStringsWithChar("help", commandLineStringArray[1])) {
						if (compareStringsWithChar("get", commandLineStringArray[2]))
							return "usage: httpc get [-v] [-h key:value] URL";
						else if (compareStringsWithChar("post", commandLineStringArray[2]))
							return "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL";

						return "Invalid syntax";
					} else if (compareStringsWithChar("get", commandLineStringArray[1])) {
						// Check if it is not an empty url
						if (compareStringsWithChar("", commandLineStringArray[2])) {
							// Remove the apostrophes around the url
							String urlString = commandLineStringArray[2].replaceAll("'", "");

							return urlString;
						}

						return "Invalid syntax";
					}
					return "Invalid syntax";
				}

				return "Invalid syntax";
			}
		} else {
			return "Invalid syntax";
		}
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
			for (;;) {
				SocketChannel client = server.accept();
				logger.info("New client from {}", client.getRemoteAddress());
				// We may use a custom Executor instead of ForkJoinPool in a real-world
				// application
				ForkJoinPool.commonPool().submit(() -> readEchoAndRepeat(client));
			}
		}
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