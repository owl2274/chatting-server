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
		JMenu menu=new JMenu("����(F)");
		exit=new JMenuItem("���α׷� ����(X)");
		
		//setMnemonic(����) : JMenu�� ����Ű�� �ο��ϴ� �޼ҵ�
		// => JMenu�� �󺧸� ����Ű�� ������ ���ڰ� ������ ��� _�� ǥ��
		menu.setMnemonic('F');//Alt+F
		exit.setMnemonic('X');
		
		//setAccelerator(KeyStroke.getKeyStroke(����,���Ű))
		// => JMenuItem�� ����Ű�� �ο��ϴ� �޼ҵ�
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
		String [] states = {"�Ϲ�","�ӼӸ�"}; 
		msgState = new JComboBox<String>(states);
		targetId = new JTextField(8);
		inputMsg = new JTextField(25);
		
		msgState.addActionListener(this);
		targetId.setEnabled(false);
		inputPanel.add(msgState);
		inputPanel.add(targetId);
		inputPanel.add(inputMsg);
		
		outputMsg.setFont(new Font("����ü", Font.BOLD, 18));
		inputMsg.setFont(new Font("����ü", Font.BOLD, 18));
		
		
		outputMsg.setFocusable(false);
		
		//getContentPane() : JFrame �ν��Ͻ��� Container �ν��Ͻ��� ��ȯ�ϴ� �޼ҵ�
		
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
            System.out.println("[���� ��û]");
            socket.connect(new InetSocketAddress("localhost", 9070));
            System.out.println("[���� ����]");
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
		//EventObject.getSource() : �̺�Ʈ�� �߻��� �̺�Ʈ �ҽ��� ��ȯ�ϴ� �޼ҵ�
		// => �̺�Ʈ�� �߻��� ���۳�Ʈ�� Object ���·� ��ȯ
		Object eventSource=event.getSource();
		
		//�̺�Ʈ�� �߻��� ���۳�Ʈ�� �����Ͽ� �̺�Ʈ ó��
		
		if (eventSource == exit) {
			System.exit(0);
		} else if (eventSource == inputMsg) {
			// ���۳�Ʈ.getText() : ���۳�Ʈ�� ���ڿ��� ��ȯ�ϴ� �޼ҵ�
			String message = inputMsg.getText();// �Է� ���ڿ��� ��ȯ�޾� ����
			if (!message.equals("")) {// �Էµ� ���ڿ��� ������ ���
				// TextArea.append(String) : TextArea ���۳�Ʈ�� ���ڿ��� �߰��ϴ� �޼ҵ�
				String state = (String)msgState.getSelectedItem();
				if(state == "�Ϲ�") {
					message = "msg/"+message;
				}else if (state == "�ӼӸ�") {
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
			if(msgState.getSelectedItem() == "�Ϲ�") {
				targetId.setEnabled(false);
			}else if(msgState.getSelectedItem() == "�ӼӸ�") {
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
				outputMsg.append("�������� ������ ������ϴ�.");
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}