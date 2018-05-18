package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * The Noun Type class defines a noun type for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class NounType extends Base {
	/** One-based number of the noun type. */
	public final int num;
	
	/** Name of the noun type. */
	public final String name;
	
	/** Array of nouns of this noun type. */
	public final List<Noun> nouns;
	
	@Override
	public String toString() { return name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " nouns=" + Helper.getArrayAsString(nouns.toArray());
	}
	
	/**
	 * Constructor. Only the Puzzle class should create noun types.
	 * @param num Current number of noun types.
	 * @param name Name of the noun type.
	 */
	NounType(final int num, final String name) {
		this.num = num;
		this.name = name;
		this.nouns = new ArrayList<>();
	}
	
	/**
	 * Creates the new noun using the default value for title.
	 * Only the NounType class should create nouns, but still needs to be public.
	 * @param name Name of the noun.
	 * @return Noun
	 */
	public Noun addNoun(final String name) {
		return addNoun(name, null);
	}
	
	/**
	 * Creates and appends the new noun to the nouns array of this noun type.
	 * Only the NounType class should create nouns, but still needs to be public.
	 * @param name Name of the noun.
	 * @param title Title of the noun.
	 * @return Noun
	 */
	public Noun addNoun(final String name, final String title) {
		Noun noun = new Noun(this.nouns.size() + 1, this, name, title);
		this.nouns.add(noun);
		return noun;
	}
	
	/**
	 * Returns the noun with the given one-based number of this noun type.
	 * @param num One-based number of the noun.
	 * @return Noun
	 */
	public Noun getNoun(final int num) {
		return this.nouns.get(num - 1);
	}
	
	/** Resets the nouns of this noun type. */
	public void reset() {
		for (Noun noun : nouns) noun.reset();
	}
}
