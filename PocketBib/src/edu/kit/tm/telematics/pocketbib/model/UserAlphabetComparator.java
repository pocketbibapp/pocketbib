package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;

import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;

/**
 * implements the sortalgorithm that compares the users alphabetically,
 * ascending or descending
 * 
 */
public class UserAlphabetComparator implements Comparator<LoggedInUser> {

	/** comparator for ascending sort **/
	public static final UserAlphabetComparator ASC = new UserAlphabetComparator(true);
	/*** comparator for descending sort */
	public static final UserAlphabetComparator DESC = new UserAlphabetComparator(false);
	/** stores whether descending or ascending sort selected **/
	private final int sortOrder;

	/**
	 * Sorts the users in alphabetic order
	 * 
	 * @param asc
	 *            ascending or descending sort order
	 */
	private UserAlphabetComparator(boolean asc) {
		if (asc)
			sortOrder = 1;
		else
			sortOrder = -1;
	}

	@Override
	public int compare(LoggedInUser a, LoggedInUser b) {

		int lastNameSorted = a.getLastName().toLowerCase().compareTo(b.getLastName().toLowerCase());
		if (lastNameSorted == 0) {
			return sortOrder * (a.getFirstName().toLowerCase().compareTo(b.getFirstName().toLowerCase()));
		}

		return sortOrder * lastNameSorted;
	}

}
