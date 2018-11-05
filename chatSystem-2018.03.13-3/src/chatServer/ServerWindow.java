package chatServer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;



public class ServerWindow extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	JTextField input;
	JTextArea output;
	
	JScrollPane sp;
	Socket socket = null;
	
	PipeForRead reader;
	PipeForWrite writer;
	
	AccountManager manager;
	
	
	public ServerWindow(String title) {
		super(title);
		
		
		

		

		output=new JTextArea();
		sp=new JScrollPane(output);
		sp.setAutoscrolls(true);

		
		
		input=new JTextField();
		
		output.setFont(new Font("굴림체", Font.BOLD, 18));
		input.setFont(new Font("굴림체", Font.BOLD, 18));
		
		
		output.setFocusable(false);
		
		//getContentPane() : JFrame 인스턴스의 Container 인스턴스를 반환하는 메소드
		Container container=getContentPane();
		
		container.add(sp,BorderLayout.CENTER);
		container.add(input,BorderLayout.SOUTH);

		input.addActionListener(this);
		

		
		//setDefaultCloseOperation(종료옵션) : 닫기 버튼을 누른 경우
		//실행될 이벤트 처리 기능을 변경하는 메소드
		//종료옵션 : 닫기 버튼을 누른 경우에 대한 처리 방법를 설정하기 위한 상수변수
		//1.DO_NOTHING_ON_CLOSE : 프로그램을 종료하지 않는 옵션
		//2.HIDE_ON_CLOSE : 프로그램을 숨기는 옵션(기본)
		//3.DISPOSE_ON_CLOSE : 메모리 정리 후 프로그램 종료
		//4.EXIT_ON_CLOSE : 무조건 프로그램 종료 - System.exit(0)
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setBounds(500, 100, 300, 400);
		setVisible(true);
	}
	public void start() {

		ServerSocket serverSocket = null;
		Socket socket = null;
		ExecutorService executorService = new ThreadPoolExecutor(1, 5, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		MessagePublisher<String> server = new MessagePublisher<String>();
		manager = new AccountManager();
		try {
			serverSocket = new ServerSocket(9070);


			
			System.out.println("서버가 시작되었습니다.");
			server.registerSubscriber(new FileLogger());
			
			
			reader = new PipeForRead();
			writer = new PipeForWrite();
			
			server.registerSubscriber(reader);
			server.contractWith(writer);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						output.append(reader.takeMessage()+"\n");
						sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
						
					}
				}
			}).start();
			while(true) {
				System.out.println("[연결 기다림]");
                socket = serverSocket.accept();
                InetAddress isa =  socket.getInetAddress();
                System.out.println("[연결 수락함] " + isa.getHostName());
 
                executorService.execute(new LoginThread(socket, server));
                
                
                
				
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class LoginThread implements Runnable {
		Socket socket;
		MessagePublisher<String> server;
		public LoginThread(Socket socket,MessagePublisher<String> server) {
			this.socket = socket;
			this.server = server;
		}
		
		public void run() {
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String id = reader.readLine();
				System.out.println(id);
				String pwd = reader.readLine();
				System.out.println(pwd);
				System.out.println("wait for account");
				Account ac = manager.checkAccount(id, pwd);
				if (ac != null) {
					writer.write("success\n");
					writer.flush();
					NetworkClient client = new NetworkClient(ac, socket,server);
					server.contractWith(client);
					server.registerSubscriber(client);
				} else {
					writer.write("failure\n");
					writer.flush();
					writer.close();
					reader.close();
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		ServerWindow client= new ServerWindow("I'm Server");
		client.start();
		
		
	}

	
	
	@Override
	public void actionPerformed(ActionEvent event) {
		//EventObject.getSource() : 이벤트를 발생한 이벤트 소스를 반환하는 메소드
		// => 이벤트가 발생된 컴퍼넌트를 Object 형태로 반환
		Object eventSource=event.getSource();
		
		if(eventSource instanceof JTextField) {
			//컴퍼넌트.getText() : 컴퍼넌트의 문자열을 반환하는 메소드
			String message=input.getText();//입력 문자열을 반환받아 저장
			if(!message.equals("")) {//입력된 문자열이 존재할 경우
				//TextArea.append(String) : TextArea 컴퍼넌트에 문자열을 추가하는 메소드

				
				writer.putMessage(message);
				
				input.setText("");
				
				
			}
		}
	}
	
	
	

	

}
