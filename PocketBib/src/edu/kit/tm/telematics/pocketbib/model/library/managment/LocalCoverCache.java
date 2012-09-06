package edu.kit.tm.telematics.pocketbib.model.library.managment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * This class implements the interface ItemCoverProvider. It is a local cache
 * for covers and thumbnails.
 */
public class LocalCoverCache extends Application implements ItemCoverProvider {

	/** the collection of saved covers */
	private final LruCache<Item, Drawable> covers;

	/** the collection of saved thumbnails */
	private final LruCache<Item, Drawable> thumbnails;

	/** the actual context of the application */
	private final Context context;

	/** the cache directory card */
	private final File cacheDir;

	/** Tag for debbuging purposes */
	private final static String TAG = "LocalCoverCache";

	/**
	 * Creates a new LocalCoverCache.
	 */
	@TargetApi(8)
	public LocalCoverCache() {
		context = PocketBibApp.getAppContext();

		// get the available memory size
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int memSize = manager.getMemoryClass() * 1024;

		// reserve 20% of the memory for covers
		covers = new LruCache<Item, Drawable>((int) (memSize * 0.2)) {
			@Override
			protected int sizeOf(Item key, Drawable value) {
				return getBitmapSize(((BitmapDrawable) value).getBitmap());
			}
		};

		// reserve 40% of the memory for thumbnails
		thumbnails = new LruCache<Item, Drawable>((int) (memSize * 0.4)) {
			@Override
			protected int sizeOf(Item key, Drawable value) {
				return getBitmapSize(((BitmapDrawable) value).getBitmap());
			}
		};

		// only 60 % is used to not use up all of the space for the application

		if (Constants.API_LEVEL >= 8)
			cacheDir = context.getExternalCacheDir();
		else
			cacheDir = context.getCacheDir();
		// Log.i(TAG, "cachDir: " + ((cacheDir == null) ? null :
		// cacheDir.toString()));
	}

	/**
	 * Returns the size of a Bitmap in bytes.
	 * 
	 * @param bitmap
	 *            the Bitmap
	 * @return the size in bytes
	 */
	@TargetApi(12)
	private static int getBitmapSize(Bitmap bitmap) {
		if (Constants.API_LEVEL >= 12) {
			return bitmap.getByteCount();
		} else {
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}

	public void clearCache() {
		for (File child : cacheDir.listFiles()) {
			if (child.getName().endsWith(".jpg") && child.getName().contains("cover")
					|| child.getName().contains("thumbnail")) {
				child.delete();
			}
		}
	}

	@Override
	public Drawable getCover(Item item) {
		Drawable cover = covers.get(item);

		if (cover != null)
			return cover;

		cover = readFromCache("cover-" + item.getItemId() + ".jpg");

		if (cover != null)
			putCover(item, cover);

		return cover;
	}

	@Override
	public Drawable getThumbnail(Item item) {
		Drawable thumb = thumbnails.get(item);

		if (thumb != null)
			return thumb;

		thumb = readFromCache("thumbnail-" + item.getItemId() + ".jpg");

		if (thumb != null)
			putThumbnail(item, thumb);

		return thumb;
	}

	/**
	 * Reads a drawable from the cache. The filename has to be cover-X.jpg for
	 * cover drawables and thumbnail-X.jpg for thumbnail drawables where X is
	 * the id of the item.
	 * 
	 * @param filename
	 *            the filename.
	 * @return the drawable; <code>null</code> if it doesn't exist
	 */
	private Drawable readFromCache(String filename) {
		File file = new File(cacheDir, filename);

		if (!file.exists())
			return null;

		try {
			return Drawable.createFromStream(new FileInputStream(file), filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Saves a drawable in the cache folder.
	 * 
	 * @param drawable
	 *            the drawable
	 * @param item
	 *            the item
	 * @param isThumbnail
	 *            true, if the drawable is a thumbnail, false if the drawable is
	 *            cover
	 */
	private void save(Drawable drawable, Item item, boolean isThumbnail) {
		if (drawable == null || item == null) {
			Log.w(TAG, "Cannot save null into cache (drawable=" + drawable + ", item=" + item + ")");
			return;
		}

		String filename = ((isThumbnail) ? "thumbnail-" : "cover-") + item.getItemId() + ".jpg";
		File file = new File(cacheDir, filename);

		if (!file.exists()) {
			try {
				FileOutputStream out = new FileOutputStream(file);
				((BitmapDrawable) drawable).getBitmap().compress(CompressFormat.JPEG, 100, out);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Log.v(TAG, "Blocked overwrite of " + filename + "(" + item.getTitle() + ").");
		}

	}

	/**
	 * Saves the cover locally
	 * 
	 * @param item
	 *            the item
	 * @param cover
	 *            the cover of the item
	 */
	public void putCover(Item item, Drawable cover) {
		covers.put(item, cover);
		new SaveTask(cover, item, false).execute();
	}

	/**
	 * Saves the thumbnail locally
	 * 
	 * @param item
	 *            the item
	 * @param thumbnail
	 *            the thumbnail of the item
	 */
	public void putThumbnail(Item item, Drawable thumbnail) {
		thumbnails.put(item, thumbnail);
		new SaveTask(thumbnail, item, true).execute();
	}

	/**
	 * A task for saving drawables in a background thread.
	 */
	private class SaveTask extends AsyncTask<Void, Void, Void> {

		private Drawable drawable;

		private Item item;

		private boolean isThumbnail;

		/**
		 * Creates a new SaveTask.
		 * 
		 * @param drawable
		 *            the drawable
		 * @param item
		 *            the item
		 * @param isThumbnail
		 *            true, if the drawable is a thumbnail, or false if the
		 *            thumbnail is a cover image
		 */
		public SaveTask(Drawable drawable, Item item, boolean isThumbnail) {
			super();
			this.drawable = drawable;
			this.item = item;
			this.isThumbnail = isThumbnail;
		}

		@Override
		protected Void doInBackground(Void... params) {
			save(drawable, item, isThumbnail);
			return null;
		}
	}
}
