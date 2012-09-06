package edu.kit.tm.telematics.pocketbib.model.impl.edu.kit;

import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.AUTHOR;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.AVAILABLE_COPIES;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.DESCRIPTION;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.EDITION;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.ISBN;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.ISSN;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.ITEM_ID;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.PAGE_COUNT;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.POSITION;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.PUBLICATION_DATE;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.PUBLICATION_YEAR;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.PUBLISHER;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.RATING_COUNT;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.SUM_RATING;
import static edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants.TITLE;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.EMAIL;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.FIRST_NAME;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.IS_ACTIVE;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.IS_ADMINISTRATOR;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.LAST_NAME;
import static edu.kit.tm.telematics.pocketbib.model.Constants.UserConstants.USER_ID;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemCopyConstants;
import edu.kit.tm.telematics.pocketbib.model.Constants.RatingConstants;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortField;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortOrder;
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
import edu.kit.tm.telematics.pocketbib.model.library.OtherItem;
import edu.kit.tm.telematics.pocketbib.model.library.Rating;
import edu.kit.tm.telematics.pocketbib.model.library.RatingAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.InformationKey;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.UserAdapter;

/**
 * Handles the complete communication with the server
 */
public class RestClient implements FullClient {

	/** context tag of the RestClient */
	private static final String TAG = "RestClient";

	/** instance of RestClient (Singleton) */
	private static RestClient instance;

	/** Authentication key for communication with the server */
	private String authKey = PocketBibApp.getSharedPreferences().getString(Constants.KEY_AUTH_KEY, null);

	private static HttpClient client;

	private static final Response<Object> ERROR_RESPONSE = new Response<Object>(ResponseCode.ERROR_SERVER_PROBLEM, null);

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Returns the single instance of the RestClient
	 * 
	 * @return instance of the RestClient
	 */
	@TargetApi(8)
	public static RestClient getInstance() {
		if (instance == null) {
			instance = new RestClient();
			instance.initializeConnection();
		}

		return instance;
	}

