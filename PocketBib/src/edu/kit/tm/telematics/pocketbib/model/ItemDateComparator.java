package edu.kit.tm.telematics.pocketbib.model;

import java.util.Comparator;
import java.util.Date;

import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.Magazine;
import edu.kit.tm.telematics.pocketbib.model.library.OtherItem;

/**
 * implementations of the sortalgorithms for item comparison by date.
 * 
 * Book dates are mapped as 31.12.PublicationYear -> books are considered to
 * have more relevance than magazines in search forms -> so they appear above
 * magazines.
 */
public class ItemDateComparator implements Comparator<Item> {

	/** comparator for ascending sort **/
	public static ItemDateComparator ASC = new ItemDateComparator(true);
	/** comparator for descending sort **/
	public static ItemDateComparator DESC = new ItemDateComparator(false);
	/** stores whether descending or ascending sort selected **/
	private final int sortOrder;

	private ItemDateComparator(boolean asc) {
		if (asc)
			sortOrder = 1;
		else
			sortOrder = -1;
	}

	@Override
	public int compare(Item a, Item b) {

		Date dateA;
		Date dateB;

		dateA = getDate(a);
		dateB = getDate(b);

		// if both items do not have a date they are equal. If one does have a
		// date the item with date is preferred.
		if (dateA == null && dateB == null)
			return sortOrder * 0;
		if (dateA != null && dateB == null)
			return sortOrder * 1;
		if (dateA == null && dateB != null)
			return sortOrder * -1;

		return sortOrder * dateA.compareTo(dateB);
	}

	/**
	 * gets the date of a item
	 * 
	 * @param item
	 *            referenced item
	 * @return date of the item
	 */
	private Date getDate(Item item) {
		// dates are null - only if a date in the databaseset is set the date
		// will be set
		Date date = null;

		if (item instanceof Magazine) {
			// gets the date if the item is a magazine
			date = ((Magazine) item).getPublicationDate();
		}

		if (item instanceof Book) {
			// sets the date if the item is a book
			Book b = (Book) item;
			if (b.getPublicationYear() != Constants.NOT_SAVED) {
				date = new Date(b.getPublicationYear() - 1900, 12, 31);
			}
		}
		if (item instanceof OtherItem) {
			// sets the date if the item is an other item
			OtherItem other = (OtherItem) item;
			if (other.getPublicationDate() != null) {
				date = ((OtherItem) item).getPublicationDate();
			} else if (other.getPublicationYear() != Constants.NOT_SAVED) {
				date = new Date(other.getPublicationYear() - 1900, 12, 31);
			}

		}
		return date;

	}
}
