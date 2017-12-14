package siteswapsuite;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;;

public class NotatedSiteswap {

	static enum Type implements NotationType {

		EMPTY(0), ONEHANDED(1), TWOHANDED(2);

		private int defaultNumHands;

		private Type(int numHands) {
			this.defaultNumHands = numHands;
		}

		public int defaultNumHands() {
			return this.defaultNumHands;
		}

	}

	Type type;
	Siteswap siteswap;
	int startHand;

	// strings
	private static String emptyNotationDisplay = ".";
	// // regexes
	private static String oneHandedNotationPattern = "[-_0-9a-wyz&{}!\\[\\]]*";
	private static String twoHandedNotationPattern = "[-_0-9a-z&{}!\\[\\]*(,)]*";

	// querying basic info
	public Type notationType() { return this.type; }
	public Siteswap siteswap() { return this.siteswap; }

	// deep copy
	public NotatedSiteswap deepCopy() {
		return null;
	}

	// -------------------------------- ASSEMBLING --------------------------------

	public static NotatedSiteswap assemble(Siteswap siteswap, NotationType targetType, int startHand) {
		NotatedSiteswap ret = new NotatedSiteswap();
		ret.siteswap = siteswap;
		if(startHand == -1) {
			startHand = 0;
		}
		switch(siteswap.numHands()) {
			case 0:
				ret.type = Type.EMPTY;
				break;
			case 1:
				ret.type = Type.ONEHANDED;
				break;
			case 2:
				switch(targetType.defaultNumHands()) {
					case 1:
						ret.startHand = startHand;
					case 0:
						ret.type = Type.ONEHANDED;
						break;
					default:
						ret.type = Type.TWOHANDED;
				}
				break;
			default:
				Util.ErrorOut(new IncompatibleNumberOfHandsException());
				break;
		}
		return ret;
	}

	static Type defaultType(int numHands) {
		switch(numHands) {
			case 0:
				return Type.EMPTY;
			case 1:
				return Type.ONEHANDED;
			case 2:
				return Type.TWOHANDED;
			default:
				return null;
		}
	}

	// ---------------------------------- PARSING ---------------------------------

	private static Type getNotationType(String notation) throws	InvalidSiteswapNotationException {
		if(notation.length() == 0 || notation.equals(emptyNotationDisplay)) {
			return Type.EMPTY;
		}
		if(Pattern.matches(oneHandedNotationPattern, notation)) {
			return Type.ONEHANDED;
		}
		if(Pattern.matches(twoHandedNotationPattern, notation)) {
			return Type.TWOHANDED;
		}
		throw new InvalidSiteswapNotationException();
	}

	public static NotatedSiteswap parse(String notation, int numHands, int startHand) throws InvalidSiteswapNotationException {
		Type type = getNotationType(notation);
		NotatedSiteswap ret = new NotatedSiteswap();
		ret.type = type;
		List<SiteswapNotationToken> tokens;
		if(startHand == -1) {
			startHand = 0;
		}
		switch(type) {
			case EMPTY:
				if(numHands == -1) {
					numHands = type.defaultNumHands;
				}
				ret.siteswap = new Siteswap(numHands);
				break;
			case ONEHANDED:
				tokens = tokenize(notation);
				switch(numHands) {
					case -1:
					case 1:
						ret.siteswap = parseOneHanded(tokens);
						break;
					case 2:
						ret.siteswap = parseTwoHanded(tokens, startHand);
						// check for odd period
						if(ret.siteswap.period() % 2 == 1) {
							ret.siteswap.appendSiteswap(parseTwoHanded(tokens, (startHand+1)%2));
						}
						ret.startHand = startHand;
						break;
					default:
						Util.ErrorOut(new IncompatibleNumberOfHandsException());
				}
				break;
			case TWOHANDED:
				tokens = tokenize(notation);
				if(numHands == -1 || numHands == 2) {
					ret.siteswap = parseTwoHanded(tokens, startHand);
				} else {
					Util.ErrorOut(new IncompatibleNumberOfHandsException());
				}
				break;
		}
		return ret;
	}

