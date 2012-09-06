package edu.kit.tm.telematics.pocketbib.model;

import edu.kit.tm.telematics.pocketbib.controller.activity.admin.ItemAddEditActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.admin.UserAddEditActivity;


/**
 * App wide constants.
 * 
 */
public class Constants {

	public static final String PORT_HTTP = "80";
	
	public static final String PORT_HTTPS = "443";
	
	public static class Intent {
		
		/** constant used in {@link ItemAddEditActivity} and {@link UserAddEditActivity} to signal the mode the activity is in */
		public static final String KEY_ADD_EDIT_MODE = "mode";

		/** constant to show that the current mode is the add mode */ 
		public static final String VALUE_ADD_MODE = "add";

		/** constant to show that the current mode is the edit mode */
		public static final String VALUE_EDIT_MODE = "edit";

		/** constant key for a parcelable user to be passed with an intent */
		public static final String KEY_USER_PARCEL = "user_parcel";

		/** constant key for the current active admin tab to be used with an intent */
		public static final String KEY_ADMIN_ACTIVE_TAB = "active";

		public static final int VALUE_ADMIN_ACTIVE_TAB_ITEMS = 0;

		public static final int VALUE_ADMIN_ACTIVE_TAB_USERS = 1;

		public static final String KEY_SERIALIZED_ITEM = "item";
		
		public static final String KEY_ITEM_PARCEL = "item_parcel";

		/**
		 * The other intent that started the intent.
		 */
		public static final String KEY_CALLING_INTENT = "calling_intent";
		
		public static final String VALUE_SEARCH_RESULTS_ACTIVITY = "edu.kit.tm.telematics.pocketbib"
				+ ".controller.activity.SearchResultsActivity";
		
		public static final String VALUE_ADMIN_ACTIVITY = "edu.kit.tm.telematics.pocketbib"
				+ ".controller.activity.AdminActivity";
		
		public static final String VALUE_ITEM_DETAIL_ACTIVITY = "edu.kit.tm.telematics.pocketbib"
				+ ".controller.activity.ItemDetailActivity";
		
		private Intent() {
		}
	}
	
	/** constants for the sort field which are given to the RestClient */
	public static enum SortField { TITLE, NAME, RELEVANCE, DATE, RATING };
	
	/** constants for sort order which are given to the RestClient */
	public static enum SortOrder  { ASC, DESC };

	/**
	 * Query parameters for the extended search. These constants define the keys
	 * used in a query.
	 */
	public static class QueryParameters {

		/** the author (used for books) */
		public static final String KEY_AUTHOR = "author";

		/** the ISBN (used for books) */
		public static final String KEY_ISBN = "isbn";

		/** the ISSN (used for magazines) */
		public static final String KEY_ISSN = "issn";

		/** the title of the work */
		public static final String KEY_TITLE = "title";

		/** the description of the work */
		public static final String KEY_DESCRIPTION = "description";

		/** the page count (number of pages) */
		public static final String KEY_PAGE_COUNT = "pagecount";

		/** the publisher */
		public static final String KEY_PUBLISHER = "publisher";

		/** the position inside the library */
		public static final String KEY_POSITION = "position";

		/**
		 * the publication date (only applicable to Magazines and OtherItems,
		 * not to Books)
		 */
		public static final String KEY_PUBLICATION_DATE = "publication_date";

		/**
		 * the publication year (only applicable to Books and OtherItems, not to
		 * Magazines)
		 */
		public static final String KEY_PUBLICATION_YEAR = "publication_year";

		/**
		 * the type of the item (possible values are {@code VALUE_TYPE_BOOK},
		 * {@code VALUE_TYPE_MAGAZINE} or any other wished type of publication
		 * (e.g. CD, DVD, ...)
		 */
		public static final String KEY_TYPE = "type";

		/** the availability of the item */
		public static final String KEY_AVAILABLE = "available";

		/** the value "Book" for {@code KEY_TYPE} */
		public static final String VALUE_TYPE_BOOK = "Book";

		/** the value "Magazine" for {@code KEY_TYPE} */
		public static final String VALUE_TYPE_MAGAZINE = "Magazine";

		/**
		 * the value for {@link #KEY_AVAILABLE}, describing an available (not
		 * lent) item
		 */
		public static final String VALUE_AVAILABILITY_AVAILABLE = "true";

		/**
		 * the value for {@link #KEY_AVAILABLE}, describing an unavailable /
		 * lent item
		 */
		public static final String VALUE_AVAILABILITY_LENT = "false";

		/**
		 * Private constructor -- no instantiation allowed!
		 */
		private QueryParameters() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * the SharedPreference-key for storing the serialized LoggedInUser-object
	 * of the current user
	 */
	public static final String KEY_SERIALIZED_CURRENT_USER = "pocketbib.current_user";

	/** the SharedPreference-key for storing the authkey */
	public static final String KEY_AUTH_KEY = "pocketbib.kit.authkey";
	
	/** the SharedPreference-key for storing the currently saved user (as JSON string) */
	public static final String SAVED_USER = "pocketbib.kit.saved_user";

