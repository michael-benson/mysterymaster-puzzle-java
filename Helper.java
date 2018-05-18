package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * The Helper class defines helpful static methods.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Helper {
	/** Quote character as string. */
	private static final String Q = "\"";

	/** Newline character as string. */
	private static final String NL = "\n";

	/**
	 * Displays the message in the IDE for debugging purposes.
	 * @param msg Message.
	 */
	private static void print(final String msg) {
		System.out.println(msg);
	}

	// <editor-fold defaultstate="collapsed" desc="Numeric">

	/**
	 * Returns true if the value of the given string is an integer, otherwise false.
	 * @param str String.
	 * @return boolean.
	 */
	public static boolean isInt(final String str) {
		try {
			Integer.parseInt(str);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Returns the integer value of the given string representation of an integer, or zero.<br>
	 * Note: The user should first use isInt if the string can be "0".
	 * @param str String.
	 * @return integer.
	 */
	public static int toInt(final String str) {
		try {
			return Integer.parseInt(str);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Returns true if the integer value of the noun's name is not divisible by the given number, otherwise false.
	 * @param noun Noun.
	 * @param num Integer value.
	 * @return  boolean.
	 */
	public static boolean isNotDivisibleBy(final Noun noun, final int num) {
		if (noun == null || !Helper.isInt(noun.name)) return false;
		int val = Integer.parseInt(noun.name);
		return val % num != 0;
	}
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="String">

	/**
	 * Returns a multi-line string as one line by replacing each new line with the given string.
	 * @param msg Message.
	 * @param sep Separator.
	 * @return String.
	 */
	public static String getMsgAsOneLine(final String msg, final String sep) {
		return msg.replaceAll(NL, sep);
	}
	
	/**
	 * Returns the given string converted to title case.
	 * @param str String.
	 * @return String.
	 */
	public static String toTitleCase(final String str) {
		StringBuilder rs = new StringBuilder();
		boolean flag = true;

		for (char c : str.toCharArray()) {
			if (Character.isSpaceChar(c))
				flag = true;
			else if (flag) {
				c = Character.toTitleCase(c);
				flag = false;
			}
			rs.append(c);
		}
		
		return rs.toString();
	}
	
	/**
	 * Returns the array of objects as a comma-delimited string.
	 * @param objs Array of objects.
	 * @return String.
	 */
	static String getArrayAsString(final Object[] objs) {
		return getArrayAsString(objs, ",");
	}
	
	/**
	 * Returns the array of objects as a comma-delimited string.
	 * @param objs Array of objects.
	 * @param sep0 Separator.
	 * @return String.
	 */
	public static String getArrayAsString(final Object[] objs, final String sep0) {
		StringBuilder msg = new StringBuilder();
		if (objs != null) {
			msg.append("{");
			String sep = "";
			for (Object obj : objs) {
				msg.append(sep).append(obj.toString());
				sep = sep0;
			}
			msg.append("}");
		}
		return msg.toString();
	}
	
	/**
	 * Returns the list of objects as a comma-delimited string.
	 * @param objs List of objects.
	 * @return String.
	 */
	public static String getListAsString(final List objs) {
		return getListAsString(objs, ",");
	}
	
	/**
	 * Returns the array of objects as a comma-delimited string.
	 * @param objs List of objects.
	 * @param sep0 Separator.
	 * @return String.
	 */
	public static String getListAsString(final List objs, final String sep0) {
		StringBuilder msg = new StringBuilder();
		if (objs != null) {
			msg.append("[");
			String sep = "";
			for (Object obj : objs) {
				msg.append(sep).append(obj.toString());
				sep = sep0;
			}
			msg.append("]");
		}
		return msg.toString();
	}
	
	public static String getChartAsText(final Puzzle puzzle, final int chartCol1, final boolean isSolution) {
		if (puzzle == null) return "";
		StringBuilder txt = new StringBuilder();

		String caption = isSolution ? "Solution" : "Chart";
		txt.append(caption).append(NL);

		int t = chartCol1;
		List<NounType> nounTypes = puzzle.nounTypes;
		NounType nounType1 = nounTypes.get(t);

		int w = 20;
		String pad = new String(new char[w]).replace('\0', ' ');
		String tmp;

		int k = 0;
		for (int j = 0; j < puzzle.maxNounTypes; j++) {
			if (k == t) ++k;
			NounType nounType = (j == 0 ? nounType1 : nounTypes.get(k++));
			tmp = nounType.name + pad;
			txt.append(tmp.substring(0, w));
		}
		txt.append(NL);
		for (int i = 0; i < puzzle.maxNouns; i++) {
			k = 0;
			for (int j = 0; j < puzzle.maxNounTypes; j++) {
				if (k == t) ++k;
				Noun noun1 = nounType1.nouns.get(i);
				tmp = " ";
				if (j == 0)
					tmp =noun1.title;
				else {
					Noun noun2 = Mark.getPairNoun(noun1, nounTypes.get(k));
					if (noun2 != null) tmp = noun2.title;
					++k;
				}
				tmp += pad;
				txt.append(tmp.substring(0, w));
			}
			txt.append(NL);
		}
		return txt.toString();
	}
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Matrix">
	
	/**
	 * Displays the 2D array for debugging purposes.
	 * @param a 2D array a[][].
	 */
	public static void sayArray2D(final int[][] a) {
		StringBuilder msg = new StringBuilder();
		if (a != null) {
			for (int i1 = 0; i1 < a.length; i1++) {
				String sep = "";
				for (int i2 = 0; i2 < a[i1].length; i2++) {
					msg.append(sep).append(a[i1][i2]);
					sep = ",";
				}
				msg.append(NL);
			}
			print(msg.toString());
		}
	}
	
	/**
	 * Returns a 2-dimensional array with each element initialized to the given value.
	 * @param d1 Size of dimension 1.
	 * @param d2 Size of dimension 2.
	 * @param v Initial value.
	 * @return 2D array of integers.
	 */
	public static int[][] getArray2D(final int d1, final int d2, final int v) {
		int[][] a = new int[d1][d2];
		for (int i1 = 0; i1 < d1; i1++) {
			for (int i2 = 0; i2 < d2; i2++) {
				a[i1][i2] = v;
			}
		}
		return a;
	}
	
	/**
	 * Returns a 2-dimensional array with each element initialized to the given value.
	 * @param d1 Size of dimension 1.
	 * @param d2 Size of dimension 2.
	 * @param v Initial value.
	 * @return 2D array of verbs.
	 */
	public static Verb[][] getArray2D(final int d1, final int d2, final Verb v) {
		Verb[][] a = new Verb[d1][d2];
		for (int i1 = 0; i1 < d1; i1++) {
			for (int i2 = 0; i2 < d2; i2++) {
				a[i1][i2] = v;
			}
		}
		return a;
	}
	
	public static double[] solveEquations(final double[][] a) {
		int n = a[0].length - 1;
		//print("Helper.solveEquations n=" + n);
		double[] x = new double[n];

		for (int i = 0; i < n - 1; i++) {
			// Search for row p where A[p, i] is not zero.
			int p; // swap row index
			for (p = i; p < n; p++) {
				if (a[p][i] != 0) break;
			}
			// No unique solution exists
			if (p == n) return x;

			if (p != i) {
				// Swap rows.
				for (int c = 0; c < n + 1; c++) {
					double m = a[p][c];
					a[p][c] = a[i][c];
					a[i][c] = m;
				}
			}

			// Gaussian Elimination.
			for (int j = i + 1; j < n; j++) {
				double m = a[j][i] / a[i][i];
				for (int c = 0; c < n + 1; c++) {
					a[j][c] = a[j][c] - m * a[i][c];
				}
			}
		}

		// No unique solution exists
		if (a[n - 1][n - 1] == 0) return x;

		// Backward Substitution.
		x[n - 1] = a[n - 1][n] / a[n - 1][n - 1];
		for (int i = n - 2; i >= 0; i--) {
			double s = 0.0;
			for (int j = i + 1; j < n; j++) {
				s += a[i][j] * x[j];
			}
			x[i] = (a[i][n] - s) / a[i][i];
		}

		return x;
	}	
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="General">
	
	/**
	 * Returns the nouns in nouns1 that are not in nouns2.
	 * @param nouns1 List 1 of nouns.
	 * @param nouns2 List 2 of nouns.
	 * @return Array of nouns.
	 */
	public static List<Noun> getListExcept(final List<Noun> nouns1, final List<Noun> nouns2) {
		List<Noun> nouns = new ArrayList<>();

		for (Noun noun1 : nouns1) {
			boolean found = false;
			for (Noun noun2 : nouns2) {
				if (noun1 == noun2) {
					found = true;
					break;
				}
			}
			if (!found) nouns.add(noun1);
		}

		return nouns;
	}
	
	/**
	 * Returns the nouns in nouns1 that are not in nouns2.
	 * @param nouns1 Array 1 of nouns.
	 * @param nouns2 Array 2 of nouns.
	 * @return Array of nouns.
	 */
	public static Noun[] getArrayExcept(final Noun[] nouns1, final Noun[] nouns2) {
		ArrayList<Noun> nouns = new ArrayList<>();

		for (Noun noun1 : nouns1) {
			boolean found = false;
			for (Noun noun2 : nouns2) {
				if (noun1 == noun2) {
					found = true;
					break;
				}
			}
			if (!found) nouns.add(noun1);
		}

		return nouns.toArray(new Noun[nouns.size()]);
	}
	
	/**
	 * Returns the puzzle as text.
	 * @param puzzle Puzzle
	 * @return String.
	 */
	static String getPuzzleAsText(final Puzzle puzzle) {
		StringBuilder msg = new StringBuilder();
		
		msg.append("Nouns").append(NL);
		for (NounType nounType : puzzle.nounTypes) {
			msg.append(nounType.name).append(": ").append(getListAsString(nounType.nouns)).append(NL);
		}
		
		msg.append(NL).append("Verbs").append(NL);
		for (Verb verb : puzzle.verbs) {
			msg.append(verb.asString()).append(NL);
		}
		
		msg.append(NL).append("Static Puzzle Verbs").append(NL);
		msg.append(Puzzle.IsNot.asString()).append(NL);
		msg.append(Puzzle.Maybe.asString()).append(NL);
		msg.append(Puzzle.Is.asString()).append(NL);
		
		msg.append(NL).append("Links").append(NL);
		for (Link link : puzzle.links) {
			msg.append(link.asString()).append(NL);
		}
		
		msg.append(NL).append("Facts").append(NL);
		for (Fact fact : puzzle.facts) {
			msg.append(fact.asString()).append(NL);
		}
		
		msg.append(NL).append("Rules").append(NL);
		for (Rule rule : puzzle.rules) {
			msg.append(rule.asString()).append(NL);
		}
		
		return msg.toString();
	}
	// </editor-fold>
}
