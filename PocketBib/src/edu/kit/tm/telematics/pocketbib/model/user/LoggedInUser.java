package edu.kit.tm.telematics.pocketbib.model.user;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants;
import edu.kit.tm.telematics.pocketbib.model.PasswordGenerator;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.user.InformationKey.InformationCategory;

/**
 * This class represents any registered user. To get the currently logged in
 * User on the current device use {@code User.getCurrentUser()}.
 * 
 */
public class LoggedInUser extends User implements Parcelable {

	@SuppressWarnings("unused")
	/** Tag for debugging purposes */
	private final static String TAG = "LoggedInUser";

	/**
	 * Prepares a new LoggedInUser object.
	 * 
	 * @return the new LoggedInUser
	 */
	public static LoggedInUser createNew() {
		return new LoggedInUser();
	}

	/**
	 * Prepares a LoggedInUser object to be filled with information from the
	 * database.
	 * 
	 * @param userId
	 *            the user id
	 * @return the LoggedInUser
	 */
	public static LoggedInUser instantiateExisting(int userId) {
		return new LoggedInUser().setUserId(userId);
	}

	/**
	 * the user id (or {@code Constants.ID_NOT_SAVED} when not stored in
	 * database)
	 */
	private Integer userId = Constants.ID_NOT_SAVED;

	/** the first name */
	private String firstName = null;

	/** the last name */
	private String lastName = null;

	/** the email address, which is also used as account identifier */
	private String email = null;

	/**
	 * activation status of the user (true if the account is activated, false if
	 * the account is deactivated)
	 */
	private boolean isActive = true;

	/** the administrator status of the user */
	private boolean isAdministrator = false;

	/** the clear text password */
	private String password = null;

	/** the hashed password */
	private String passwordhash = null;
	
	
	private boolean isDeleted = false;

	/**
	 * a Map containing additional information about the user. Refer to
	 * {@code PocketBibApp.getInformationKeys()} to get all usable
	 * InformationKeys
	 */
	private Map<InformationKey, String> informationMap = new HashMap<InformationKey, String>();

