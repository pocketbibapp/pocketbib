package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import static edu.kit.tm.telematics.pocketbib.model.Constants.Intent.KEY_ADD_EDIT_MODE;
import static edu.kit.tm.telematics.pocketbib.model.Constants.Intent.VALUE_ADD_MODE;
import static edu.kit.tm.telematics.pocketbib.model.Constants.Intent.VALUE_EDIT_MODE;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.WelcomeActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.InformationKey;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.UserAdapter;
import edu.kit.tm.telematics.pocketbib.view.DialogUtil;

public class UserAddEditActivity extends BaseActivity {

	/** layout containing idTextView */
	private ViewGroup idLayout;

	/** layout containing isAdminToggle */
	private ViewGroup adminToggleLayout;

	/** layout containing isActiveToggle */
	private ViewGroup activeToggleLayout;

	private ViewGroup lentItemsLayout;

	/** button which redirects to ManageBorrowedItemsActivity */
	private Button showBorrowedItemsButton;

	/** TextView, containing the number of borrowed items */
	private TextView lentItemsTextView;

	/** TextView, containing the ID of the user */
	private TextView idTextView;

	/** EditText for the e-mail address */
	private EditText emailEditText;

	/** EditText for the first name */
	private EditText firstNameEditText;

	/** EditText for the last name */
	private EditText lastNameEditText;

	/** EditText for the password */
	private EditText passwordEditText;

	/**
	 * Map containing EditTexts for additional information fields like location
	 * and telephone number
	 */
	private Map<InformationKey, EditText> extraInformationFields;

	/** toggle defining the administrator status */
	private CompoundButton isAdminToggle;

	/** toggle defining the activation of the user */
	private CompoundButton isActiveToggle;

	/** the user to be edited */
	private LoggedInUser user = null;

	/** layout containing extraInformationFields */
	private ViewGroup extraInformationFieldLayout;

	/**
	 * the new password. If a {@link #resetPassoword} is true and newPassword is
	 * {@code null} an automatically generated password will be sent to the
	 * users e-mail address. <br>
	 * If newPassword is set, the password will always be stored in the
	 * database, but an e-mail will only be sent if resetPassword is true.
	 */
	private String newPassword = null;

	/**
	 * if true, a e-mail with a new password will be sent to the user when
	 * saving
	 */
	private boolean resetPassword;

	/** the tag for logging purposes */
	private final static String TAG = "UserAddEditActivity";

	/**
	 * Edit modes
	 */
	private static enum Mode {
		ADD_USER, EDIT_USER, EDIT_MY_ACCOUNT
	};

	/** the current mode */
	private Mode mode;
	
	private String foundEmail = null;

