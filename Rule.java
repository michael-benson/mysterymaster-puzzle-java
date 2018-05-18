package com.mysterymaster.puzzle;

import java.util.List;
import java.util.function.Function;

/**
 * The Rule class defines a rule for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Rule extends Base {
	/** One-based number of the rule. */
	public final int num;
	
	/** Name of the rule. */
	public final String name;
	
	/** Optional array of nouns referenced in the rule. */
	public final List<Noun> nouns;
	
	/** Whether the rule should be referenced (true) or ignored (false). */
	public boolean enabled;
	
	/** Number of times the rule has been referenced. */
	public int hits = 0;
	
	/** Initial/reset value of the enabled field. */
	private final boolean initEnabled;
	
	/** Function that checks for rule violations and/or triggers marks to be entered. */
	public Function<Mark, Integer> f;
	
	@Override
	public String toString() { return name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " nouns=" + Q + Helper.getListAsString(nouns) + Q + " enabled=" + Q + enabled + Q + " hits=" + Q + hits + Q + " initEnabled=" + Q + initEnabled + Q + " f=" + Q + f + Q;
	}
	
	/**
	 * Constructor. Only the Puzzle class should create rules.
	 * @param num Current number of rules.
	 * @param name Name of the rule.
	 * @param nouns Optional array of nouns referenced in the rule. May be null.
	 * @param initEnabled Initial/reset value of the enabled field.
	 */
	public Rule(final int num, final String name, final List<Noun> nouns, final boolean initEnabled) {
		this.num = num;
		this.name = name;
		this.nouns = nouns;
		this.enabled = initEnabled;
		this.initEnabled = this.enabled;
		this.f = null;
	}
	
	/** Resets the rule. */
	public void reset() {
		enabled = initEnabled;
		hits = 0;
	}
}
