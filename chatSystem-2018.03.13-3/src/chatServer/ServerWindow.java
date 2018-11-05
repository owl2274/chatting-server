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
		
		output.setFont(new Font("����ü", Font.BOLD, 18));
		input.setFont(new Font("����ü", Font.BOLD, 18));
		
		
		output.setFocusable(false);
		
		//getContentPane() : JFrame �ν��Ͻ��� Container �ν��Ͻ��� ��ȯ�ϴ� �޼ҵ�
		Container container=getContentPane();
		
		container.add(sp,BorderLayout.CENTER);
		container.add(input,BorderLayout.SOUTH);

		input.addActionListener(this);
		

		
		//setDefaultCloseOperation(����ɼ�) : �ݱ� ��ư�� ���� ���
		//����� �̺�Ʈ ó�� ����� �����ϴ� �޼ҵ�
		//����ɼ� : �ݱ� ��ư�� ���� ��쿡 ���� ó�� ����� �����ϱ� ���� �������
		//1.DO_NOTHING_ON_CLOSE : ���α׷��� �������� �ʴ� �ɼ�
		//2.HIDE_ON_CLOSE : ���α׷��� ����� �ɼ�(�⺻)
		//3.DISPOSE_ON_CLOSE : �޸� ���� �� ���α׷� ����
		//4.EXIT_ON_CLOSE : ������ ���α׷� ���� - System.exit(0)
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


			
			System.out.println("������ ���۵Ǿ����ϴ�.");
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
				System.out.println("[���� ��ٸ�]");
                socket = serverSocket.accept();
                InetAddress isa =  socket.getInetAddress();
                System.out.println("[���� ������] " + isa.getHostName());
 
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
		//EventObject.getSource() : �̺�Ʈ�� �߻��� �̺�Ʈ �ҽ��� ��ȯ�ϴ� �޼ҵ�
		// => �̺�Ʈ�� �߻��� ���۳�Ʈ�� Object ���·� ��ȯ
		Object eventSource=event.getSource();
		
		if(eventSource instanceof JTextField) {
			//���۳�Ʈ.getText() : ���۳�Ʈ�� ���ڿ��� ��ȯ�ϴ� �޼ҵ�
			String message=input.getText();//�Է� ���ڿ��� ��ȯ�޾� ����
			if(!message.equals("")) {//�Էµ� ���ڿ��� ������ ���
				//TextArea.append(String) : TextArea ���۳�Ʈ�� ���ڿ��� �߰��ϴ� �޼ҵ�

				
				writer.putMessage(message);
				
				input.setText("");
				
				
			}
		}
	}
	
	
	

	

}
