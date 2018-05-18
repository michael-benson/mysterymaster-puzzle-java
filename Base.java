package com.mysterymaster.puzzle;

/**
 * The Base class is the parent class for non-static classes in the Puzzle project.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-16
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public abstract class Base {
	/** Quote character as string. */
	public static final String Q = "\"";

	/** Newline character as string. */
	public static final String NL = "\n";
	
	/** Tab character as string. */
	public final static String TAB = "\t";

	/**
	 * Returns a string representation of the object for debugging purposes.
	 * @return String.
	 */
	public abstract String asString();
	
	/**
	 * Displays the message in the IDE for debugging purposes.
	 * @param msg Message.
	 */
	public static void print(final String msg) {
		System.out.println(msg);
	}
}
