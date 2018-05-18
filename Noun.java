package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * The Noun class defines a noun for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Noun extends Base {
	/** One-based number of the noun. */
	public final int num;
	
	/** Name of the noun. This is used to create the name of the fact. */
	public String name;
	
	/** Noun type of the noun. */
	public final NounType type;
	
	/**
	 * Title of the noun. Set to the first-capped name if not given. Displayed in the Nouns, Chart, and Grids forms.
	 * Note: This value is updated if the noun is a placer.
	 */
	public String title;
	
	/** Mark = pairs[t2 - 1] for each noun type. Mark may be null. Initialized in the validate method. */
	public Mark[] pairs;
	
	/** Facts that references this noun. Set in the validate method. */
	public final List<Fact> facts = new ArrayList<>();
	
	/** Original value for the name of the noun. */
	private final String originalName;
	
	/** Original value for the title of the noun. */
	private final String originalTitle;

	/** Remember the last updated value if the noun is a placer. */
	public String oldName;
	
	@Override
	public String toString() { return name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " type=" + Q + type + Q + " title=" + Q + title + Q + " originalName=" + Q + originalName + Q + " originalTitle=" + Q + originalTitle + Q + " pairs.length=" + Q + pairs.length + Q;
	}
	
	/**
	 * Constructor. Only the NounType class should create nouns.
	 * @param num Current number of nouns.
	 * @param type Noun type of the noun.
	 * @param name Name of the noun.
	 * @param title Optional title of the noun.
	 */
	Noun(final int num, final NounType type, final String name, final String title) {
		this.num = num;
		this.type = type;
		this.name = name;
		this.title = title == null ? Helper.toTitleCase(name) : title;
		this.originalName = this.name;
		this.originalTitle = this.title;
	}
	
	/**
	 * Constructor using the default value for title.
	 * @param num Current number of nouns.
	 * @param type Noun type of the noun.
	 * @param name Name of the noun.
	 */
	public Noun(final int num, final NounType type, final String name) {
		this(num, type, name, null);
	}
	
	/** Resets the name and title, along with the pairs. */
	public void reset() {
		resetPlacer();
		for (int i = 0; i < pairs.length; i++) pairs[i] = null;
	}
	
	/**
	 * Updates the noun if it is a placer. Called by mark.addPlacer.
	 * @param value Value
	 */
	void updatePlacer(final String value) {
		oldName = name;
		name = value;
		title = value;
	}
	
	/** Resets the noun if it is a placer. Called by mark.clearPlacers.  */
	void resetPlacer() {
		oldName = name;
		name = originalName;
		title = originalTitle;
	}
}
