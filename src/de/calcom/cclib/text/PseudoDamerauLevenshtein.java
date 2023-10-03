/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry Polivaev
 *
 *  This file's author is Felix Natter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.calcom.cclib.text;

import java.util.*;

/**
 * Pseudo-Damerau-Levenshtein (aka "Optimal String Distance")
 * implementation which allows some non-adjacent transpositions(?)
 * Computes the edit distance with insertions/deletions/substitutions/transpositions.
 * Optionally the edit distance of a semi-global alignment is computed which
 * allows the search term to be shifted free-of-cost (i.e. dist("file", "a file is")==0).
 * TODO: use unicode code points instead of chars !!
 * @see "Some properties are explained in the unit test org.freeplane.features.filter.EditDistanceStringMatchingStrategiesTest"
 * @author Felix Natter <fnatter@gmx.net>
 *
 */
public class PseudoDamerauLevenshtein {
	public enum Type { Global, SemiGlobal }

    private int[][] matrix;
	private String searchTerm;
	private String searchText;
	private final int costIndel = 1;
	private final int costMismatch = 1;
	private final int costTranspos = 1;
	private Type type;
	private Stack<Alignment> alignmentsInProgress;
	private ArrayList<Alignment> alignmentsDone;
	
	public class Alignment implements Comparable<Alignment>
	{
		private final String searchTermString;
		private final String searchTextString;
		private final double prob;
		private final int matchStart;
		private final int matchEnd;
		private final int r, c;
		
		public int getMatchStart()
		{
			return matchStart;
		}
		
		public int getMatchEnd()
		{
			return matchEnd;
		}
		
		public boolean overlapsWith(final Alignment other)
		{	
			return (matchStart <= other.matchStart && other.matchStart <= matchEnd-1) || // endpoint of this lies in other
				   (other.matchStart <= matchStart && matchStart <= other.matchEnd-1); // endpoint of other lies in this
				   
		}
				
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + c;
			result = prime * result + matchEnd;
			result = prime * result + matchStart;
			long temp;
			temp = Double.doubleToLongBits(prob);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + r;
			result = prime
					* result
					+ ((searchTermString == null) ? 0 : searchTermString
							.hashCode());
			result = prime
					* result
					+ ((searchTextString == null) ? 0 : searchTextString
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Alignment other = (Alignment) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (c != other.c) {
				return false;
			}
			if (matchEnd != other.matchEnd) {
				return false;
			}
			if (matchStart != other.matchStart) {
				return false;
			}
			if (Double.doubleToLongBits(prob) != Double
					.doubleToLongBits(other.prob)) {
				return false;
			}
			if (r != other.r) {
				return false;
			}
			if (!Objects.equals(searchTermString, other.searchTermString))
				return false;
			return Objects.equals(searchTextString, other.searchTextString);
        }



		public String getMatch()
		{
			return searchText.substring(matchStart, matchEnd);
		}
		
		public int compareTo(final Alignment other)
		{
			if (prob == other.prob)
			{
				return Integer.compare(getMatch().length(), other.getMatch().length());
			}
			else
			{
				return Double.compare(prob, other.prob);
			}
		}
		
		public void print()
		{
			System.out.format("Alignment@%x[%.2f]:\n%s\n%s\n=> matches '%s' [%d,%d]\n",
					hashCode(), prob, searchTermString, searchTextString, getMatch(),
					matchStart,matchEnd);
		}
		
		@Override
		public String toString()
		{
			return String.format("Ali@%x[%s,%.2f,%d,%d]", hashCode(), getMatch(), prob, matchStart, matchEnd);
		}
		
		public Alignment(final String searchTermString, final String searchTextString, final double prob,
				final int matchStart, final int matchEnd, final int r, final int c)
		{
			this.searchTermString = searchTermString;
			this.searchTextString = searchTextString;
			this.prob = prob;
			this.matchStart = matchStart;
			this.matchEnd = matchEnd;
			this.r = r;
			this.c = c;
		}



		private PseudoDamerauLevenshtein getOuterType() {
			return PseudoDamerauLevenshtein.this;
		}
	}
	
	private boolean isMatch(int i, int j) {
		char col = searchTerm.charAt(i-1);
		char row = searchText.charAt(j-1);
        return col == row || row == '-';
	}
	
