package burp;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

public class LineEntry {

	private int port =-1;
	private String host = "";
	private String protocol ="";
	//these three == IHttpService, helpers.buildHttpService to build. 

	private byte[] request = {};
	private byte[] response = {};
	// request+response+httpService == IHttpRequestResponse

	//used in UI,the fields to show
	private String url = "";
	private int statuscode = -1;
	private int contentLength = -1;
	private String MIMEtype = "";
	private String title = "";
	private String IP = "";
	private String CDN = "";
	private String webcontainer = "";
	private String time = "";


	@JSONField(serialize=false)
	private String messageText = "";//use to search
	@JSONField(serialize=false)
	private String bodyText = "";//use to adjust the response changed or not
	//don't store these two field to reduce config file size.

	//field for user 
	private boolean isNew =true;
	private boolean isChecked =true;
	private String comment ="";

	@JSONField(serialize=false)//表明不序列号该字段,messageinfo对象不能被fastjson成功序列化
	private IHttpRequestResponse messageinfo;

	//remove IHttpRequestResponse field ,replace with request+response+httpService(host port protocol). for convert to json.

	@JSONField(serialize=false)//表明不序列号该字段
	private BurpExtender burp;
	@JSONField(serialize=false)
	private IExtensionHelpers helpers;
	@JSONField(serialize=false)
	private IBurpExtenderCallbacks callbacks;

	LineEntry(){

	}

	public LineEntry(IHttpRequestResponse messageinfo) {
		this.messageinfo = messageinfo;
		this.callbacks = BurpExtender.getCallbacks();
		this.helpers = this.callbacks.getHelpers();
		parse();
	}

	public LineEntry(IHttpRequestResponse messageinfo,boolean isNew,boolean Checked,String comment) {
		this.messageinfo = messageinfo;
		this.callbacks = BurpExtender.getCallbacks();
		this.helpers = this.callbacks.getHelpers();
		parse();

		this.isNew = isNew;
		this.isChecked = Checked;
		this.comment = comment;
	}

	public LineEntry(IHttpRequestResponse messageinfo,boolean isNew,boolean Checked,String comment,Set<String> IPset,Set<String> CDNset) {
		this.messageinfo = messageinfo;
		this.callbacks = BurpExtender.getCallbacks();
		this.helpers = this.callbacks.getHelpers();
		parse();

		this.isNew = isNew;
		this.isChecked = Checked;
		this.comment = comment;
		if (this.IP != null) {
			this.IP = IPset.toString().replace("[", "").replace("]", "");
		}

		if (this.CDN != null) {
			this.CDN = CDNset.toString().replace("[", "").replace("]", "");
		}
	}

