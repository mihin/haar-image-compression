package math.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	@SuppressWarnings("deprecation")
//	StreamHandler sh = new StreamHandler(System.out, null);
//	private static Logger mLogger = Logger.getLogger("dwt_logger");
	private static Logger mLogger = null;
//	private Log(){
//		 //get the top Logger:
//		mLogger = java.util.logging.Logger.getLogger("");
//
//	    // Handler for console (reuse it if it already exists)
//	    Handler consoleHandler = null;
//	    //see if there is already a console handler
//	    for (Handler handler : mLogger.getHandlers()) {
//	        if (handler instanceof ConsoleHandler) {
//	            //found the console handler
//	            consoleHandler = handler;
//	            break;
//	        }
//	    }
//
//
//	    if (consoleHandler == null) {
//	        //there was no console handler found, create a new one
//	        consoleHandler = new ConsoleHandler();
//	        mLogger.addHandler(consoleHandler);
//	    }
//	    //set the console handler to fine:
//	    consoleHandler.setLevel(java.util.logging.Level.FINEST);
//	}
	private Log(){
		mLogger = java.util.logging.Logger.getLogger("dwt_logger");
		ConsoleHandler consoleHandler = new ConsoleHandler();
        mLogger.addHandler(consoleHandler);
        consoleHandler.setLevel(java.util.logging.Level.ALL);
	}
	public static Logger get(){
		if (mLogger == null) new Log();
		return mLogger;
	}
}
