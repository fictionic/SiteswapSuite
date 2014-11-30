
public class TransitionFinder {

	public static Siteswap getTransition(Siteswap s1, Siteswap s2) {
		return null;
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = Parser.parse(args[0]);
			System.out.println(Parser.deParse(ss));
			System.out.println(ss.isValid());
		}
	}
}
