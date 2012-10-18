package math;

import java.util.logging.Level;

import math.utils.Log;

public class Launcher {

	public static void main(String [] in){
//		StreamHandler sh = new StreamHandler(System.out, new SimpleFormatter()); 
//		Log.get().addHandler(sh);
		int level = 1;
		boolean toRecostr = true;
		Level logLevel = Level.FINEST;
		Log.getInstance().setLevel(logLevel);
		TransmormationManager m = new TransmormationManager(level, toRecostr);
		
		Log.getInstance().log(Level.CONFIG, "TransmormationManager launch. Decomposition depth "+level+
				", recostruction = "+toRecostr+". Logging level is "+logLevel.getName());
		
//		m.logFile = new File("log.txt");
		
		
		m.startTransforms();
	
//		m.loadDecompCoefs();
		
//		m.reconstructImage();
	}
}
