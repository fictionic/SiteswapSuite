package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgTree {
	// global options
	List<ArgWithOptions> globalArgs;
	List<ArgChain> argChains;

	public ArgTree() {
		this.argChains = new ArrayList<>();
		this.globalArgs = new ArrayList<>();
	}

	private static List<ArgWithOptions> parseArgsToFlatList(String[] args) throws ParseError {
		List<ArgWithOptions> flatList = new ArrayList<>();
		for(int i=0; i<args.length; i++) {
			String arg = args[i];
			ArgWithOptions parsedArg = ArgWithOptions.parse(arg);
			// add follow-up if required and present
			Argument.FollowUp requires = parsedArg.head.arg.requires;
			if(requires == Argument.FollowUp.INT) {
				try {
					parsedArg.head.followUpInt = Integer.parseInt(args[++i]);
				} catch(NumberFormatException e) {
					throw new ParseError("follow-up '" + args[i-1] + "' cannot be coerced into an integer");
				} catch(ArrayIndexOutOfBoundsException e) {
					throw new ParseError("argument '" + args[i-1] + "' requires integer follow-up");
				}
			} else if(requires == Argument.FollowUp.STRING) {
				try {
					parsedArg.head.followUpString = args[++i];
				} catch(ArrayIndexOutOfBoundsException e) {
					throw new ParseError("argument '" + args[i-1] + "' requires string follow-up");
				}
			}
			flatList.add(parsedArg);
		}
		return flatList;
	}

	public static ArgTree parseArgTree(String[] args) throws ParseError {
		List<ArgWithOptions> flatList = parseArgsToFlatList(args);
		ArgTree argTree = new ArgTree();
		for(ArgWithOptions parsedArg : flatList) {
			switch(parsedArg.head.arg.ownRole) {
				case FIRST:
					if(argTree.argChains.size() == 0) {
						argTree.addGlobalArg(parsedArg);
					} else {
						throw new ParseError("argument '" + parsedArg.head.arg + "' must appear before all others");
					}
					break;
				case INPUT:
					argTree.addInputArg(parsedArg);
					break;
				case CHAIN:
					if(argTree.argChains.size() == 0) {
						throw new ParseError("argument '" + parsedArg.head.arg + "' must appear after an input argument");
					}
					if(parsedArg.head.arg == Argument.OPS) {
						// add a link for each operation to last chain
						for(ArgWithFollowUp operationArg : parsedArg.tail) {
							argTree.addOperationArg(operationArg);
						}
					} else { // Argument.INFO
						// add all info args to last link of last chain
						for(ArgWithFollowUp infoArg : parsedArg.tail) {
							argTree.addInfoArg(infoArg);
						}
					}
					break;
				case OPERATION:
					if(argTree.argChains.size() == 0) {
						throw new ParseError("argument '" + parsedArg.head.arg + "' must appear after an input argument");
					}
					argTree.addOperationArg(parsedArg.head);
					break;
				case INFO:
					if(argTree.argChains.size() == 0) {
						throw new ParseError("argument '" + parsedArg.head.arg + "' must appear after an input argument");
					}
					argTree.addInfoArg(parsedArg.head);
					break;
				default:
					throw new ParseError("argument '" + parsedArg.head.arg + "' appears in wrong place");
			}
		}
		return argTree;
	}

	private void addGlobalArg(ArgWithOptions parsedArg) {
		this.globalArgs.add(parsedArg);
	}

	private void addInputArg(ArgWithOptions parsedArg) {
		this.argChains.add(new ArgChain(parsedArg));
	}

	private void addOperationArg(ArgWithFollowUp operationArg) {
		this.getLastChain().newLink(operationArg);
	}

	private void addInfoArg(ArgWithFollowUp infoArg) {
		this.getLastChain().getLastLink().addInfoArg(infoArg);
	}

	private ArgChain getLastChain() {
		return this.argChains.get(this.argChains.size()-1);
	}

	class ArgChain {
		ArgWithOptions input; // head = input arg and follow-up; tail = input options
		List<ArgLink> argLinks;

		ArgChain(ArgWithOptions chainInput) {
			this.input= chainInput;
			this.argLinks = new ArrayList<>();
			// add first link, with null operation
			// (infos to print about unmodified input go here)
			this.newLink(null);
		}

		void newLink(ArgWithFollowUp operation) {
			this.argLinks.add(new ArgLink(operation));
		}

		ArgLink getLastLink() {
			return this.argLinks.get(this.argLinks.size()-1);
		}

		class ArgLink {
			ArgWithFollowUp operation;
			List<ArgWithFollowUp> infoArgs;

			ArgLink(ArgWithFollowUp operation) {
				this.operation = operation;
				this.infoArgs = new ArrayList<>();
			}

			void addInfoArg(ArgWithFollowUp infoArg) {
				this.infoArgs.add(infoArg);
			}

			public String toString() {
				StringBuilder ret = new StringBuilder();
				ret.append("link: ");
				if(this.operation != null) {
					ret.append("op=");
					ret.append(this.operation.toString());
				} else {
					ret.append("(noop)");
				}
				ret.append(this.infoArgs.toString());
				return ret.toString();
			}
		}

		public String toString() {
			StringBuilder ret = new StringBuilder();
			ret.append("chain: ");
			ret.append(this.input.head.toString());
			ret.append(this.input.tail.toString());
			ret.append(this.argLinks.toString());
			ret.append("\n");
			return ret.toString();
		}

	}

	public String toString() {
		return this.argChains.toString();
	}

	public static void main(String[] args) {
		try {
			ArgTree tree = ArgTree.parseArgTree(args);
			System.out.println(tree);
		} catch(ParseError e) {
			e.printStackTrace();
		}
	}

}