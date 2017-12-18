package siteswapsuite;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.Arrays;

public class Util {

	// math stuff

	public static int lcm(int x, int y) {
		return x * y / gcd(x,y);
	}

	public static int gcd(int x, int y) {
        int r=0, a, b;
        a = (x > y) ? x : y; // a is greater number
        b = (x < y) ? x : y; // b is smaller number
        r = b;
        while(a % b != 0)
        {
            r = a % b;
            a = b;
            b = r;
        }
        return r;
	}

	// java stuff

	public static void ErrorOut(SiteswapException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	}

	public static enum DebugLevel {
		QUIET,
		ERROR,
		INFO,
		DEBUG
	}

	public static Set<String> debugClasses = null;
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
		boolean shouldPrint = false;
		if(minLevel == DebugLevel.DEBUG) {
			if(debugClasses == null) {
				return;
			}
			if(debugClasses.isEmpty() || debugClasses.contains(callingClassName)) {
				shouldPrint = true;
			}
		} else if(Util.debugLevel.compareTo(minLevel) >= 0) {
			shouldPrint = true;
		}
		if(shouldPrint) {
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
				if(objectStr.indexOf('\n') != -1) {
					StringBuilder builder = new StringBuilder();
					String[] lines = objectStr.split("\n");
					if(lines.length > 0) {
						for(String line : lines) {
							builder.append(callingClassName + ": " + line + "\n");
						}
					} else {
						for(char c : objectStr.toCharArray()) {
							builder.append(callingClassName + ": \n");
						}
					}
					builder.deleteCharAt(builder.length()-1);
					toPrint = builder.toString();
				} else {
					toPrint = callingClassName + ": " + objectStr;
				}
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
