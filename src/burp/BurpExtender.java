package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IExtensionStateListener;
import burp.IHttpRequestResponse;
import burp.IHttpService;
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



	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.stderr = new PrintWriter(callbacks.getStderr(), true);
		this.stdout.println(ExtensionName);
		this.stdout.println(github);
		
		ConfigGUI gui= new ConfigGUI();

		callbacks.setExtensionName(this.ExtensionName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerHttpListener(this);
		callbacks.addSuiteTab(this);
	}

	
	@Override
	public void extensionUnloaded() {
		callbacks.saveExtensionSetting("knifeconfig", "test");//TODO
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (!messageIsRequest) {//当时response的时候再进行存储，以便获取整个请求响应对
			
			String message = new LineEntry(messageInfo).ToJson();
			redisWrite(message);
//			//add/update/append header
//			if (toolFlag == (toolFlag & gui.checkEnabledFor())) {
//				//if ((config.isOnlyForScope() && callbacks.isInScope(url))|| !config.isOnlyForScope()) {
//				if (!gui.config.isOnlyForScope()||callbacks.isInScope(url)){
//					try {
//						String message = new LineEntry(messageInfo).ToJson();
//						redisWrite(message);
//					} catch (Exception e) {
//						stderr.print(e.getStackTrace());
//					}
//				}
//			}
		}else {//response

		}
	}
	
    public static void redisWrite(String message) {
        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        stdout.println("连接成功");
        //存储数据到列表中
        jedis.lpush("RequestResponseList", message);
        // 获取存储的数据并输出
        List<String> list = jedis.lrange("RequestResponseList", 0 ,2);
        for(int i=0; i<list.size(); i++) {
        	stdout.println("列表项为: "+list.get(i));
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
		// TODO Auto-generated method stub
		return "2redis";
	}


	@Override
	public Component getUiComponent() {
		// TODO Auto-generated method stub
		return gui.getContentPane();
	}
}
