package burp;

import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	BlockingQueue<CollectEntry> taskQueue;
	Comsumer comsumer;


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
		taskQueue = new LinkedBlockingQueue<CollectEntry>();
		comsumer = new Comsumer(taskQueue,1);
		comsumer.start();
	}

	
	@Override
	public void extensionUnloaded() {
//		callbacks.saveExtensionSetting("knifeconfig", "test");//TODO
		comsumer.stopThread();
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (!messageIsRequest) {//当时response的时候再进行存储，以便获取整个请求响应对
			if (toolFlag == IBurpExtenderCallbacks.TOOL_PROXY) {
				CollectEntry entry = parser(messageInfo);
				if (null != entry) {
					taskQueue.add(entry);
				}
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
		url = formateURL(url);
		String mimeType = getMimeType(messageInfo);
		int status = getter.getStatusCode(messageInfo);
		String refer = getter.getHeaderValueOf(true, messageInfo,"Referer");
		if (null == refer) return null;
		if (Util.uselessExtension(url.getPath())) return null;
		if (mimeType.equalsIgnoreCase("image")) return null;
		if (status!=200 && status!=401) return null;

		List<IParameter> paras = getter.getParas(messageInfo);
		Set<String> paraNames = new HashSet<String>();
		for (IParameter para:paras) {
			if (para.getType() != IParameter.PARAM_COOKIE) {
				//不包含cookie参数，因为统一登录的情况下，cookie参数会在多个站点出现，导致排名靠前。
				//另外cookie参数对于参数爆破似乎也没有意义。
				String name = para.getName();
				paraNames.add(name);
			}
		}
		
		String urlAsKey = url.toString().replaceFirst(url.getProtocol()+"://", "");
		
		String path = url.getPath();
		if (path.startsWith("/")) {
			path = path.replaceFirst("/", "");
		}
		String[] tmp = path.split("/");
		Set<String> dirs = new HashSet<String>();
		String filename = "";
		for (int i =0;i<tmp.length;i++) {
			String item = tmp[i];
			if (!item.equals("")){
				if (i==tmp.length-1) {
					filename = item;
				}else {
					dirs.add(item);
				}
			}
		}
		
		return new CollectEntry(urlAsKey,dirs,filename,paraNames);
	}
	
	public static URL formateURL(URL url){
		String urlwithoutquery = url.toString();
		
		if (urlwithoutquery.contains("?")) {
			urlwithoutquery = urlwithoutquery.substring(0, urlwithoutquery.indexOf("?"));
		}
		if ( 443 == url.getPort() || 80 == url.getPort() ) {//-1 if the port is not set
			urlwithoutquery = urlwithoutquery.replaceFirst(":"+url.getPort(), "");
		}
		urlwithoutquery = urlwithoutquery.replaceFirst(url.getProtocol()+"://", "http://");
		
		try {
			return new URL(urlwithoutquery);
		} catch (MalformedURLException e) {
			return url;
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
	
	public static void main(String[] args) throws Exception {
		URL url = new URL("https://esg-fisaas-core-shenzhen-xili1-oss.sit.sf-express.com/v1.2/AUTH_ESG-FISAAS-CORE/sfosspublic001/5c4d2df4524b4c38a47b6157cea87b25.png/");
		System.out.println(url.getPort());
		String urlpath = url.getPath();
		File file = new File (urlpath);
		
		System.out.println(file.getPath());
		System.out.println(file.getParent());
		String[] tmp = file.getParent().split("\\");
		for (String aa:tmp) {
			System.out.println(aa+" 111");
		}
//		String item = jedis.get("11111");
//		System.out.println(item);
	}
}
