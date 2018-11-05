package chatClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RudeClient {
	public static void main(String[] args) {
		Socket socket;
		try {
			socket = new Socket();

			System.out.println("[연결 요청]");
			socket.connect(new InetSocketAddress("localhost", 9070));
			System.out.println("[연결 성공]");
			
		    Thread writer = new Thread(new ClientWriter(socket));
		    Thread receiver = new Thread(new ClientReceiver(socket));
			receiver.start();
			writer.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	static class ClientWriter implements Runnable{
		Socket socket = null;
		BufferedWriter writer;
		
		ClientWriter(Socket socket) throws IOException{
			this.socket = socket;
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		
		public void run() {
			try {
				int i=0;
				while (true) {
					System.out.println("ㅎㅎㅎ");
					writer.write("핳"+(i++)+"\n");
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	static class ClientReceiver implements Runnable{
		Socket socket = null;
		BufferedReader reader;
		
		ClientReceiver(Socket socket) throws IOException{
			this.socket = socket;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		
		public void run() {
			try {
				while (true) {
					String message = reader.readLine();
					System.out.println(message);
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
