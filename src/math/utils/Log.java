package math.utils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class Log {
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
	public Log(){
		mLogger = java.util.logging.Logger.getLogger("dwt_logger");
//		ConsoleHandler consoleHandler = new ConsoleHandler();
//        mLogger.addHandler(consoleHandler);
//        consoleHandler.setLevel(java.util.logging.Level.ALL);
//		Formatter mFormatter = new SimpleFormatter();
		Formatter mFormatter = new Formatter() {
			public String format(LogRecord record) {
				StringBuffer sb = new StringBuffer();
				sb.append(record.getMessage());
				sb.append(" (");
				sb.append(record.getLevel());
				sb.append(", ");
				sb.append(record.getSourceClassName());
				sb.append(")\n");
				return sb.toString();
			}
		};
		StreamHandler consoleHandler = new StreamHandler(System.out, mFormatter);
      mLogger.addHandler(consoleHandler);
      mLogger.setUseParentHandlers(false);
      consoleHandler.setLevel(java.util.logging.Level.ALL);

	}
	public static Logger get(){
//		if (mLogger == null) new Log();
		return mLogger;
	}
}
