package com.mysterymaster.puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The Smart Rule class defines methods that return a function for the given puzzle and rule.<br>
 * The returned function checks for rule violations and triggers based on the current mark.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-10
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class SmartRule extends Base {
	/** Puzzle object. Set in the constructor. */
	private final Puzzle puzzle;

	/** Solver object. Set in the validate method. */
	ISolver solver;
	
	@Override
	public String toString() { return "SmartRule"; }
	
	@Override
	public String asString() { return this.toString(); }
	
	/**
	 * Constructor.
	 * @param puzzle Puzzle.
	 */
	public SmartRule(final Puzzle puzzle) {
		this.puzzle = puzzle;
	}
	
	// <editor-fold defaultstate="collapsed" desc="matchAtLeastOne">
	
	/**
	 * Returns true if noun 1 can be with any noun 2 in the list.
	 * Called by SmartRule.getMatchAtLeastOne.
	 * @param noun1 Noun 1.
	 * @param nouns2 List of nouns.
	 * @return Boolean.
	 */
	private boolean canBeWith2(final Noun noun1, final List<Noun> nouns2) {
		for (Noun noun2 : nouns2) {
			if (noun1.type == noun2.type) continue;
			if (Mark.getPairNounNum(noun1, noun2.type) == noun2.num) return true;
			if (solver.canBeWith(noun1, noun2)) return true;
		}
		return false;
	}

	/**
	 * Returns noun 2 from the list if it is the only noun that can be with noun1.<br>
	 * Note: This returns null if noun 1 is already with a noun 2.
	 * Called by SmartRule.getMatchAtLeastOne
	 * @param noun1 Noun 1.
	 * @param nouns2 List of nouns.
	 * @return Noun.
	 */
	private Noun isOnlyNoun(final Noun noun1, final List<Noun> nouns2) {
		Noun noun = null;
		for (Noun noun2 : nouns2) {
			if (noun1.type == noun2.type) continue;
			if (Mark.getPairNoun(noun1, noun2.type) == noun2) return null;
			if (!solver.canBeWith(noun1, noun2)) continue;
			if (noun != null) return null;
			noun = noun2;
		}
		return noun;
	}

	/**
	 * Returns the matchAtLeastOne function to enforce rule where noun1 is with at least one noun in nouns2.<br>
	 * See puzzles: AtTheAlterAltar, DogDuty, ModernNovels, PsychicPhoneFriends.
	 * @param rule Rule.
	 * @param noun1 Noun 1.
	 * @param nouns2 Array of nouns for noun 2.
	 * @return Function matchAtLeastOne
	 */
	public Function<Mark, Integer> getMatchAtLeastOne(final Rule rule, final Noun noun1, final List<Noun> nouns2) {
		return mark -> {
			int rs = 0;

			// Violation if noun1 cannot be with any noun in nouns2.
			if (!canBeWith2(noun1, nouns2)) return -1;

			// Trigger if noun1 can only be with one noun in nouns2.
			Noun noun2 = isOnlyNoun(noun1, nouns2);
			if (noun2 != null) {
				String msg = noun1.name + " must be with " + noun2.name + ".";
				rs = solver.addMarkByRule(mark, rule, ' ', noun1, Puzzle.Is, noun2, msg);
			}

			// Example: For "Dog Duty", Whiley belongs to a woman.
			// If Whiley can be with nounX, but no woman can be with nounX, then Whiley is not with nounX.
			// TODO Do this for other SmartRules? PsychicPhoneFriends benefits.
			for (NounType nounType : puzzle.nounTypes) {
				if (noun1.type == nounType) continue;
				for (Noun nounX : nounType.nouns) {
					if (solver.getGridVerb(noun1, nounX) == Puzzle.IsNot) continue;
					boolean ok = false;
					for (Noun noun : nouns2) {
						if (noun.type == nounType || solver.getGridVerb(noun, nounX) != Puzzle.IsNot) {
							ok = true;
							break;
						}
					}
					if (!ok) {
						String msg = "SmartRule.matchAtLeastOne: No item in list can be with " + nounX.name + ".";
						//print(msg);
						rs = solver.addMarkByRule(mark, rule, ' ', noun1, Puzzle.IsNot, nounX, msg);
						if (rs != 0) return rs;
					}
				}
			}
			return rs;
		};
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="matchOneToExactlyOne">
	
	/**
	 * Returns the matchOneToExactlyOne function to enforce rule where exactly one noun in nouns1 is with exactly one noun in nouns2.<br>
	 * See puzzles: ModernNovels.
	 * @param rule Rule.
	 * @param nouns1 Array of nouns for noun 1.
	 * @param nouns2 Array of nouns for noun 2.
	 * @return Function matchOneToExactlyOne.
	 */
	public Function<Mark, Integer> getMatchOneToExactlyOne(final Rule rule, final List<Noun> nouns1, final List<Noun> nouns2) {
		return mark -> {
			int rs = 0;

			// Example: ModernNovels has exactly one of the two men (Oscar, Peter) chose a Faulkner novel ("Light in August", "Absalom! Absalom!").
			// If  only noun1 in list1 can be with noun2 in list2,
			// and only noun2 in list2 can be with noun1, then noun1 must be with noun2.
			// Also, there is a rule violation if all the counts are zero.

			// Get number of nouns in list1, list2.
			int n1 = nouns1.size();
			int n2 = nouns2.size();
			int[] counts = new int[n1];

			boolean scanFlag = true;
			int i1 = -1; // index of noun1 with count of one, and all others zero.
			int i2 = -1; // index of noun2 that can be with noun1.

			// Examine each noun in list1.
			for (int i = 0; i < n1; i++) {
				Noun noun1 = nouns1.get(i);
				counts[i] = 0;

				// Examine each noun in list2.
				for (int j = 0; j < n2; j++) {
					Noun noun2 = nouns2.get(j);
					// Ignore noun2 if it has the same type as noun1.
					if (noun2.type == noun1.type) continue;

					// Abort if noun1 is already with noun2.
					if (Mark.isPair(noun1, noun2)) {
						scanFlag = false;
						break;
					}

					// Remember index of noun2 if noun1 can be with noun2.
					if (solver.canBeWith(noun1, noun2)) {
						// Abort if count is more than one.
						if (++counts[i] > 1) {
							scanFlag = false;
							break;
						}
						i2 = j;
					}
				}

				if (!scanFlag) break;
				// Remember index of noun1 if count is one.
				if (counts[i] == 1) {
					// Abort if more than one noun1 has a count of one.
					if (i1 != -1) {
						scanFlag = false;
						break;
					}
					i1 = i;
				}
			}

			if (scanFlag) {
				if (i1 != -1 && i2 != -1) {
					// There is only one noun1 that can be with noun2.
					Noun noun1 = nouns1.get(i1);
					Noun noun2 = nouns2.get(i2);
					String msg = noun1.name + " must be with " + noun2.name + ".";
					rs = solver.addMarkByRule(mark, rule, ' ', noun1, Puzzle.Is, noun2, msg);
					if (rs != 0) return rs;
				}
				else {
					// If all the counts are zero, then this is a rule violation.
					for (int i = 0; i < n1; i++) {
						if (counts[i] != 0) {
							scanFlag = false;
							break;
						}
					}
					if (scanFlag) return -1;
				}
			}

			// Rule violation if the number of matches between nouns in list1 and list2 is more than one.
			if (SmartRule.getNumMatches(nouns1, nouns2) > 1) return -1;

			return rs;
		};
	}
	
	/**
	 * Returns the number of matches (zero or more) between the nouns in both lists.
	 * @param nouns1 Array of nouns for noun 1.
	 * @param nouns2 Array of nouns for noun 2.
	 * @return Number of matches.
	 */
	public static int getNumMatches(final List<Noun> nouns1, final List<Noun> nouns2) {
		int cnt = 0;
		for (Noun noun1 : nouns1) {
			for (Noun noun2 : nouns2) {
				if (noun2.type == noun1.type) continue;
				if (Mark.isPair(noun1, noun2)) ++cnt;
			}
		}
		return cnt;
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="matchOneToOne">
	
	/**
	 * Returns the matchOneToOne function to enforce rule where each noun in nouns1 is uniquely matched with one noun in nouns2.<br>
	 * See puzzles: ModernNovels, SmallTownMotels.
	 * @param rule Rule.
	 * @param nouns1 Array of nouns for noun 1.
	 * @param nouns2 Array of nouns for noun 2.
	 * @return Function matchOneToOne.
	 */
	public Function<Mark, Integer> getMatchOneToOne(final Rule rule, final List<Noun> nouns1, final List<Noun> nouns2) {
		int listLength = nouns1.size();
		Verb[][] grid = Helper.getArray2D(listLength, listLength, null);
		return mark -> {
			int rs = 0;

			// Populate the grid with the current marks. Enter 'X' if both nouns have the same type.
			for (int row = 0; row < nouns1.size(); row++) {
				Noun noun1 = nouns1.get(row);
				for (int col = 0; col < nouns2.size(); col++) {
					Noun noun2 = nouns2.get(col);
					grid[row][col] = solver.getGridVerb(noun1, noun2);
					if (noun1.type == noun2.type) grid[row][col] = Puzzle. IsNot;
				}
			}

			// a) Rule violation if there is more than one 'O' per row (may not happen too often).
			// Trigger: If a row has one 'O', enter 'X' for the other cols in that row.
			for (int row = 0; row < nouns1.size(); row++) {
				Noun noun1 = nouns1.get(row);
				int cnt = 0;
				for (int col = 0; col < nouns2.size(); col++) {
					if (grid[row][col] == Puzzle.Is) ++cnt;
				}
				if (cnt > 1) {
					//print("DBG SmartGrid a) Too many positive marks in row!");
					return 1;
				}
				if (cnt == 1) {
					for (int col = 0; col < nouns2.size(); col++) {
						Noun noun2 = nouns2.get(col);
						if (grid[row][col] != Puzzle.Maybe) continue;
						String msg = "Only one of each noun in list2 can be with one of each noun in list1.";
						//print(msg);
						rs = solver.addMarkByRule(mark, rule, 'a', noun1, Puzzle.IsNot, noun2, msg);
						if (rs != 0) return rs;
						grid[row][col] = Puzzle.IsNot;
					}
				}
			}

			// b) Rule violation if there is more than one 'O' per col (may not happen too often).
			// Trigger: If a col has one 'O', enter 'X' for the other rows in that col.
			for (int col = 0; col < nouns2.size(); col++) {
				Noun noun2 = nouns2.get(col);
				int cnt = 0;
				for (int row = 0; row < nouns1.size(); row++) {
					if (grid[row][col] == Puzzle.Is) ++cnt;
				}
				if (cnt > 1) {
					//print("DBG SmartGrid b) Too many positive marks in col!");
					return 1;
				}
				if (cnt == 1) {
					for (int row = 0; row < nouns1.size(); row++) {
						Noun noun1 = nouns1.get(row);
						if (grid[row][col] != Puzzle.Maybe) continue;
						String msg = "Only one of each noun in list1 can be with one of each noun in list2.";
						//print(msg);
						rs = solver.addMarkByRule(mark, rule, 'b', noun1, Puzzle.IsNot, noun2, msg);
						if (rs != 0) return rs;
						grid[row][col] = Puzzle.IsNot;
					}
				}
			}

			// c) Rule violation if there is all 'X' in the row (may not happen too often).
			// Trigger: If a row has all 'X' except one '?', enter 'O' for the '?'.
			for (int row = 0; row < nouns1.size(); row++) {
				Noun noun1 = nouns1.get(row);
				int i = -1;
				int[] cnts = new int[] {0, 0, 0};
				for (int col = 0; col < nouns2.size(); col++) {
					Verb verb = grid[row][col];
					cnts[verb.num] += 1;
					if (verb == Puzzle.Maybe) i = col;
				}
				if (cnts[0] == listLength) {
					//print("SmartGrid c) All negative marks in row!");
					return 1;
				}
				if (cnts[0] == listLength - 1 && cnts[1] == 0 && cnts[2] == 1) {
					Noun noun2 = nouns2.get(i);
					String msg = "Only one noun in list2 is available for noun1.";
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'c', noun1, Puzzle.Is, noun2, msg);
					if (rs != 0) return rs;
					grid[row][i] = Puzzle.Is;
				}
			}

			// d) Rule violation if there is all 'X' in the col (may not happen too often).
			// Trigger: if a col has all 'X' except one '?', enter 'O' for the '?'.
			for (int col = 0; col < nouns2.size(); col++) {
				Noun noun2 = nouns2.get(col);
				int i = -1;
				int[] cnts = new int[] {0, 0, 0};
				for (int row = 0; row < nouns1.size(); row++) {
					Verb verb = grid[row][col];
					cnts[verb.num] += 1;
					if (verb == Puzzle.Maybe) i = row;
				}
				if (cnts[0] == listLength) {
					//print("SmartGrid d) All negative marks in col!");
					return 1;
				}
				if (cnts[0] == listLength - 1 && cnts[1] == 0 && cnts[2] == 1) {
					Noun noun1 = nouns1.get(i);
					String msg = "Only one noun in list1 is available for noun2.";
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'd', noun1, Puzzle.Is, noun2, msg);
					if (rs != 0) return rs;
					grid[i][col] = Puzzle.Is;
				}
			}

			//printGrid();
			return rs;
		};
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="matchOneList">
	
	/**
	 * Returns true if there is coverage (or nothing to do), otherwise false for no coverage.<br>
	 * Coverage means there is at least one unique noun from list1 that can be with a noun from list2.
	 * Called by SmartRule.getMatchOneList.
	 * @param nouns1 Array of nouns for noun 1.
	 * @param nouns2 Array of nouns for noun 2.
	 * @return boolean.
	 */
	private boolean hasCoverage(final List<Noun> nouns1, final List<Noun> nouns2) {
		boolean rs = true;
		int n = nouns1.size();

		// Find unique nouns in nouns2 that can be with the nouns in nouns1.
		List<Noun> nouns = new ArrayList<>();
		int nbad = 0;
		for (Noun noun1 : nouns1) {
			int cnt = 0;
			for (Noun noun2 : nouns2) {
				Verb verb = solver.getGridVerb(noun1, noun2);
				if (verb == Puzzle.Is) return rs;
				if (verb == Puzzle.IsNot) continue;
				++cnt;
				if (!nouns.contains(noun2)) nouns.add(noun2);
			}
			if (cnt == 0) ++nbad;
		}

		rs = (nouns.isEmpty() || nbad == n) || (nouns.size() >= n && nbad == 0);
		return rs;
	}

	/**
	 * Returns the matchOneList function to enforce the rule where the nouns in nouns1 must be with one list of nouns in array2.<br>
	 * See puzzles: Overdue, PlayingCards.
	 * @param rule Rule.
	 * @param nouns1 Array of nouns for noun 1.
	 * @param array2 2D-array of nouns.
	 * @return Function matchOneList.
	 */
	public Function<Mark, Integer> getMatchOneList(final Rule rule, final List<Noun> nouns1, final List<List<Noun>> array2) {
		return mark -> {
			int rs = 0;

			// Trigger if a noun1 is with a noun2 in one of the lists of array2, then the other nouns in nouns1 are not with any nouns in the other lists.
			// Example: If Wicks is with a Wednesday, then Jones is not with a Thursday.
			if (mark.verb == Puzzle.Is) {
				Noun nounX1 = null;
				Noun nounX2 = null;
				int idx2 = -1;
				for (Noun noun : nouns1) {
					if (mark.noun1 == noun) {
						nounX1 = mark.noun1;
						nounX2 = mark.noun2;
						idx2 = SmartRule.getListIndex(nounX2, array2);
					}
					else if (mark.noun2 == noun) {
						nounX1 = mark.noun2;
						nounX2 = mark.noun1;
						idx2 = SmartRule.getListIndex(nounX2, array2);
					}
					if (idx2 > -1) break;
				}

				// The other nouns in nouns1 are not with any nouns in the other lists.
				if (idx2 > -1) {
					//print("matchOneList: noun1 " + nounX1 + " is in list[" + idx2 + "].");
					int idx = -1;
					for (List<Noun> list2 : array2) {
						if (++idx == idx2) continue;
						for (Noun noun2 : list2) {
							if (noun2 == nounX2) continue;
							for (Noun noun1 : nouns1) {
								if (noun1 == nounX1) continue;
								String msg = noun1.name + " is not with " + noun2.name + ".";
								rs = solver.addMarkByRule(mark, rule, 'a', noun1, Puzzle.IsNot, noun2, msg);
								if (rs != 0) return rs;
							}
						}
					}
				}
			}

			// Trigger for each nouns2 in array2, if there are not enough nouns in nouns2 to cover nouns1, then the nouns in nouns2 are not with the nouns in nouns1.
			for (List<Noun> nouns2 : array2) {
				if (hasCoverage(nouns1, nouns2)) continue;
				for (Noun noun1 : nouns1) {
					for (Noun noun2 : nouns2) {
						String msg = noun1.name + " is not with " + noun2.name + ".";
						rs = solver.addMarkByRule(mark, rule, 'a', noun1, Puzzle.IsNot, noun2, msg);
						if (rs != 0) return rs;
					}
				}
			}
			return rs;
		};
	}
	
	/**
	 * Returns the zero-based index of the list that the given noun is in, otherwise -1.
	 * @param nounX Noun X.
	 * @param lists 2D-array of nouns.
	 * @return Zero-based index.
	 */
	private static int getListIndex(final Noun nounX, final List<List<Noun>> lists) {
		int rs = -1;
		int idx = rs;
		for (List<Noun> list : lists) {
			++idx;
			for (Noun noun : list) {
				if (noun == nounX) return idx;
			}
		}
		return rs;
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="isNotBetween">
	
	/**
	 * Returns the isNotBetween function to enforce the rule where noun1 is not between noun2 and noun3, where any two nouns may be slots.<br>
	 * Assumes the slots are ordered by number (either low to high or high to low). See puzzles: AllTiredOut.
	 * @param rule Rule.
	 * @param nounType Noun type.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @param noun3 Noun 3.
	 * @return Function isNotBetween.
	 */
	public Function<Mark, Integer> getIsNotBetween(final Rule rule, final NounType nounType, final Noun noun1, final Noun noun2, final Noun noun3) {
		return mark -> {
			//print("isNotBetween mark=" + mark.num + " nounType=" + nounType.num + " noun1=" + Q + noun1 + Q + " noun2=" + Q + noun2 + Q + " noun3=" + Q + noun3 + Q);
			int rs = 0;

			// Use one-based numbers for each slot.
			int slotA = (noun1.type == nounType) ? noun1.num : Mark.getPairNounNum(noun1, nounType);
			int slotB = (noun2.type == nounType) ? noun2.num : Mark.getPairNounNum(noun2, nounType);
			int slotC = (noun3.type == nounType) ? noun3.num : Mark.getPairNounNum(noun3, nounType);

			// Violation if nounA is between nounB and nounC.
			if (slotA > 0 && slotB > 0 && slotC > 0) {
				if (slotB < slotA && slotA < slotC) return -1;
				if (slotC < slotA && slotA < slotB) return -1;
				return rs;
			}

			// Invoke trigger if two of the slots are known.
			int n = nounType.nouns.size();

			char ch = ' ';
			Noun noun = null;
			int i1 = 0, i2 = 0;

			// a) A < B so C is not less than A.
			if (slotA > 0 && slotB > slotA) {
				ch = 'a'; noun = noun3; i1 = 0; i2 = slotA - 1;
			}
			// b) B < A so C is not more than A.
			if (slotB > 0 && slotA > slotB) {
				ch = 'b'; noun = noun3; i1 = slotA; i2 = n;
			}
				// c) A < C so B is not less than A.
			else if (slotA > 0 && slotC > slotA) {
				ch = 'c'; noun = noun2; i1 = 0; i2 = slotA - 1;
			}
				// d) C < A so B is not more than A.
			else if (slotC > 0 && slotA > slotC) {
				ch = 'd'; noun = noun2; i1 = slotA; i2 = n;
			}
				// e) B < C so A is not between B and C.
			else if (slotB > 0 && slotC > slotB) {
				ch = 'e'; noun = noun1; i1 = slotB; i2 = slotC - 1;
			}
				// f) C < B so A is not between C and B.
			else if (slotC > 0 && slotB > slotC) {
				ch = 'f'; noun = noun1; i1 = slotC; i2 = slotB - 1;
			}

			String msg = noun1.name + " is not between " + noun2.name + " and " + noun3.name + ".";
			for (int i = i1; i < i2; i++) {
				Noun slot = nounType.nouns.get(i);
				if (solver.getGridVerb(noun, slot) == Puzzle.IsNot) continue;
				//print(noun.name + " is not with " + slot.name);
				rs = solver.addMarkByRule(mark, rule, ch, noun, Puzzle.IsNot, slot, msg);
				if (rs != 0) return rs;
			}

			return rs;
		};
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="isRelated">
	
	/**
	 * Returns the isRelated function to enforce the rule where noun1 is related to at least one noun in nouns2.<br>
	 * See puzzles: AllTiredOut
	 * @param rule Rule.
	 * @param noun1 Noun 1.
	 * @param link Link.
	 * @param nouns2 Noun 2.
	 * @return Function isRelated.
	 */
	public Function<Mark, Integer> getIsRelated(final Rule rule, final Noun noun1, final Link link, final List<Noun> nouns2) {
		return mark -> {
			//print("isRelated rule=" + Q + rule.num + Q + " noun1=" + Q + noun1 + Q + " link=" + Q + link + Q + " nouns2=" + Q + nouns2 + Q);
			int rs = 0;
			NounType slots = link.nounType;
			Noun slot1 = (noun1.type == slots) ? noun1 : Mark.getPairNoun(noun1, slots);

			if (slot1 != null) {
				Noun nounB, slotB;
				boolean ok;

				// Violation if all nouns are slotted and noun1 is not related to any noun in nouns2.
				ok = false;
				for (Noun noun2 : nouns2) {
					Noun slot = (noun2.type == slots) ? noun2 : Mark.getPairNoun(noun2, slots);
					if (slot == null || link.f.apply(slot1, slot) == Puzzle.Is) { ok = true; break; }
				}
				if (!ok) return -1;

				// Violation if all slots related to noun1 are full, and no slot contains a noun in the list.
				// Example: For AllTiredOut, rule 2 is "Grace stood next to at least one man in line (clue 7)."
				// If Grace is 1st and a woman is 2nd, then this is a violation.
				ok = false;
				for (Noun slot : slots.nouns) {
					if (link.f.apply(slot1, slot) != Puzzle.Is) continue;
					for (Noun noun : nouns2) {
						nounB = Mark.getPairNoun(slot, noun.type);
						if (nounB == null || nounB == noun) { ok = true; break; }
					}
					if (ok) break;
				}
				if (!ok) return -1;

				// Violation if all slots related to noun1 cannot have any noun in nouns2.
				ok = false;
				for (Noun slot : slots.nouns) {
					if (link.f.apply(slot1, slot) != Puzzle.Is) continue;
					for (Noun noun : nouns2) {
						if (solver.getGridVerb(slot, noun) != Puzzle.IsNot) { ok = true; break; }
					}
					if (ok) break;
				}
				if (!ok) return -1;

				// Trigger if only one noun in list can be related to noun1, then place it.
				// Example: If I manually place Grace first and Ethan fifth, then Jeff must be second!
				nounB = null; slotB = null;
				int cnt = 0;
				for (Noun slot : slots.nouns) {
					if (link.f.apply(slot1, slot) != Puzzle.Is) continue;
					for (Noun noun : nouns2) {
						Noun slotX = Mark.getPairNoun(noun, slots);
						if (slotX == slot) {
							//print(noun.name + " is already in " + slot.name);
							cnt = 2;
							break;
						}
						if (slotX != null) continue;

						if (solver.getGridVerb(noun, slot) == Puzzle.Maybe) {
							//print(noun.name + " may be in " + slot.name);
							if (++cnt > 1) break;
							nounB = noun; slotB = slot;
						}
					}
					if (cnt > 1) break;
				}
				//print("cnt=" + cnt);
				if (cnt == 1) {
					String msg = nounB.name + " must be with " + slotB.name + ".";
					//print("Rule " + rule.num + " " + msg);
					rs = solver.addMarkByRule(mark, rule, 'a', nounB, Puzzle.Is, slotB, msg);
					if (rs != 0) return rs;
				}
			}

			// Trigger if noun1 can be in slotX, but no noun in list can be related to slotX, then noun1 cannot be in slotX.
			if (slot1 == null) {
				for (Noun slotX : slots.nouns) {
					if (solver.getGridVerb(noun1, slotX) != Puzzle.Maybe) continue;
					boolean ok = false;
					String msg = noun1.name + " is not with " + slotX.name + ".";
					for (Noun slot2 : slots.nouns) {
						if (link.f.apply(slotX, slot2) != Puzzle.Is) continue;
						for (Noun noun2 : nouns2) {
							if (solver.getGridVerb(noun2, slot2) != Puzzle.IsNot) {
								ok = true;
								break;
							}
						}
						if (ok) break;
					}
					if (!ok) {
						//print("SmartRule.isRelated Rule " + rule.num + " on mark " + mark.num + ". " + msg);
						rs = solver.addMarkByRule(mark, rule, 'b', noun1, Puzzle.IsNot, slotX, msg);
						if (rs != 0) return rs;
					}
				}
			}

			return rs;			
		};
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="inOppositeGroup">
	
	/**
	 * Returns the inOppositeGroup function to enforce the rule where noun1 and noun2 are not in the same group.<br>
	 * See puzzles: Big5GameRangers.
	 * @param rule Rule.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @param nounType Noun type.
	 * @param map Array of numbers.
	 * @param groupName Group name.
	 * @param groupNames Array of group names.
	 * @return Function inOppositeGroup.
	 */
	public Function<Mark, Integer> getInOppositeGroup(final Rule rule, final Noun noun1, final Noun noun2, final NounType nounType, final int[] map, final String groupName, final String[] groupNames) {
		return mark -> {
			int rs = 0;

			Noun nounA = (noun1.type == nounType) ? noun1 : Mark.getPairNoun(noun1, nounType);
			Noun nounB = (noun2.type == nounType) ? noun2 : Mark.getPairNoun(noun2, nounType);
			if (nounA == null && nounB == null) return rs;

			int g1 = (nounA == null) ? -1 : map[nounA.num - 1];
			int g2 = (nounB == null) ? -1 : map[nounB.num - 1];

			// Violation if both nouns are in the same group, otherwise success if both are in opposite groups.
			// TODO Should I return only for failure, and let the trigger have a shot?
			if (nounA != null && nounB != null) {
				return (g1 == g2) ? -1 : 0;
			}

			// Triggers.
			String msg = noun1.name + " and " + noun2.name + " have the opposite " + groupName + ".";
			for (Noun noun : nounType.nouns) {
				// If noun1's group is known, then noun2 is not with a noun of that group.
				if (nounA != null && map[noun.num - 1] == g1) {
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'a', noun2, Puzzle.IsNot, noun, msg);
					if (rs != 0) return rs;
				}
				// If noun2's group is known, then noun1 is not with a noun of that group.
				if (nounB != null && map[noun.num - 1] == g2) {
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'b', noun1, Puzzle.IsNot, noun, msg);
					if (rs != 0) return rs;
				}
			}

			return rs;
		};
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="inSameGroup">
	
	public Function<Mark, Integer> getInSameGroup(final Rule rule, final Noun noun1, final Noun noun2, final NounType nounType, final int[] map, final String groupName, final String[] groupNames) {
		return mark -> {
			int rs = 0;

			Noun nounA = (noun1.type == nounType) ? noun1 : Mark.getPairNoun(noun1, nounType);
			Noun nounB = (noun2.type == nounType) ? noun2 : Mark.getPairNoun(noun2, nounType);

			int g1 = (nounA == null) ? -1 : map[nounA.num - 1];
			int g2 = (nounB == null) ? -1 : map[nounB.num - 1];

			// Violation if both nouns are in opposite groups, otherwise success if both in same group.
			// TODO Should I return only for failure, and let the trigger have a shot?
			if (nounA != null && nounB != null) {
				return (g1 != g2) ? -1 : 0;
			}

			// Triggers.
			String msg = noun1.name + " and " + noun2.name + " have the same " + groupName + ".";
			// If noun1's group is known, then noun2 is not with a noun of another group.
			if (nounA != null && nounB == null) {
				for (Noun noun : nounType.nouns) {
					if (map[noun.num - 1] == g1) continue;
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'a', noun2, Puzzle.IsNot, noun, msg);
					if (rs != 0) return rs;
				}
			}

			// If noun2's group is known, then noun1 is not with a noun of another group.
			if (nounA == null && nounB != null) {
				for (Noun noun : nounType.nouns) {
					if (map[noun.num - 1] == g2) continue;
					//print(msg);
					rs = solver.addMarkByRule(mark, rule, 'b', noun1, Puzzle.IsNot, noun, msg);
					if (rs != 0) return rs;
				}
			}

			// Examine counts if there are only two groups.
			if (nounA != null || nounB != null) return rs;

			// Example is from Big5GameRangers.
			// * Elephant camp can be run by Ethan or Julia.
			// * Buffalo camp can be run by Delia, Ethan, or Julia.
			// If there are less than two nouns in a group, then those nouns are not possible candidates.

			ArrayList<Noun> group1 = new ArrayList<>(); ArrayList<Noun> group1Noun1 = new ArrayList<>(); ArrayList<Noun> group1Noun2 = new ArrayList<>();
			ArrayList<Noun> group2 = new ArrayList<>(); ArrayList<Noun> group2Noun1 = new ArrayList<>(); ArrayList<Noun> group2Noun2 = new ArrayList<>();

			// Populate the lists.
			for (Noun noun : nounType.nouns) {
				int i = noun.num - 1;
				Verb verb1 = solver.getGridVerb(noun, noun1);
				if (verb1 == Puzzle.Maybe) {
					if (map[i] == 0) group1Noun1.add(noun); else group2Noun1.add(noun);
				}
				Verb verb2 = solver.getGridVerb(noun, noun2);
				if (verb2 == Puzzle.Maybe) {
					if (map[i] == 0) group1Noun2.add(noun); else group2Noun2.add(noun);
				}
				if (verb1 == Puzzle.Maybe || verb2 == Puzzle.Maybe) {
					if (map[i] == 0) group1.add(noun); else group2.add(noun);
				}
			}

			//print(mark.num + " Group 1: " + group1.size() + ","  + group1Noun1.size() + "," + group1Noun2.size() + " Group 2: " + group2.size() + "," + group2Noun1.size() + "," + group2Noun2.size());

			if ((group1.size() < 2 || group1Noun1.size() < 1 || group1Noun2.size() < 1) && group1.size() > 0) {
				msg = "There are not enough " + groupNames[0] + " for " + noun1.name + " and " + noun2.name + ".";
				rs = doListEliminator2(rule, mark, noun1, noun2, group1Noun1, group1Noun2, msg);
				if (rs != 0) return rs;
			}

			if ((group2.size() < 2 || group2Noun1.size() < 1 || group2Noun2.size() < 1) && group2.size() > 0) {
				msg = "There are not enough " + groupNames[1] + " for " + noun1.name + " and " + noun2.name + ".";
				rs = doListEliminator2(rule, mark, noun1, noun2, group2Noun1, group2Noun2, msg);
				if (rs != 0) return rs;
			}
			return rs;
		};
	}
	
	/**
	 * Triggers marks for the given rule where<br>
	 * (a) The nouns in list 1 are not with noun 1, and<br>
	 * (b) The nouns in list 2 are not with noun 2.
	 * @param rule Rule.
	 * @param mark Mark.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @param list1 Nouns that are not with noun 1.
	 * @param list2 Nouns that are not with noun 2.
	 * @param msg Message.
	 * @return Zero for success, nonzero for failure.
	 */
	private int doListEliminator2(final Rule rule, final Mark mark, final Noun noun1, final Noun noun2, final ArrayList<Noun> list1, final ArrayList<Noun> list2, final String msg) {
		int rs = 0;
		for (Noun noun : list1) {
			rs = solver.addMarkByRule(mark, rule, 'a', noun1, Puzzle.IsNot, noun, msg);
			if (rs != 0) return rs;
		}
		for (Noun noun : list2) {
			rs = solver.addMarkByRule(mark, rule, 'b', noun2, Puzzle.IsNot, noun, msg);
			if (rs != 0) return rs;
		}
		return rs;
	}
	
	// </editor-fold>
}