	/** the SharedPreference-key for storing the server adress */
	public static final String KEY_LIBRARY_SERVER = "pocketbib.kit.server_adress";

	/** the SharedPreference-key for storing the server port */
	public static final String KEY_LIBRARY_SERVER_PORT = "pocketbib.kit.server_port";
	
	public static final String KEY_LIBRARY_SERVER_SSL = "pocketbib.kit.server_ssl";
	
	/** the SharedPreference-key for storing the cover cache quality */
	public static final String KEY_CACHE_QUALITY = "pocketbib.kit.cache_quality";
	
	/** Constant value for an unsaved Object */
	// null funktioniert hier leider nicht, da dieser Wert mit int-Werten verglichen wird und
	// Java beim casten von null-Integer auf int eine NullPointerException schmei�t!
	public static final Integer ID_NOT_SAVED = -1;

	/** Constant value for an unset int, long, double, float or byte field */
	public static final int NOT_SAVED = -1;

	/** the filename of the SharedPreferences */
	public static final String SHARED_PREFERENCES_FILE = "PocketBib.prefs";

	/** the Intent-Extra key for the query data, supplied as JSON String */
	public static final String KEY_QUERY_DATA = "edu.kit.tm.telematics.pocketbib.query.data";

	/** the Intent-Extra key for the query type */
	public static final String KEY_QUERY_TYPE = "edu.kit.tm.telematics.pocketbib.query.type";

	/** the Intent-Extra value for an extended query */
	public static final String VALUE_QUERY_TYPE_EXTENDED = "extended";

	/**
	 * The Intent-Extra value for a simple query. This is transmitted, when the
	 * user clicks on "search" and expects a EditText to enter the query
	 */
	public static final String VALUE_QUERY_TYPE_SIMPLE = "simple";

	/**
	 * The Intent-Extra value for a barcode scan. This is transmitted, when the
	 * user clicks on "search" and expects a EditText to enter the query
	 */
	public static final String VALUE_QUERY_TYPE_SCAN_BARCODE = "barcode";

	/**
	 * The Intent-Extra value for a voice search. This is transmitted, when the
	 * user clicks on "search" and expects a EditText to enter the query
	 */
	public static final String VALUE_QUERY_TYPE_VOICE = "voice";

	/**
	 * API level of the current android environment. Necessary for some workarounds for old Android versions.
	 */
	public static final int API_LEVEL = android.os.Build.VERSION.SDK_INT;
	
	public static final String KEY_BARCODE_SCANNING_COMPLETE = "barcodeScanningComplete";

	/**
	 * Private constructor - no instantiation necessary!
	 */
	private Constants() {
		throw new UnsupportedOperationException();
	}

	
	public class UserConstants {
		public static final String EMAIL = "email";
		public static final String FIRST_NAME = "firstname";
		public static final String LAST_NAME = "lastname";
		public static final String IS_ACTIVE = "is_active";
		public static final String IS_ADMINISTRATOR = "is_admin";
		public static final String USER_ID = "id";
		public static final String ADDITIONAL_INFORMATION = "additional_information";
		public static final String BUILDING = "building";
		public static final String ROOMNR = "roomnr";
		public static final String NOTE = "note";
		public static final String TELEPHONE = "telephone";
		public static final String PASSWORD = "password";
		public static final String PASSWORDHASH = "passwordhash";
		public static final String IS_DELETED = "is_deleted";
	}

	public class ItemCopyConstants {
		public static final String ITEM_COPY_ID = "id";
		public static final String ITEM_ID = "item_id";
		public static final String CREATION_TIME = "creation_time";
		public static final String IS_ACTIVE = "is_active";
	}

	public class RatingConstants {
		public static final String RATING__ID = "id";
		public static final String ITEM_ID = "item_id";
		public static final String USER_ID = "user_id";
		public static final String CREATION_TIME = "creation_time";
		public static final String RATING = "rating";
		public static final String COMMENT = "comment";
	}

	public class ItemConstants {
		public static final String TYPE = "type";
		public static final String TYPE_BOOK = "Book";
		public static final String TYPE_MAGAZINE = "Magazine";
		public static final String TYPE_OTHER_ITEM = "OtherItem";
		public static final String AUTHOR = "author";
		public static final String AVAILABLE_COPIES = "availableCopies";
		public static final String SUM_RATING = "sum_rating";
		public static final String DESCRIPTION = "description";
		public static final String EDITION = "edition";
		public static final String ITEM_ID = "id";
		public static final String IS_ACTIVE = "is_active";
		public static final String PAGE_COUNT = "num_pages";
		public static final String POSITION = "position";
		public static final String PUBLICATION_YEAR = "publication_year";
		public static final String PUBLISHER = "publisher";
		public static final String RATING_COUNT = "num_rating";
		public static final String RATING = "rating";
		public static final String TITLE = "title";
		public static final String ISBN = "isbn13";
		public static final String ISSN = "issn";
		public static final String PUBLICATION_DATE = "publication_date";
		public static final String PRICE = "price";
		public static final String DETAIL_URL = "detail_url";
	}
}