	/**
	 * Creates a new LoggedInUser. Use the factory methods
	 * LoggedInUser.createNew() and LoggedInUser.instantiateExisting() to create
	 * objects.
	 */
	private LoggedInUser() {}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LoggedInUser) {
			LoggedInUser u = (LoggedInUser) o;

			if (u.isStoredInDatabase() && this.isStoredInDatabase()) {
				return u.userId.equals(this.userId);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the email.
	 * 
	 * @return the email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the first name.
	 * 
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Returns additional information about the user, specified by an
	 * InformationKey. Refer to {@link PocketBibApp.getInformationKeys()} for
	 * usable InformationKeys.
	 * 
	 * @param key
	 *            the InformationKey
	 * @return the associated information
	 */
	public String getInformation(InformationKey key) {
		return informationMap.get(key);
	}

	/**
	 * Returns a copy of the Map with all additional information. The Map can be
	 * changed without the LoggedInUser object being affected.
	 * 
	 * @return a copy of the information Map
	 */
	public Map<InformationKey, String> getInformationMap() {
		// return a defensive copy!
		return new HashMap<InformationKey, String>(informationMap);
	}

	/**
	 * Returns the last name
	 * 
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Returns a telephone number of the LoggedInUser, or {@code null} if no
	 * number is provided.
	 * 
	 * @param user
	 *            the user
	 * @return a telephone number, or {@code null} if no number is provided.
	 */
	public String getTelephoneNumber() {

		for (InformationKey key : informationMap.keySet()) {
			if (key.getCategory() == InformationCategory.TELEPHONE_NUMBER) {
				return informationMap.get(key);
			}
		}

		return null;
	}

	/**
	 * Returns the user id
	 * 
	 * @return the user id
	 */
	public Integer getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		return userId;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public boolean isAdministrator() {
		return isAdministrator;
	}

	/**
	 * Returns true, if the user account is stored in the database
	 * 
	 * @return true, if the user is stored in the database.
	 */
	public boolean isStoredInDatabase() {
		return userId != null && !userId.equals(Constants.ID_NOT_SAVED);
	}

	/**
	 * Removes the LoggedInUser from the database.
	 * 
	 * @return true, if the removal succeeds, false on error
	 */
	public ResponseCode remove() {
		if (!isStoredInDatabase()) {
			// LoggedInUser is not stored in the database - no deletion required
			return ResponseCode.OK;
		}

		ResponseCode responseCode = PocketBibApp.getRegistrationProvider().removeUser(this);

		if (responseCode == ResponseCode.OK) {
			this.userId = Constants.ID_NOT_SAVED;
		}

		return responseCode;
	}

	/**
	 * Saves the LoggedInUser to the database by either inserting or updating.
	 * 
	 * @return true, if the saving succeeds, false on error.
	 */
	public ResponseCode save() {
		Response<Integer> response = PocketBibApp.getRegistrationProvider().insertOrUpdateUser(this);
		ResponseCode responseCode = response.getResponseCode();

		if (responseCode == ResponseCode.OK) {
			this.userId = response.getData();
		}

		return responseCode;
	}

	/**
	 * Returns the password.
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the password hash.
	 * @return the password hash
	 */
	public String getPasswordhash() {
		return passwordhash;
	}

	/**
	 * Sets a new password and sends it optionally via e-mail.
	 * 
	 * @param password
	 *            the new password
	 * @param sendEmail
	 *            true, if an e-mail containing the password shall be sent to
	 *            the user
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setPassword(String password, boolean sendEmail) {
		if (sendEmail) {
			this.passwordhash = null;
			this.password = password;
		} else {
			this.password = null;
			this.passwordhash = PocketBibApp.getRegistrationProvider().hash(password);
		}
		return this;
	}

	/**
	 * Assigns a automatically generated password and sends it via email when
	 * {@link #save()} is called.
	 * 
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setAutomaticallyGeneratedPassword() {
		setPassword(PasswordGenerator.generatePassword(6, 8), true);
		return this;
	}

	/**
	 * Sets the activation status of the user account.
	 * 
	 * @param isActive
	 *            the activation status
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setActive(boolean isActive) {
		this.isActive = isActive;
		/*
		 * if (this.isActive) { Log.e("SetIsActive", "true"); } else {
		 * Log.e("SetIsActive", "false"); }
		 */
		return this;
	}
	
	
	/**
	 * Sets the delete status of the user account.
	 * 
	 * @param isDeleted
	 *            the delete status
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;

		return this;
	}

	/**
	 * Sets the administrator status of the user account.
	 * 
	 * @param isAdministrator
	 *            the administrator status
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setAdministrator(boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
		/*
		 * if (this.isAdministrator) { Log.e("SetIsAdmin", "true"); } else {
		 * Log.e("SetIsAdmin", "false"); }
		 */
		return this;
	}

	/**
	 * Sets the email
	 * 
	 * @param email
	 *            the email
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setEmail(String email) {
		this.email = email;
		return this;
	}

	/**
	 * Sets the first name.
	 * 
	 * @param firstName
	 *            the first name
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * Sets additional information about the user.
	 * 
	 * @param key
	 *            the InformationKey (refer to {@link
	 *            PocketBibApp.getInformationKeys()} for usable
	 *            InformationKeys.)
	 * @param value
	 *            the associated information
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setInformation(InformationKey key, String value) {
		informationMap.put(key, value);
		return this;
	}

	/**
	 * Sets the information Map, containing additional information about the
	 * user.
	 * 
	 * @param informationMap
	 *            the information Map, or {@code null} to clear the current Map
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setInformationMap(Map<InformationKey, String> informationMap) {
		this.informationMap = (informationMap == null) ? new HashMap<InformationKey, String>() : informationMap;
		return this;
	}

	/**
	 * Sets the last name.
	 * 
	 * @param lastname
	 *            the last name
	 * @return the current LoggedInUser ({@code this})
	 */
	public LoggedInUser setLastName(String lastname) {
		this.lastName = lastname;
		return this;
	}

	/**
	 * Sets the user id.
	 * 
	 * @param userId
	 *            the user id
	 * @return the current LoggedInUser ({@code this})
	 */
	private LoggedInUser setUserId(int userId) {
		this.userId = userId;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(50);
		b.append("LoggedInUser [loggedIn=true").append(", active=").append(isActive).append(", admin=")
				.append(isAdministrator).append(", id=").append(userId).append(", email=").append(email)
				.append(", firstname='").append(firstName).append(", lastname='").append(lastName).append("'");

		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			if (informationMap.containsKey(key)) {
				b.append(", ").append(key.getName()).append("=").append(informationMap.get(key));
			}
		}
		return b.append("]").toString();
	}

	/**
	 * Returns the object as a {@link JSONObject}.
	 * @return the object as a {@link JSONObject}
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			if (!Constants.ID_NOT_SAVED.equals(userId))
				json.putOpt(UserConstants.USER_ID, getUserId());
			
			json.putOpt(UserConstants.EMAIL, getEmail());
			json.putOpt(UserConstants.FIRST_NAME, getFirstName());
			json.putOpt(UserConstants.LAST_NAME, getLastName());
			json.putOpt(UserConstants.IS_ACTIVE, isActive());
			json.putOpt(UserConstants.IS_DELETED, isDeleted());
			json.putOpt(UserConstants.IS_ADMINISTRATOR, isAdministrator());
			json.putOpt(UserConstants.PASSWORD, getPassword());
			json.putOpt(UserConstants.PASSWORDHASH, getPasswordhash());
			
			for (InformationKey key : PocketBibApp.getInformationKeys()) {
				if (informationMap.containsKey(key)) {
					json.putOpt(key.getName(), informationMap.get(key));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			json = null;
		}
		return json;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b = new Bundle();

		b.putString(UserConstants.EMAIL, getEmail());
		b.putString(UserConstants.FIRST_NAME, getFirstName());
		b.putString(UserConstants.LAST_NAME, getLastName());
		b.putBoolean(UserConstants.IS_ACTIVE, isActive());
		b.putBoolean(UserConstants.IS_ADMINISTRATOR, isAdministrator());
		b.putInt(UserConstants.USER_ID, getUserId());

		Bundle additionalInformation = new Bundle();

		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			if (informationMap.containsKey(key)) {
				additionalInformation.putString(key.getName(), informationMap.get(key));
			}
		}

		b.putBundle(UserConstants.ADDITIONAL_INFORMATION, additionalInformation);

		dest.writeBundle(b);

	}

	/**
	 * A public CREATOR field that generates instances of the {@link LoggedInUser} class from a Parcel. 
	 */
	public static final Parcelable.Creator<LoggedInUser> CREATOR = new Parcelable.Creator<LoggedInUser>() {
		public LoggedInUser createFromParcel(Parcel in) {
			return new LoggedInUser(in);
		}

		public LoggedInUser[] newArray(int size) {
			return new LoggedInUser[size];
		}
	};

	/**
	 * Creates a <code>LoggedInUser</code> from a {@link Parcel} created by {@link #writeToParcel(Parcel, int)}.
	 * @param source the <code>LoggedInUser</code> as a parcel object
	 * @return the resulting <code>LoggedInUser</code>
	 */
	protected LoggedInUser(Parcel in) {
		Bundle b = in.readBundle();
		Bundle additionalInformation = b.getBundle(UserConstants.ADDITIONAL_INFORMATION);

		email = b.getString(UserConstants.EMAIL);
		firstName = b.getString(UserConstants.FIRST_NAME);
		lastName = b.getString(UserConstants.LAST_NAME);
		isActive = b.getBoolean(UserConstants.IS_ACTIVE);
		isAdministrator = b.getBoolean(UserConstants.IS_ADMINISTRATOR);
		userId = b.getInt(UserConstants.USER_ID);

		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			if (additionalInformation.containsKey(key.getName())) {
				informationMap.put(key, additionalInformation.getString(key.getName()));
			}
		}
	}
}
