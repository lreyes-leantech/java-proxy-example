package com.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.interfaces.Headers;
import com.stream.consumer.InputStreamConsumer;
import com.types.HttpRequestMethodType;

public class Proxy implements Runnable {

	private static final int PORT = 8080;
	private static Path path = Paths.get("output.txt");
	private Socket client;

	public Proxy(Socket client) {
		this.client = client;
	}

	public static void main(String args[]) throws Exception {

		// create listener socket to listen for requests from browser
		ServerSocket listener = new ServerSocket(PORT);
		System.out.println("Server started.\nListening for connections on port : " + PORT + " ...");

		while (true) {
			Proxy proxyServer = new Proxy(listener.accept());
			Thread thread = new Thread(proxyServer);
			thread.start();
		}

	}

	@Override
	public void run() {
		ByteArrayOutputStream httpBytesHeaders = new ByteArrayOutputStream();

		try {
			new InputStreamConsumer(this.client.getInputStream(), httpBytesHeaders, 1000L).consume();
		} catch (IOException e) {
			System.err.println("Error reading client request " + e.getMessage());
		}

		Headers request = new HttpRequestHeaders(new String(httpBytesHeaders.toByteArray()));
		logRequestHeaders(request.toString().getBytes());
		
		System.out.println("from: " + client.getInetAddress() + " " + request.getRequestMethodEnum() + " "
				+ request.getHost() + " " + request.getPort());

		Socket to = null;
		try {
			to = new Socket(request.getHost(), request.getPort());
		} catch (IOException e) {
			System.err.println("Error creating sever socket " + e.getMessage());
		}

		try {
			forwardRequest(request, to);

		} catch (IOException e) {
			System.err.println("Error forwarding request " + e.getMessage());
		}

	}

	private static void logRequestHeaders(byte[] data) {
		try {
			Files.write(path, data, StandardOpenOption.APPEND);
		} catch (IOException ex) {
			System.err.println("Error writing file " + ex.getStackTrace());
		}
	}

	private void forwardRequest(final Headers request, final Socket to) throws IOException {
		if (isGetOrPostMethodType(request)) {
			handleGetPostMethod(to, client.getOutputStream(), request);

		} else if (isConnectMethodType(request)) {
			client.getOutputStream()
					.write("HTTP/1.1 200 Connection established\r\nProxy-connection: Keep-alive\r\n\r\n".getBytes());
			client.getOutputStream().flush();

			ThreadPoolManager.getInstance()
					.execute(new InputStreamConsumer(client.getInputStream(), to.getOutputStream()));
			ThreadPoolManager.getInstance()
					.execute(new InputStreamConsumer(to.getInputStream(), client.getOutputStream()));

		} else {
			System.out.println(request);
		}
	}

	private static void handleGetPostMethod(final Socket to, final OutputStream output, final Headers request)
			throws IOException {

		try {
			OutputStream toOut = to.getOutputStream();

			toOut.write(request.toString().getBytes());
			toOut.flush();
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage() + " " + e);
			return;
		}
		try {

			boolean availableBytes = false;
			int bytesReaded = 0;
			byte[] bytesBuffer = new byte[1024];

			do {
				InputStream input = to.getInputStream();
				try {
					bytesReaded = input.read(bytesBuffer);

					// if bytesReaded is -1 then EOF
					if (bytesReaded == -1) {
						System.out.println("EOF has been readed.");
						break;
					}
				} catch (IOException e) {
				}

				try {
					output.write(bytesBuffer, 0, bytesReaded);
					output.flush();
				} catch (Exception e) {
					System.err.println("Error writing in GetPostMethod");
				}

				availableBytes = input.available() > 0;

			} while (availableBytes);

		} finally {
			to.close();
		}
	}

	private static boolean isConnectMethodType(Headers request) {
		return request.getRequestMethodEnum() == HttpRequestMethodType.CONNECT;
	}

	private static boolean isGetOrPostMethodType(Headers request) {
		return request.getRequestMethodEnum() == HttpRequestMethodType.GET
				|| request.getRequestMethodEnum() == HttpRequestMethodType.POST;
	}

}
