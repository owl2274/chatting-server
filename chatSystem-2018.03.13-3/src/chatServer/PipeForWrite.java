package chatServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class PipeForWrite implements Author<String> {
	BlockingQueue<String> messageQueue;
	
	public PipeForWrite() {
		messageQueue = new PriorityBlockingQueue<String>();
	}
	
	public void putMessage(String message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String write() {
		
		try {
			return messageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void close() {
		
	}

}
