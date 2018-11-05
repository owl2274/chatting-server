package chatServer;


public interface Author<T>{

	public T write();
	public void close();
}
