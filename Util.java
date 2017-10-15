package siteswapsuite;

import java.io.PrintStream;

public class Util {
	public static enum DebugLevel {
		QUIET,
		ERROR,
		INFO,
		DEBUG
	}
	public static DebugLevel debugLevel = DebugLevel.INFO;
	public static void printf(Object toPrint) {
		printf(toPrint, DebugLevel.INFO);
	}
	public static void printf(Object toPrint, DebugLevel minLevel) {
		if(debugLevel.compareTo(minLevel) >= 0) {
			PrintStream ps; 
			if(minLevel == DebugLevel.DEBUG) {
				ps = System.err;
			} else {
				ps = System.out;
			}
			if(toPrint == null) {
				ps.println("{null}");
			} else {
				ps.println(toPrint);
			}
		}
	}

}
