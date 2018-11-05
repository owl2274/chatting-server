package chatServer;


public interface Publisher<T>{
	public void contractWith(Author<T> author);
	public void breakContract(Author<T> author);
	public void registerSubscriber(Subscriber<T> subscriber);
	public void removeSubscriber(Subscriber<T> Subscriber);
}
