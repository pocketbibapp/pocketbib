package edu.kit.tm.telematics.pocketbib.model.library.managment;

import edu.kit.tm.telematics.pocketbib.model.library.Item;
import android.graphics.drawable.Drawable;

/**
 * Provider class for Item covers and thumbnails.
 */
public interface ItemCoverProvider {

	/**
	 * Returns a cover for the given Item or null if no cover is found.
	 * @param item the Item
	 * @return the cover
	 */
	public Drawable getCover(Item item);
	
	/**
	 * Returns a thumbnail for the given Item or null if no thumbnail is found.
	 * @param item the Item
	 * @return the thumbnail
	 */
	public Drawable getThumbnail(Item item);
}
