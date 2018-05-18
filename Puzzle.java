package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Puzzle class defines a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-10
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public class Puzzle extends Base {
	/** Negative verb. The static verbs exist even when there is no puzzle. */
	public final static Verb IsNot  = new Verb(0, "is not", "X");

	/** Positive verb. The static verbs exist even when there is no puzzle. */
	public final static Verb Is = new Verb(1, "is", "O");

	/** Possible verb. The static verbs exist even when there is no puzzle. */
	public final static Verb Maybe = new Verb(2, "may be", " ");

	/** Default link. Note that all links are dependent on the nouns. */
	public static Link With = null;

	/** Name of the puzzle module. */
	public String myName = null;

	/** Title of the puzzle. */
	public String myTitle = null;

	/** List of noun types. */
	public final List<NounType> nounTypes = new ArrayList<>();

	/** Array of verbs. There must always be three verbs. */
	public final Verb[] verbs;

	/** List of links. */
	public final List<Link> links = new ArrayList<>();

	/** List of facts. */
	public final List<Fact> facts = new ArrayList<>();

	/** List of rules. */
	public final List<Rule> rules = new ArrayList<>();

	/** Maximum number of noun types. */
	public int maxNounTypes = 0;

	/** Maximum number of nouns per type. This must be the same for all noun types. */
	public int maxNouns = 0;

	/** Maximum number of links. */
	public int maxLinks = 0;

	/** Maximum number of facts. */
	public int maxFacts = 0;

	/** Maximum number of rules */
	public int maxRules = 0;

	/** Puzzle validation flag. Set in the validation method. */
	public boolean isValid = false;

	/** Solver object. Set in the validate method. */
	public ISolver solver;

	/** SmartRule object. Set in the constructor method. */
	public SmartRule smartRule;

	@Override
	public String toString() {
		return myName == null ? "Puzzle" : myName;
	}

	@Override
	public String asString() {
		return Helper.getPuzzleAsText(this);
	}

	/** Constructor. Create first link "With", and update its noun type in addNounType method. */
	public Puzzle() {
		// Reset the defaults for the static verbs each time the Puzzle object is instantiated.
		IsNot.name = "is not"; IsNot.code = "X";
		Is.name = "is"; Is.code = "O";
		Maybe.name = "may be"; Maybe.code = " ";
		
		// The verbs array is defined for the puzzle.
		verbs = new Verb[] { IsNot, Is, Maybe };
		
		With = addLink("with", null);
		With.f = SmartLink.getIsWith();
		
		smartRule = new SmartRule(this);
	}

	/**
	 * Resets the puzzle data used for solving a puzzle.<br>
	 * Called by validate, solver.reset.
	 */
	public void reset() {
		for (NounType nounType : nounTypes) { nounType.reset(); }
		for (Fact fact : facts) { fact.reset(); }
		for (Rule rule : rules) { rule.reset(); }
	}

	// <editor-fold defaultstate="collapsed" desc="Add Methods">

	/**
	 * Creates and appends the new noun type object to the nounTypes array.
	 * @param name Name of the noun type.
	 * @return The new Noun Type object.
	 */
	public final NounType addNounType(final String name) {
		NounType nounType = new NounType(nounTypes.size() + 1, name);
		nounTypes.add(nounType);
		maxNounTypes = nounTypes.size();
		if (nounType.num == 1) {
			maxNouns = nounType.nouns.size();
			With.nounType = nounType;
		}
		return nounType;
	}
	
	/**
	 * Creates and appends the new link object to the links array.
	 * @param name Name of the link.
	 * @param nounType Noun type.
	 * @return The new Link object.
	 */
	public final Link addLink(final String name, final NounType nounType) {
		Link link = new Link(links.size(), name, nounType);
		links.add(link);
		maxLinks = links.size();
		return link;
	}
	
	/**
	 * Returns a list of the given nouns. Called by the addXX methods for facts and rules.
	 * @param nouns Variable number of nouns.
	 * @return List of nouns.
	 */
	public final List<Noun> getList(final Noun... nouns) {
		return Arrays.asList(nouns);
	}
	
	// The following methods add one fact where the nouns are one-to-one.
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final Noun noun2) {
		return addFact(clueNum, noun1, verb, link, noun2, null);
	}
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final Noun noun2, final String name) {
		return addFact(clueNum, noun1, verb, link, noun2, name, true);
	}
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final Noun noun2, final String name, final boolean initEnabled) {
		String txt = name;
		if (name == null || name.length() < 1) {
			txt = sayFact(noun1, verb, link, noun2);
		}

		// Don't enter duplicate facts.
		boolean ok = true;
		for (Fact oldFact : facts) {
			if (oldFact.verb != verb) continue;
			if (oldFact.noun1 == noun1 && oldFact.link == link && oldFact.noun2 == noun2)
				ok = false;
			else if (oldFact.noun1 == noun2 && oldFact.link == link && oldFact.link == With && oldFact.noun2 == noun1)
				ok = false;
			if (!ok) {
				//console.log("Warning! This fact already exists: " + oldFact.num + " " + oldFact.name);
				return null;
			}
		}

		String msg = getClueNumMsg(clueNum, txt);
		Fact fact = new Fact(facts.size() + 1, msg, noun1, verb, link, noun2, initEnabled);
		facts.add(fact);
		maxFacts = facts.size();
		return fact;
	}
	
	
	// The following methods add facts where the nouns are one-to-many.
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final List<Noun> nouns2) {
		return addFact(clueNum, noun1, verb, link, nouns2, null);
	}
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final List<Noun> nouns2, final String name) {
		return addFact(clueNum, noun1, verb, link, nouns2, name, true);
	}
	
	public final Fact addFact(final String clueNum, final Noun noun1, final Verb verb, final Link link, final List<Noun> nouns2, final String name, final boolean initEnabled) {
		Fact fact = null;
		for (Noun noun2 : nouns2) {
			if (noun1 == noun2 || (link == With && noun1.type == noun2.type)) continue;
			fact = addFact(clueNum, noun1, verb, link, noun2, name, initEnabled);
		}
		return fact;
	}
	
	
	// The following methods add facts where the nouns are many-to-one.
	// Note: The second noun CANNOT be null because of ambiguity with the many-to-many overloads.
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final Noun noun2) {
		return addFact(clueNum, nouns1, verb, link, noun2, null);
	}
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final Noun noun2, final String name) {
		return addFact(clueNum, nouns1, verb, link, noun2, name, true);
	}
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final Noun noun2, final String name, final boolean initEnabled) {
		Fact fact = null;
		for (Noun noun1 : nouns1) {
			if (noun1 == noun2 || (link == With && noun1.type == noun2.type)) continue;
			fact = addFact(clueNum, noun1, verb, link, noun2, name, initEnabled);
		}
		return fact;
	}
	
	// The following methods add facts where the nouns are many-to-many. The second list may be empty.
	// Note: The second list must be empty instead of null to avoid ambiguity with the many-to-one overloads.

	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link) {
		return addFact(clueNum, nouns1, verb, link, Collections.emptyList());
	}
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2) {
		return addFact(clueNum, nouns1, verb, link, nouns2, null);
	}
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2, final String name) {
		return addFact(clueNum, nouns1, verb, link, nouns2, name, true);
	}
	
	public final Fact addFact(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2, final String name, final boolean initEnabled) {
		Fact fact = null;
		if (nouns2.isEmpty()) {
			for (int i = 0; i < nouns1.size() - 1; i++) {
				Noun nounA = nouns1.get(i);
				for (int j = i + 1; j < nouns1.size(); j++) {
					Noun nounB = nouns1.get(j);
					if (nounA == nounB || (link == With && nounA.type == nounB.type)) continue;
					fact = addFact(clueNum, nounA, verb, link, nounB, name, initEnabled);
				}
			}
		}
		else {
			for (Noun noun1 : nouns1) {
				for (Noun noun2 : nouns2) {
					if (noun1 == noun2 || (link == With && noun1.type == noun2.type)) continue;
					fact = addFact(clueNum, noun1, verb, link, noun2, name, initEnabled);
				}
			}
		}
		return fact;
	}
	
	// See puzzles: CicusAnniversaries.
	public final Fact addFactsInSequence(final String clueNum, final List<Noun> nouns, final Verb verb, final Link link) {
		return addFactsInSequence(clueNum, nouns, verb, link, null);
	}
	
	public final Fact addFactsInSequence(final String clueNum, final List<Noun> nouns, final Verb verb, final Link link, final String name) {
		return addFactsInSequence(clueNum, nouns, verb, link, name, true);
	}
	
	public final Fact addFactsInSequence(final String clueNum, final List<Noun> nouns, final Verb verb, final Link link, final String name, final boolean initEnabled) {
		Fact fact = null;
		for (int i = 0; i < nouns.size() - 1; i++) {
			fact = addFact(clueNum, nouns.get(i), verb, link, nouns.get(i + 1), name, initEnabled);
		}
		return fact;
	}
	
	// See puzzles: ExtraKeys.
	public final Fact addFactsOneToOne(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2, final String name, final boolean initEnabled) {
		Fact fact = null;
		int n = nouns1.size();
		if (n != nouns2.size()) return fact;

		for (int i = 0; i < n; i++) {
			fact = addFact(clueNum, nouns1.get(i), verb, link, nouns2.get(i), name, initEnabled);
		}
		
		return fact;
	}
	
	public final Fact addFactsOneToOne(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2) {
		return addFactsOneToOne(clueNum, nouns1, verb, link, nouns2, null);
	}

	public final Fact addFactsOneToOne(final String clueNum, final List<Noun> nouns1, final Verb verb, final Link link, final List<Noun> nouns2, final String name) {
		return addFactsOneToOne(clueNum, nouns1, verb, link, nouns2, name, true);
	}
	

	// See puzzles: EllisIslandIdylls.
	public final Fact addFactsOneToOne(final String clueNum, final NounType nounType1, final Verb verb, final Link link, final NounType nounType2) {
		return addFactsOneToOne(clueNum, nounType1, verb, link, nounType2, null);
	}

	public final Fact addFactsOneToOne(final String clueNum, final NounType nounType1, final Verb verb, final Link link, final NounType nounType2, final String name) {
		return addFactsOneToOne(clueNum, nounType1, verb, link, nounType2, name, true);
	}
	
	public final Fact addFactsOneToOne(final String clueNum, final NounType nounType1, final Verb verb, final Link link, final NounType nounType2, final String name, final boolean initEnabled) {
		Fact fact = null;
		int n = nounType1.nouns.size();
		
		for (int i = 0; i < n; i++) {
			fact = addFact(clueNum, nounType1.nouns.get(i), verb, link, nounType2.nouns.get(i), name, initEnabled);
		}
		return fact;
	}
	
	
	// See puzzles: DandySalespeople.
	protected final Fact addFactsStartsWith(final String clueNum, final Noun noun1, final NounType nounType2, final boolean flag, final char ch) {
		return addFactsStartsWith(clueNum, noun1, nounType2, flag, ch, null);
	}

	public final Fact addFactsStartsWith(final String clueNum, final Noun noun1, final NounType nounType2, final boolean flag, final char ch,final  String name) {
		return addFactsStartsWith(clueNum, noun1, nounType2, flag, ch, name, true);
	}
	
	public final Fact addFactsStartsWith(final String clueNum, final Noun noun1, final NounType nounType2, final boolean flag, final char ch, final String name, final boolean initEnabled) {
		Fact fact = null;
		for (Noun noun2 : nounType2.nouns) {
			if ((noun2.name.charAt(0) == ch) == flag) {
				fact = addFact(clueNum, noun1, IsNot, With, noun2, name, initEnabled);
			}
		}
		return fact;
	}
	
	
	// See puzzles: ExtraKeys.
	protected final Fact addFactsIsNotFirstChar(final String clueNum, final NounType nounType1, final NounType nounType2, final boolean flag) {
		return addFactsIsNotFirstChar(clueNum, nounType1, nounType2, flag, null);
	}

	public final Fact addFactsIsNotFirstChar(final String clueNum, final NounType nounType1, final NounType nounType2, final boolean flag, final String name) {
		return addFactsIsNotFirstChar(clueNum, nounType1, nounType2, flag, name, true);
	}

	public final Fact addFactsIsNotFirstChar(final String clueNum, final NounType nounType1, final NounType nounType2, final boolean flag, final String name, final boolean initEnabled) {
		Fact fact = null;
		for (Noun noun1 : nounType1.nouns) {
			for (Noun noun2 : nounType2.nouns) {
				if ((noun1.name.charAt(0) == noun2.name.charAt(0)) == flag) {
					fact = addFact(clueNum, noun1, IsNot, With, noun2, name, initEnabled);
				}
			}
		}
		return fact;
	}

	// See puzzles: ShooOutOfTheGarden.
	protected final Fact addFactsNotConsecutive(final String clueNum, final List<Noun> nouns, final Link link) {
		return addFactsNotConsecutive(clueNum, nouns, link, null);
	}
	
	public final Fact addFactsNotConsecutive(final String clueNum, final List<Noun> nouns, final Link link, final String name) {
		return addFactsNotConsecutive(clueNum, nouns, link, name, true);
	}
	
	public final Fact addFactsNotConsecutive(final String clueNum, final List<Noun> nouns, final Link link, final String name, final boolean initEnabled) {
		Fact fact = null;
		int n = nouns.size();
		NounType type = link.nounType;
		int max = type.nouns.size();

		if (2 * n - 1 == max) {
			for (Noun noun : nouns) {
				for (int i = 1; i < max; i += 2) {
					Noun slot = type.nouns.get(i);
					fact = addFact(clueNum, noun, IsNot, With, slot, name, initEnabled);
				}
			}
		}
		else {
			for (int i1 = 0; i1 < n - 1; i1++) {
				Noun noun1 = nouns.get(i1);
				for (int i2 = i1 + 1; i2 < n; i2++) {
					Noun noun2 = nouns.get(i2);
					fact = addFact(clueNum, noun1, IsNot, link, noun2, name, initEnabled);
					fact = addFact(clueNum, noun2, IsNot, link, noun1, name, initEnabled);
				}
			}
		}
		
		return fact;
	}
	
	/**
	 * Creates and appends the new rule to the rules array.
	 * @param clueNum Clue number.
	 * @param name Name of the rule.
	 * @return The new Rule object.
	 */
	public final Rule addRule(final String clueNum, final String name) {
		return addRule(clueNum, name, null);
	}
	
	/**
	 * Creates and appends the new rule to the rules array.
	 * @param clueNum Clue number.
	 * @param name Name of the rule.
	 * @param nouns Optional array of nouns referenced in the rule.
	 * @return The new Rule object.
	 */
	public final Rule addRule(final String clueNum, final String name, final List<Noun> nouns) {
		return addRule(clueNum, name, nouns, true);
	}
	
	/**
	 * Creates and appends the new rule to the rules array.
	 * @param clueNum Clue number.
	 * @param name Name of the rule.
	 * @param nouns Optional array of nouns referenced in the rule.
	 * @param initEnabled True if fact should be initially enabled, otherwise false.
	 * @return The new Rule object.
	 */
	public final Rule addRule(final String clueNum, final String name, final List<Noun> nouns, final boolean initEnabled) {
		String msg = getClueNumMsg(clueNum, name);
		Rule rule = new Rule(rules.size() + 1, msg, nouns, initEnabled);
		rules.add(rule);
		maxRules = rules.size();
		return rule;
	}
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Get Methods">

	/**
	 * Returns the noun type given by its one-based number.
	 * @param num One-based number of the noun type.
	 * @return The noun type object.
	 */
	public NounType getNounType(final int num) {
		return nounTypes.get(num - 1);
	}

	/**
	 * Returns the noun give by its one-based type number and one-based number.
	 * @param typeNum One-based number of the noun's type.
	 * @param num One-based number of the noun.
	 * @return The Noun object.
	 */
	public Noun getNoun(final int typeNum, final int num) {
		return nounTypes.get(typeNum - 1).nouns.get(num - 1);
	}

	/**
	 * Returns the verb given by its zero-based number.
	 * @param num Zero-based number of the verb.
	 * @return The Verb object.
	 */
	public Verb getVerb(final int num) {
		return verbs[num];
	}

	/**
	 * Returns the clue number in parenthesis from the clueNum or name. Called by addFact, addRule.
	 * @param clueNum Clue number.
	 * @param name Name of the fact or rule.
	 * @return String.
	 */
	private static String getClueNumMsg(final String clueNum, final String name) {
		if (clueNum == null || clueNum.length() < 1) return name;

		int i = name.length() - 1;
		if (i < 0) return name;

		StringBuilder msg = new StringBuilder();
		msg.append(name.substring(0, i)).append(" (");

		switch (clueNum.charAt(0)) {
			case 'A':
				msg.append("analysis");
				if (clueNum.length() > 1) msg.append(" ").append(clueNum.substring(1));
				break;
			case '0':
				msg.append("intro");
				break;
			default:
				msg.append("clue");
				if (clueNum.contains(",")) msg.append("s");
				msg.append(" ").append(clueNum);
				break;
		}

		msg.append(")").append(name.charAt(i));
		return msg.toString();
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Say Fact">

	/**
	 * Returns the name of the fact given it's two nouns, verb, and link.<br>
	 * The child puzzle should override this method.
	 * @param noun1 Noun1.
	 * @param verb Verb.
	 * @param link Link.
	 * @param noun2 Noun2.
	 * @return Name of the fact.
	 */
	public String sayFact(final Noun noun1, final Verb verb, final Link link, final Noun noun2) {
		return "PARENT " + noun1.name + " " + verb.name + " " + link.name + " " + noun2.name + ".";
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Validate">

	/**
	 * Validates the puzzle. If valid, the isValid flag is true, otherwise false.
	 * @param solver ISolver.
	 * @return Zero if successful or nonzero for failure.
	 */
	public int validate(final ISolver solver) {
		int rs = 0;

		// Validate the properties.
		if (myName == null || myName.length() == 0) throw new Error("The name of the puzzle must be given!");
		if (myTitle == null || myTitle.length() == 0) throw new Error("The title of the puzzle must be given!");

		// Validate the nouns.
		maxNounTypes = nounTypes.size();
		if (maxNounTypes < 2) throw new Error("The puzzle must have at least two noun types!");

		maxNouns = nounTypes.get(0).nouns.size();
		if (maxNouns < 2) throw new Error("The puzzle must have at least two nouns per type!");
		for (NounType nounType : nounTypes) {
			if (nounType.nouns.size() != maxNouns) throw new Error("The puzzle must have the same number of nouns per type!");
		}

		// Validate the verbs.
		if (verbs.length != Verb.MAX_VERBS) throw new Error("The puzzle must have exactly " + Verb.MAX_VERBS + " verbs!");

		// Validate the links.
		maxLinks = links.size();
		if (maxLinks < 1) throw new Error("The puzzle must have be at least one link!");

		// Validate the first link (with).
		if (links.get(0).nounType == null) links.get(0).nounType = nounTypes.get(0);

		// Validate every link has a noun type and a function.
		for (Link link : links) {
			if (link.nounType == null) {
				throw new Error("Link " + link.num + " must have a noun type!" + NL + link.name);
			}
			if (link.f == null) {
				throw new Error("Link " + link.num + " must have a function!" + NL + link.name);
			}
			link.update();
		}

		// Validate the facts.
		maxFacts = facts.size();
		for (Fact fact : facts) {
			if (fact.verb == Maybe) {
				throw new Error("Fact " + fact.num + " cannot use the possible verb!" + NL + fact.name);
			}
			if (fact.noun1 == fact.noun2) {
				throw new Error("Fact " + fact.num + " cannot have both nouns be the same!" + NL + fact.name);
			}
			Link link = fact.link;
			NounType type = link.nounType;
			if (link.num < 1 && fact.noun1.type == fact.noun2.type) {
				throw new Error("Fact " + fact.num + " cannot state that two nouns of the same type are [not] together!" + NL + fact.name);
			}
			if (fact.noun1.type == type && fact.noun2.type == type) {
				throw new Error("Fact " + fact.num + " cannot have the link and both nouns with the same type!" + NL + fact.name);
			}
		}

		// Validate the rules.
		maxRules = rules.size();
		for (Rule rule : rules) {
			if (rule.f == null) {
				throw new Error("Rule " + rule.num + " must have a function!" + NL + rule.name);
			}
		}

		// Verify there is at least one fact or rule.
		if (facts.size() < 1 && rules.size() < 1) {
			throw new Error("The puzzle must have at least one fact or rule!");
		}

		// Initialize the sizes of the arrays for each noun.
		for (NounType nounType : nounTypes) {
			for (Noun noun : nounType.nouns) {
				noun.pairs = new Mark[maxNounTypes];

				for (Fact fact : facts) {
					Link link = fact.link;
					if (link.num < 1) continue;
					if (link.nounType == fact.noun1.type || link.nounType == fact.noun2.type) continue;
					if (fact.noun1 == noun || fact.noun2 == noun) {
						noun.facts.add(fact);
					}
				}
			}
		}

		// The puzzle is valid.
		isValid = true;
		reset();

		// Finally, set the solver for this puzzle.
		this.solver = solver;
		smartRule.solver = solver;
		return rs;
	}

	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Answer">

	/** Puzzle answer. */
	public int[][] answer = null;

	/**
	 * Returns true if the solution is correct (or the answer field is null), otherwise false.<br>
	 * Called by the Solver.
	 * @return Boolean.
	 */
	boolean isAnswer() {
		if (answer == null) return true;
		NounType nounType1 = nounTypes.get(0);
		for (Noun noun1 : nounType1.nouns) {
			for (NounType nounType2 : nounTypes) {
				if (nounType2.num == 1) continue;
				if ((Mark.getPairNounNum(noun1, nounType2) - 1) != answer[nounType2.num - 2][noun1.num - 1]) return false;
			}
		}
		return true;
	}

	// </editor-fold>
}