	/**
	 * <code>true</code> if a value has been changed; <code>false</code>
	 * otherwise
	 */
	private boolean valueChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_user_add_edit);

		// fetch all views from the XML resource
		idLayout = (ViewGroup) findViewById(R.id.layout_id);
		idTextView = (TextView) idLayout.findViewById(R.id.label_id);
		emailEditText = (EditText) findViewById(R.id.input_email);
		firstNameEditText = (EditText) findViewById(R.id.input_first_name);
		lastNameEditText = (EditText) findViewById(R.id.input_last_name);
		passwordEditText = (EditText) findViewById(R.id.input_password);
		adminToggleLayout = (ViewGroup) findViewById(R.id.layout_admin);
		isAdminToggle = (CompoundButton) adminToggleLayout.findViewById(R.id.switch_admin);
		activeToggleLayout = (ViewGroup) findViewById(R.id.layout_active);
		isActiveToggle = (CompoundButton) activeToggleLayout.findViewById(R.id.switch_active);
		extraInformationFieldLayout = (ViewGroup) findViewById(R.id.layout_additional_fields);
		lentItemsLayout = (ViewGroup) findViewById(R.id.layout_lent_items);
		lentItemsTextView = (TextView) lentItemsLayout.findViewById(R.id.label_lent_items);
		showBorrowedItemsButton = (Button) lentItemsLayout.findViewById(R.id.button_show_lent_items);

		// add all extraInformationFields to the Activity
		extraInformationFields = new HashMap<InformationKey, EditText>(PocketBibApp.getInformationKeys().length);
		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			addExtraInformationForm(key);
		}

		String requestedMode = getIntent().getStringExtra(KEY_ADD_EDIT_MODE);

		// set the mode and further behaviour
		if (requestedMode != null && requestedMode.equals(VALUE_ADD_MODE)) {
			if (User.getCurrentUser().isAdministrator()) {
				// the current user is an admin and creates a new account
				initializeAddUserMode();
			} else {
				Log.e(TAG, "Adding a user is not allowed for the current user.");
				finish();
			}
		} else if (requestedMode != null && requestedMode.equals(VALUE_EDIT_MODE)) {
			if (User.getCurrentUser().isAdministrator()) {
				user = (LoggedInUser) getIntent().getParcelableExtra(Constants.Intent.KEY_USER_PARCEL);

				if (user == null) {
					Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}

				if (user.equals(User.getCurrentUser())) {
					// the current user is an admin and changes their own
					// profile
					initializeEditMyAccountMode();
				} else {
					// the current user is an admin and changes another account
					initializeEditUserMode();
				}
			} else {
				Log.e(TAG, "Editing a user is not allowed for the current user.");
				finish();
			}
		} else {
			if (User.getCurrentUser() instanceof LoggedInUser) {
				// the current user is no administrator, so the only accessable
				// mode is 'Edit My Account'

				user = (LoggedInUser) getIntent().getParcelableExtra(Constants.Intent.KEY_USER_PARCEL);

				Assert.assertNotNull(user);

				initializeEditMyAccountMode();
			} else {
				// the current user is a guest - finish the Activity silently
				Log.e(TAG, TAG + " may not be used without user account.");
				finish();
			}
		}

		emailEditText.addTextChangedListener(textWatcher);
		firstNameEditText.addTextChangedListener(textWatcher);
		lastNameEditText.addTextChangedListener(textWatcher);
		passwordEditText.addTextChangedListener(textWatcher);
		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			extraInformationFields.get(key).addTextChangedListener(textWatcher);
		}
	}

	/**
	 * Initializes the activity to add a new account
	 */
	private void initializeAddUserMode() {
		Assert.assertTrue(User.getCurrentUser().isAdministrator());
		Assert.assertNull(user);

		setDisplayShowUp(AdminActivity.class, true);

		mode = Mode.ADD_USER;
		resetPassword = true;

		setTitle(R.string.title_user_add);

		isActiveToggle.setChecked(true);

		lentItemsLayout.setVisibility(View.GONE);

		emailEditText.removeTextChangedListener(textWatcher);
		emailEditText.addTextChangedListener(emailTextWatcher);
	}

	/**
	 * Initializes the activity to edit the user's own account
	 */
	private void initializeEditMyAccountMode() {
		// Assert.assertTrue(User.getCurrentUser() instanceof LoggedInUser);
		// Assert.assertEquals(User.getCurrentUser(), user);

		setDisplayShowUp(WelcomeActivity.class, true);

		mode = Mode.EDIT_MY_ACCOUNT;
		resetPassword = false;

		setTitle(R.string.title_my_account);
		setUserFields();

		if (!User.getCurrentUser().isAdministrator()) {
			idLayout.setVisibility(View.GONE);
			emailEditText.setInputType(InputType.TYPE_NULL);
			emailEditText.setEnabled(false);
			firstNameEditText.setInputType(InputType.TYPE_NULL);
			firstNameEditText.setEnabled(false);
			lastNameEditText.setInputType(InputType.TYPE_NULL);
			lastNameEditText.setEnabled(false);
		}

		adminToggleLayout.setVisibility(View.GONE);
		activeToggleLayout.setVisibility(View.GONE);

		// dont display showBorrowedItemsButton when in EDIT_MY_ACCOUNT mode.
		// Users edit their borrowed items on WelcomeActivity.
		showBorrowedItemsButton.setVisibility(View.GONE);
		new LoadBorrowedItemsCountTask().execute();
	}

	/**
	 * Initializes the activity to edit an account. The account may not be the
	 * own account of the current user.
	 */
	private void initializeEditUserMode() {
		Assert.assertTrue(User.getCurrentUser().isAdministrator());
		Assert.assertNotNull(user);
		Assert.assertFalse(User.getCurrentUser().equals(user));

		setDisplayShowUp(AdminActivity.class, true);

		mode = Mode.EDIT_USER;
		resetPassword = false;

		setTitle(R.string.title_user_edit);
		setUserFields();
		new LoadBorrowedItemsCountTask().execute();
	}

	/**
	 * Fills all fields with user information.
	 */
	private void setUserFields() {
		Assert.assertNotNull(user);

		Log.i(TAG, "setUserFields(" + user + ")");

		emailEditText.setText(user.getEmail());
		firstNameEditText.setText(user.getFirstName());
		lastNameEditText.setText(user.getLastName());
		isAdminToggle.setChecked(user.isAdministrator());
		isActiveToggle.setChecked(user.isActive());
		idTextView.setText("" + user.getUserId());

		passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		passwordEditText.setText("********");

		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			extraInformationFields.get(key).setText(user.getInformation(key));
		}
	}

	/**
	 * Adds a form for an InformationKey to the Activity and adds the EditText
	 * to {@link #extraInformationFields}.
	 * 
	 * @param key
	 *            the InformationKey.
	 */
	private void addExtraInformationForm(InformationKey key) {
		Assert.assertNotNull(key);

		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup form = (ViewGroup) li.inflate(R.layout.include_form, null);

		((TextView) form.findViewById(R.id.title)).setText(key.getLabelStringRes());

		EditText editText = (EditText) form.findViewById(R.id.input);
		editText.setInputType(key.getInputType());
		editText.setImeOptions(key.getImeOptions());

		extraInformationFields.put(key, editText);
		extraInformationFieldLayout.addView(form);
	}

	private TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			valueChanged = true;

			emailEditText.removeTextChangedListener(textWatcher);
			firstNameEditText.removeTextChangedListener(textWatcher);
			lastNameEditText.removeTextChangedListener(textWatcher);
			passwordEditText.removeTextChangedListener(textWatcher);
			for (InformationKey key : PocketBibApp.getInformationKeys()) {
				extraInformationFields.get(key).removeTextChangedListener(textWatcher);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.user_add_edit, menu);

		menu.findItem(R.id.item_my_account).setVisible(false);

		if (mode == Mode.ADD_USER) {
			menu.findItem(R.id.item_reload).setVisible(false);
			menu.findItem(R.id.item_delete).setVisible(false);

			// the user is already in the admin panel, so hide the
			// "Administration" menu item
			menu.findItem(R.id.item_administration).setVisible(false);
		} else if (mode == Mode.EDIT_USER) {
			// the user is already in the admin panel, so hide the
			// "Administration" menu item
			menu.findItem(R.id.item_administration).setVisible(false);
		} else if (mode == Mode.EDIT_MY_ACCOUNT) {
			menu.findItem(R.id.item_delete).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_save:
			DialogUtil.showProgressDialog(this, R.string.dialog_title_saving, null, new Runnable() {
				@Override
				public void run() {
					save();
				}
			});
			return true;

		case R.id.item_delete:
			onRemoveClick(null);
			return true;

		case R.id.item_discard:
			finish();
			return true;

		case R.id.item_reload:
			DialogUtil.showProgressDialog(this, R.string.dialog_title_reloading, null, new Runnable() {
				@Override
				public void run() {
					Response<LoggedInUser> response = PocketBibApp.getRegistrationProvider().getUser(user.getUserId());

					if (response.getResponseCode() != ResponseCode.OK || response.getData() == null) {
						DialogUtil.showErrorDialog(UserAddEditActivity.this, response.getResponseCode()
								.getErrorStringRes());
					} else {
						user = response.getData();

						if (mode == Mode.EDIT_MY_ACCOUNT) {
							// clone the user and refresh the current user

							Parcel p = Parcel.obtain();
							p.writeValue(user);
							p.setDataPosition(0);
							User.setCurrentUser((LoggedInUser) p.readValue(LoggedInUser.class.getClassLoader()));
							p.recycle();
						}

						idLayout.post(new Runnable() {
							@Override
							public void run() {
								setUserFields();
							}
						});
					}
				}
			});
			return true;

		case android.R.id.home:
			Intent intent;

			if (mode == Mode.ADD_USER || mode == Mode.EDIT_USER) {
				Assert.assertTrue(User.getCurrentUser().isAdministrator());
				intent = new Intent(this, AdminActivity.class);
				intent.putExtra(Constants.Intent.KEY_ADMIN_ACTIVE_TAB, Constants.Intent.VALUE_ADMIN_ACTIVE_TAB_USERS);
			} else {
				intent = new Intent(this, WelcomeActivity.class);
			}

			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (valueChanged) {
				confirmActivityLeaving(intent, true);
			} else {
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Removes the user account after affirmation.
	 * 
	 * @param v
	 *            the source view (may be null)
	 */
	public void onRemoveClick(View v) {
		Assert.assertNotNull(user);
		Assert.assertTrue(User.getCurrentUser().isAdministrator());

		if (!user.isStoredInDatabase()) {
			finish();
			return;
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_delete_user);

		// display the real user name if given
		if (user.getFirstName().length() > 0 && user.getLastName().length() > 0) {
			dialog.setMessage(getString(R.string.dialog_message_delete_user, user.getFirstName(), user.getLastName()));
		} else {
			dialog.setMessage(getString(R.string.dialog_message_delete_user_fallback));
		}

		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.setPositiveButton(R.string.dialog_action_delete_user, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DialogUtil.showProgressDialog(UserAddEditActivity.this, R.string.dialog_title_deletion, null,
						new Runnable() {
							@Override
							public void run() {
								PocketBibApp.getRegistrationProvider().removeUser(user);
								finish();
							}
						});
			}
		});

		dialog.show();
	}

	/**
	 * Shows a dialog to let the user change the password.
	 * 
	 * @param v
	 *            the source view (may be null)
	 */
	public void onChangePasswordClick(View v) {
		Assert.assertNotNull(mode);

		CharSequence[] items = { getString(R.string.button_set_password_manually), null };

		if (mode == Mode.ADD_USER) {
			items[1] = getString(R.string.button_create_password_automatically);
		} else if (mode == Mode.EDIT_USER) {
			items[1] = getString(R.string.button_reset_password_via_email);
		} else if (mode == Mode.EDIT_MY_ACCOUNT) {
			changePasswordManually();
			return;
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_change_password);
		// dialog.setMessage(R.string.dialog_message_change_password);
		dialog.setItems(items, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					changePasswordManually();
				} else {
					if (mode == Mode.ADD_USER) {
						newPassword = null;
						resetPassword = true;
						passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT);
						passwordEditText.setText(R.string.label_password_automatically_generated);
					} else if (mode == Mode.EDIT_USER) {
						newPassword = null;
						resetPassword = true;
						passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT);
						passwordEditText.setText(R.string.label_password_automatically_generated);
					} else {
						// else: mode == Mode.EDIT_MY_ACCOUNT
						// users may not reset their own password in 'My
						// Account'
						throw new AssertionError();
					}
				}
			}
		});
		dialog.show();
	}

	/**
	 * Shows a dialog to let the user set the password manually.
	 */
	private void changePasswordManually() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_enter_new_password);

		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final EditText passwordEditText = (EditText) li.inflate(R.layout.include_password_edittext, null);
		dialog.setView(passwordEditText);

		dialog.setNegativeButton(R.string.cancel, null);
		dialog.setPositiveButton(R.string.label_continue, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String password = passwordEditText.getText().toString();
				if (password.length() > 6) {
					confirmPassword(password);
				} else if (password.length() > 0) {
					showPasswordLengthDialog();
				}
			}
		});

		AlertDialog window = dialog.create();
		window.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		window.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		window.show();
	}

	private void showPasswordLengthDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.dialog_title_error)
				.setMessage(R.string.dialog_message_min_password_length).setNeutralButton(android.R.string.ok, null)
				.show();
	}

	/**
	 * Shows a dialog to let the user confirm a password. This is called from
	 * {@link #changePasswordManually()} to verify the entered password.<br>
	 * <br>
	 * If the passwords match, the password will be stored until the user is
	 * saved in the database.
	 * 
	 * @param password
	 *            the password to be verified
	 */
	private void confirmPassword(final String password) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_confirm_password);

		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final EditText passwordEditText = (EditText) li.inflate(R.layout.include_password_edittext, null);
		dialog.setView(passwordEditText);

		dialog.setNegativeButton(R.string.cancel, null);
		dialog.setPositiveButton(R.string.label_continue, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

				if (passwordEditText.getText().toString().equals(password)) {
					newPassword = password;
					passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					passwordEditText.setText("********");
					Toast.makeText(UserAddEditActivity.this, R.string.toast_password_set, Toast.LENGTH_SHORT).show();

					// only send the password via email when the user account is
					// being created
					if (mode == Mode.ADD_USER) {
						resetPassword = true;
					} else {
						resetPassword = false;
					}
				} else {
					showPasswordsDontMatchDialog();
				}
			}
		});

		AlertDialog window = dialog.create();
		window.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		window.show();
	}

	/**
	 * Displays a dialog indicating that two entered passwords don't match.
	 */
	private void showPasswordsDontMatchDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_error);
		dialog.setMessage(R.string.dialog_message_passwords_dont_match);
		dialog.setNegativeButton(R.string.cancel, null);
		dialog.setPositiveButton(R.string.dialog_action_passwords_dont_match_try_again, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				changePasswordManually();
			}
		});
		dialog.show();
	}

	public void onBackPressed() {
		if (valueChanged) {
			confirmActivityLeaving(null, true);
		} else {
			finish();
		}
	}

	/**
	 * Shows a dialog which asks the user whether changes shall be saved and
	 * leaves the Activity.
	 * 
	 * @param intent
	 *            the Intent to be executed to leave the activity
	 * @param finish
	 *            true, if the current activity shall be finished
	 */
	private void confirmActivityLeaving(final Intent intent, final boolean finish) {
		// Ask for confirmation when the user presses back
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_save_changes);
		dialog.setMessage(R.string.dialog_message_save_changes);
		dialog.setNeutralButton(R.string.dialog_action_save_changes_abort, null);
		dialog.setNegativeButton(R.string.dialog_action_save_changes_no, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (intent != null) {
					startActivity(intent);
				}

				if (finish) {
					finish();
				}
			}
		});
		dialog.setPositiveButton(R.string.dialog_action_save_changes_yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveAndLeave(intent, finish);
			}
		});
		dialog.show();
	}

	private void saveAndLeave(final Intent intent, final boolean finish) {
		DialogUtil.showProgressDialog(UserAddEditActivity.this, R.string.dialog_title_saving, null, new Runnable() {
			@Override
			public void run() {
				// save and leave in case of success
				if (save()) {
					if (intent != null) {
						startActivity(intent);
					}

					if (finish) {
						finish();
					}
				}
			}
		});
	}

	/**
	 * Tries to save the user. On success the activity is finished.
	 * 
	 * @return true on success, false on error
	 */
	protected boolean save() {
		if (mode == Mode.ADD_USER) {
			Assert.assertTrue(User.getCurrentUser().isAdministrator());
			user = LoggedInUser.createNew();
		}

		if (User.getCurrentUser().isAdministrator()) {
			user.setActive(isActiveToggle.isChecked());
			user.setAdministrator(isAdminToggle.isChecked());
			user.setFirstName(firstNameEditText.getText().toString().trim());
			user.setLastName(lastNameEditText.getText().toString().trim());
			user.setEmail(emailEditText.getText().toString().trim());

			// when creating a new user, an e-mail has to be sent at any time!
			Assert.assertFalse(mode == Mode.ADD_USER && !resetPassword);

			if (newPassword == null && resetPassword) {
				user.setAutomaticallyGeneratedPassword();
			} else if (newPassword != null) {
				user.setPassword(newPassword, resetPassword);
			}
		} else {
			// If the current user is no administrator, the mode has to be 'Edit
			// My Account'
			Assert.assertTrue(mode == Mode.EDIT_MY_ACCOUNT);
			// Assert.assertFalse(resetPassword);

			if (newPassword != null) {
				user.setPassword(newPassword, false);
			}
		}

		for (InformationKey key : PocketBibApp.getInformationKeys()) {
			EditText editText = extraInformationFields.get(key);
			user.setInformation(key, editText.getText().toString().trim());
		}

		// Check if all required fields are set
		if (user.getEmail().length() == 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					emailEditText.requestFocus();
					DialogUtil.getErrorDialogRunnable(UserAddEditActivity.this, R.string.dialog_message_email_required)
							.run();
				}
			});

			return false;
		} else if (!LoggedInUser.validEmail(user.getEmail())) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					emailEditText.requestFocus();
					DialogUtil.getErrorDialogRunnable(UserAddEditActivity.this,
							R.string.dialog_message_valid_email_required).run();
				}
			});

			return false;
		} else if (user.getFirstName().length() == 0 || user.getLastName().length() == 0) {
			runOnUiThread(DialogUtil.getErrorDialogRunnable(this, R.string.dialog_message_name_required));
			return false;
		}

		Log.i(TAG, "Trying to save user " + user);

		ResponseCode response = user.save();

		if (response != ResponseCode.OK) {
			Log.e(TAG, "Saving failed!");
			runOnUiThread(DialogUtil.getErrorDialogRunnable(this, response.getErrorStringRes()));

			user = null;
		} else {
			Log.i(TAG, "Saving succeeded! Leaving " + TAG + "...");

			if (mode == Mode.EDIT_MY_ACCOUNT) {
				User.setCurrentUser(user);
			}
			// close the Activity when finished
			finish();
		}

		return response == ResponseCode.OK;
	}

	public void showLentItems(View source) {
		Intent intent = new Intent(this, ManageBorrowedItemsActivity.class);
		intent.putExtra(Constants.Intent.KEY_USER_PARCEL, user);

		if (valueChanged) {
			confirmActivityLeaving(intent, true);
		} else {
			startActivity(intent);
		}
	}

	private class LoadBorrowedItemsCountTask extends AsyncTask<Void, Void, Response<ItemAdapter>> {

		@Override
		protected Response<ItemAdapter> doInBackground(Void... params) {
			return PocketBibApp.getLibraryManager().getBorrowedItems(user);
		}

		@Override
		protected void onPostExecute(Response<ItemAdapter> result) {
			ItemAdapter adapter = result.getData();

			if (result.getResponseCode() == ResponseCode.OK && adapter != null) {
				lentItemsTextView.setText("" + adapter.getCount());
			} else {
				lentItemsTextView.setText("-");
			}
		}

	}

	private class CheckExistingUserTask extends AsyncTask<Void, Void, Response<UserAdapter>> {

		@Override
		protected Response<UserAdapter> doInBackground(Void... params) {
			String email = emailEditText.getText().toString().trim();
			if (email == null || email.length() == 0 || email.equals(foundEmail)) {
				return null;
			} else {
				return PocketBibApp.getRegistrationProvider().searchUsers(email);
			}
		}

		@Override
		protected void onPostExecute(Response<UserAdapter> response) {

			if (response != null && response.getResponseCode() == ResponseCode.OK) {
				String email = emailEditText.getText().toString().trim();
				for (LoggedInUser user : response.getData()) {
					if (email.equals(user.getEmail())) {
						final LoggedInUser correctUser = user;
						AlertDialog.Builder dialog = new AlertDialog.Builder(UserAddEditActivity.this);
						dialog.setTitle(R.string.dialog_title_user_exists);
						dialog.setMessage(R.string.dialog_message_user_exists);
						dialog.setCancelable(false);
						dialog.setNegativeButton(android.R.string.cancel,  new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								emailEditText.setText("");
								foundEmail = null;
							}
							
						});
						dialog.setPositiveButton(R.string.dialog_button_item_exists, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(UserAddEditActivity.this, UserAddEditActivity.class);
								intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE);
								intent.putExtra(Constants.Intent.KEY_USER_PARCEL, correctUser);
								startActivity(intent);
								finish();
							}
						});
						dialog.create().show();
					}
				}
			} else if (response != null) {
				DialogUtil.showErrorDialog(UserAddEditActivity.this, response.getResponseCode().getErrorCode());
			}
		}

	}

	private final TextWatcher emailTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(android.text.Editable s) {
			valueChanged = true;
			
			if (foundEmail == null || !foundEmail.equals(emailEditText.getText().toString().trim()))
				new CheckExistingUserTask().execute();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};
}