	public void initializeConnection() {
		if (PocketBibApp.getSharedPreferences().getBoolean(Constants.KEY_LIBRARY_SERVER_SSL, false)) {

			try {
				HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

				X509TrustManager tm = new X509TrustManager() {

					public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};

				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[] { tm }, null);

				DefaultHttpClient raw_client = new DefaultHttpClient();

				SchemeRegistry registry = new SchemeRegistry();
				MySSLSocketFactory socketFactory = new MySSLSocketFactory(ctx);
				socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
				registry.register(new Scheme("https", socketFactory, Integer.valueOf(PocketBibApp
						.getSharedPreferences().getString(Constants.KEY_LIBRARY_SERVER_PORT, "8080"))));
				ClientConnectionManager mgr = new ThreadSafeClientConnManager(raw_client.getParams(), registry);
				client = new DefaultHttpClient(mgr, raw_client.getParams());

				// Set verifier
				HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
		} else {
			if (Constants.API_LEVEL >= 8) {
				if (client instanceof AndroidHttpClient) {
					((AndroidHttpClient) client).close();
				}

				client = AndroidHttpClient
						.newInstance("Mozilla/5.0 (Linux; Android) AppleWebKit (KHTML, like Gecko) PocketBib");
			} else {
				client = new DefaultHttpClient();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static <T> Response<T> getErrorResponse() {
		return (Response<T>) ERROR_RESPONSE;
	}

	/**
	 * Handles the communication with the server and sends the request to the
	 * server and returns the answer of the server
	 * 
	 * @param request
	 *            HTTPRequst object with information like URL, kind of reqeust,
	 *            etc.
	 * @return answer string from the server. Should be JSON
	 */
	private static Response<String> makeRequest(HttpUriRequest request) {
		assertNotNull(request);
		Log.v(TAG, "Requesting " + request.getURI().toString() + " (DOMAIN=" + getDomain() + ")");

		StringBuilder stringBuilder = new StringBuilder();
		int statusCode = ResponseCode.ERROR_SERVER_NOT_AVAILABLE.getErrorCode();
		try {
			HttpResponse response = client.execute(request);

			statusCode = response.getStatusLine().getStatusCode();

			Log.v(TAG, "StatusCode: " + String.valueOf(statusCode));

			if (statusCode == 200) {
				InputStream content = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				content.close();
				return new Response<String>(ResponseCode.OK, stringBuilder.toString());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			request.abort();
		}

		return new Response<String>(ResponseCode.getFromErrorCode(statusCode), null);
	}

	private static String getDomain() {
		boolean useHttps = PocketBibApp.getSharedPreferences().getBoolean(Constants.KEY_LIBRARY_SERVER_SSL, true);
		String host = PocketBibApp.getSharedPreferences().getString(Constants.KEY_LIBRARY_SERVER, "fpieper.de");
		String port = PocketBibApp.getSharedPreferences().getString(Constants.KEY_LIBRARY_SERVER_PORT, "8080");

		if (useHttps)
			return "https://" + host + "/";
		else
			return "http://" + host + ":" + port + "/";

	}

	/**
	 * Get JSON from the server as JSON Uses HTTP GET
	 * 
	 * @param url
	 *            URL of the server resource
	 * @return gotten JSON from the server
	 */
	private static Response<String> getJson(String url) {
		assertNotNull(url);
		assertFalse(TextUtils.isEmpty(url));

		HttpGet httpGet = new HttpGet(getDomain() + url);
		return makeRequest(httpGet);
	}

	/**
	 * Put JSON to the server and return the server response as JSON Uses HTTP
	 * PUT
	 * 
	 * @param url
	 *            URL of the server resource
	 * @param json
	 *            JSON to send
	 * @return id of the created object in the database
	 */
	private static Response<Integer> putJson(String url, JSONObject json) {
		assertFalse(TextUtils.isEmpty(url));

		StringEntity entity = null;

		HttpPut put = new HttpPut(getDomain() + url);
		put.setHeader("Accept", "application/json");
		put.setHeader("Content-type", "application/json");

		try {
			if (json != null) {
				entity = new StringEntity(json.toString(), "UTF-8");
				put.setEntity(entity);
			} else {
				Log.w(TAG, "putJson() called with: [json=null, url=" + url + "]");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return getErrorResponse();
		}

		Response<String> responseString = makeRequest(put);
		if (responseString.getResponseCode() == ResponseCode.OK) {
			try {
				JSONObject responseJson = new JSONObject(responseString.getData());
				if (responseJson.has("id") && !responseJson.getString("id").equals("null")) {
					return new Response<Integer>(responseString.getResponseCode(), responseJson.getInt("id"));
				} else {
					Log.e(TAG, "Got a OK-Response, but no ID!");
					return new Response<Integer>(ResponseCode.ERROR_SERVER_PROBLEM, null);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return getErrorResponse();
			}
		}

		return new Response<Integer>(responseString.getResponseCode(), null);
	}

	/**
	 * Send JSON to the server and return the server response as JSON
	 * 
	 * @param request
	 *            HTTPRequst object with information like URL, kind of reqeust,
	 *            etc.
	 * @param json
	 *            JSON to send
	 * @return gotten JSON from the server
	 */
	private static Response<String> getSendJson(HttpUriRequest request, JSONObject json) {
		assertNotNull(request);

		StringEntity entity = null;
		try {
			if (json != null) {
				entity = new StringEntity(json.toString(), "UTF-8");
				((HttpEntityEnclosingRequestBase) request).setEntity(entity);
			} else {
				Log.w(TAG, "getSendJson() called with: [json=null, request=" + request + "]");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return getErrorResponse();
		}

		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");
		return makeRequest(request);
	}

	/**
	 * Specific SHA-1 hash implementation from:
	 * http://www.lanwirt.de/2011/07/12/sha-1-hashes-in-android-erstellen/
	 * 
	 * @param s
	 *            string to hash
	 * @return hashed string
	 */
	protected static final String sha1(final String s) {
		assertNotNull(s);

		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return null;
	}

	/**
	 * Wrapper for the specific hash function
	 * 
	 * @param s
	 *            string to hash
	 * @return hashed string
	 */
	public final String hash(String s) {
		return sha1(s);
	}

	/**
	 * Converts the given raw string to an Date object
	 * 
	 * @param rawDate
	 *            String with the formatted date
	 * @return Date object
	 */
	protected static final Date parseDate(String rawDate) {
		Date date = null;
		try {
			if (rawDate == null || rawDate.equals("null")) {
				Log.v(TAG, "parseDate() called with date = null");
			} else {
				date = DATE_FORMAT.parse(rawDate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return date;
	}

	/**
	 * Accesses an optional JSON field and returns null if the field is not set
	 * or equals "null".
	 * 
	 * @param json
	 *            the JSON object
	 * @param key
	 *            the key
	 * @return the value or null if not set
	 */
	protected final static String getOptJson(JSONObject json, String key) {
		String value = "null";
		try {
			if (json.has(key)) {
				value = json.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		if (value.equals("null")) {
			return null;
		} else {
			return value;
		}
	}

	/**
	 * Converts a JSON object with user data to an user object
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return user
	 */
	protected LoggedInUser jsonToUser(JSONObject json) {
		LoggedInUser user = null;

		if (json != null) {
			try {
				user = LoggedInUser.instantiateExisting(json.getInt(USER_ID)).setEmail(getOptJson(json, EMAIL))
						.setFirstName(getOptJson(json, FIRST_NAME)).setLastName(getOptJson(json, LAST_NAME))
						.setActive(json.getBoolean(IS_ACTIVE)).setAdministrator(json.getBoolean(IS_ADMINISTRATOR));

				for (InformationKey key : PocketBibApp.getInformationKeys()) {
					user.setInformation(key, getOptJson(json, key.getName()));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return user;
	}

	/**
	 * Converts a JSON object with item copy data to an item copy object
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return item copy
	 */
	private ItemCopy jsonToItemCopy(JSONObject json) {
		assertNotNull(json);

		try {
			ItemCopy copy = ItemCopy.instantiateExisting(json.getInt(ItemCopyConstants.ITEM_COPY_ID),
					getItem(json.getInt(ItemCopyConstants.ITEM_ID)).getData(),
					json.getBoolean(ItemCopyConstants.IS_ACTIVE),
					parseDate(json.getString(ItemCopyConstants.CREATION_TIME)));

			if (!json.getString("lend").equals("null")) {

				copy.setUser(getUser(json.getJSONObject("lend").getInt("user_id")).getData());
			}

			return copy;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts a JSON object with rating data to an rating object
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return rating
	 */
	private <T extends Item> Rating<T> jsonToRating(JSONObject json, T item) {
		assertNotNull(json);
		assertNotNull(item);

		LoggedInUser user;
		try {
			Response<LoggedInUser> userResponse = getUser(json.getInt(RatingConstants.USER_ID));
			
			if(userResponse.getResponseCode() != ResponseCode.OK || userResponse.getData() == null)
				return null;
			
			user = userResponse.getData();
			
			Date date = parseDate(json.getString(RatingConstants.CREATION_TIME));
			Rating<T> rating = Rating
					.instantiateExisting(json.getInt(RatingConstants.RATING__ID), user, item, date)
					.setRating(json.getInt(RatingConstants.RATING))
					.setComment(
							json.getString(RatingConstants.COMMENT).equals("null") ? null : json
									.getString(RatingConstants.COMMENT));
			return rating;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts a JSON object with item data to an item object
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return item
	 */
	private Item jsonToCorrectItem(JSONObject json) {
		assertNotNull(json);

		try {
			Item item;
			String type = json.getString(Constants.ItemConstants.TYPE);

			if (type.equals("Book")) {
				item = Book.instantiateExisting(json.getInt(ITEM_ID));
			} else if (type.equals("Magazine")) {
				item = Magazine.instantiateExisting(json.getInt(ITEM_ID));
			} else {
				item = OtherItem.instantiateExisting(json.getInt(ITEM_ID));
			}

			item.setTitle(getOptJson(json, TITLE));
			item.setDescription(getOptJson(json, DESCRIPTION));
			item.setSumRating(json.getDouble(SUM_RATING));
			item.setRatingCount(json.getInt(RATING_COUNT));
			item.setAvailableCopies(json.getInt(AVAILABLE_COPIES));
			item.setPosition(getOptJson(json, POSITION));
			item.setEdition(getOptJson(json, EDITION));
			item.setPageCount(json.getInt(PAGE_COUNT));
			item.setPublisher(getOptJson(json, PUBLISHER));
			item.setActive(json.getBoolean(IS_ACTIVE));

			if (item instanceof Book) {
				((Book) item)
						.setIsbn(Isbn.initIsbn(json.getString(ISBN)))
						.setAuthor(getOptJson(json, AUTHOR))
						.setPublicationYear(
								(getOptJson(json, PUBLICATION_YEAR) == null) ? null : json.getInt(PUBLICATION_YEAR));
			} else if (item instanceof Magazine) {
				((Magazine) item).setIssn(Issn.initIssn(json.getString(ISSN))).setPublicationDate(
						parseDate(json.getString(PUBLICATION_DATE)));
			} else {
				((OtherItem) item)
						.setIsbn(Isbn.initIsbn(json.getString(ISBN)))
						.setAuthor(getOptJson(json, AUTHOR))
						.setIssn(Issn.initIssn(json.getString(ISSN)))
						.setPublicationDate(parseDate(json.getString(PUBLICATION_DATE)))
						.setPublicationYear(
								(getOptJson(json, PUBLICATION_YEAR) == null) ? null : json.getInt(PUBLICATION_YEAR));
				if (!type.equals("OtherItem")) {
					((OtherItem) item).setType(type);
				}
			}
			return item;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts a JSON list of items to a list of item objects
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return list of items
	 */
	private Response<ItemAdapter> jsonToItemList(Response<String> response) {
		assertNotNull(response);

		JSONArray json;
		List<Item> list = new ArrayList<Item>();
		if (response.getResponseCode() == ResponseCode.OK) {
			try {
				json = new JSONArray(response.getData());
				for (int i = 0; i < json.length(); i++) {
					JSONObject item = json.getJSONObject(i);
					Item itemObject = jsonToCorrectItem(item);

					if (itemObject != null) {
						list.add(itemObject);
					}
				}

				return new Response<ItemAdapter>(response.getResponseCode(), new ItemAdapter(
						PocketBibApp.getAppContext(), list));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return new Response<ItemAdapter>(response.getResponseCode(), null);

	}

	/**
	 * Converts a JSON list of users to a list of user objects
	 * 
	 * @param response
	 *            JSON response from the server
	 * @return list of users
	 */
	private Response<UserAdapter> jsonToUserList(Response<String> response) {
		assertNotNull(response);

		JSONArray json;
		List<LoggedInUser> list = new ArrayList<LoggedInUser>();
		if (response.getResponseCode() == ResponseCode.OK) {
			try {
				json = new JSONArray(response.getData());
				for (int i = 0; i < json.length(); i++) {
					JSONObject user = json.getJSONObject(i);
					LoggedInUser userObject = jsonToUser(user);
					if (userObject != null) {
						list.add(userObject);
					}
				}

				return new Response<UserAdapter>(response.getResponseCode(), new UserAdapter(
						PocketBibApp.getAppContext(), list));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return new Response<UserAdapter>(response.getResponseCode(), null);

	}

	/**
	 * Borrow a copy of the given item for one user
	 * 
	 * @param the
	 *            item to borrow
	 * @return code of response (200 if everything is okay)
	 */
	public ResponseCode borrowItem(Item item) {
		assertNotNull(item);
		assertTrue(User.getCurrentUser() instanceof LoggedInUser);

		Response<String> response = getJson(authKey + "/item/" + item.getItemId() + "/borrow");
		return response.getResponseCode();
	}

	/**
	 * Load the book with the given isbn
	 * 
	 * @param isbn
	 *            isbn to lookup
	 * @return the loaded book
	 */
	public Book getBook(Isbn isbn) {
		assertNotNull(isbn);

		Response<String> response = getJson("book/" + isbn.getIsbn13());

		if (response.getResponseCode() == ResponseCode.OK) {
			try {
				JSONObject json = new JSONObject(response.getData());
				return (Book) jsonToCorrectItem(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Load the borrowed items of one user
	 * 
	 * @param user
	 *            user to lookup
	 * @return the borrowed items
	 */
	public Response<ItemAdapter> getBorrowedItems(LoggedInUser user) {
		assertNotNull(user);

		Response<String> response = getJson(authKey + "/user/" + user.getUserId() + "/currentBorrowed");

		if (response.getResponseCode() == ResponseCode.OK) {
			List<Item> list = new ArrayList<Item>();

			try {
				JSONArray json = new JSONArray(response.getData());

				for (int i = 0; i < json.length(); i++) {
					JSONObject item = json.getJSONObject(i);
					list.add(jsonToCorrectItem(item));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return new Response<ItemAdapter>(response.getResponseCode(), null);
			}
			return new Response<ItemAdapter>(response.getResponseCode(), new ItemAdapter(PocketBibApp.getAppContext(),
					list));
		}

		return new Response<ItemAdapter>(response.getResponseCode(), null);

	}

	/**
	 * Load the item with the given id
	 * 
	 * @param itemId
	 *            item id of the item to load
	 * @return the loaded item
	 */
	public Response<Item> getItem(int itemId) {
		assertTrue(itemId >= 0);

		Response<String> response = getJson("item/" + String.valueOf(itemId));
		if (response.getResponseCode() == ResponseCode.OK && response.getData() != null) {
			try {
				JSONObject json = new JSONObject(response.getData());
				Item item = jsonToCorrectItem(json);
				return new Response<Item>(response.getResponseCode(), item);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return new Response<Item>(response.getResponseCode(), null);
	}

	/**
	 * Load the copies of one otem from the server
	 * 
	 * @param itemId
	 *            the item id of the copies to load
	 * @return the loaded copies of the item
	 */
	public Response<ItemCopyAdapter> getItemCopies(int itemId) {
		assertTrue(itemId >= 0);

		Response<String> response = getJson("item/" + itemId);
		List<ItemCopy> list = new ArrayList<ItemCopy>();
		if (response.getResponseCode() == ResponseCode.OK && response.getData() != null) {
			try {
				JSONArray json = new JSONArray(new JSONObject(response.getData()).getString("item_copies"));

				for (int i = 0; i < json.length(); i++) {
					JSONObject copy = json.getJSONObject(i);
					list.add(jsonToItemCopy(copy));
				}

				return new Response<ItemCopyAdapter>(response.getResponseCode(), new ItemCopyAdapter(
						PocketBibApp.getAppContext(), list));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return new Response<ItemCopyAdapter>(response.getResponseCode(), null);
	}

	/**
	 * Load the ratings for the given item from the server
	 * 
	 * @param itemId
	 *            the item id of the ratings to load
	 * @return the loaded ratings
	 */
	public Response<RatingAdapter> getItemRatings(int itemId) {
		assertTrue(itemId >= 0);

		Response<String> response = getJson("item/" + itemId);
		List<Rating<? extends Item>> list = new ArrayList<Rating<? extends Item>>();

		if (response.getResponseCode() == ResponseCode.OK) {
			try {
				JSONArray json = new JSONArray(new JSONObject(response.getData()).getString("ratings"));
				for (int i = 0; i < json.length(); i++) {
					JSONObject rating = json.getJSONObject(i);
					list.add(jsonToRating(rating, jsonToCorrectItem(new JSONObject(response.getData()))));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return new Response<RatingAdapter>(response.getResponseCode(), null);
			}

			RatingAdapter adapter = new RatingAdapter(PocketBibApp.getAppContext(), list);
			return new Response<RatingAdapter>(response.getResponseCode(), adapter);
		}

		return new Response<RatingAdapter>(response.getResponseCode(), null);
	}

	/**
	 * Load a magazine
	 * 
	 * @param issn
	 *            issn of the magazine
	 * @return the loaded magazine else null
	 */
	public Magazine getMagazine(Issn issn) {
		assertNotNull(issn);

		Response<String> response = getJson("magazine/" + issn.getIssn());

		if (response.getResponseCode() == ResponseCode.OK) {
			try {
				JSONObject json = new JSONObject(response.getData());
				return (Magazine) jsonToCorrectItem(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	/**
	 * Load the user with given id from the server
	 * 
	 * @param userId
	 *            id of the user to load, or null for the current user
	 * @return the loaded user with response code else response code with null
	 */
	public Response<LoggedInUser> getUser(Integer userId) {
		Response<String> response;

		if (userId == null) {
			response = getJson(authKey + "/user");
		} else {
			assertTrue(userId >= 0);
			response = getJson("user/" + userId);
		}

		if (response.getResponseCode() == ResponseCode.OK && response.getData() != null) {
			try {
				JSONObject json = new JSONObject(response.getData());
				return new Response<LoggedInUser>(response.getResponseCode(), jsonToUser(json));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new Response<LoggedInUser>(response.getResponseCode(), null);
	}

	/**
	 * Insert or update the given copy
	 * 
	 * @param copy
	 *            the copy of an item to save
	 * @return code of response (200 if everything is okay)
	 */
	public Response<Integer> insertOrUpdateCopy(ItemCopy copy) {
		assertNotNull(copy);

		JSONObject json = copy.toJson();
		return putJson(authKey + "/item/" + copy.getItem().getItemId(), json);
	}

	/**
	 * Insert or update the item
	 * 
	 * @param item
	 *            item to save
	 * @return code of response (200 if everything is okay)
	 */
	public Response<Integer> insertOrUpdateItem(Item item) {
		assertNotNull(item);

		JSONObject json = item.toJson();
		return putJson(authKey + "/item", json);
	}

	/**
	 * Insert or update the given rating
	 * 
	 * @param rating
	 *            the rating to save
	 * @return code of response (200 if everything is okay)
	 */
	public Response<Integer> insertOrUpdateRating(Rating<? extends Item> rating) {
		assertNotNull(rating);

		return putJson(authKey + "/item/" + rating.getItem().getItemId() + "/rate", rating.toJson());
	}

	/**
	 * Insert or update the given user
	 * 
	 * @param user
	 *            the user to save
	 * @return code of response (200 if everything is okay)
	 */
	public Response<Integer> insertOrUpdateUser(LoggedInUser user) {
		assertNotNull(user);

		JSONObject json = user.toJson();
		return putJson(authKey + "/user", json);
	}

	/**
	 * Load a saved user from the saved sharedpreferences
	 * 
	 * @return code of response (200 if everything is okay, else 404)
	 */
	public ResponseCode loadSavedUser() {
		ResponseCode code = ResponseCode.getFromErrorCode(404);

		if (authKey != null) {
			String savedUser = PocketBibApp.getSharedPreferences().getString(Constants.SAVED_USER, null);

			if (savedUser == null) {
				return code;
			}

			try {
				User.setCurrentUser(jsonToUser(new JSONObject(savedUser)));
				code = ResponseCode.OK;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return code;
	}

	/**
	 * Login an user
	 * 
	 * @param email
	 *            email of the user, used for identifying
	 * @param password
	 *            password of the user
	 * @param persistent
	 *            if true save the credentials
	 * @return code of response (200 if everything is okay)
	 */
	public ResponseCode login(String email, String password, boolean persistent) {
		assertNotNull(email);
		assertNotNull(password);

		// Creates POST request
		JSONObject json = new JSONObject();
		String passwordhash = sha1(password);

		try {
			json.put("email", email);
			json.put("passwordhash", passwordhash);
		} catch (JSONException e) {
			e.printStackTrace();
			return ResponseCode.ERROR_UNKNOWN;
		}

		HttpPost post = new HttpPost(getDomain() + "getAuthKey");
		Response<String> response = getSendJson(post, json);
		ResponseCode code = response.getResponseCode();

		if (code == ResponseCode.OK) {
			assertNotNull(response.getData());

			try {
				JSONObject responseJson = new JSONObject(response.getData());
				authKey = responseJson.getString("authKey");

				// With the new authKey requests the current user
				Response<LoggedInUser> userResponse = getUser(null);
				code = userResponse.getResponseCode();

				if (code == ResponseCode.OK) {
					LoggedInUser user = userResponse.getData();

					assertNotNull(user);
					User.setCurrentUser(user);

					if (persistent) {
						SharedPreferences.Editor prefsEditor = PocketBibApp.getSharedPreferences().edit();
						prefsEditor.putString(Constants.SAVED_USER, user.toJson().toString());
						prefsEditor.putString(Constants.KEY_AUTH_KEY, authKey);
						prefsEditor.commit();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return ResponseCode.ERROR_SERVER_PROBLEM;
			}
		}

		return code;
	}

	/**
	 * Logout the current user
	 */
	public void logout() {
		User.setCurrentUser(null);
		authKey = null;

		SharedPreferences.Editor prefsEditor = PocketBibApp.getSharedPreferences().edit();
		prefsEditor.remove(Constants.KEY_AUTH_KEY);
		prefsEditor.commit();
	}

	/**
	 * Removes a copy of an item
	 * 
	 * @param copy
	 *            the copy to remove
	 * @return code of response (200 if everything is okay, 409 if cannot remove
	 *         this item)
	 */
	public ResponseCode removeCopy(ItemCopy copy) {
		assertNotNull(copy);
		ResponseCode code;
		if (copy == null || copy.getItemCopyId() == Constants.ID_NOT_SAVED) {
			code = ResponseCode.getFromErrorCode(400);
		} else {
			code = makeRequest(new HttpDelete(getDomain() + authKey + "/itemCopy/" + copy.getItemCopyId()))
					.getResponseCode();
		}

		Log.d(TAG, String.valueOf(code.getErrorCode()));

		return code;
	}

	/**
	 * Remove an item
	 * 
	 * @param item
	 *            item to remove
	 * @return code of response (200 if everything is okay, 409 if cannot remove
	 *         this item)
	 */
	public ResponseCode removeItem(Item item) {
		assertNotNull(item);
		ResponseCode code;
		if (item == null || item.getItemId() == Constants.ID_NOT_SAVED) {
			code = ResponseCode.getFromErrorCode(400);
		} else {
			code = makeRequest(new HttpDelete(getDomain() + authKey + "/item/" + item.getItemId())).getResponseCode();
		}

		return code;
	}

	/**
	 * Remove a rating
	 * 
	 * @param rating
	 *            rating to remove
	 * @return code of response (200 if everything is okay, 409 if cannot remove
	 *         this item)
	 */
	public ResponseCode removeRating(Rating<? extends Item> rating) {
		assertNotNull(rating);
		ResponseCode code;
		if (rating == null || rating.getRatingId() == Constants.ID_NOT_SAVED) {
			code = ResponseCode.getFromErrorCode(404);
		} else {
			code = makeRequest(new HttpDelete(getDomain() + authKey + "/rating/" + rating.getRatingId()))
					.getResponseCode();
		}

		return code;
	}

	/**
	 * Removes an user
	 * 
	 * @param user
	 *            user to remove
	 * @return code of response (200 if everything is okay)
	 */
	public ResponseCode removeUser(LoggedInUser user) {
		assertNotNull(user);

		ResponseCode code = makeRequest(new HttpDelete(getDomain() + authKey + "/user/" + user.getUserId()))
				.getResponseCode();

		return code;
	}

	/**
	 * Returns an item
	 * 
	 * @param item
	 *            the item to return
	 * @return code of response (200 if everything is okay)
	 */
	public ResponseCode returnItem(Item item) {
		assertNotNull(item);

		Response<String> response = getJson(authKey + "/item/" + item.getItemId() + "/return");
		return response.getResponseCode();
	}

	/**
	 * Extended search for items
	 * 
	 * @param queryParameters
	 *            searchmap for the different fields
	 * @return list of items
	 */
	public Response<ItemAdapter> searchLibrary(Map<String, String> queryParameters) {
		assertNotNull(queryParameters);

		JSONObject jsonRequest = new JSONObject();
		for (String key : queryParameters.keySet()) {
			try {
				jsonRequest.putOpt(key, queryParameters.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
				return getErrorResponse();
			}
		}

		Response<String> response = getSendJson(new HttpPost(getDomain() + "item/search"), jsonRequest);
		return jsonToItemList(response);
	}

	/**
	 * Search an item on the server
	 * 
	 * @param query
	 *            search query for the fulltext search
	 * @return list of items
	 */
	public Response<ItemAdapter> searchLibrary(String query, boolean showDisabled) {
		JSONObject jsonRequest = new JSONObject();

		try {
			jsonRequest.put("query", query);
			jsonRequest.putOpt("showDisabled", showDisabled);
		} catch (JSONException e) {
			e.printStackTrace();
			return getErrorResponse();
		}

		Response<String> response = getSendJson(new HttpPost(getDomain() + "item/search"), jsonRequest);
		return jsonToItemList(response);
	}

	/**
	 * Extended user search
	 * 
	 * @param queryParameters
	 *            query parameters
	 * @return list of users
	 */
	public Response<UserAdapter> searchUsers(Map<String, String> queryParameters) {
		assertNotNull(queryParameters);

		if (queryParameters == null) {
			return jsonToUserList(new Response<String>(ResponseCode.getFromErrorCode(400), "[]"));
		}

		JSONObject jsonRequest = new JSONObject();

		for (String key : queryParameters.keySet()) {
			try {
				jsonRequest.putOpt(key, queryParameters.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
				return getErrorResponse();
			}
		}

		Response<String> response = getSendJson(new HttpPost(getDomain() + authKey + "/user/search"), jsonRequest);
		return jsonToUserList(response);
	}

	/**
	 * Search Users
	 * 
	 * @param query
	 *            the search query to lookup
	 * @return list of users
	 */
	public Response<UserAdapter> searchUsers(String query) {
		JSONObject jsonRequest = new JSONObject();

		if (query == null) {
			query = "";
		}

		try {
			jsonRequest.put("query", query);
		} catch (JSONException e) {
			e.printStackTrace();
			return getErrorResponse();
		}

		Response<String> response = getSendJson(new HttpPost(getDomain() + authKey + "/user/search"), jsonRequest);
		return jsonToUserList(response);
	}

	/**
	 * Search for an item
	 * 
	 * @param query
	 *            the search query
	 * @param sortField
	 *            fie (200 if everything is okay)ld to sort after
	 * @param sortOrder
	 *            order to sort
	 * @param offset
	 *            start of the search limit
	 * @param limit
	 *            end of the search limit
	 * @return list of items
	 */
	public Response<ItemAdapter> searchItems(String query, SortField sortField, SortOrder sortOrder, int offset,
			int limit, boolean showDisabled) {
		assertNotNull(sortField);
		assertNotNull(sortOrder);
		assertTrue(offset >= 0);
		assertTrue(limit >= 1);

		String field;

		switch (sortField) {
		case TITLE:
			field = Constants.ItemConstants.TITLE;
			break;
		case DATE:
			field = Constants.ItemConstants.PUBLICATION_YEAR;
			break;
		case RATING:
			field = Constants.ItemConstants.RATING;
			break;
		case RELEVANCE:
			if (query != null)
				field = "rank";
			else
				field = Constants.ItemConstants.TITLE;
			break;
		default:
			throw new AssertionError("Invalid sortField " + sortField);
		}

		JSONObject jsonRequest = new JSONObject();
		try {
			jsonRequest.put("query", query);
			jsonRequest.putOpt("orderField", field);
			jsonRequest.putOpt("orderDirection", sortOrder);
			jsonRequest.putOpt("offset", offset);
			jsonRequest.putOpt("limit", limit);
			jsonRequest.putOpt("showDisabled", showDisabled);
		} catch (JSONException e) {
			e.printStackTrace();
			return getErrorResponse();
		}

		Response<String> response = getSendJson(new HttpPost(getDomain() + "item/search"), jsonRequest);
		return jsonToItemList(response);
	}

	/**
	 * Returns a list of all users
	 * 
	 * @return
	 */
	public Response<UserAdapter> getUsers() {
		Response<String> response = getJson(authKey + "/users");
		return jsonToUserList(response);
	}
}
