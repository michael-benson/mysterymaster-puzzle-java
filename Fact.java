package com.mysterymaster.puzzle;

/**
 * The Fact class defines a fact for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Fact extends Base {
	/** One-based number of the fact. */
	public final int num;
	
	/** Name of the fact. */
	public final String name;
	
	/** Type of the fact. Either 1, 2, 3, or 4. */
	public int type = 0;
	
	/** Noun 1 of the fact. */
	public final Noun noun1;
	
	/** Verb of the fact. */
	public final Verb verb;
	
	/** Link of the fact. */
	public final Link link;
	
	/** Noun 2 of the fact. */
	public final Noun noun2;
	
	/** Initial/reset value of the enabled field. */
	public boolean enabled;
	
	/** Number of times the fact was referenced by the Solver. */
	public int hits = 0;
	
	/** Initial/reset value of the enabled field. */
	private final boolean initEnabled;
	
	@Override
	public String toString() { return this.name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " type=" + Q + type + Q + " noun1=" + Q + noun1 + Q + " verb=" + Q + verb + Q + " link=" + Q + link + Q + " noun2=" + Q + noun2 + Q + " enabled=" + Q + enabled + Q + " hits=" + Q + hits + Q + " initEnabled=" + Q + initEnabled + Q;
	}
	
	/**
	 * Constructor. Only the Puzzle class should create facts.
	 * @param num Current number of facts.
	 * @param name Name of the fact.
	 * @param noun1 Noun 1 of the fact.
	 * @param verb Verb of the fact.
	 * @param link Link of the fact.
	 * @param noun2 Noun 2 of the fact.
	 * @param initEnabled Initial/reset value of the enabled field.
	 */
	Fact(final int num, final String name, final Noun noun1, final Verb verb, final Link link, final Noun noun2, final boolean initEnabled) {
		this.num = num;
		this.name = name;
		this.noun1 = noun1;
		this.verb = verb;
		this.link = link;
		this.noun2 = noun2;
		this.enabled = initEnabled;
		this.initEnabled = this.enabled;
		
	if (link.num == 0)
		this.type = 1;
	else if (noun1.type == link.nounType || noun2.type == link.nounType)
		this.type = 2;
	else if (noun1.type == noun2.type)
		this.type = 3;
	else if (this.noun1.type != noun2.type)
		this.type = 4;
	}
	
	/** Resets the fact. */
	public void reset() {
		enabled = initEnabled;
		hits = 0;
	}
	
	/**
	 * Returns the message that the fact is being examined.
	 * @return Message
	 */
	public String msgBasedOn() { return "fact " + num; }
	
	/**
	 * Returns the message that the fact is disabled.
	 * @return Message
	 */
	public String msgDisabled() { return "fact " + num + " is disabled."; }
}
