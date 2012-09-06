package edu.kit.tm.telematics.pocketbib.model.user;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.library.Item;

/**
 * Utility class for user related functions.
 */
public class UserUtil {

	/**
	 * A email type, containing a predefined subject and message.
	 */
	public static enum EmailType {
		// @formatter:off
		ITEM_RETURN_REQUEST(R.string.email_subject_item_return_request, R.string.email_message_item_return_request),
		EMPTY_REQUEST(R.string.empty, R.string.empty);
		// @formatter:on

		// The string resources may define several placeholders:
		// %1$s - first name of the receiver
		// %2$s - last name of the receiver
		// %3$s - first name of the sender
		// %4$s - last name of the sender
		// %5$s - title of the item

		/** the subject String resource */
		private final int subjectStringRes;

		/** the message String resource */
		private final int messageStringRes;

		/**
		 * Creates a new email type.
		 * 
		 * @param subjectStringRes
		 *            the subject String resource
		 * @param messageStringRes
		 *            the message String resource
		 */
		private EmailType(int subjectStringRes, int messageStringRes) {
			this.subjectStringRes = subjectStringRes;
			this.messageStringRes = messageStringRes;
		}
	}

	/**
	 * Private constructor -- this is a utility class!
	 */
	private UserUtil() {
	}

	/**
	 * Calls the user, if any telephone number is provided in the LoggedInUser
	 * object. This method fails silently if no or an invalid telephone number
	 * is provided.
	 * 
	 * Use {@link hasTelephoneNumber()} for checking if the user has a telephone
	 * number.
	 * 
	 * @param context
	 *            the context
	 * @param user
	 *            the user to be called
	 */
	public static void callUser(Context context, LoggedInUser user) {
		assert context != null && user != null;

		String telephoneNumber = user.getTelephoneNumber();

		if (telephoneNumber != null) {
			Intent callIntent = new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + telephoneNumber));
			context.startActivity(callIntent);
		}
	}

	/**
	 * Launches the email app to send an email to the given user.
	 * 
	 * @param context
	 *            the context
	 * @param user
	 *            the email receiver
	 * @param type
	 *            the email type
	 * @param item
	 *            the Item (only used with EmailType.ITEM_RETURN_REQUEST), may
	 *            be null with other requests
	 */
	public static void emailUser(Context context, LoggedInUser user, EmailType type, Item item) {
		Assert.assertTrue(context != null);
		Assert.assertTrue(user != null);
		Assert.assertTrue(type != null);

		if (type == EmailType.ITEM_RETURN_REQUEST)
			Assert.assertTrue(item != null);

		// The receiving e-mail address
		String[] recipients = new String[] { user.getEmail(), "", };

		// The name of the current user (the e-mail sender)
		String currentUserFirstName = "", currentUserLastName = "";
		if (User.getCurrentUser() instanceof LoggedInUser) {
			currentUserFirstName = ((LoggedInUser) User.getCurrentUser()).getFirstName();
			currentUserLastName = ((LoggedInUser) User.getCurrentUser()).getLastName();
		}

		String itemTitle = (item == null) ? "" : item.getTitle();

		// The string resources may define several placeholders:
		// %1$s - first name of the receiver
		// %2$s - last name of the receiver
		// %3$s - first name of the sender
		// %4$s - last name of the sender
		// %5$s - title of the item

		// the subject
		String subject = PocketBibApp.getAppContext().getString(type.subjectStringRes, user.getFirstName(),
				user.getLastName(), currentUserFirstName, currentUserLastName, itemTitle);

		// the e-mail message
		String message = PocketBibApp.getAppContext().getString(type.messageStringRes, user.getFirstName(),
				user.getLastName(), currentUserFirstName, currentUserLastName, itemTitle);

		// create the Intent for sending the email
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
		//emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		//emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		//emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
		//emailIntent.setType("text/plain");
		
		String uri = "mailto:" + user.getEmail();
		try {
			subject = URLEncoder.encode(subject, "UTF-8");
			message = URLEncoder.encode(message, "UTF-8");
			uri = "mailto:" + user.getEmail() + "?subject=" + subject + "&body=" + message;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		emailIntent.setData(Uri.parse(uri));
		
		// Send the Intent, open the email app!
		context.startActivity(Intent.createChooser(emailIntent, "E-Mail"));
	}
}
