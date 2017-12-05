package siteswapsuite;

public class Main {

	public static void main(String[] args) {
		try {
			// assemble cmdline args into tree structure
			ArgTree argTree = ArgTree.parseArgTree(args);
			Util.printf(argTree);
			// convert argument tree into parallel command tree
			Command command = new Command(argTree);
			// run command
			command.run();
		} catch(ParseError e) {
			Util.printf(e.getMessage(), Util.DebugLevel.ERROR);
		}
	}

}
