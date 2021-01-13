package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	static Jedis jedis;
	Getter getter;



	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.getter= new Getter(helpers);
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
			if (toolFlag == IBurpExtenderCallbacks.TOOL_PROXY) {
				storeToRedis(parser(messageInfo));//TODO 需不需要异步呢？先试试
			}
		}
	}
	
	public String getMimeType(IHttpRequestResponse messageInfo) {
		try {
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
			String MIMEtype = analyzeResponse.getStatedMimeType();
			if(MIMEtype == null) {
				MIMEtype = analyzeResponse.getInferredMimeType();
			}
			return MIMEtype;
		} catch (Exception e) {
			return null;
			//e.printStackTrace();
		}
	}
	
	public CollectEntry parser(IHttpRequestResponse messageInfo) {
		URL url = getter.getFullURL(messageInfo);
		String mimeType = getMimeType(messageInfo);
		int status = getter.getStatusCode(messageInfo);
		if (Util.uselessExtension(url.getPath())) return null;
		if (mimeType.equalsIgnoreCase("image")) return null;
		if (status!=200 && status!=401) return null;
		
		String urlwithoutquery = url.toString();
		if (urlwithoutquery.contains("?")) {
			urlwithoutquery = urlwithoutquery.substring(0, urlwithoutquery.indexOf("?"));
		}
		List<IParameter> paras = getter.getParas(messageInfo);
		Set<String> paraNames = new HashSet<String>();
		for (IParameter para:paras) {
			String name = para.getName();
			paraNames.add(name);
		}
		
		String path = url.getPath();
		String[] tmp = path.split("/");
		Set<String> dirs = new HashSet<String>();
		String filename = "";
		for (int i =0;i<tmp.length;i++) {
			String item = tmp[i];
			if (!item.equals("")){
				if (i==tmp.length-1 && path.endsWith("/")) {
					filename = item;
				}else {
					dirs.add(item);
				}
			}
		}
		
		return new CollectEntry(urlwithoutquery,dirs,filename,paraNames);
	}
	
	public static void storeToRedis(CollectEntry entry) {
		String key = entry.getUrl();
		String item = jedis.get(key);
		if (null != item) {
			CollectEntry oldEntry = CollectEntry.fromJson(item);
			oldEntry.getParameters().addAll(entry.getParameters());
			//url相同，dirs和filename必然相同，只有参数可能变化。
			jedis.lpush(key, oldEntry.toJson());
		}else {
			jedis.lpush(key, entry.toJson());
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
	
	public static void main(String[] args) {
		String url = "/admin/queryPageBtn/";
		System.out.println(url.split("/").length);
		String[] tmp = url.split("/");
		for (String aa:tmp) {
			System.out.println(aa+"111");
		}
		
				
	}
}
