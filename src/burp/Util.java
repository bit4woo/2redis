package burp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	public static String getNowTimeString() {
		SimpleDateFormat simpleDateFormat = 
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return simpleDateFormat.format(new Date());
	}
}
