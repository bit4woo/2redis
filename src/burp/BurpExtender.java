package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;


public class BurpExtender implements IBurpExtender,IHttpListener,IExtensionStateListener,ITab {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public static String ExtensionName = "2redis v0.1 by bit4woo";
	public static String github = "https://github.com/bit4woo/2redis";
	public static PrintWriter stdout;
	public static PrintWriter stderr;
	
	public IExtensionHelpers helpers;
	public int proxyServerIndex=-1;
	ConfigGUI gui;
	RedisClient redisClient;
	StatefulRedisConnection<String, String> connection;
	Getter getter;

	private static void flushStd(){
		try{
			stdout = new PrintWriter(callbacks.getStdout(), true);
			stderr = new PrintWriter(callbacks.getStderr(), true);
		}catch (Exception e){
			stdout = new PrintWriter(System.out, true);
			stderr = new PrintWriter(System.out, true);
		}
	}

	public static PrintWriter getStdout() {
		flushStd();//不同的时候调用这个参数，可能得到不同的值
		return stdout;
	}

	public static PrintWriter getStderr() {
		flushStd();
		return stderr;
	}

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.stderr = new PrintWriter(callbacks.getStderr(), true);
		this.stdout.println(ExtensionName);
		this.stdout.println(github);
		this.getter = new Getter(helpers);
		
		gui= new ConfigGUI();

		callbacks.setExtensionName(this.ExtensionName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerHttpListener(this);
		callbacks.addSuiteTab(this);
		
		
		redisClient = RedisClient.create("redis://localhost/0");
		connection = redisClient.connect();

		stdout.println("Connected to Redis");
		System.out.println("Connected to Redis");
	}

	
	@Override
	public void extensionUnloaded() {
		callbacks.saveExtensionSetting("knifeconfig", "test");//TODO
		connection.close();
		redisClient.shutdown(); 
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (!messageIsRequest) {//当时response的时候再进行存储，以便获取整个请求响应对
			if (toolFlag == (toolFlag & gui.checkEnabledFor())) {
				URL url = getter.getURL(messageInfo);
				String mimeType = getter.getMimeType(messageInfo);
				if (Util.uselessExtension(url.getPath())) return;
				if (mimeType.equalsIgnoreCase("image")) return;
				//if ((config.isOnlyForScope() && callbacks.isInScope(url))|| !config.isOnlyForScope()) {
				if (!gui.config.isOnlyForScope()||callbacks.isInScope(url)){
					try {
						String message = new LineEntry(messageInfo).ToJson();
						connection.sync().set("RequestResponseList", message);//异步方式，提高速度
						stdout.println("push to redis: "+url.toString());
					} catch (Exception e) {
						stderr.print(e.getStackTrace());
					}
				}
			}
		}
	}

	public static IBurpExtenderCallbacks getCallbacks() {
		return callbacks;
	}


	public static void setCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
	}


	public static String getExtensionName() {
		return ExtensionName;
	}


	public static void setExtensionName(String extensionName) {
		ExtensionName = extensionName;
	}


	public static String getGithub() {
		return github;
	}


	public static void setGithub(String github) {
		BurpExtender.github = github;
	}

	public static void setStdout(PrintWriter stdout) {
		BurpExtender.stdout = stdout;
	}


	public static void setStderr(PrintWriter stderr) {
		BurpExtender.stderr = stderr;
	}


	@Override
	public String getTabCaption() {
		return "2redis";
	}


	@Override
	public Component getUiComponent() {
		return gui.getContentPane();
	}
	
	public static void main(String args[]) {
		RedisClient redisClient = RedisClient.create("redis://localhost/0");
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		System.out.println("Connected to Redis");
		connection.sync().set("key", "Hello World");

		connection.close();
		redisClient.shutdown(); 
	}
}
