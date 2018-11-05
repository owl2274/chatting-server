package chatServer;

public interface Subscriber<T> {
	public boolean read(T message);
	public void close();
}
