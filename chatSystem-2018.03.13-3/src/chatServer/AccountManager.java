package chatServer;

import java.util.HashMap;

public class AccountManager {
	HashMap<String,Account> accounts;
	public AccountManager() {
		accounts = new HashMap<String,Account>();
		accounts.put("ȫ�浿", new Account("ȫ�浿", "ȫ�浿"));
		accounts.put("����ġ", new Account("����ġ", "����ġ"));
		accounts.put("������", new Account("������", "������"));
		accounts.put("�ƹ���", new Account("�ƹ���", "�ƹ���"));
	}
	Account checkAccount(String id,String password) {
		Account ac = accounts.get(id);
		
		try {
			if(ac.getPassword().equals(password))
				return ac;
			else 
				return null;
		}catch(NullPointerException e) {
			return null;
		}
		
	}
}
