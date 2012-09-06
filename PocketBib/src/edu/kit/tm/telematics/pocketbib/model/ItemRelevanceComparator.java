package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;

import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * implementations for item comparison by relevance
 */
public class ItemRelevanceComparator implements Comparator<Item> {

	/** comparator for ascending sort **/
	public static ItemRelevanceComparator ASC = new ItemRelevanceComparator();
	
	/** comparator for descending sort **/
	public static ItemRelevanceComparator DESC = new ItemRelevanceComparator();

	/**
	 * compares two items by relevance
	 * 
	 * @param asc
	 *            ascending or descending sort order
	 */
	private ItemRelevanceComparator() {
	}

	@Override
	public int compare(Item a, Item b) {
		/**
		 * The real sorting is done in ArrayListAdapter.sort for RelevanceComparators.
		 */
		return 0;
	}

}
