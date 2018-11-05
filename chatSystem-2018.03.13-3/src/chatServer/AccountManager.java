package chatServer;

import java.util.HashMap;

public class AccountManager {
	HashMap<String,Account> accounts;
	public AccountManager() {
		accounts = new HashMap<String,Account>();
		accounts.put("홍길동", new Account("홍길동", "홍길동"));
		accounts.put("전우치", new Account("전우치", "전우치"));
		accounts.put("일지매", new Account("일지매", "일지매"));
		accounts.put("아무개", new Account("아무개", "아무개"));
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
