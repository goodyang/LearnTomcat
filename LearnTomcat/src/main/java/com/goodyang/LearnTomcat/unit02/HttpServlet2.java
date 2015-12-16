package com.goodyang.LearnTomcat.unit02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServlet2 {
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
	private boolean     shutdown = false;
	
	public static void main(String[] args) {
		HttpServlet2 servlet = new HttpServlet2();
		servlet.await();
	}
	
	public void await() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while(!shutdown) {
			Socket socket = null;
			InputStream input = null;
			OutputStream output = null;
			try{
				socket = serverSocket.accept();
				input = socket.getInputStream();
				output = socket.getOutputStream();
				
				Request request = new Request(input);
				request.parse();
				Response response = new Response(output);
				response.setRequest(request);
				
				if(request.getUri()!=null){
					if(request.getUri().startsWith("/servlet/")){
						ServletProcessor2 processor = new ServletProcessor2();
						processor.process(request, response);
					}else {
						StaticResourceProcessor processor = new StaticResourceProcessor();
						processor.process(request, response);
					}
					socket.close();
					shutdown = request.getUri().endsWith(SHUTDOWN_COMMAND);
				}				
			}catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
