package chatServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class NetworkClient implements Author<String>,Subscriber<String>{
	Socket socket;
	Account account;
	BufferedReader reader;
	BufferedWriter writer;
	
	measureTime timer;
	MessagePublisher<String> server;
	
	public NetworkClient(Account account,Socket socket,MessagePublisher<String> server) throws IOException	{
		this.account = account;
		this.socket = socket;
		this.server = server;
		
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		timer = new measureTime(8000,6);
	}
	@Override
	public String write() {
		
		try {
			while (true) {
				timer.startMeasure();
				String message = reader.readLine();
				if (timer.isExcessive())
					return null;

				String[] str = message.split("/");

				switch(str[0]) {
				case "msg":
					message = "[" + account.getId() + "]" + str[1];
					return message;
				case "wsp":
					Iterator<Subscriber<String>> iterator = server.getSubscriberList();
					String [] whisper = str[1].split(":");

					while(iterator.hasNext()) {
						NetworkClient target;
						try {
							target = (NetworkClient)iterator.next();
						}catch (ClassCastException e) {
							continue;
						}
						if(whisper[0].equals(target.getId())) {
		
							message = "("+account.getId()+"->"+target.getId()+")"+whisper[1];
//							target.read(message);
							ArrayList<Subscriber<String>> arrays = new ArrayList<Subscriber<String>>();
							arrays.add(this);
							arrays.add(target);
							server.sendToTargets(message, arrays.iterator());
							break;
						}
					}
					
					break;
				default:
					return null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	@Override
	public boolean read(String message) {
		try {
			writer.write(message+"\n");
			
			writer.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getId() {
		return account.getId();
	}
}
