package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;

import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;

/**
 * implementation of the sortalgorithm for users as a relevance comparison
 * 
 * 
 */
public class UserRelevanceComparator implements Comparator<LoggedInUser> {

	/** comparator for ascending sort **/
	public static final UserRelevanceComparator ASC = new UserRelevanceComparator();
	
	/** comparator for descending sort **/
	public static final UserRelevanceComparator DESC = new UserRelevanceComparator();


	/**
	 * compares two users relevance
	 * 
	 * @param asc
	 *            ascending or descending sort order
	 */
	private UserRelevanceComparator() {
	}

	@Override
	public int compare(LoggedInUser a, LoggedInUser b) {
		/**
		 * The real sorting is done in ArrayListAdapter.sort for RelevanceComparators.
		 */
		return 0;
	}

}
