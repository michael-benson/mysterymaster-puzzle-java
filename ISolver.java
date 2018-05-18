package com.mysterymaster.puzzle;

import java.util.List;

/**
 * The Solver interface is implemented by the Solver class in the Solver package.<br>
 * Note: This is necessary to avoid circular dependency between the Puzzle and Solver packages.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-16
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public interface ISolver {
	/** Maximum number of levels. */
	public static final int MAX_LEVELS = 4;

	/** Maximum number of laws. */
	public static final int MAX_LAWS = 5;

	/**
	 * Displays a debug message, which may output to the IDE.
	 * @param msg Message.
	 */
	void jot(String msg);

	/**
	 * Returns the maximum number of marks. Called by DandySalespeople.
	 * @return Value of maxMarks.
	 */
	int getMaxMarks();

	/**
	 * Returns true if noun 1 is/maybe related to noun 2. Called by NewSelfImprovement.
	 * @param noun1 Noun 1.
	 * @param link Link.
	 * @param noun2 Noun 2.
	 * @return boolean.
	 */
	boolean maybeRelated(Noun noun1, Link link, Noun noun2);

	/**
	 * Returns the first noun of noun type 3 that noun 1 and noun 2 can be with, otherwise null.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @param nounType3 Noun type 3.
	 * @return Noun of type 3, or null.
	 */
	Noun getCommonNoun(Noun noun1, Noun noun2, NounType nounType3);

	/**
	 * Returns true if noun 1 can be with noun 2, otherwise false. Called by SmartRule.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @return Boolean
	 */
	boolean canBeWith(Noun noun1, Noun noun2);

	/**
	 * Returns true if all nouns in the list cannot be with noun2, otherwise false.
	 * Called by ModernNovels.
	 * @param nouns Array of nouns.
	 * @param noun2 Noun 2.
	 * @return Boolean
	 */
	boolean cannotBeWith(List<Noun> nouns, Noun noun2);

	/**
	 * Returns the verb of the mark given by the two nouns, or the possible verb if the mark is null.
	 * Called by SmartRule.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @return Verb.
	 */
	Verb getGridVerb(Noun noun1, Noun noun2);

	/**
	 * Enters the mark triggered by the given rule. Called by SmartRule or the rule function.
	 * @param mark Mark.
	 * @param rule Rule.
	 * @param refSub Reference character.
	 * @param noun1 Noun1.
	 * @param verb Verb.
	 * @param noun2 Noun2.
	 * @param msg Message.
	 * @return Status.
	 */
	int addMarkByRule(Mark mark, Rule rule, char refSub, Noun noun1, Verb verb, Noun noun2, String msg);
}
