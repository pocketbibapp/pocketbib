package edu.kit.tm.telematics.pocketbib.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;

/**
 * A utility class for dialogs. All functions may be executed from the main ui
 * thread or background threads.
 */
public class DialogUtil {

	private static String TAG = "DialogUtil";

	/**
	 * private constructor - can not be instantiated
	 */
	private DialogUtil() {
	}

	public static void showToast(final Activity context, final int stringRes) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, stringRes, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * Displays an error dialog. This function can be called from background
	 * threads.
	 * 
	 * @param context
	 *            activity that is the context of the alertdialog
	 * @param errorTextRes
	 *            errordescription
	 * @param args
	 *            errordescription
	 */
	public static void showErrorDialog(Activity activity, int errorTextRes, String... args) {
		final String error = PocketBibApp.getAppContext().getResources().getString(errorTextRes, ((Object[]) args));
		Log.e(TAG, "showErrorDialog() : " + error);

		activity.runOnUiThread(getErrorDialogRunnable(activity, errorTextRes, args));
	}

	public static Runnable getErrorDialogRunnable(final Activity activity, final int errorTextRes, final String... args) {
		final String error = PocketBibApp.getAppContext().getResources().getString(errorTextRes, ((Object[]) args));

		return new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder errorMessageBuilder = new AlertDialog.Builder(activity);
				errorMessageBuilder.setTitle(R.string.error_dialog_heading);
				errorMessageBuilder.setMessage(error);
				errorMessageBuilder.setNeutralButton(android.R.string.ok, null);
				errorMessageBuilder.show();
			}
		};
	}

	/**
	 * Executes a Runnable and shows an indeterminate progress dialog while the
	 * Runnable is executed. This function can be called from background
	 * threads.
	 * 
	 * @param context
	 *            the Context
	 * @param titleStringRes
	 *            the title
	 * @param message
	 *            the message
	 * @param runnable
	 *            the Runnable to be executed
	 */
	public static void showProgressDialog(final Context context, final int titleStringRes, final CharSequence message,
			final Runnable runnable) {
		if (runnable == null) {
			return;
		}

		new AsyncTask<Void, Void, Void>() {
			private ProgressDialog dialog;

			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(context);
				dialog.setIndeterminate(true);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(false);

				if (titleStringRes != -1) {
					dialog.setTitle(titleStringRes);
				}

				if (message != null) {
					dialog.setMessage(message);
				}
				dialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				runnable.run();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (dialog.isShowing())
					dialog.dismiss();
			}

			@Override
			protected void onCancelled() {
				if (dialog.isShowing())
					dialog.dismiss();
			}
		}.execute();
	}
}
