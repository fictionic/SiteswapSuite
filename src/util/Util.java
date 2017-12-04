package siteswapsuite;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.Arrays;

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
		String callingClassNameFull = stElements[3].getClassName();
		int dollarIndex = callingClassNameFull.indexOf("$");
		if(dollarIndex != -1) {
			return callingClassNameFull.substring(0, dollarIndex);
		} else {
			return callingClassNameFull;
		}
	}

	public static void printf(Object object, boolean newLine) {
		printf(object, DebugLevel.INFO, newLine, getCallingClassName());
	}

	public static void printf(Object object) {
		printf(object, DebugLevel.INFO, true, getCallingClassName());
	}

	public static void printf(Object object, DebugLevel minLevel) {
		printf(object, minLevel, true, getCallingClassName());
	}

	public static void printf(Object object, DebugLevel minLevel, boolean newLine) {
		printf(object, minLevel, newLine, getCallingClassName());
	}

	// master method
	private static void printf(Object object, DebugLevel minLevel, boolean newLine, String callingClassName) {
		if((minLevel == DebugLevel.DEBUG && debugClasses.contains(callingClassName)) || Util.debugLevel.compareTo(minLevel) >= 0) {
			PrintStream ps;
			if(minLevel == DebugLevel.DEBUG || minLevel == DebugLevel.ERROR) {
				ps = System.err;
			} else {
				ps = System.out;
			}
			String objectStr;
			if(object == null) {
				objectStr = "{null}";
			} else {
				objectStr = object.toString();
			}
			String toPrint;
			if(minLevel == DebugLevel.DEBUG) {
				toPrint = callingClassName + ": " + objectStr;
			} else {
				toPrint = objectStr;
			}
			if(newLine) {
				ps.println(toPrint);
			} else {
				ps.print(toPrint);
			}
		}
	}

}
