package burp;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Util {
	public static String getNowTimeString() {
		SimpleDateFormat simpleDateFormat = 
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return simpleDateFormat.format(new Date());
	}
	
	public static boolean uselessExtension(String urlpath) {
		List extensionList =Arrays.asList(".gif","jpg","png","css","webp","woff","mp4","mov","","");//这种方式对比set的逐个添加更快吗？
		if (!urlpath.contains(".")) {
			return false;
		}
		String extension = urlpath.substring(urlpath.lastIndexOf("."),urlpath.length()).toLowerCase();
		if (extension.contains("!")) {//097c10c96aaf3339.jpg!q90!cc_190x150
			extension = extension.substring(0,extension.indexOf("!"));
		}
		if (extensionList.contains(extension)) {
			return true;
		}
		return false;
	}
	
	public static void main(String args[]) {
		URL test;
		try {
			test = new URL("https://img1.360buyimg.com:443/pop/s190x150_jfs/t1/53715/21/4641/67782/5d257edaE61d746ce/097c10c96aaf3339.jpg!q90!cc_190x150?a=111");
			String path = test.getPath();
			String file = test.getFile();
			System.out.println(path);
			System.out.println(file);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
