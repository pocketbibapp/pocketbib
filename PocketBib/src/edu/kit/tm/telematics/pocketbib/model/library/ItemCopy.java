package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemCopyConstants;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;

public class ItemCopy {

	/**
	 * The identifier for a item copy.
	 */
	private Integer itemCopyId = Constants.ID_NOT_SAVED;
	
	/**
	 * The corresponding library item for this copy.
	 */
	private Item item;
	
	/**
	 * Status of this copy: does it appear in the user library catalog.
	 */
	private boolean isActive;
	
	/**
	 * The time this copy was added to the library.
	 */
	private Date creationTime;
	
	/**
	 * Constructs a item copy.
	 */
	private ItemCopy() {}
	
	/** the user that is currently borrowing this copy */
	private LoggedInUser borrowingUser = null;
	
	/**
	 * Creates a item copy with the given parameters.
	 * 
	 * @param itemCopyId the identifier for this item copy object in the database
	 * @param item the corresponding library item
	 * @param isActive if the copy is in active use or has been deactivated
	 * @param creationTime the time this copy was added to the library
	 * @return the created item copy
	 */
	public static ItemCopy instantiateExisting(int itemCopyId, Item item, boolean isActive, Date creationTime) {
		if (itemCopyId <= 0) {
			throw new IllegalArgumentException("Item copy needs an item id.");
		} else if (item == null) {
			throw new IllegalArgumentException("Item Copy needs an item.");
		} else {
			ItemCopy copy = new ItemCopy();
			copy.itemCopyId = itemCopyId;
			copy.item = item;
			copy.isActive = isActive;
			copy.creationTime = creationTime;
			return copy;
		}
	}
	
	/**
	 * Creates a new copy for an item.
	 * 
	 * @param item
	 *            the item with the new copy
	 * @return the newly created copy
	 */
	public static ItemCopy createNew(Item item) {
		if (item != null) {
		ItemCopy copy = new ItemCopy();
		copy.item = item;
		return copy;
		} else {
			throw new IllegalArgumentException("Item Copy needs an item.");
		}
	}

	/**
	 * Checks if the copy is in active use or has been deactivated
	 * @return <code>true</code> if the copy is in active use; <code>false</code> if the copy is deactivated
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Checks if the copy is in active use or has been deactivated
	 * @param isActive if the copy is in active use or has been deactivated
	 * @return the copy 
	 */
	public ItemCopy setActive(boolean isActive) {
		this.isActive = isActive;
		return this;
	}

	/**
	 * Returns the identifier for this copy.
	 * @return the identifier for this copy
	 */
	public int user() {
		return itemCopyId;
	}

	/**
	 * Returns the library item which this copy belongs to.
	 * @return the library item which this copy belongs to
	 */
	public Item getItem() {
		return item;
	}
	
	/**
	 * Returns the user that is currently borrowing this copy.
	 * @return the user that is currently borrowing this copy
	 */
	public LoggedInUser getUser() {
		return borrowingUser;
	}

	/**
	 * Sets the user that is currently borrowing this copy
	 * @param user the user that is currently borrowing this copy
	 * 
	 */
	public ItemCopy setUser(LoggedInUser user) {
		this.borrowingUser = user;
		return this;
	}

	
	/**
	 * Returns the unique identifier of this copy
	 * @return the unique identifier
	 */
	public Integer getItemCopyId() {
		return itemCopyId;
	}

	/**
	 * Returns time this copy was added to the library.
	 * @return time this copy was added to the library
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns the object as a {@link JSONObject}.
	 * @return the object as a {@link JSONObject}
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.putOpt(ItemCopyConstants.ITEM_COPY_ID, this.getItemCopyId());
			json.putOpt(ItemCopyConstants.ITEM_ID, this.getItem().getItemId());
			json.putOpt(ItemCopyConstants.IS_ACTIVE, this.isActive());
			//json.putOpt(ItemCopyConstants.CREATION_TIME, this.getCreationTime().toGMTString());
		} catch (JSONException e) {
			e.printStackTrace();
			json = null;
		}
		return json;
	}

	@Override
	public String toString() {
		return "ItemCopy [id=" + itemCopyId + " isActive=" + isActive + " user=" + borrowingUser + " item=" + item + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ItemCopy) {
			ItemCopy copy = (ItemCopy) o;
			return itemCopyId != Constants.ID_NOT_SAVED && item.itemId == copy.itemCopyId;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return itemCopyId;
	}
	
	
}
