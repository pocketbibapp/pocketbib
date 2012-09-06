package edu.kit.tm.telematics.pocketbib.model.library.managment;

import java.util.Map;

import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopy;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopyAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.Rating;
import edu.kit.tm.telematics.pocketbib.model.library.RatingAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
/**
 * Provides administrative functions.
 *
 */
public interface LibraryManager {
	/**
	 * borrows an item
	 * @param item the item that is borrowed
	 * @return indicates the status of the execution
	 */
	public ResponseCode borrowItem(Item item);
	/**
	 * removes an rating
	 * @param rating the rating
	 * @return indicates the status of the execution
	 */
	public ResponseCode removeRating(Rating<? extends Item> rating);
	/**
	 * returns the borrowed items of a user.
	 * @param user the user 
	 * @return the borrowed item of the user
	 */
	public Response<ItemAdapter> getBorrowedItems(LoggedInUser user);
	/**
	 * returns the item with the specified ID.
	 * @param itemId ID of the item
	 * @return the item
	 */
	public Response<Item> getItem(int itemId);
	/**
	 * returns the copies of an item.
	 * @param itemId ID of the item
	 * @return the copies of the item
	 */
	public Response<ItemCopyAdapter> getItemCopies(int itemId);
	/**
	 * returns the ratings of an item.
	 * @param itemId ID of the item
	 * @return the ratings of the item
	 */
	public Response<RatingAdapter> getItemRatings(int itemId);
	/**
	 * Adds a new copy to the database or updates the item if an item with the same item id exists.
	 * @param copy the copy that is added or modified
	 * @return indicates the status of the execution
	 */
	public Response<Integer> insertOrUpdateCopy(ItemCopy copy);
	/**
	 * Adds a new item to the database or updates the item if an item with the same item id exists.
	 * @param item the item that is added or modified
	 * @return indicates the status of the execution
	 * @see {@link Item#getItemId()
	 */
	public Response<Integer> insertOrUpdateItem(Item item);
	/**
	 * saves a rating
	 * @param rating the rating 
	 * @return indicates the status of the execution
	 */
	public Response<Integer> insertOrUpdateRating(Rating<? extends Item> rating);
	/**
	 * removes a copy of a item.
	 * @param copy the copy that is disabled
	 * @return indicates the status of the execution
	 */
	public ResponseCode removeCopy(ItemCopy copy);
	/**
	 * removes a item.
	 * @param item the item that is disabled
	 * @return indicates the status of the execution
	 */
	public ResponseCode removeItem(Item item);
	/**
	 * gives a borrowed item back
	 * @param item the item that was borrowed
	 * @return indicates the status of the execution
	 */
	public ResponseCode returnItem(Item item);
	/**
	 * Searches for the item with the specified parameters.
	 * @param queryParameters The parameters that are searched
	 * @return the results of the search
	 */
	public Response<ItemAdapter> searchLibrary(Map<String, String> queryParameters);
	/**
	 * Searches for the item with the specified parameter.
	 * @param query The parameter that is searched
	 * @param showDisabled true means: show disabled items (needed for admin ui)
	 * @return the results of the search
	 */
	public Response<ItemAdapter> searchLibrary(String query, boolean showDisabled);
}
