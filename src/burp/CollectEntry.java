package burp;

import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CollectEntry {
	private String url;
	private Set<String> dirs;
	private String file;
	private Set<String> parameters;
	
	CollectEntry(String url,Set<String> dirs,String file,Set<String> parameters){
		this.url = url; //用作redis存储中的key
		this.dirs = dirs;
		this.parameters = parameters;
		this.file = file;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public Set<String> getDirs() {
		return dirs;
	}

	public void setDirs(Set<String> dirs) {
		this.dirs = dirs;
	}

	public Set<String> getParameters() {
		return parameters;
	}

	public void setParameters(Set<String> parameters) {
		this.parameters = parameters;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	public String toJson() {
		return JSON.toJSONString(this);
	}
	
	public static CollectEntry fromJson(String json) {
		return (CollectEntry)JSONObject.parse(json);
	}
}
