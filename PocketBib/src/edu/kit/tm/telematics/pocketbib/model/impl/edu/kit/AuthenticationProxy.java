package edu.kit.tm.telematics.pocketbib.model.impl.edu.kit;

import java.util.Map;

import android.util.Log;
import edu.kit.tm.telematics.pocketbib.model.FullClient;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
import edu.kit.tm.telematics.pocketbib.model.library.Issn;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopy;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopyAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.Magazine;
import edu.kit.tm.telematics.pocketbib.model.library.Rating;
import edu.kit.tm.telematics.pocketbib.model.library.RatingAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.UserAdapter;

/**
 * A proxy class for a real client, which handles the authentication.
 */
public class AuthenticationProxy implements FullClient {
	
	/** the underlying, real client */
	private final RestClient client;
	
	/** the instance */
	private static AuthenticationProxy instance;
	
	/**
	 * Returns the instance of the AuthenticationProxy.
	 * @return the instance of the AuthenticationProxy.
	 */
	public static AuthenticationProxy getInstance() {
		if(instance == null) {
			instance = new AuthenticationProxy(RestClient.getInstance());
		}
		return instance;
	}
	

	/**
	 * Private constructor - no external instantiation allowed.
	 * @param client the underlying client
	 */
	private AuthenticationProxy(RestClient client) {
		this.client = client;
	}

	public ResponseCode borrowItem(Item item) {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			return client.borrowItem(item);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public ResponseCode removeRating(Rating<? extends Item> rating) {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			return client.removeRating(rating);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public Book getBook(Isbn isbn) {
		return client.getBook(isbn);
	}

	public Response<ItemAdapter> getBorrowedItems(LoggedInUser user) {
		if(User.getCurrentUser().isAdministrator() || user.equals(User.getCurrentUser())) {
			return client.getBorrowedItems(user);
		} else {
			return new Response<ItemAdapter>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	public Response<Item> getItem(int itemid) {
		return client.getItem(itemid);
	}

	public Response<ItemCopyAdapter> getItemCopies(int itemId) {
		return client.getItemCopies(itemId);
	}
	
	public Response<RatingAdapter> getItemRatings(int itemId) {
		return client.getItemRatings(itemId);
	}

	public Magazine getMagazine(Issn issn) {
		return client.getMagazine(issn);
	}

	public Response<LoggedInUser> getUser(int id) {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			return client.getUser(id);
		} else {
			return new Response<LoggedInUser>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}
	
	public Response<Integer> insertOrUpdateCopy(ItemCopy copy) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.insertOrUpdateCopy(copy);
		} else {
			return new Response<Integer>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	public Response<Integer> insertOrUpdateItem(Item item) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.insertOrUpdateItem(item);
		} else {
			return new Response<Integer>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	public Response<Integer> insertOrUpdateRating(Rating<? extends Item> rating) {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			return client.insertOrUpdateRating(rating);
		} else {
			return new Response<Integer>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	public Response<Integer> insertOrUpdateUser(LoggedInUser user) {
		if(User.getCurrentUser().isAdministrator()) {
			Log.w("DEBUG!", "AuthenticationProxy: insertOrUpdateUser admin");
			return client.insertOrUpdateUser(user);
		} else if (User.getCurrentUser() instanceof LoggedInUser
				&& ((LoggedInUser) User.getCurrentUser()).getEmail().equals(user.getEmail())) {
			Log.w("DEBUG!", "AuthenticationProxy: insertOrUpdateUser admin");
			return client.insertOrUpdateUser(user);
		} else {
			Log.w("DEBUG!", "AuthenticationProxy: insertOrUpdateUser failed");
			return new Response<Integer>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}
	
	public ResponseCode loadSavedUser() {
		if(! (User.getCurrentUser() instanceof LoggedInUser)) {
			return client.loadSavedUser();
		} else {
			return ResponseCode.ERROR_UNKNOWN;
		}
	}

	public ResponseCode login(String username, String password, boolean persistent) {
		if(! (User.getCurrentUser() instanceof LoggedInUser)) {
			return client.login(username, password, persistent);
		} else {
			// ERROR_LOGIN_FAILED means wrong email or password. this should be unreachable.  
			return ResponseCode.ERROR_UNKNOWN;
		}
	}

	public void logout() {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			client.logout();
		}
	}

	public ResponseCode removeCopy(ItemCopy copy) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.removeCopy(copy);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public ResponseCode removeItem(Item item) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.removeItem(item);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public ResponseCode removeUser(LoggedInUser user) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.removeUser(user);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public ResponseCode returnItem(Item item) {
		if(User.getCurrentUser() instanceof LoggedInUser) {
			return client.returnItem(item);
		} else {
			return ResponseCode.ERROR_NO_RIGHTS;
		}
	}

	public Response<ItemAdapter> searchLibrary(
			Map<String, String> queryParameters) {
		return client.searchLibrary(queryParameters);
	}

	public Response<ItemAdapter> searchLibrary(String query, boolean showDisabled) {
		return client.searchLibrary(query, showDisabled);
	}

	public Response<UserAdapter> searchUsers(Map<String, String> queryParameters) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.searchUsers(queryParameters);
		} else {
			return new Response<UserAdapter>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}
	
	public Response<UserAdapter> getUsers() {
		if(User.getCurrentUser().isAdministrator()) {
			return client.getUsers();
		} else {
			return new Response<UserAdapter>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	public Response<UserAdapter> searchUsers(String query) {
		if(User.getCurrentUser().isAdministrator()) {
			return client.searchUsers(query);
		} else {
			return new Response<UserAdapter>(ResponseCode.ERROR_NO_RIGHTS, null);
		}
	}

	@Override
	public Response<LoggedInUser> getUser(Integer id) {
		return client.getUser(id);
	}
	
	public final String hash(final String s) {
		return client.hash(s);
	}

	
}
