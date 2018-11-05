package chatServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;

public class FileLogger implements Subscriber<String>{
	FileChannel fileChannel;
	Charset charset;
	Path path;
	
	Queue<Exception> exceptionQueue;
	
	public FileLogger() {
		path = Paths.get("c:/r_temp/file.txt");
		exceptionQueue = new LinkedList<Exception>();
 
        
        charset = Charset.forName("UTF-8");
        
    
        try {
			Files.createDirectories(path.getParent());
	        fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			exceptionQueue.add(e);

		}
        



	}
	@Override
	public boolean read(String message) {
		ByteBuffer byteBuffer = charset.encode(message+"\n");
		try {
			fileChannel.write(byteBuffer);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			exceptionQueue.add(e);
			return false;
		}
	}
	@Override
	public void close() {
		try {
			fileChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



}
