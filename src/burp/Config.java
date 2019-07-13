package burp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

public class Config {
	
	private String ConfigName = "";
	private int enableStatus = IBurpExtenderCallbacks.TOOL_PROXY;
	private boolean onlyForScope = true;
    
	Config(){
    	//to resolve "default constructor not found" error
	}
    
	public Config(String ConfigName){
		this.ConfigName = ConfigName;
	}

	public String getConfigName() {
		return ConfigName;
	}

	public void setConfigName(String configName) {
		ConfigName = configName;
	}

	public int getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(int enableStatus) {
		this.enableStatus = enableStatus;
	}

	public boolean isOnlyForScope() {
		return onlyForScope;
	}

	public void setOnlyForScope(boolean onlyForScope) {
		this.onlyForScope = onlyForScope;
	}
	
	@JSONField(serialize=false)//表明不序列号该字段
	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return JSONObject.toJSONString(this);
	}
	
	public Config FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return JSON.parseObject(json, Config.class);
	}
}
