package edu.kit.tm.telematics.pocketbib.model.library.managment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * Manager class for Item covers. This class acts as facade - all requests for
 * covers and thumbnails should go through this class!
 */

public class ItemCoverManager implements ItemCoverProvider {
	
	/** the local cache for covers and thumbnails */
	private final LocalCoverCache cache;

	/** a list of cover and thumbnail providers */
	private final List<ItemCoverProvider> providerList;
	
	/** if now thumbnail is found, this one will be used */
	private final Drawable fallbackThumbnail;

	/** if now cover is found, this one will be used */
	private final Drawable fallbackCover;
	
	@SuppressWarnings("unused")
	/** used for debugging purposes */
	private final static String TAG = "ItemCoverManager";
	

	/** Creates a manager of item cover provider*/ 
	public ItemCoverManager() {
		cache = new LocalCoverCache();
		providerList = new ArrayList<ItemCoverProvider>();
		fallbackThumbnail = PocketBibApp.getAppContext().getResources().getDrawable(R.drawable.fallback_thumbnail);
		fallbackCover = PocketBibApp.getAppContext().getResources().getDrawable(R.drawable.fallback_cover);
	}

	/**
	 * Returns the thumbnail that is supposed to be used if no correct one could be found
	 * @return the fallback thumbnail
	 */
	public Drawable getFallbackThumbnail() {
		return fallbackThumbnail;
	}
	
	
	public LocalCoverCache getLocalCoverCache() {
		return cache;
	}
	
	/**
	 * Returns the cover that is supposed to be used if no correct one could be found
	 * @return the fallback cover
	 */
	public Drawable getFallbackCover() {
		return fallbackCover;
	}
	
	/**
	 * Adds a item cover provider.
	 * @param provider the provider.
	 * @return <code>true</code> if the provider was added, <code>false</code>
	 *         if the provider was already managed beforehand or is <code>null</code>
	 */
	public boolean addProvider(ItemCoverProvider provider) {
		if(provider != null && ! providerList.contains(provider)) {
			providerList.add(provider);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Removes a item cover provider.
	 * @param provider the provider.
	 * @return <code>true</code> if the provider was removed, <code>false</code>
	 *         if the provider wasn't managed beforehand and therefore couldn't
	 *         be deleted
	 */
	public boolean removeProvider(ItemCoverProvider provider) {
		return providerList.remove(provider);
	}
	
	/**  Gets the cover */ 
	public Drawable getCover(Item item) {
		//At first, look in the local cache
		Drawable cover = cache.getCover(item);
		
		if(cover == null) {
			// If not found in the cache, ask each provider one after one
			for (ItemCoverProvider provider : providerList) {
				cover = provider.getCover(item);
				
				if(cover != null) {
					cache.putCover(item, cover);
					break;
				}
			}
		}
		
		return cover;
	}
	
	/** gets the thumbnail*/ 
	public Drawable getThumbnail(Item item) {
		//At first, look in the local cache
		Drawable thumb = cache.getThumbnail(item);
		
		if(thumb == null) {
			// If not found in the cache, ask each provider one after one
			for (ItemCoverProvider provider : providerList) {
				thumb = provider.getThumbnail(item);
				
				if(thumb != null) {
					cache.putThumbnail(item, thumb);
					//Log.i(TAG, "Thumbnail has been added to cache for item: " + item);
					break;
				}
			}
		} else {
			//Log.i(TAG, "Thumbnail was in cache for item: " + item);
		}
		
		return thumb;
	}
}