	public int distance() {
		
		matrix = new int[searchTerm.length()+1][searchText.length()+1];
		
		 // first column: start-gap penalties for searchTerm
		for (int i = 0; i <= searchTerm.length(); i++)
			matrix[i][0] = i*costIndel;
		
		// first row: start-gap penalties for searchText
		if (type == Type.Global)
		{
			for (int j = 1; j <= searchText.length(); j++)
				matrix[0][j] = j*costIndel;
		}
		else if (type == Type.SemiGlobal)
		{
			Arrays.fill(matrix[0], 0);
		}
		
		// compute the rest of the matrix
		for (int i = 1; i <= searchTerm.length(); i++) 
		{
			for (int j = 1; j <= searchText.length(); j++) 
			{
				int cost_try_match = matrix[i-1][j-1] + (isMatch(i,j) ? 0 : costMismatch);
				int cost_ins = matrix[i-1][j] + costIndel;
				int cost_del = matrix[i][j-1] + costIndel;
				matrix[i][j] = Math.min(cost_try_match, Math.min(cost_ins, cost_del));
				
				if (i >= 2 && j >= 2 &&
					    searchTerm.charAt(i-2) == searchText.charAt(j-1) &&
					    searchTerm.charAt(i-1) == searchText.charAt(j-2))
				{
					matrix[i][j] = Math.min(matrix[i][j], matrix[i-2][j-2] + costTranspos);
				}
		  	}
		}
		if (type == Type.Global)
		{
			return matrix[searchTerm.length()][searchText.length()];
		}
		else
		{
			int min = Integer.MAX_VALUE;
			for (int j = 1; j <= searchText.length()+1; j++)
			{
				min = Math.min(min, matrix[searchTerm.length()][j-1]);
			}
			return min;
		}
	
	}
	
	private void writeMatrix(int[][] H)
	{
		for (int i = 0; i < H.length; i++)
		{
			for (int j = 0; j < H[0].length; j++)
			{
				System.out.format(" %3d", H[i][j]);
			}
			System.out.println();
		}
	}
	
	public List<Alignment> computeAlignments(final double minProb)
	{
		alignmentsInProgress = new Stack<>();
		alignmentsDone = new ArrayList<>();
		
		int dist = distance(); // this computes the Dynamic Programming matrix according to Levenshtein
		
		if (type == Type.Global && getMatchProb(dist) > minProb)
		{
			alignmentsInProgress.push(new Alignment("", "", getMatchProb(dist), 0, searchText.length(),
					searchTerm.length(), searchText.length()));
		}
		else
		{
			// semi-global "substring" alignment
			StringBuilder searchTermSuffix = new StringBuilder();
			StringBuilder searchTextSuffix = new StringBuilder();
			for (int c = searchText.length() + 1; c >= 1; c--)
			{
				if (c <= searchText.length())
				{
					searchTermSuffix.append('-');
					searchTextSuffix.insert(0, searchText.charAt(c-1));
				}
				double prob = getMatchProb(matrix[searchTerm.length()][c-1]); 
				if (prob > minProb)
				{
					alignmentsInProgress.push(new Alignment(searchTermSuffix.toString(), searchTextSuffix.toString(),
							prob, 0, searchText.length() - searchTextSuffix.length(), searchTerm.length(), c - 1));
				}
			}
		}
		
		while (!alignmentsInProgress.isEmpty())
		{
			developAlignment(alignmentsInProgress.pop());
		}
		
		// filter (overlapping) alignments
		alignmentsDone = filterAlignments(alignmentsDone);
		
		sortAlignments(alignmentsDone);

        matrix = null;

		return alignmentsDone;
	}
	
