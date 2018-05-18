package com.mysterymaster.puzzle;

/**
 * The Verb class defines a verb for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Verb extends Base {
	/** Maximum number of verbs. */
	public static final int MAX_VERBS = 3;

	/** Zero-based number of the verb. */
	public final int num;
	
	/** Name of the verb. */
	public String name;
	
	/** Type of the verb. */
	public final String type;
	
	/** One character string that represents the verb, usually 'X', 'O', and ' '. */
	public String code;
	
	@Override
	public String toString() { return name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " type=" + Q + type + Q + " code=" + Q + code + Q;
	}

	/**
	 * Constructor.
	 * @param num Zero-based number of the verb.
	 * @param name Name of the verb.
	 * @param code One-character code representing the verb.
	 */
	Verb(final int num, final String name, final String code) {
		final String[] Types = { "False", "True", "Unknown" };

		this.num = num;
		this.type = Types[num];
		this.name = name;
		this.code = code;
	}
}
