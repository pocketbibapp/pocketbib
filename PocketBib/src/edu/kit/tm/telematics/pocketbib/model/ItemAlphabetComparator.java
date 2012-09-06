package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;

import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * implementation of item comparison as alphabetic comparison, ascending or
 * descending
 * 
 */
public class ItemAlphabetComparator implements Comparator<Item> {

	/** comparator for ascending sort **/
	public static final ItemAlphabetComparator ASC = new ItemAlphabetComparator(true);
	/** comparator for descending sort **/
	public static final ItemAlphabetComparator DESC = new ItemAlphabetComparator(false);
	/** stores whether descending or ascending sort selected **/
	private final int sortOrder;

	/**
	 * compares two items alphabetically
	 * 
	 * @param asc
	 *            ascending or descending sort order
	 */
	private ItemAlphabetComparator(boolean asc) {
		if (asc)
			sortOrder = 1;
		else
			sortOrder = -1;
	}

	@Override
	public int compare(Item a, Item b) {
		return sortOrder * (a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase()));
	}

}
