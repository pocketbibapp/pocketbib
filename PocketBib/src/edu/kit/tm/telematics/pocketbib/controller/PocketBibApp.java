package edu.kit.tm.telematics.pocketbib.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.FullClient;
import edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants;
import edu.kit.tm.telematics.pocketbib.model.impl.com.amazon.AmazonClient;
import edu.kit.tm.telematics.pocketbib.model.impl.edu.kit.AuthenticationProxy;
import edu.kit.tm.telematics.pocketbib.model.library.managment.ItemCoverManager;
import edu.kit.tm.telematics.pocketbib.model.library.managment.ItemInformationManager;
import edu.kit.tm.telematics.pocketbib.model.library.managment.LibraryManager;
import edu.kit.tm.telematics.pocketbib.model.user.InformationKey;
import edu.kit.tm.telematics.pocketbib.model.user.InformationKey.InformationCategory;
import edu.kit.tm.telematics.pocketbib.model.user.RegistrationProvider;

/**
 * The Application class.
 * 
 */
public class PocketBibApp extends Application {

	/** the instance */
	private static PocketBibApp instance = null;

	/** the SharedPreferences */
	private static SharedPreferences sharedPreferences = null;

	/** usable InformationKeys for the Telematics library */
	private static InformationKey[] telematicsInformationKeys = null;

	/** the registration and library client for the Telematics lbirary */
	private static FullClient telematicsClient = null;

	/** the cover manager */
	private static ItemCoverManager coverManager = null;

	/** the information manager */
	private static ItemInformationManager informationManager = null;

	@Override
	public void onCreate() {
		super.onCreate();

		if (instance != null)
			return;
		
			
		// set the instance
		instance = this;

		// initialize the SharedPreferences
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.myapppreferences, false);

		// initialize the InformationKeys
		telematicsInformationKeys = new InformationKey[] {
				new InformationKey(UserConstants.ROOMNR, InformationCategory.LOCATION, R.string.label_information_room_number),
				new InformationKey(UserConstants.BUILDING, InformationCategory.LOCATION, R.string.label_information_building_number),
				new InformationKey(UserConstants.TELEPHONE, InformationCategory.TELEPHONE_NUMBER, R.string.label_information_telephone_number,
						InputType.TYPE_CLASS_PHONE),
				new InformationKey(UserConstants.NOTE, InformationCategory.OTHER, R.string.label_information_note,
						InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE,
						EditorInfo.IME_ACTION_UNSPECIFIED) };
		
		
		// initialize the cover manager
		coverManager = new ItemCoverManager();
		if (Constants.API_LEVEL >= 14) {
			coverManager.addProvider(AmazonClient.getInstance());
		}

		// initialize the information manager for 3rd party information provider
		informationManager = new ItemInformationManager();
		informationManager.addBookInformationProvider(AmazonClient.getInstance());		 
		

		// initialize the client instance for library management and
		// registration purposes
		telematicsClient = AuthenticationProxy.getInstance();
	}

	/**
	 * Returns the ItemInformationManager for 3rd party sites like amazon.com
	 * 
	 * @return the ItemInformationManager
	 */
	public static ItemInformationManager getItemInformationManager() {
		return informationManager;
	}

	/**
	 * Returns the ItemCoverManager.
	 * 
	 * @return the ItemCoverManager
	 */
	public static ItemCoverManager getItemCoverManager() {
		return coverManager;
	}

	/**
	 * Returns the library management client for the current library.
	 * 
	 * @return the library management client
	 */
	public static LibraryManager getLibraryManager() {
		return telematicsClient;
	}

	/**
	 * Returns the registration provider and management client for the current
	 * library.
	 * 
	 * @return the registration provider and management client
	 */
	public static RegistrationProvider getRegistrationProvider() {
		return telematicsClient;
	}

	/**
	 * The information keys, usable for the current library
	 * 
	 * @return the InformationKeys
	 */
	public static InformationKey[] getInformationKeys() {
		return telematicsInformationKeys;
	}

	/**
	 * Returns the instance
	 * 
	 * @return the instance
	 */
	public static PocketBibApp getInstance() {
		return instance;
	}

	/**
	 * Returns the application context. You can use the application context for
	 * resolving Strings, dimensions, integers and more hardcoded data. DO NOT
	 * use the application context for view or activity related functions.
	 * 
	 * @return the application context
	 */
	public static Context getAppContext() {
		return instance.getApplicationContext();
	}

	/**
	 * Returns the SharedPreferences of the application
	 * 
	 * @return the SharedPreferences
	 */
	public static SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}
}