	private static Siteswap parseOneHanded(List<SiteswapNotationToken> tokens) throws InvalidSiteswapNotationException {
		Siteswap ret = new Siteswap(1);
		int b = 0;
		boolean inMulti = false;
		boolean bang = false;
		for(int i=0; i<tokens.size(); i++) {
			SiteswapNotationToken curToken = tokens.get(i);
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			switch(curToken.type) {
				case BANG:
					if(bang || inMulti || ret.period() == 0) {
						throw new InvalidSiteswapNotationException();
					}
					bang = true;
					break;
				case MULTI_OPEN:
					if(inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					inMulti = true;
					break;
				case MULTI_CLOSE:
					if(!inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					inMulti = false;
					break;
				case TOSS:
					// get Toss object from TossToken
					Toss newToss;
					if(curToken.toss.isInfinite) {
						InfinityType infiniteHeight;
						if(curToken.toss.isNegative) {
							infiniteHeight = InfinityType.NEGATIVE_INFINITY;
						} else {
							infiniteHeight = InfinityType.POSITIVE_INFINITY;
						}
						newToss = new Toss(infiniteHeight, curToken.toss.isAntitoss);
					} else {
						int finiteHeight = curToken.toss.finiteAbsoluteHeight;
						if(curToken.toss.isNegative) {
							finiteHeight *= -1;
						}
						newToss = new Toss(finiteHeight, 0, curToken.toss.isAntitoss);
					}
					if(!inMulti && !bang) {
						ret.appendEmptyBeat();
					}
					ret.addToss(ret.period()-1, 0, newToss);
					if(bang) {
						bang = false;
					}
					break;
			}
			Util.printf("siteswap: " + ret, Util.DebugLevel.DEBUG);
		}
		// see about trailing special tokens
		if(bang) {
			throw new InvalidSiteswapNotationException();
		}
		return ret;
	}

	private static Siteswap parseTwoHanded(List<SiteswapNotationToken> tokens, int startHand) throws InvalidSiteswapNotationException {
		Siteswap ret = new Siteswap(2);
		int b = 0;
		int h = (startHand + 1) % 2; // because it gets flipped before each toss
		boolean inMulti = false;
		boolean inSync = false;
		boolean bang = false;
		boolean star = false;
		int lastStarIndex = -1;
		for(int i=0; i<tokens.size(); i++) {
			SiteswapNotationToken curToken = tokens.get(i);
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			switch(curToken.type) {
				case BANG:
					if(bang || inMulti || ret.period() == 0) {
						throw new InvalidSiteswapNotationException();
					}
					bang = true;
					break;
				case MULTI_OPEN:
					if(inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					inMulti = true;
					if(!bang && !inSync) {
						Util.printf("adding new beat", Util.DebugLevel.DEBUG);
						ret.appendEmptyBeat(); // append an empty beat for the multiplex, unless we shouldn't
						if(!star) {
							h = (h + 1) % 2;
							Util.printf("swapping fromHand to " + h, Util.DebugLevel.DEBUG);
						}
					}
					break;
				case MULTI_CLOSE:
					if(!inMulti || bang || star) {
						throw new InvalidSiteswapNotationException();
					}
					inMulti = false;
					break;
				case STAR:
					if(star || inMulti || ret.period() == 0) {
						throw new InvalidSiteswapNotationException();
					}
					star = true;
					lastStarIndex = i;
					break;
				case SYNC_OPEN:
					if(inSync || inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					inSync = true;
					if(!bang) {
						Util.printf("adding new beat", Util.DebugLevel.DEBUG);
						ret.appendEmptyBeat(); // append an empty beat for the sync beat, unless we shouldn't
						// don't swap hand, cuz it'll happen when we add the first toss
					}
					break;
				case COMMA:
					if(!inSync || inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					break;
				case SYNC_CLOSE:
					if(!inSync || inMulti) {
						throw new InvalidSiteswapNotationException();
					}
					inSync = false;
					ret.appendEmptyBeat(); // append an empty beat after each sync beat
					break;
				case TOSS:
					// make new beat, if necessary
					if(!bang && !inSync && !inMulti) {
						Util.printf("adding new beat", Util.DebugLevel.DEBUG);
						ret.appendEmptyBeat();
					}
					// swap fromHand, if necessary
					if(!star && !inMulti) {
						h = (h + 1) % 2;
						Util.printf("swapping fromHand to " + h, Util.DebugLevel.DEBUG);
					}
					// get Toss object from TossToken
					Toss newToss;
					if(curToken.toss.isInfinite) {
						// get infiniteHeight
						InfinityType infiniteHeight;
						if(curToken.toss.isNegative) {
							infiniteHeight = InfinityType.NEGATIVE_INFINITY;
						} else {
							infiniteHeight = InfinityType.POSITIVE_INFINITY;
						}
						newToss = new Toss(infiniteHeight, curToken.toss.isAntitoss);
					} else {
						// get finiteHeight
						int finiteHeight = curToken.toss.finiteAbsoluteHeight;
						if(curToken.toss.isNegative) {
							finiteHeight *= -1;
						}
						// get destHand
						int destHand = (h + curToken.toss.finiteAbsoluteHeight) % 2;
						if(curToken.toss.isFlippedHand) {
							destHand = (destHand + 1) % 2;
						}
						newToss = new Toss(finiteHeight, destHand, curToken.toss.isAntitoss);
					}
					// add it
					Util.printf("adding toss: " + newToss, Util.DebugLevel.DEBUG);
					ret.addToss(ret.period()-1, h, newToss);
					// unset flags
					bang = false;
					star = false;
					break;
			}
			Util.printf("siteswap: " + ret, Util.DebugLevel.DEBUG);
		}
		// see about trailing special tokens
		if((bang || star) && ret.period() == 0) {
			throw new InvalidSiteswapNotationException();
		}
		if(bang) {
			if(ret.beatIsEmpty(ret.period()-1)) {
				ret.removeBeat(ret.period()-1);
			} else {
				throw new InvalidSiteswapNotationException();
			}
		}
		if(star) {
			tokens.remove(lastStarIndex);
			if(ret.period() % 2 == 0) {
				ret.appendSiteswap(parseTwoHanded(tokens, h));
			}
		}
		return ret;
	}

	// ------------------------------- TOKENIZATION -------------------------------

	private static enum TossHeightState {
		READY, // when there is no pending toss, and we can parse a new token
		SEEN_MINUS, // when we've seen a minus
		SEEN_ANTI, // when we've seen an underscore
		SEEN_BOTH, // when we've seen a minus and an underscore
		INSIDE_CURLY, // when we're reading a {literal height}
		AFTER_HEIGHT; // when we're awaiting a possible landing modifier
	}

	private static List<SiteswapNotationToken> tokenize(String notation) throws InvalidSiteswapNotationException {
		TossHeightState tossHeightState = TossHeightState.READY;
		StringBuilder curlyHeight = null; // height string parsed from {literal height} indication
		List<SiteswapNotationToken> tokens = new ArrayList<>();
		SiteswapNotationToken curToken = null;
		for(int i=0; i<notation.length(); i++) {
			char c = notation.charAt(i);
			Util.printf("c=" + c, Util.DebugLevel.DEBUG);
			switch(c) {
				case '*':
				case '!':
				case '[':
				case ']':
				case '(':
				case ')':
				case ',':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.parseNonTossChar(c));
							tokens.add(curToken);
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '{':
					switch(tossHeightState) {
						case INSIDE_CURLY:
							throw new InvalidSiteswapNotationException();
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS); // create new toss
							// fall through
						case SEEN_MINUS:
						case SEEN_ANTI:
						case SEEN_BOTH:
							curlyHeight = new StringBuilder();
							tossHeightState = TossHeightState.INSIDE_CURLY;
							break;
					}
					break;
				case '}':
					switch(tossHeightState) {
						case INSIDE_CURLY:
							curToken.toss.finiteAbsoluteHeight = Integer.parseInt(curlyHeight.toString());
							tossHeightState = TossHeightState.AFTER_HEIGHT;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '-':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS);
							curToken.toss.isNegative = true;
							tossHeightState = TossHeightState.SEEN_MINUS;
							break;
						case SEEN_ANTI:
							curToken.toss.isNegative = true;
							tossHeightState = TossHeightState.SEEN_BOTH;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '_':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS);
							curToken.toss.isAntitoss = true;
							tossHeightState = TossHeightState.SEEN_ANTI;
							break;
						case SEEN_MINUS:
							curToken.toss.isAntitoss = true;
							tossHeightState = TossHeightState.SEEN_BOTH;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case 'x':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							// make sure it's not an infinite toss
							if(curToken.toss.isInfinite) {
								throw new InvalidSiteswapNotationException();
							}
							curToken.toss.isFlippedHand = true;
							tokens.add(curToken);
							tossHeightState = TossHeightState.READY;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				default:
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS); // create new toss
							// fall through
						case SEEN_ANTI:
						case SEEN_MINUS:
						case SEEN_BOTH:
							// make sure it's a numeral
							TossToken rawTossToken = SiteswapNotationToken.parseTossChar(c);
							if(rawTossToken == null) {
								throw new InvalidSiteswapNotationException();
							}
							curToken.toss.isInfinite = rawTossToken.isInfinite;
							curToken.toss.finiteAbsoluteHeight = rawTossToken.finiteAbsoluteHeight;
							tossHeightState = TossHeightState.AFTER_HEIGHT;
							break;
						case INSIDE_CURLY:
							curlyHeight.append(c);
							break;
					}
			}
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			Util.printf("tokens: " + tokens, Util.DebugLevel.DEBUG);
		}
		if(tossHeightState == TossHeightState.AFTER_HEIGHT) {
			tokens.add(curToken);
		}
		return tokens;
	}

