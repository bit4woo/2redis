package burp;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

public class Comsumer extends Thread {
	private final BlockingQueue<CollectEntry> taskQueue;
	private int threadNo;
	private boolean stopflag = false;

	private static IBurpExtenderCallbacks callbacks = BurpExtender.getCallbacks();//静态变量，burp插件的逻辑中，是可以保证它被初始化的。;
	public PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
	public PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);
	public IExtensionHelpers helpers = callbacks.getHelpers();

	public Comsumer(BlockingQueue<CollectEntry> taskQueue,int threadNo) {
		this.threadNo = threadNo;
		this.taskQueue = taskQueue;
		stopflag= false;
	}

	public void stopThread() {
		stopflag = true;
	}

	@Override
	public void run() {
		while(true){
			try {
				if (stopflag) {
					break;
				}
				if (taskQueue.isEmpty()) {
					sleep(1*500);
					continue;
				}
				CollectEntry entry = taskQueue.take();
				storeToRedis(entry);
			} catch (Exception error) {
				error.printStackTrace(stderr);
			}
		}
	}
	
	public static void storeToRedis(CollectEntry entry) {
		if (null == entry) {
			return;
		}
		String key = entry.getUrl();
		if (BurpExtender.jedis.exists(key)) {
			String item = BurpExtender.jedis.get(key); 
			CollectEntry oldEntry = CollectEntry.fromJson(item);
			oldEntry.getParameters().addAll(entry.getParameters());
			//url相同，dirs和filename必然相同，只有参数可能变化。
			BurpExtender.jedis.set(key, oldEntry.toJson());
		}else {
			BurpExtender.jedis.set(key, entry.toJson());
		}
	}
}