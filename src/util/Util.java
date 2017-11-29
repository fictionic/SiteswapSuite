package siteswapsuite;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Util {
	public static enum DebugLevel {
		QUIET,
		ERROR,
		INFO,
		DEBUG
	}

	public static Set<String> debugClasses = new HashSet<>();
	public static DebugLevel debugLevel = DebugLevel.INFO;

	private static String getCallingClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String callingClassNameFull = stElements[stElements.length-4].getClassName();
		int dollarIndex = callingClassNameFull.indexOf("$");
		if(dollarIndex != -1) {
			return callingClassNameFull.substring(0, dollarIndex);
		} else {
			return callingClassNameFull;
		}
	}

	public static void printf(Object toPrint, boolean newLine) {
		printf(toPrint, DebugLevel.INFO, newLine, getCallingClassName());
	}

	public static void printf(Object toPrint) {
		printf(toPrint, DebugLevel.INFO, true, getCallingClassName());
	}

	public static void printf(Object toPrint, DebugLevel minLevel) {
		printf(toPrint, minLevel, true, getCallingClassName());
	}

	public static void printf(Object toPrint, DebugLevel minLevel, boolean newLine) {
		printf(toPrint, minLevel, newLine, getCallingClassName());
	}

	// master method
	private static void printf(Object toPrint, DebugLevel minLevel, boolean newLine, String callingClassName) {
		if(minLevel == DebugLevel.DEBUG && !debugClasses.contains(callingClassName)) {
			return;
		}
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
