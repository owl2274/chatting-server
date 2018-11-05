package chatClient;

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
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;



public class ClientGui extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	JTextField inputId;
	JTextField inputPwd;
	JLabel idLabel;
	JLabel pwdLabel;
	JButton sendAccount;
	JScrollPane sp;
	JTextField inputMsg;
	JTextArea outputMsg;
	JMenuItem exit;
	JComboBox<String> msgState;
	JTextField targetId;

	
	Socket socket = null;
	BufferedWriter writer;
	
	
	public ClientGui(String title) {
		super(title);
		
		JMenuBar mb=new JMenuBar();
		JMenu menu=new JMenu("파일(F)");
		exit=new JMenuItem("프로그램 종료(X)");
		
		//setMnemonic(문자) : JMenu에 단축키를 부여하는 메소드
		// => JMenu의 라벨명에 단축키와 동일한 문자가 존재할 경우 _로 표시
		menu.setMnemonic('F');//Alt+F
		exit.setMnemonic('X');
		
		//setAccelerator(KeyStroke.getKeyStroke(문자,기능키))
		// => JMenuItem에 단축키를 부여하는 메소드
		exit.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		//exit.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		
		menu.addSeparator();
		menu.add(exit);
		mb.add(menu);
		setJMenuBar(mb);
		
		socket = new Socket();
		
		inputId = new JTextField(8);
		inputPwd = new JTextField(12);
		idLabel = new JLabel("ID:");
		pwdLabel = new JLabel("password:");
		sendAccount = new JButton("login");
		
		JPanel [] panels = {new JPanel(),new JPanel()};
		panels[0].add(idLabel);
		panels[0].add(inputId);
		
		panels[1].add(pwdLabel);
		panels[1].add(inputPwd);
		JPanel panel = new JPanel();
		
		panel.add(panels[0]);
		panel.add(panels[1]);
		panel.add(sendAccount);
		
		outputMsg=new JTextArea();
		sp=new JScrollPane(outputMsg);
		
		JPanel inputPanel = new JPanel();
		String [] states = {"일반","귓속말"}; 
		msgState = new JComboBox<String>(states);
		targetId = new JTextField(8);
		inputMsg = new JTextField(25);
		
		msgState.addActionListener(this);
		targetId.setEnabled(false);
		inputPanel.add(msgState);
		inputPanel.add(targetId);
		inputPanel.add(inputMsg);
		
		outputMsg.setFont(new Font("굴림체", Font.BOLD, 18));
		inputMsg.setFont(new Font("굴림체", Font.BOLD, 18));
		
		
		outputMsg.setFocusable(false);
		
		//getContentPane() : JFrame 인스턴스의 Container 인스턴스를 반환하는 메소드
		
		Container container=getContentPane();
		container.add(panel,BorderLayout.NORTH);
		container.add(sp,BorderLayout.CENTER);
		container.add(inputPanel,BorderLayout.SOUTH);

		outputMsg.setEnabled(false);
		inputMsg.setEnabled(false);
		
		sendAccount.addActionListener(this);
		exit.addActionListener(this);
		inputMsg.addActionListener(this);
		

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setBounds(500, 100, 450, 400);
		setVisible(true);
	}
	public boolean tryConnect() {
		

        try {
            
        	socket = new Socket();
            System.out.println("[연결 요청]");
            socket.connect(new InetSocketAddress("localhost", 9070));
            System.out.println("[연결 성공]");
			Thread receiver = new Thread(new ClientReceiver(socket));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String id = inputId.getText();
			String pwd = inputPwd.getText();
			writer.write(id+"\n");
		
			writer.write(pwd+"\n");
			writer.flush();
			System.out.println(id+ " " +pwd);
			System.out.println("wait for answer");
			
			String answer = reader.readLine();
			if(answer.equals("success")) {
				receiver.start();
				
				return true;
			}else if(answer.equals("failure")) {
				System.out.println("fail");
				socket.close();
				return false;
			}else {
				socket.close();
				return false;
			}
			
			
        } catch (IOException e) {
        	e.printStackTrace();
        	return false;
        }
		
	}
	
	public static void main(String[] args) {
		ClientGui client= new ClientGui("chatClient");
		
		
	}

	
	
	@Override
	public void actionPerformed(ActionEvent event) {
		//EventObject.getSource() : 이벤트를 발생한 이벤트 소스를 반환하는 메소드
		// => 이벤트가 발생된 컴퍼넌트를 Object 형태로 반환
		Object eventSource=event.getSource();
		
		//이벤트가 발생된 컴퍼넌트를 구분하여 이벤트 처리
		
		if (eventSource == exit) {
			System.exit(0);
		} else if (eventSource == inputMsg) {
			// 컴퍼넌트.getText() : 컴퍼넌트의 문자열을 반환하는 메소드
			String message = inputMsg.getText();// 입력 문자열을 반환받아 저장
			if (!message.equals("")) {// 입력된 문자열이 존재할 경우
				// TextArea.append(String) : TextArea 컴퍼넌트에 문자열을 추가하는 메소드
				String state = (String)msgState.getSelectedItem();
				if(state == "일반") {
					message = "msg/"+message;
				}else if (state == "귓속말") {
					message = "wsp/"+targetId.getText()+":"+message;
				}
					
				
				try {
					writer.write(message+"\n");
					writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				inputMsg.setText("");
				
				
			}
		}else if(eventSource == sendAccount) {
			if(tryConnect()) {
				inputId.setEnabled(false);
				inputPwd.setEnabled(false);
				sendAccount.setEnabled(false);
				outputMsg.setEnabled(true);
				inputMsg.setEnabled(true);
			}
		}else if(eventSource == msgState) {
			if(msgState.getSelectedItem() == "일반") {
				targetId.setEnabled(false);
			}else if(msgState.getSelectedItem() == "귓속말") {
				targetId.setEnabled(true);
			}
				
		}
	}
	
	class ClientReceiver extends Thread{
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
					if(message==null)
						break;
					System.out.println(message);
					outputMsg.append(message + "\n");
					sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				}
				outputMsg.append("서버와의 연결이 끊겼습니다.");
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}