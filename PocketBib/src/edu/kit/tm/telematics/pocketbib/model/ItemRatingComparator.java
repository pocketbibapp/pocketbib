package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;

import android.util.Log;
import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * implemention of sortalgorithm that sorts items by rating
 */
public class ItemRatingComparator implements Comparator<Item> {

	/** comparator for ascending sort **/
	public static ItemRatingComparator ASC = new ItemRatingComparator(true);
	/** comparator for descending sort **/
	public static ItemRatingComparator DESC = new ItemRatingComparator(false);
	/** stores whether descending or ascending sort selected **/
	private final int sortOrder;

	/**
	 * compares two ratings
	 * 
	 * @param asc
	 *            ascending of descending sort order
	 */
	private ItemRatingComparator(boolean asc) {
		if (asc)
			sortOrder = 1;
		else
			sortOrder = -1;
	}

	@Override
	public int compare(Item a, Item b) {
			Log.i("ItemRatingComparator", "Comparing " + a.getAverageRating() + " to " + b.getAverageRating());
		
		if(a.getAverageRating() == null && b.getAverageRating() != null) {
			return -1;
		} else if(a.getAverageRating() == null && b.getAverageRating() == null) {
			return 0;
		} else {
			return sortOrder * a.getAverageRating().compareTo(b.getAverageRating());
		}
	}

}
