package com.mysterymaster.puzzle;

import java.util.function.BiFunction;

/**
 * The Smart Link class defines static methods that return a function for a link.<br>
 * The link function returns a verb based on two nouns with the same noun type as the link.<br>
 * This class takes responsibility of defining links from the Puzzle class.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class SmartLink {
	/** Constructor. */
	public SmartLink() {
		throw new Error("SmartLink is a static class!");
	}
	
	/**
	 * Returns lambda expression for the isWith function:
	 * <blockquote>Returns positive verb if both nouns are equal (i.e., are the same noun), otherwise negative verb.</blockquote>
	 * @return Function isWith.
	 */
	static BiFunction<Noun, Noun, Verb> getIsWith() {
		return (noun1, noun2) -> noun1.num == noun2.num ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isLessThan function:
	 * <blockquote>Returns positive verb if the number for noun1 is less than the number for noun2 minus n, otherwise negative verb.</blockquote>
	 * <blockquote>For n = 1, this means "before, but not just before."</blockquote>
	 * @param n Offset number.
	 * @return Function isLessThan.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsLessThan(int n) {
		return (noun1, noun2) -> noun1.num < noun2.num - n ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isLessBy function:
	 * <blockquote>Returns positive verb if the number for noun1 is exactly n less than the number for noun2, otherwise negative verb.</blockquote>
	 * @param n Offset number.
	 * @return Function isLessBy.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsLessBy(int n) {
		return (noun1, noun2) -> noun1.num == noun2.num - n ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isMoreThan function:
	 * <blockquote>Returns positive verb if the number for noun1 is more than the number for noun2 plus n, otherwise negative verb.</blockquote>
	 * <blockquote>For n = 1, this means "after, but not just after."</blockquote>
	 * @param n Offset number.
	 * @return Function isMoreThan.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsMoreThan(int n) {
		return (noun1, noun2) -> noun1.num > noun2.num + n ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isMoreBy function:
	 * <blockquote>Returns positive verb if the number for noun1 is exactly n more than the number for noun2, otherwise negative verb.</blockquote>
	 * @param n Offset number.
	 * @return Function isMoreBy.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsMoreBy(int n) {
		return (noun1, noun2) -> noun1.num == noun2.num + n ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isNextTo function:
	 * <blockquote>Returns positive verb if the number for noun1 is exactly 1 less or 1 more than the number for noun2, otherwise negative verb.</blockquote>
	 * @return Function isNextTo.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsNextTo() {
		return (noun1, noun2) -> (noun1.num == noun2.num - 1) || (noun1.num == noun2.num + 1) ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isOffsetBy function:
	 * <blockquote>Returns positive verb if the number for noun1 is exactly n less than or n more than the number for noun2, otherwise negative verb.</blockquote>
	 * <blockquote>Equivalent to isNextTo when n is one.</blockquote>
	 * @param n Offset number.
	 * @return Function isOffsetBy.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsOffsetBy(int n) {
		return (noun1, noun2) -> (noun1.num == noun2.num - n) || (noun1.num == noun2.num + n) ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the isOutsideOf function:
	 * <blockquote>Returns positive verb if the number for noun1 is either n less than or n more than the number for noun2, otherwise negative verb.</blockquote>
	 * @param n Offset number.
	 * @return Function isOutsideOf.
	 */
	public static BiFunction<Noun, Noun, Verb> getIsOutsideOf(int n) {
		return (noun1, noun2) -> (noun1.num < noun2.num - n) || (noun1.num > noun2.num + n) ? Puzzle.Is : Puzzle.IsNot;
	}
	
	/**
	 * Returns lambda expression for the hasRatio function:
	 * <blockquote>Returns positive verb if the number for noun1 times n1 equals the number for noun2 times n2, otherwise negative verb.</blockquote>
	 * @param n1 Offset number on the left hand side.
	 * @param n2 Offset number on the right hand side.
	 * @return Function hasRatio.
	 */
	public static BiFunction<Noun, Noun, Verb> getHasRatio(int n1, int n2) {
		return (noun1, noun2) -> (n1 * noun1.num == n2 * noun2.num) ? Puzzle.Is : Puzzle.IsNot;
	}
}