	// -------------------------------- DISPLAYING --------------------------------

	public String display() {
		if(this.siteswap.period() == 0) {
			return emptyNotationDisplay;
		}
		switch(this.type) {
			case EMPTY:
				return emptyNotationDisplay;
			case ONEHANDED:
				if(this.siteswap.numHands() == 1) {
					return this.displayOneHanded();
				} else {
					return this.displayAsync();
				}
			case TWOHANDED:
				return this.displayTwoHanded();
			default:
				return null; //FIXME
		}
	}

	private String displayOneHanded() {
		StringBuilder builder = new StringBuilder();
		for(int b=0; b<this.siteswap.period(); b++) {
			int numTossesAtSite = this.siteswap.numTossesAtSite(b,0);
			if(numTossesAtSite == 0) {
				builder.append('0');
			} else if(numTossesAtSite == 1) {
				builder.append(displayToss(this.siteswap.getToss(b,0,0)));
			} else {
				builder.append('[');
				for(int t=0; t<numTossesAtSite; t++) {
					builder.append(displayToss(this.siteswap.getToss(b,0,t)));
				}
				builder.append(']');
			}
		}
		return builder.toString();
	}

	private String displayAsync() {
		// NOTE: need to print 'x's here because async patterns
		// can be the result of transitions, even though a string
		// with an 'x' will never get parsed as ONEHANDED
		StringBuilder builder = new StringBuilder();
		int curHand = this.startHand;
		for(int b=0; b<this.siteswap.period(); b++) {
			int numTossesAtSite = this.siteswap.numTossesAtSite(b,curHand);
			if(numTossesAtSite == 0) {
				builder.append('0');
			} else if(numTossesAtSite == 1) {
				Toss toss = this.siteswap.getToss(b,curHand,0);
				builder.append(displayToss(toss));
				if(tossIsFlipped(toss, curHand)) {
					builder.append('x');
				}
			} else {
				builder.append('[');
				for(int t=0; t<numTossesAtSite; t++) {
					Toss toss = this.siteswap.getToss(b,curHand,t);
					builder.append(displayToss(toss));
					if(tossIsFlipped(toss, curHand)) {
						builder.append('x');
					}
				}
				builder.append(']');
			}
			curHand = (curHand + 1) % 2;
		}
		return builder.toString();
	}

