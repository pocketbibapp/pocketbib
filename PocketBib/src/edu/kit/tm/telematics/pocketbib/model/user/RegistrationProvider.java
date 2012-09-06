package edu.kit.tm.telematics.pocketbib.model.user;

import java.util.Map;

import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;

public interface RegistrationProvider {
	/**
	 * gets a user with a specific id
	 * @param id id of the user
	 * @return list of loggedinusers
	 */
	public Response<LoggedInUser> getUser(Integer id);

	/**
	 * inserts a user
	 * @param user user
	 * @return integer of the newly created user
	 */
	public Response<Integer> insertOrUpdateUser(LoggedInUser user);

	/**
	 * If a user has been stored, loads it back to be used as current user.
	 * @return response code
	 */
	public ResponseCode loadSavedUser();
	
	/**
	 * logs in a user in
	 * @param username username
	 * @param password password
	 * @param persistent persistent logn
	 * @return response code
	 */
	public ResponseCode login(String username, String password, boolean persistent);

	/**
	 * logges a user off
	 */
	public void logout();

	/**
	 * Removes a user
	 * @param user user
	 * @return response code
	 */
	public ResponseCode removeUser(LoggedInUser user);

	/**
	 * search for multiple users
	 * 
	 * @param queryParameters search parameters
	 * @return list of users
	 */
	public Response<UserAdapter> searchUsers(Map<String, String> queryParameters);

	/**
	 * search for a user
	 * @param queury search parameter
	 * @return list of users
	 */
	public Response<UserAdapter> searchUsers(String queury);
	
	
	/**
	 * get a list of all users
	 * @return list of users
	 */
	public Response<UserAdapter> getUsers();

	/**
	 * Hashes the string
	 * @param string the string
	 * @return the hashed string
	 */
	public String hash(final String string);

	
	
}
