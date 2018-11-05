package chatServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class PipeForRead implements Subscriber<String> {
	BlockingQueue<String> messageQueue;
	
	public PipeForRead() {
		messageQueue = new PriorityBlockingQueue<String>();
	}
	
	public String takeMessage() {
		try {
			return messageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean read(String message) {
		try {
			messageQueue.put(message);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
