package chatServer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessagePublisher<T> implements Publisher<T> {
	Collection<Subscriber<T>> subscriberVector;
	Collection<Author<T>> authorVector;
//	Hashtable<Author<T>,Future<?>> authorHash;
	
	BlockingQueue<T> messageQueue;
	Thread transmission;
	ExecutorService executorService;
	
	Queue<Exception> exceptionQueue;

	public MessagePublisher() {
		subscriberVector = new Vector<Subscriber<T>>();
		authorVector = new Vector<Author<T>>();
		messageQueue = new PriorityBlockingQueue<T>();
		transmission = new Transmission();
		transmission.start();
		executorService = new ThreadPoolExecutor(3, 50, 300L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	@Override
	public void contractWith(Author<T> author) {
		synchronized (authorVector) {
			authorVector.add(author);
		}
		executorService.submit(new AuthorRoom(author));
//		new Thread(new AuthorRoom(author)).start();
	}

	@Override
	public void breakContract(Author<T> author) {
		synchronized (authorVector) {
			authorVector.remove(author);
			author.close();
		}
	}

	@Override
	public void registerSubscriber(Subscriber<T> subscriber) {
		synchronized (subscriberVector) {
			subscriberVector.add(subscriber);
		}
	}

	@Override
	public void removeSubscriber(Subscriber<T> Subscriber) {
		synchronized (subscriberVector) {
			subscriberVector.remove(Subscriber);
			Subscriber.close();
		}
	}
	public void removeAllSubscribers(Collection<Subscriber<T>> subscribers) {
		synchronized (subscriberVector){
			for(Subscriber<T> subscriber:subscribers) {
				subscriberVector.remove(subscriber);
				subscriber.close();
			}
		}
	}
	
	public void postMessage() {

		Collection<Subscriber<T>> removeSubscribers = new Vector<Subscriber<T>>();
		
				T message;
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					exceptionQueue.add(e);
					return;
				}
				synchronized (subscriberVector) {
					for (Subscriber<T> subscriber : subscriberVector) {
						
						if (!subscriber.read(message))
							removeSubscribers.add(subscriber);
					}
					
				}
				subscriberVector.removeAll(removeSubscribers);
		
	}
	//모든 subscriber가 아닌, 특정 subscriber에게만 메세지를 전달하기 위한 메소드. 클래스 외부에서 author에 의해 사용되는 경우가 많다. 
	public void sendToTargets(T message,Iterator<Subscriber<T>> Subscribers) {
		Collection<Subscriber<T>> removeSubscribers = new Vector<Subscriber<T>>();
		
		while(Subscribers.hasNext()) {
			Subscriber<T> subscriber = Subscribers.next();
			if (!subscriber.read(message))
				removeSubscribers.add(subscriber);
		}
		
		subscriberVector.removeAll(removeSubscribers);
		
	}
	public Iterator<Author<T>> getAuthorList(){
		return authorVector.iterator();
	}
	
	public Iterator<Subscriber<T>> getSubscriberList(){
		return subscriberVector.iterator();
	}
	
	public void receiveMessage(T message) {
		System.out.println(message);
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			exceptionQueue.add(e);
		}
		
	}

	
	public Exception getException() {
		return null;
	}

	class AuthorRoom implements Runnable {
		Author<T> author;
		
		public AuthorRoom(Author<T> author) {
			this.author = author;
		}

		@Override
		public void run() {
			while (true) {
				T message = author.write();
		
				if (message == null) {
					breakContract(author);
					System.out.println(Thread.currentThread().getName()+" is down");
					return;
				}
				receiveMessage(message);
				
			}
		}
		
	}
	class Transmission extends Thread{

		@Override
		public void run() {
			while(true) {
	
				postMessage();
				
				
			}
			
		}
		
	}
}
