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
	public static void printf(Object toPrint, boolean newLine) {
		printf(toPrint, DebugLevel.INFO, newLine);
	}
	public static void printf(Object toPrint) {
		printf(toPrint, true);
	}
	public static void printf(Object toPrint, DebugLevel minLevel) {
		printf(toPrint, minLevel, true);
	}
	public static void printf(Object toPrint, DebugLevel minLevel, boolean newLine) {
		if(debugLevel.compareTo(minLevel) >= 0) {
			PrintStream ps; 
			if(minLevel == DebugLevel.DEBUG || minLevel == DebugLevel.ERROR) {
				ps = System.err;
			} else {
				ps = System.out;
			}
			if(toPrint == null) {
				if(newLine) {
					ps.println("{null}");
				} else {
					ps.print("{null}");
				}
			} else {
				if(newLine) {
					ps.println(toPrint);
				} else {
					ps.print(toPrint);
				}
			}
		}
	}

}