	private String displayTwoHanded() {
		StringBuilder builder = new StringBuilder();
		for(int b=0; b<this.siteswap.period(); b++) {
			builder.append('(');
			for(int h=0; h<2; h++) {
				int numTossesAtSite = this.siteswap.numTossesAtSite(b,h);
				if(numTossesAtSite == 0) {
					builder.append('0');
				} else if(numTossesAtSite == 1) {
					Toss toss = this.siteswap.getToss(b,h,0);
					builder.append(displayToss(toss));
					if(tossIsFlipped(toss, h)) {
						builder.append('x');
					}
				} else {
					builder.append('[');
					for(int t=0; t<numTossesAtSite; t++) {
						Toss toss = this.siteswap.getToss(b,h,t);
						builder.append(displayToss(toss));
						if(tossIsFlipped(toss, h)) {
							builder.append('x');
						}
					}
					builder.append(']');
				}
				if(h == 0) {
					builder.append(',');
				}
			}
			builder.append(')');
			// see about a bang
			if(b == this.siteswap.period()-1 || !this.siteswap.beatIsEmpty(b+1)) {
				builder.append('!');
			} else {
				b++;
			}
		}
		return builder.toString();
	}

	static String displayToss(Toss toss) {
		StringBuilder builder = new StringBuilder();
		ExtendedInteger height = toss.height();
		if(height.sign() < 0) {
			builder.append('-');
			height.negate();
		}
		if(toss.charge() < 0) {
			builder.append('_');
		}
		if(height.isInfinite()) {
			builder.append('&');
			return builder.toString();
		}
		int finiteHeight = height.finiteValue();
		if(finiteHeight <= 9) {
			builder.append(finiteHeight);
		} else if((10 <= finiteHeight) && (finiteHeight <= 36) && (finiteHeight != 33)) {
			builder.append(finiteHeight - 10 + 97);
		} else {
			builder.append('{');
			builder.append(finiteHeight);
			builder.append('}');
		}
		return builder.toString();
	}

	static boolean tossIsFlipped(Toss toss, int fromHand) {
		if(!toss.height().isInfinite()) {
			return (fromHand + toss.destHand()) % 2 != toss.height().finiteValue() % 2;
		}
		return false;
	}

	public static void main(String[] args) {
		try {
			NotatedSiteswap nss = parse(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			System.out.println(nss.siteswap);
			System.out.println(nss.display());
			NotatedSiteswap nss2 = assemble(nss.siteswap, nss.type, nss.startHand);
			System.out.println(nss2.display());
		} catch(InvalidSiteswapNotationException e) {
			e.printStackTrace();
		}
	}

}
