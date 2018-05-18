package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * The Mark class defines a mark for a logic puzzle.<br>
 * Note: All fields must be public so the Viewer object can access them.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Mark extends Base {
	/** Mark Type. */
	public enum Type {
		None("None"),
		User("User"),
		Level("Level"),
		Law("Law"),
		Rule("Rule");
		
		public final String name;
		
		Type(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() { return name; }
	}
	
	/** One-based number of the mark. */
	public final int num;
	
	/** Name of the mark */
	public String name;
	
	/** Type of mark. */
	public Type type;
	
	/** True if the mark has been validated by the Lawyer, otherwise false. */
	public boolean valid;
	
	/** Level number. If not Level type, this is from the mark that spawned this mark. */
	public int levelNum;
	
	/** Level character. */
	public char levelSub;
	
	/** Combines level number and level character as a string. **/
	public String levelAsString;
	
	/** Reference number (Level number, Law number, etc.). */
	public int refNum;
	
	/** Reference character (Level character, Law character, etc.). */
	public char refSub;
	
	/** Noun 1. */
	public Noun noun1;
	
	/** Verb. */
	public Verb verb;
	
	/** Noun 2. */
	public Noun noun2;
	
	/** Facts referenced by this mark. The facts and disabledFacts may have different facts. */
	public final List<Fact> facts = new ArrayList<>();
	
	/** Loner number used for assumptions. This is negative one if not used. */
	public int lonerNum;
	
	/** Mark that spawned this mark, either from a rule (trigger) or law, otherwise null. */
	public Mark refMark;
	
	/** Facts disabled by this mark. The facts and disabledFacts may have different facts. */
	public final List<Fact> disabledFacts = new ArrayList<>();
	
	/** True if mark is an assumption, otherwise false. */
	public boolean guess;
	
	/** Nouns that were updated by the current rule invoked on this mark. */
	public final List<Noun> rulePlacers = new ArrayList<>();
	
	/** Nouns that were updated by ALL of the rules invoked on this mark. */
	public final List<Noun> placers = new ArrayList<>();

	@Override
	public String toString() { return name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " type=" + Q + type + Q;
	}
	
	/**
	 * Returns true if noun 1 is with noun 2, otherwise false.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @return Boolean
	 */
	public static boolean isPair(final Noun noun1, final Noun noun2) {
		return Mark.getPairNoun(noun1, noun2.type) == noun2;
	}
	
	/**
	 * Returns noun 2 if noun 1 is with a noun of noun type 2, or null.
	 * @param noun1 Noun 1.
	 * @param nounType2 Noun type of noun 2.
	 * @return Noun 2, or null.
	 */
	public static Noun getPairNoun(final Noun noun1, final NounType nounType2) {
		Mark mark = noun1.pairs[nounType2.num - 1];
		if (mark == null) return null;
		return mark.noun1 == noun1 ? mark.noun2 : mark.noun1;
	}
	
	/**
	 * Returns the one-based number of noun 2 if noun 1 is with a noun of noun type 2, or 0.
	 * @param noun1 Noun 1.
	 * @param nounType2 Noun type of noun 2.
	 * @return One-based number of noun 2, or 0.
	 */
	public static int getPairNounNum(final Noun noun1, final NounType nounType2) {
		Mark mark = noun1.pairs[nounType2.num - 1];
		if (mark == null) return 0;
		return mark.noun1 == noun1 ? mark.noun2.num : mark.noun1.num;
	}
	
	/**
	 * Constructor.
	 * @param num Current number of marks.
	 */
	public Mark(final int num) {
		this.num = num + 1;
		reset();
	}
	
	/** Resets the mark. */
	public final void reset() {
		name = "";
		type = Mark.Type.None;
		valid = false;
		levelNum = 0;
		levelSub = ' ';
		levelAsString = "";
		refNum = 0;
		refSub = ' ';
		noun1 = null;
		verb = null;
		noun2 = null;
		facts.clear();
		lonerNum = -1;
		refMark = null;
		disabledFacts.clear();
		guess = false;
		rulePlacers.clear();
		placers.clear();
	}
	
	// Updates the mark.
	public void update(final String name, final int alevelNum, final char levelSub, final Mark.Type markType, final int refNum, final char refSub, final Noun noun1, final Verb verb, final Noun noun2, final List<Fact> facts, final int lonerNum, final Mark refMark) {
		int levelNum = markType == Mark.Type.User ? ISolver.MAX_LEVELS : alevelNum;

		this.name = name;
		this.type = markType;
		this.valid = false;
		this.levelNum = levelNum;
		this.levelSub = levelSub;
		this.levelAsString = String.format("%d%c", levelNum, levelSub);
		this.refNum = refNum;
		this.refSub = refSub;
		this.noun1 = noun1;
		this.verb = verb;
		this.noun2 = noun2;
		this.facts.clear();
		this.lonerNum = lonerNum;
		this.refMark = refMark;
		this.disabledFacts.clear();
		this.guess = markType == Mark.Type.User || (markType == Mark.Type.Level && levelNum == ISolver.MAX_LEVELS);

		if (facts != null) this.facts.addAll(facts);
	}

	/** Returns true if the rule updated any placers, otherwise false.
	 * @return boolean.
	 */
	public boolean hasRulePlacers() { return !rulePlacers.isEmpty(); }

	/**
	 * Returns true if there are rulePlacers to reset, otherwise false.
	 * @return boolean.
	 */
	public boolean hasPlacers() { return !placers.isEmpty(); }
	
	/**
	 * Returns a message for the placers that were updated by a rule.
	 * @param rule Rule.
	 * @return Message.
	 */
	public String getRulePlacersMsg(final Rule rule) {
		StringBuilder msg = new StringBuilder();
		msg.append("Rule ").append(rule.num).append(" updated");
		String sep = "";
		String dir = " to ";
		for (Noun noun : rulePlacers) {
			msg.append(sep).append(" ").append(noun.oldName).append(dir).append(noun.name);
			sep = ",";
		}
		msg.append(".");
		return msg.toString();
	}

	/**
	 * Returns a message for the placers that were reset when a mark is removed.
	 * @return Message.
	 */
	public String getResetPlacersMsg() {
		StringBuilder msg = new StringBuilder();
		msg.append("I reset");
		String sep = "";
		String dir = " from ";
		for (Noun noun : placers) {
			msg.append(sep).append(" ").append(noun.oldName).append(dir).append(noun.name);
			sep = ",";
		}
		msg.append(".");
		return msg.toString();
	}

	/**
	 * Adds the placer to the given mark when a rule updates a noun.<br>
	 * Called by Abbondanza, AstrophysicsConference, DandySalespeople.
	 * @param noun Noun.
	 * @param value String.
	 */
	public void addPlacer(final Noun noun, final String value) {
		noun.updatePlacer(value);
		rulePlacers.add(noun);
	}

	/**
	 * Adds the placer to the given mark when a rule updates a noun.<br>
	 * Called by Abbondanza, AstrophysicsConference, DandySalespeople.
	 * @param noun Noun.
	 * @param value Integer.
	 */
	public void addPlacer(final Noun noun, final int value) {
		addPlacer(noun, Integer.toString(value));
	}
	
	/**
	 * Moves the nouns from the rulePlacers array to the placers array.
	 * Called by lawyer.doRules, clearPlacers.
	 */
	public void updatePlacers() {
		placers.addAll(rulePlacers);
		rulePlacers.clear();
	}

	/** Resets each noun, and clears the placers. Called by solver.sayMarkRemoval. */
	public void clearPlacers() {
		updatePlacers(); // This is just a precaution.

		for (Noun noun : placers) { noun.resetPlacer(); }
		placers.clear();
	}

	/**
	 * Enables the facts that were disabled by this mark.
	 * @return Fact with the lowest number, or null.
	 */
	public Fact undoDisabledFacts() {
		Fact rs = null;
		for (Fact fact : disabledFacts) {
			if (!fact.enabled) {
				fact.enabled = true;
				if (rs == null || fact.num < rs.num) rs = fact;
			}
		}
		return rs;
	}
}
