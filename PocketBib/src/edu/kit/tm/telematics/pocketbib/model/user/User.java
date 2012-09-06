package edu.kit.tm.telematics.pocketbib.model.user;

import java.util.regex.Pattern;

/**
 * The basic User.
 */
public class User {
	
	/**
	 * Thrown when the current user is a guest (not logged in) but tried to
	 * perform a logged in users only action.
	 */
	@SuppressWarnings("serial")
	public static class NotLoggedInException extends Exception {}

	/**
	 * The instance of the current user of this app.
	 */
	private static User currentUser = new User();

	/**
	 * Regular expression to validate email addresses.
	 * 
	 * @see<a 
	 *        href="http://www.regular-expressions.info/email.html">http://www.regular
	 *        -expressions.info/email.html</a>
	 */
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

	/**
	 * Returns the current User. This may be a {@code User} for guests or
	 * {@code LoggedInUser} for registered users and administrators.
	 * 
	 * @return the current User.
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Sets the current user. Providing a LoggedInUser sets the LoggedInUser as
	 * currently logged in app user. Provide {@code null} when logging out.
	 * 
	 * @param user
	 *            the currently logged in User, or {@code null} on logout
	 */
	public static void setCurrentUser(LoggedInUser user) {
		currentUser = (user == null) ? new User() : user;
	}

	/**
	 * Checks if the given string is a valid email address
	 * @param email the string
	 * @return <code>true</code> if it is a valid email; <code>false</code> otherwise
	 */
	public static boolean validEmail(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * Creates a new User.
	 */
	protected User() {
	}

	@Override
	public boolean equals(Object o) {
		/*
		 * User.equals(LoggedInUser) returns always false; User.equals(User)
		 * returns always true
		 */
		if (o instanceof LoggedInUser) {
			return false;
		} else if (o instanceof User) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 13;
	}

	/**
	 * Checks if the current user is activated.
	 * 
	 * @return <code>true</code> if the current user is activated;
	 *         <code>false</code> if not
	 */

	public boolean isActive() {
		return true;
	}

	/**
	 * Checks if the current user has administrator rights.
	 * 
	 * @return <code>true</code> if the current user has administrator rights;
	 *         <code>false</code> if not
	 */
	public boolean isAdministrator() {
		return false;
	};

	@Override
	public String toString() {
		return "User [loggedIn=false, active=true, administrator=false]";
	}
}
