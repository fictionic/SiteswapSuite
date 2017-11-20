package siteswapsuite;

public class Test {
	public static void main(String[] args) {
		try {
			State state1 = NotatedState.parse(args[0], -1, -1).state;
			State state2 = NotatedState.parse(args[1], -1, -1).state;
			Util.printf(state1, Util.DebugLevel.INFO);
			Util.printf(state2, Util.DebugLevel.INFO);
			Transition transition = Transition.compute(state1, state2, 0, false, false);
			Util.printf(transition, Util.DebugLevel.INFO);
		} catch(InvalidStateNotationException | ImpossibleTransitionException e) {
			Util.printf(e.getMessage(), Util.DebugLevel.ERROR);
		}
	}
}
