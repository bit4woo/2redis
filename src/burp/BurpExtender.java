package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import redis.clients.jedis.Jedis;


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
	Jedis jedis;
	Getter getter = new Getter(helpers);



	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.stderr = new PrintWriter(callbacks.getStderr(), true);
		this.stdout.println(ExtensionName);
		this.stdout.println(github);
		
		gui= new ConfigGUI();

		callbacks.setExtensionName(this.ExtensionName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerHttpListener(this);
		callbacks.addSuiteTab(this);
		
		jedis = new Jedis("localhost");
	}

	
	@Override
	public void extensionUnloaded() {
		callbacks.saveExtensionSetting("knifeconfig", "test");//TODO
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
						//redisWrite(message);
						jedis.lpush("RequestResponseList", message);
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


	public static PrintWriter getStdout() {
		return stdout;
	}


	public static void setStdout(PrintWriter stdout) {
		BurpExtender.stdout = stdout;
	}


	public static PrintWriter getStderr() {
		return stderr;
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
}