	/**
	 * Keep only non-overlapping matches (alignments) while preferring alignments with high score (prob)
	 * TODO: this is a heuristic, is the problem NP complete?
	 * 
	 * @param alignments alignments list to filter
	 * @return filtered alignment list
	 */
	static ArrayList<Alignment> filterAlignments(final ArrayList<Alignment> alignments)
	{
		if (alignments.isEmpty())
			return new ArrayList<>();
		
		// sort by score and match length (see Alignment.compareTo()) 
		alignments.sort(Collections.reverseOrder());
		
		ArrayList<Alignment> clusters = new ArrayList<>(alignments.size());
		// start with a single cluster
		clusters.add(alignments.get(0));
		alignments.remove(0);
		
		// assign alignments to clusters
		for (Alignment ali: alignments)
		{
			boolean found_cluster = false;
			for (int j = 0; j < clusters.size(); j++)
			{
				if (ali.overlapsWith(clusters.get(j)))
				{
					found_cluster = true;
					// keep either current cluster center or set to 'ali'
					if (ali.compareTo(clusters.get(j)) > 0)
					{
						clusters.set(j, ali);
					}
				}
			}
			if (!found_cluster)
			{
				clusters.add(ali);
			}
		}
		return clusters;
	}	
	
	/**
	 * Sort alignments (matches) by start positions
	 * @param alignments list of alignments to sort
	 */
	static void sortAlignments(final ArrayList<Alignment> alignments)
	{
		alignments.sort(new Comparator<Alignment>() {

            public int compare(Alignment o1, Alignment o2) {
                return Integer.compare(o1.matchStart, o2.matchStart);
            }

        });
	}

	private void developAlignment(final Alignment ali)
	{

        if (ali.r == 0 && ali.c == 0)
		{
			alignmentsDone.add(ali);
        }
		else
		{
			
			// match/mismatch
			if (ali.r >= 1 && ali.c >= 1 && matrix[ali.r][ali.c] == matrix[ali.r-1][ali.c-1] + (isMatch(ali.r,ali.c) ? 0 : costMismatch))
			{

				
				alignmentsInProgress.push(new Alignment(null, null,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 1, ali.c - 1)
						);
			}

            if (type == Type.SemiGlobal && ali.r == 0)
			{

				int c = ali.c, matchStart = ali.matchStart;
				StringBuilder searchTermPrefix = new StringBuilder();
				StringBuilder searchTextPrefix = new StringBuilder();
				while (c > 0)
				{
                    matchStart += 1;
					c--;
				}
				alignmentsInProgress.push(new Alignment( null, null,
						ali.prob, matchStart, ali.matchEnd, 0, 0)
						);
			}

			// insertion
			if (ali.c >= 1 && matrix[ali.r][ali.c] == matrix[ali.r][ali.c-1] + costIndel)
			{
				alignmentsInProgress.push(new Alignment(null, null,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r, ali.c - 1)
						);
			}
						
			// deletion
			if (ali.r >= 1 && matrix[ali.r][ali.c] == matrix[ali.r-1][ali.c] + costIndel)
			{
				alignmentsInProgress.push(new Alignment(null, null,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 1, ali.c)
						);
			}
			
			// Damerau-Extension (transpositions)
			if (ali.r >= 2 && ali.c >= 2 && matrix[ali.r][ali.c] == matrix[ali.r-2][ali.c-2] + costTranspos &&
			    searchTerm.charAt(ali.r-2) == searchText.charAt(ali.c-1) &&
			    searchTerm.charAt(ali.r-1) == searchText.charAt(ali.c-2))
			{
				alignmentsInProgress.push(new Alignment(null, null,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 2, ali.c - 2)
						);
			}
		}
	}

	private float getMatchProb(final int distance)
	{
		if (type == Type.SemiGlobal)
		{
			return 1.0F - ((float)distance / searchTerm.length());
		}
		else
		{
			return 1.0F - ((float)distance / Math.min(searchTerm.length(), searchText.length()));
		}
	}
	
	public float matchProb()
	{
		int dist = distance();
		matrix = null;
		return getMatchProb(dist);
	}
	
	public PseudoDamerauLevenshtein() {
	}

	public void init(String searchTerm, String searchText,
			boolean subStringMatch, boolean caseSensitive) 
	{
		if (searchTerm == null || searchText == null)
		{
			throw new IllegalArgumentException("Null searchText/searchTerm!");
		}

		if (caseSensitive)
		{
			this.searchTerm = searchTerm;
			this.searchText = searchText;
		}
		else
		{
			this.searchTerm = searchTerm.toLowerCase();
			this.searchText= searchText.toLowerCase();
		}
		this.type = subStringMatch ? Type.SemiGlobal : Type.Global;
	}

}
