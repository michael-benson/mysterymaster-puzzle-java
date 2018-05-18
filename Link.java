package com.mysterymaster.puzzle;

import java.util.List;
import java.util.function.BiFunction;

/**
 * The Link class defines a link for a logic puzzle.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Link extends Base {
	/** Zero-based number of the link. */
	public final int num;
	
	/** Name of the link. */
	public final String name;
	
	/** Noun type of the link. */
	public NounType nounType;
	
	/** True if function is one-to-one (at most one positive verb per row). Set in the validate method. */
	public boolean oneToOne;
	
	/** Function that returns either negative or positive verb given two nouns of the link's noun type. */
	public BiFunction<Noun, Noun, Verb> f;
	
	private boolean ssNeg;
	private boolean ssPos;

	@Override
	public String toString() { return this.name; }
	
	@Override
	public String asString() {
		return "num=" + Q + num + Q + " name=" + Q + name + Q + " nounType=" + Q + nounType + Q + " oneToOne=" + Q + oneToOne + Q + " ssNeg=" + Q + ssNeg + Q + " ssPos=" + Q + ssPos + Q;
	}
	
	/**
	 * Constructor. Only the Puzzle class should create links.
	 * @param num Current number of links.
	 * @param name Name of the link.
	 * @param nounType Noun Type of the link.
	 */
	Link(final int num, final String name, final NounType nounType) {
		this.num = num;
		this.name = name;
		this.nounType = nounType;
		this.oneToOne = false;
		this.f = null;
		this.ssNeg = true;
		this.ssPos = true;
	}

	/**
	 * Returns the link's verb for the two nouns. Use this instead of the function f.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @return Verb.
	 */
	public Verb getVerb(final Noun noun1, final Noun noun2) {
		return f.apply(noun1, noun2);
	}
	
	/**
	 * Returns true if the link is one-to-one, otherwise false.
	 * @param link Link.
	 * @return True if the link is one-to-one, otherwise false.
	 */
	private static boolean isOneToOne(final Link link) {
		List<Noun> slots = link.nounType.nouns;
		for (Noun slot1 : slots) {
			int cnt = 0;
			for (Noun slot2 : slots) {
				Verb verb = link.f.apply(slot1, slot2);
				if (verb == Puzzle.Is && ++cnt > 1) return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if A and B can be in the same slot given the link and verb.
	 * If false, we can quickly say A is not with B for a fact of type 2 or 4.
	 * @param link Link.
	 * @param verb Verb.
	 * @return True if two nouns can be in the same slot, otherwise false.
	 */
	private static boolean inSameSlot(final Link link, final Verb verb) {
		List<Noun> slots = link.nounType.nouns;
		for (Noun slot : slots) {
			if (link.f.apply(slot, slot) == verb) return true;
		}
		return false;
	}
	
	// TODO Test this.
	void update() {
		this.oneToOne = isOneToOne(this);
		ssNeg = inSameSlot(this, Puzzle.IsNot);
		ssPos = inSameSlot(this, Puzzle.Is);
	}
	
	/**
	 * Returns true if two nouns can be in the same slot for this link.
	 * @param verb Verb.
	 * @return True if two nouns can be in the same slot, otherwise false.
	 */
	public boolean canBeWith(final Verb verb) {
		return verb.num < 0 ? ssNeg : ssPos;
	}
}
