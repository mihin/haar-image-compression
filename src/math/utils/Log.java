package math.utils;

import java.util.logging.Logger;

public class Log {
	@SuppressWarnings("deprecation")
	private static Logger mLogger = Logger.global;
	public static Logger get(){
		return mLogger;
	}
}