	@JSONField(serialize=false)//表明不序列号该字段
	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return JSONObject.toJSONString(this);
	}

	public static LineEntry FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return JSON.parseObject(json, LineEntry.class);
	}

	public void parse() {
		try {
			IResponseInfo responseInfo = helpers.analyzeResponse(messageinfo.getResponse());
			IHttpService service = this.messageinfo.getHttpService();
			port = service.getPort();
			host = service.getHost();
			protocol = service.getProtocol();

			request = messageinfo.getRequest();
			response = messageinfo.getResponse();

			Getter getter = new Getter(helpers);

			messageText = new String(messageinfo.getRequest())+new String(messageinfo.getResponse());

			statuscode = responseInfo.getStatusCode();

			MIMEtype = responseInfo.getStatedMimeType();
			if(MIMEtype == null) {
				MIMEtype = responseInfo.getInferredMimeType();
			}

			url = this.messageinfo.getHttpService().toString();


			webcontainer = getter.getHeaderValueOf(false, messageinfo, "Server");

			bodyText = new String(getter.getBody(false, messageinfo));
			try{
				contentLength = Integer.parseInt(getter.getHeaderValueOf(false, messageinfo, "Content-Length").trim());
			}catch (Exception e){
				if (contentLength==-1 && bodyText!=null) {
					contentLength = bodyText.length();
				}
			}

			Pattern p = Pattern.compile("<title(.*?)</title>");
			//<title ng-bind="service.title">The Evolution of the Producer-Consumer Problem in Java - DZone Java</title>
			Matcher m  = p.matcher(bodyText);
			while ( m.find() ) {
				title = m.group(0);
			}
			if (title.equals("")) {
				Pattern ph = Pattern.compile("<title [.*?]>(.*?)</title>");
				Matcher mh  = ph.matcher(bodyText);
				while ( mh.find() ) {
					title = mh.group(0);
				}
			}
			if (title.equals("")) {
				Pattern ph = Pattern.compile("<h[1-6]>(.*?)</h[1-6]>");
				Matcher mh  = ph.matcher(bodyText);
				while ( mh.find() ) {
					title = mh.group(0);
				}
			}

			/*
			编码转没有成功，好像还引起了栈溢出....奇怪！
			 */

//			if (!title.equals("")){//编码转换
//				String charSet = getResponseCharset(messageinfo);
//				Charset systemCharset = Charset.defaultCharset();
//				if (charSet != null){
//					title = new String(title.getBytes(charSet),systemCharset);
//				}
//			}



			time = Util.getNowTimeString();//这是动态的，会跟随系统时间自动变化


		}catch(Exception e) {
			//e.printStackTrace(burp.stderr);
		}
	}

	public void DoDirBrute() {

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatuscode() {
		return statuscode;
	}

	public void setStatuscode(int statuscode) {
		this.statuscode = statuscode;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public String getMIMEtype() {
		return MIMEtype;
	}

	public void setMIMEtype(String mIMEtype) {
		MIMEtype = mIMEtype;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getCDN() {
		return CDN;
	}

	public void setCDN(String cDN) {
		CDN = cDN;
	}

	public String getWebcontainer() {
		return webcontainer;
	}

	public void setWebcontainer(String webcontainer) {
		this.webcontainer = webcontainer;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	/*
	 * public String getMessageText() { return messageText; }
	 *
	 * public void setMessageText(String messageText) { this.messageText =
	 * messageText; }
	 */


	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public IHttpRequestResponse getMessageinfo() {
		//		if (messageinfo == null){
		//			try{
		//				messageinfo = callbacks.getHelpers().buildHttpMessage()
		//				IHttpRequestResponse messageinfo = new IHttpRequestResponse();
		//				messageinfo.setRequest(this.request);//始终为空，why??? because messageinfo is null ,no object to set content.
		//				messageinfo.setRequest(this.response);
		//				IHttpService service = callbacks.getHelpers().buildHttpService(this.host,this.port,this.protocol);
		//				messageinfo.setHttpService(service);
		//			}catch (Exception e){
		//				System.out.println("error "+url);
		//			}
		//		}
		return messageinfo;
	}

	public void setMessageinfo(IHttpRequestResponse messageinfo) {
		this.messageinfo = messageinfo;
	}

	public String getBodyText() {
		IResponseInfo analyzeResponse = helpers.analyzeResponse(this.response);//java.lang.NullPointerException why???? 
		// helpers will be null if this object is recovered from json.
		//IResponseInfo analyzeResponse = helpers.analyzeResponse(this.getResponse()); 
		int bodyOffset = analyzeResponse.getBodyOffset();
		byte[] byte_body = Arrays.copyOfRange(this.response, bodyOffset, this.response.length);//not length-1
		return new String(byte_body);
	}


	/*
Content-Type: text/html;charset=UTF-8

<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta charset="utf-8">
<script type="text/javascript" charset="utf-8" src="./resources/jrf-resource/js/jrf.min.js"></script>
 */
	public String getResponseCharset(IHttpRequestResponse messageInfo){
		Getter getter = new Getter(helpers);
		String contentType = getter.getHeaderValueOf(false,messageInfo,"Content-Type");
		String charSet = null;
		if (contentType.toLowerCase().contains("charset=")){
			charSet = contentType.toLowerCase().split("charset=")[1];
		}else {
			byte[] body = getter.getBody(false,messageInfo);
			Pattern pDomainNameOnly = Pattern.compile("charset=(.*?)>");
			Matcher matcher = pDomainNameOnly.matcher(new String(body));
			if (matcher.find()) {
				charSet = matcher.group(0).toLowerCase();
				charSet = charSet.replace("\"","");
				charSet = charSet.replace(">","");
				charSet = charSet.replace("/","");
				charSet = charSet.replace("charset=","");
			}
		}
		return charSet;
	}

	public String getHeaderValueOf(boolean messageIsRequest,String headerName) {
		helpers = BurpExtender.getCallbacks().getHelpers();
		List<String> headers=null;
		if(messageIsRequest) {
			if (this.request == null) {
				return null;
			}
			IRequestInfo analyzeRequest = helpers.analyzeRequest(this.request);
			headers = analyzeRequest.getHeaders();
		}else {
			if (this.response == null) {
				return null;
			}
			IResponseInfo analyzeResponse = helpers.analyzeResponse(this.response);
			headers = analyzeResponse.getHeaders();
		}


		headerName = headerName.toLowerCase().replace(":", "");
		String Header_Spliter = ": ";
		for (String header : headers) {
			if (header.toLowerCase().startsWith(headerName)) {
				return header.split(Header_Spliter, 2)[1];//分成2部分，Location: https://www.jd.com
			}
		}
		return null;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public byte[] getRequest() {
		return request;
	}

	public void setRequest(byte[] request) {
		this.request = request;
	}

	public byte[] getResponse() {
		return response;
	}

	public void setResponse(byte[] response) {
		this.response = response;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public IExtensionHelpers getHelpers() {
		return helpers;
	}

	public void setHelpers(IExtensionHelpers helpers) {
		this.helpers = helpers;
	}

	public Object getValue(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String args[]) {
		LineEntry x = new LineEntry();
		x.setRequest("xxxxxx".getBytes());
		System.out.println(JSON.toJSON(x));

		System.out.println(JSON.toJSONString(x));
		System.out.println(JSONObject.toJSONString(x));
		System.out.println(JSONObject.toJSON(x));
	}
}
