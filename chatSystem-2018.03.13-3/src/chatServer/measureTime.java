package chatServer;

import java.util.LinkedList;
import java.util.Queue;

public class measureTime {
	long totalTime=0;

	long cnt=0;
	
	long start;
	long end;
	Queue<Long> queue;
	
	final long limitTime;
	final long limitCnt;
	public measureTime() {
		limitTime = 3000;
		limitCnt = 5;
		queue = new LinkedList<Long>();
		
	}
	
	public measureTime(long limitTime,long limitCnt) {
		
		this.limitTime = limitTime;
		this.limitCnt = limitCnt;
		queue = new LinkedList<Long>();
		
	}
	
	public void startMeasure() {
		start = System.currentTimeMillis();
	}
	
	public boolean isExcessive() {
		long end = System.currentTimeMillis();
		cnt++;
		totalTime = totalTime + (end-start);
/*			System.out.println("totalTime = "+totalTime);
		System.out.println("Cnt = "+cnt);*/
		queue.add(end-start);
		if(cnt>=limitCnt) {
			
			
			if(totalTime <=limitTime) {
	/*			System.out.println("totalTime = "+totalTime);
				System.out.println("limitTime = "+limitTime);*/
				return true;
			}
			totalTime -=queue.remove();
		}

		
		return false;
	}
}
