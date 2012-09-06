package edu.kit.tm.telematics.pocketbib.model.impl.com.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.OtherItem;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.managment.BookInformationProvider;
import edu.kit.tm.telematics.pocketbib.model.library.managment.ItemCoverProvider;

/**
 * A client class for Amazon.com.
 */
public class AmazonClient implements BookInformationProvider, ItemCoverProvider {

	/** Amazon individual access key for the Amazon Product Advertising Api */
	private static final String AWS_ACCESS_KEY_ID = "AKIAJLMAEFKMXINFH6EQ";

	/** the authorization key */
	private static final String AWS_SECRET_KEY = "7vMF/ewoJxH2bt+6K3rscddOxv8aqNIk/muqKCIG";

	/** locale Amazon host */
	private static final String ENDPOINT = "webservices.amazon.de";

	/** the instance */
	private static AmazonClient instance = null;

	/** the HTTP client */
	private HttpClient client;

	/** Handles the signed requests for Amazon  */
	private SignedRequestsHelper awsHelper;

	/** User Agent */
	private final static String USER_AGENT = "Mozilla/5.0 (Linux; Android) AppleWebKit (KHTML, like Gecko) PocketBib";


	/**
	 * Constructor of AmazonClient.
	 * Creates a HTTP client to communicate with Amazon
	 */
	@TargetApi(8)
	private AmazonClient() {
		/**if (Constants.API_LEVEL >= 8) {
			client = AndroidHttpClient
					.newInstance(USER_AGENT);
		} else {
			client = new DefaultHttpClient();
		}**/
		
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        
        client = new DefaultHttpClient(ccm, params);

		try {
			awsHelper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		} catch (Exception e) {
			Log.e("AmazonClient", "Cannot instantiate AWS Helper. " + e.getMessage());
		}
	}
	
	/**
	 * Loads an image from the given URL
	 * @param url URL of the image to load
	 * @return the loaded image
	 */
	private static Drawable loadImage(String url) {
	    try {
	        InputStream i = (InputStream) new URL(url).getContent();
	        Drawable image = Drawable.createFromStream(i, "src name");
	        return image;
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	/**
	 * Gets an DOM document with item and cover information about the given item or isbn from Amazon 
	 * One parameter can be null.
	 * @param item search this item 
	 * @param isbn lookup this isbn
	 * @return DOM document
	 */
	private Document getXmlFromAmazon(Item item, Isbn isbn) {
		if (isbn == null && item == null) {
			Log.e("AmazonClient Isbn And Item", "is null");
			return null;
		}
		if (awsHelper == null) {
			Log.w("AmazonClient", "Cannot fetch Book information - no aws helper is present!");
			return null;
		}
		String isbn13 = null;
		if (isbn != null) {
			isbn13 = isbn.getIsbn13();
		} else if (item instanceof Book && ((Book)item).getIsbn() != null) {
			isbn13 = ((Book)item).getIsbn().getIsbn13();
		} else if (item instanceof OtherItem && ((OtherItem)item).getIsbn() != null) {
			isbn13 = ((OtherItem)item).getIsbn().getIsbn13();
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", "AWSECommerceService");
		params.put("Version", "2011-08-01");
		if (isbn13 != null) {
			params.put("Operation", "ItemLookup");
			params.put("ItemId", isbn13);
			params.put("IdType", "EAN");
		} else {
			params.put("Operation", "ItemSearch");
			params.put("Keywords", item.getTitle());
		}
		params.put("ResponseGroup", "Medium, Images");
		params.put("SearchIndex", "Books");
		params.put("AssociateTag", "pockeforandro-20");
		HttpGet request = new HttpGet(awsHelper.sign(params));
		Log.i("Amazon Request", awsHelper.sign(params));
		try {
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream content = response.getEntity().getContent();
				Document doc = null;
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(content);
				return doc;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			request.abort();
		}
		return null;
	}
	
	/**
	 * Returns the instance of the Amazon client.
	 * 
	 * @return the Amazon client
	 */
	public static AmazonClient getInstance() {
		if (instance == null) {
			instance = new AmazonClient();
		}
		return instance;
	}	

	/*
	 * (non-Javadoc)
	 * @see edu.kit.tm.telematics.pocketbib.model.library.managment.BookInformationProvider#getBook(edu.kit.tm.telematics.pocketbib.model.library.Isbn)
	 */
	public Book getBook(Isbn isbn) {
		Document doc = getXmlFromAmazon(null, isbn);
		if (doc != null) {
			try {
				String title = doc.getElementsByTagName("Title").item(0).getFirstChild().getNodeValue();
				String edition = doc.getElementsByTagName("Edition").item(0).getFirstChild().getNodeValue();
				String author = doc.getElementsByTagName("Author").item(0).getFirstChild().getNodeValue();
				Integer pageCount = Integer.valueOf(doc.getElementsByTagName("NumberOfPages").item(0).getFirstChild().getNodeValue());
				String publisher = doc.getElementsByTagName("Publisher").item(0).getFirstChild().getNodeValue();
				String detailUrl = doc.getElementsByTagName("DetailPageURL").item(0).getFirstChild().getNodeValue();
				Integer pubYear = Integer.valueOf(doc.getElementsByTagName("PublicationDate").item(0).getFirstChild().getNodeValue().split("-")[0]);
				Integer price = Integer.valueOf(doc.getElementsByTagName("LowestNewPrice").item(0).getFirstChild().getFirstChild().getNodeValue());
				Book book = Book.createNew();
				book.setTitle(title)
						.setEdition(edition)
						.setPageCount(Integer.valueOf(pageCount))
						.setPublisher(publisher)
						.setPrice(price)
						.setDetailUrl(detailUrl);
				book.setIsbn(isbn).setAuthor(author).setPublicationYear(pubYear);
				return book;
			} catch (NullPointerException e) {
				//e.printStackTrace();  //NullPointerException can occure if there is no information about this isbn and Amazon retrieves an error document 
			}
		}		
		return null;
	}

	/**
	 * Gets a cover from Amazon
	 * @param item load a cover for this item
	 * @return the loaded cover
	 */
	@Override
	public Drawable getCover(Item item) {
		Document doc = getXmlFromAmazon(item, null);
		if (doc != null) {
			try {
				String url = doc.getElementsByTagName("LargeImage").item(0).getFirstChild().getFirstChild().getNodeValue();
				Log.i("large image url", url);
				return loadImage(url);
			} catch (NullPointerException e) {
				//e.printStackTrace();  //NullPointerException can occure if there is no information about this isbn and Amazon retrieves an error document 
			}
		}
		return null;
	}

	/**
	 * Gets a thumbnail of the cover from Amazon
	 * @param item load a cover for this item
	 * @return the loaded cover
	 */
	@Override
	public Drawable getThumbnail(Item item) {
		Document doc = getXmlFromAmazon(item, null);
		if (doc != null) {
			try {
				String url = doc.getElementsByTagName("MediumImage").item(0).getFirstChild().getFirstChild().getNodeValue();
				Log.i("medium image url", url);
				return loadImage(url);
			} catch (NullPointerException e) {
				//e.printStackTrace();  //NullPointerException can occure if there is no information about this isbn and Amazon retrieves an error document 
			}
		}
		return null;
	}
}
