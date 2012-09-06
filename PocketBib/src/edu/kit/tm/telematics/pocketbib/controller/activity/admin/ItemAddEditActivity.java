package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.impl.edu.kit.RestClient;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
import edu.kit.tm.telematics.pocketbib.model.library.Issn;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopy;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopyAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.Magazine;
import edu.kit.tm.telematics.pocketbib.model.library.OtherItem;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.view.DialogUtil;

public class ItemAddEditActivity extends BaseActivity implements OnItemSelectedListener {

	private static final String TAG = "ItemAddEditActivity";

	private static final int INDEX_BOOK = 0;

	private static final int INDEX_MAGAZINE = 1;

	private static final int INDEX_OTHER_ITEM = 2;

	private Item item = null;

	private Button manageCopiesButton;

	private ImageButton addCopyButton;

	private EditTextEditable author, title, description, isbn, issn, pageCount, edition, publisher, publicationDate,
			position, otherType, copyCount;

	/** toggle defining the activation of the item */
	private CompoundButton isActiveToggle;

	private Editable<RatingBar> rating;

	private Editable<Spinner> type;

	private boolean isEditMode;

	private Editable<?>[] form;

	private Integer publicationYear = null;

	private Calendar publicationDateCal = null;

	private boolean isScanningIsbn = true;

	private boolean valueChanged = false;
	
	private boolean calledInformationManager = false;
	
	private String foundIsbn = null;

	private String foundIssn = null;
	
	private int allCopies = 0; 

	private boolean checkFields() {
		if (title.getText().trim().length() == 0) {
			showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_missing_title);
			return false;
		}

		if (type.view.getSelectedItemPosition() == INDEX_BOOK) {
			if (author.getText().trim().length() == 0) {
				showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_missing_author);
				return false;
			}

			if (Isbn.initIsbn(isbn.getText()) == null) {
				showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_wrong_isbn);
				return false;
			}

			if (publisher.getText().trim().length() == 0) {
				showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_missing_publisher);
				return false;
			}
		} else if (type.view.getSelectedItemPosition() == INDEX_MAGAZINE) {
			if (Issn.initIssn(isbn.getText()) == null) {
				showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_wrong_issn);
				return false;
			}

			if (publisher.getText().trim().length() == 0) {
				showErrorDialog(R.string.dialog_title_missing_information, R.string.dialog_message_missing_publisher);
				return false;
			}
		}

		return true;
	}

	private Item clone(Item source) {
		Parcel p = Parcel.obtain();
		p.writeValue(source);
		p.setDataPosition(0);
		Item clone = (Item) p.readValue(Item.class.getClassLoader());
		p.recycle();

		return clone;
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
		if (valueChanged) {
			// Ask for confirmation when the user presses back
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_title_save_changes);
			dialog.setMessage(R.string.dialog_message_save_changes);
			dialog.setNeutralButton(R.string.dialog_action_save_changes_abort, null);
			dialog.setNegativeButton(R.string.dialog_action_save_changes_no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					leave(intent, finish);
				}
			});
			dialog.setPositiveButton(R.string.dialog_action_save_changes_yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					saveAndLeave(intent, finish);
				}
			});
			dialog.show();
		} else {
			leave(intent, finish);
		}
	}

	private void initializeAddMode() {
		isEditMode = false;
		setTitle(R.string.title_add_item);

		isActiveToggle.setChecked(true);
		addCopyButton.setVisibility(View.VISIBLE);
		manageCopiesButton.setVisibility(View.GONE);
		
		// with these two lines you are not able to add an item if it already exists in database;
		// delete if unwanted
		isbn.view.addTextChangedListener(isbnOrIssnTextWatcher);
		issn.view.addTextChangedListener(isbnOrIssnTextWatcher);
	}

	private void initializeBookForm() {
		otherType.hide();
		issn.hide();

		if (publicationYear == null && publicationDateCal != null) {
			setPublicationYear(publicationDateCal.get(Calendar.YEAR));
		}
	}

	private void initializeEditMode() {
		isEditMode = true;
		setTitle(R.string.title_edit_item);

		type.hide();

		item = getIntent().getParcelableExtra(Constants.Intent.KEY_ITEM_PARCEL);

		if (item == null) {
			throw new AssertionError("Supplied no Item to ItemAddEditActivity (Key: Constants.Intent.KEY_ITEM_PARCEL)");
		}

		Log.i(TAG, "Editing " + item);

		addCopyButton.setVisibility(View.GONE);
		manageCopiesButton.setVisibility(View.VISIBLE);

		copyCount.view.setInputType(0);
		copyCount.view.setEnabled(false);

		type.view.setEnabled(false);

		if (item instanceof Book) {
			initializeBookForm();
		} else if (item instanceof Magazine) {
			initializeMagazineForm();
		} else {
			initializeOtherItemForm();
		}

		setFormValues(item);
	}

	private void initializeMagazineForm() {
		otherType.hide();
		isbn.hide();
		author.hide();

		if (publicationDateCal == null) {
			publicationYear = null;
			publicationDateCal = null;
			publicationDate.view.setText("");
		}
	}

	private void initializeOtherItemForm() {
		otherType.setIsOptional(true);
		isbn.setIsOptional(true);
		issn.setIsOptional(true);
		author.setIsOptional(true);
		publisher.setIsOptional(true);
	}

	private void leave(final Intent intent, final boolean finish) {
		if (intent != null) {
			startActivity(intent);
		}

		if (finish) {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

		if (scanResult != null) {
			final String result = scanResult.getContents();

			if (isScanningIsbn) {
				isbn.view.setText(result);
			} else {
				issn.view.setText(result);
			}
		}
	}

	public void onAddCopyClick(View view) {
		Assert.assertFalse(isEditMode);

		try {
			int count = Integer.valueOf(copyCount.getText());
			copyCount.view.setText("" + (count + 1));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			copyCount.view.setText("" + 1);
		}
	}

	public void onBackPressed() {
		if (valueChanged) {
			confirmActivityLeaving(null, true);
		} else {
			finish();
		}
	}

	@SuppressLint({ "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!User.getCurrentUser().isAdministrator()) {
			Log.e(TAG, "The current user has no admin rights.");
			finish();
			return;
		}

		setDisplayShowUp(AdminActivity.class, true);

		setContentView(R.layout.activity_admin_item_add_edit);

		author = new EditTextEditable(R.id.layout_author, R.id.label_optional_author, R.id.input_author);
		type = new Editable<Spinner>(R.id.layout_type, 0, R.id.spinner_item_type);
		otherType = new EditTextEditable(R.id.layout_item_type_other, 0, R.id.input_item_type_other);
		title = new EditTextEditable(R.id.layout_title, 0, R.id.input_title);
		description = new EditTextEditable(R.id.layout_description, R.id.label_optional_description,
				R.id.input_description);
		isbn = new EditTextEditable(R.id.layout_isbn, R.id.label_optional_isbn, R.id.input_isbn);
		issn = new EditTextEditable(R.id.layout_issn, R.id.label_optional_issn, R.id.input_issn);
		isActiveToggle = (CompoundButton) findViewById(R.id.switch_item_active);
		pageCount = new EditTextEditable(R.id.layout_page_count, R.id.label_optional_page_count, R.id.input_page_count);
		edition = new EditTextEditable(R.id.layout_edition, R.id.label_optional_edition, R.id.input_edition);
		publisher = new EditTextEditable(R.id.layout_publisher, R.id.label_optional_publisher, R.id.input_publisher);
		publicationDate = new EditTextEditable(R.id.layout_publication_date, R.id.label_optional_publication_date,
				R.id.input_publication_date);
		position = new EditTextEditable(R.id.layout_position, R.id.label_optional_position, R.id.input_position);
		rating = new Editable<RatingBar>(R.id.layout_average_rating, 0, R.id.rating);
		copyCount = new EditTextEditable(R.id.layout_copy_count, 0, R.id.input_copy_count);

		manageCopiesButton = (Button) copyCount.layout.findViewById(R.id.button_manage_copies);
		addCopyButton = (ImageButton) copyCount.layout.findViewById(R.id.button_add_copy);

		Assert.assertNotNull(manageCopiesButton);
		Assert.assertNotNull(addCopyButton);

		isActiveToggle.setChecked(true);

		publicationDate.view.setInputType(EditorInfo.TYPE_NULL);
		publicationDate.view.setFocusable(false);
		publicationDate.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateInputDialog();
			}
		});

		if (savedInstanceState != null) {
			calledInformationManager = savedInstanceState.getBoolean("calledInformationManager");
		}

		form = new Editable<?>[] { author, title, description, isbn, issn, pageCount, edition, publisher,
				publicationDate, position, otherType, rating, type, copyCount };

		type.view.setOnItemSelectedListener(this);

		String requestedMode = getIntent().getStringExtra(Constants.Intent.KEY_ADD_EDIT_MODE);
		if (Constants.Intent.VALUE_ADD_MODE.equals(requestedMode)) {
			initializeAddMode();
			type.view.setSelection(INDEX_BOOK);
		} else if (Constants.Intent.VALUE_EDIT_MODE.equals(requestedMode)) {
			initializeEditMode();
		} else {
			throw new AssertionError("Unknown mode: " + requestedMode);
		}

		for (Editable<?> e : form) {
			if (!e.equals(copyCount)) {
				e.listenForChanges();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isEditMode)
			new LoadCopyCountAsyncTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.item_add_edit, menu);

		menu.findItem(R.id.item_administration).setVisible(false);
		menu.findItem(R.id.item_my_account).setVisible(false);

		if (!isEditMode) {
			menu.findItem(R.id.item_delete).setVisible(false);
			menu.findItem(R.id.item_reload).setVisible(false);
		}

		return true;
	}

	public void onDisplayCopiesClick(View view) {
		Intent intent = new Intent(this, ManageCopiesActivity.class);
		intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);

		if (valueChanged) {
			confirmActivityLeaving(intent, true);
		} else {
			startActivity(intent);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Assert.assertTrue(position == INDEX_BOOK || position == INDEX_MAGAZINE || position == INDEX_OTHER_ITEM);

		resetForm();

		if (position == INDEX_BOOK) {
			initializeBookForm();
		} else if (position == INDEX_MAGAZINE) {
			initializeMagazineForm();
		} else {
			initializeOtherItemForm();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.item_delete:
			onRemoveClick(null);
			return true;

		case R.id.item_reload:
			DialogUtil.showProgressDialog(this, R.string.dialog_title_reloading, null, new Runnable() {
				@Override
				public void run() {
					Response<Item> response = PocketBibApp.getLibraryManager().getItem(item.getItemId());

					if (response.getResponseCode() != ResponseCode.OK || response.getData() == null) {
						DialogUtil.showErrorDialog(ItemAddEditActivity.this, response.getResponseCode()
								.getErrorStringRes());
					} else {
						item = response.getData();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setFormValues(item);
								new LoadCopyCountAsyncTask().execute();
								Toast.makeText(ItemAddEditActivity.this, R.string.reload_finished, Toast.LENGTH_SHORT)
										.show();
							}
						});
					}
				}
			});
			return true;

		case R.id.item_save:
			DialogUtil.showProgressDialog(this, R.string.dialog_title_saving, null, new Runnable() {
				@Override
				public void run() {
					save();
				}
			});
			return true;

		case R.id.item_discard:
			finish();
			return true;

		case android.R.id.home:
			Intent intent = new Intent(this, AdminActivity.class);
			intent.putExtra(Constants.Intent.KEY_ADMIN_ACTIVE_TAB, Constants.Intent.VALUE_ADMIN_ACTIVE_TAB_ITEMS);
			if (valueChanged) {
				confirmActivityLeaving(intent, true);
			} else {
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(menuItem);
		}
	}

	public void onRemoveClick(View view) {
		Assert.assertNotNull(item);
		Assert.assertTrue(User.getCurrentUser().isAdministrator());

		if (!item.isStoredInDatabase()) {
			finish();
			return;
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_delete);

		if (item.getTitle() == null || item.getTitle().length() == 0) {
			dialog.setMessage(R.string.dialog_message_delete_item_no_title);
		} else if (allCopies == 0) {
			dialog.setMessage(R.string.dialog_message_delete_item);
		} else if (allCopies == 1) {
			dialog.setMessage(getString(R.string.dialog_message_delete_item_single, item.getTitle()));
		} else {
			dialog.setMessage(getString(R.string.dialog_message_delete_item_multiple, item.getTitle(), allCopies));
		}

		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.setPositiveButton(R.string.delete, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DialogUtil.showProgressDialog(ItemAddEditActivity.this, R.string.dialog_title_deletion, null,
						new Runnable() {
							@Override
							public void run() {
								ResponseCode response = PocketBibApp.getLibraryManager().removeItem(item);

								if (response == ResponseCode.OK) {
									DialogUtil.showToast(ItemAddEditActivity.this, R.string.toast_item_deleted);
									finish();
								} else {
									DialogUtil.showErrorDialog(ItemAddEditActivity.this, response.getErrorStringRes());
								}
							}
						});
			}
		});

		dialog.show();
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey("publication_year"))
			publicationYear = savedInstanceState.getInt("publication_year");

		if (savedInstanceState.containsKey("publication_date"))
			publicationDateCal = (Calendar) savedInstanceState.getSerializable("publication_date");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		if (publicationYear != null)
			savedInstanceState.putInt("publication_year", publicationYear);

		if (publicationDateCal != null)
			savedInstanceState.putSerializable("publication_date", publicationDateCal);
		
		savedInstanceState.putBoolean("calledInformationManager", calledInformationManager);
	}

	public void onScanIsbnClick(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
		isScanningIsbn = true;
	}

	public void onScanIssnClick(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
		isScanningIsbn = false;
	}

	/**
	 * Resets the form by making everything visible and makes description,
	 * pageCount, publicationDate, edition and position optional.
	 */
	private void resetForm() {
		for (Editable<?> e : form) {
			e.show().setIsOptional(false);
		}

		if (!isEditMode)
			rating.hide();

		description.setIsOptional(true);
		pageCount.setIsOptional(true);
		publicationDate.setIsOptional(true);
		edition.setIsOptional(true);
		position.setIsOptional(true);
	}

	public boolean save() {
		Item item;

		if (!checkFields())
			return false;

		if (isEditMode)
			item = clone(this.item);
		else {
			if (type.view.getSelectedItemPosition() == INDEX_BOOK)
				item = Book.createNew();
			else if (type.view.getSelectedItemPosition() == INDEX_MAGAZINE)
				item = Magazine.createNew();
			else
				item = OtherItem.createNew();
		}

		if (!TextUtils.isEmpty(pageCount.getText())) {
			try {
				item.setPageCount(Integer.valueOf(pageCount.getText()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		item.setTitle(title.getText());
		item.setDescription(description.getText());
		item.setEdition(edition.getText());
		item.setPosition(position.getText());
		item.setPublisher(publisher.getText());
		item.setActive(isActiveToggle.isChecked());

		if (item instanceof Book) {
			Book b = (Book) item;
			b.setAuthor(author.getText());
			b.setIsbn(Isbn.initIsbn(isbn.getText()));
			b.setPublicationYear(publicationYear);
		} else if (item instanceof Magazine) {
			Magazine m = (Magazine) item;
			m.setIssn(Issn.initIssn(issn.getText()));

			if (publicationDateCal != null)
				m.setPublicationDate(publicationDateCal.getTime());
		} else {
			OtherItem o = (OtherItem) item;
			if (otherType.getText().trim().length() == 0) {
				o.setType("OtherItem");
			} else {
				o.setType(otherType.getText());
			}
			o.setAuthor(author.getText());
			o.setIsbn(Isbn.initIsbn(isbn.getText()));
			o.setIssn(Issn.initIssn(issn.getText()));

			if (publicationYear != null)
				o.setPublicationYear(publicationYear);
			else if (publicationDateCal != null)
				o.setPublicationDate(publicationDateCal.getTime());
		}

		Log.i(TAG, "Saving item " + item);
		ResponseCode code = item.save();

		Log.i(TAG, "Saving finished. " + code);

		if (code != ResponseCode.OK) {
			DialogUtil.showErrorDialog(this, code.getErrorStringRes());
			return false;
		}

		if (!isEditMode) {
			int numCopies = Integer.valueOf(copyCount.getText());
			Log.i(TAG, "Inserting " + numCopies + " copy/copies.");
			for (int i = 0; i < numCopies; i++) {
				Response<Integer> response = PocketBibApp.getLibraryManager().insertOrUpdateCopy(
						ItemCopy.createNew(item));

				Log.i(TAG, "Inserted response #" + i + ": " + response.getResponseCode());
			}
		}
		
		DialogUtil.showToast(ItemAddEditActivity.this, R.string.toast_item_saved);

		finish();
		return true;
	}

	private void saveAndLeave(final Intent intent, final boolean finish) {
		DialogUtil.showProgressDialog(ItemAddEditActivity.this, R.string.dialog_title_saving, null, new Runnable() {
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

	private void setFormValues(Item item) {
		if (item.getTitle() != null && !"".equals(item.getTitle()))
			title.view.setText(item.getTitle());

		if (item.getPageCount() != Constants.NOT_SAVED && item.getPageCount() > 0)
			pageCount.view.setText(Integer.toString(item.getPageCount()));

		if (item.getPosition() != null)
			position.view.setText(item.getPosition());

		if (item.getPublisher() != null)
			publisher.view.setText(item.getPublisher());

		if (item.getEdition() != null)
			edition.view.setText(item.getEdition());

		copyCount.view.setText(getString(R.string.label_available_copies, item.getAvailableCopies()));

		if (item.getDescription() != null)
			description.view.setText(item.getDescription());

		isActiveToggle.setChecked(item.getActive());

		if (item.getAverageRating() != null) {
			rating.view.setRating(item.getAverageRating().floatValue());
		}

		if (item instanceof Book) {
			Book b = (Book) item;

			type.view.setSelection(INDEX_BOOK);

			if (b.getAuthor() != null)
				author.view.setText(b.getAuthor());

			if (b.getIsbn() != null) {
				isbn.view.setText(b.getIsbn().getIsbn13());
			}
			if (b.getPublicationYear() != Constants.NOT_SAVED) {
				setPublicationYear(b.getPublicationYear());
			}
		} else if (item instanceof Magazine) {
			Magazine m = (Magazine) item;

			type.view.setSelection(INDEX_MAGAZINE);

			if (m.getIssn() != null) {
				issn.view.setText(m.getIssn().getIssn());
			}

			if (m.getPublicationDate() != null) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(m.getPublicationDate());
				setPublicationDate(cal);
			}

		} else {
			OtherItem o = (OtherItem) item;

			if (!o.getType().equals("OtherItem")) {
				otherType.view.setText(o.getType());
			}

			type.view.setSelection(INDEX_OTHER_ITEM);

			if (o.getAuthor() != null)
				author.view.setText(o.getAuthor());

			if (o.getIsbn() != null) {
				isbn.view.setText(o.getIsbn().getIsbn13());
			}

			if (o.getIssn() != null) {
				issn.view.setText(o.getIssn().getIssn());
			}

			if (o.getPublicationDate() != null) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(o.getPublicationDate());
				setPublicationDate(cal);
			} else if (o.getPublicationYear() > 0 && o.getPublicationYear() != Constants.NOT_SAVED) {
				setPublicationYear(o.getPublicationYear());
			}
		}
	}

	private void setFormValuesFromExternalSource(Book b) {
		if (b.getTitle() != null && b.getTitle().length() > 0)
			title.view.setText(b.getTitle());

		if (b.getPageCount() != Constants.NOT_SAVED && b.getPageCount() > 0) {
			pageCount.view.setText("" + b.getPageCount());
		}

		if (b.getPosition() != null && b.getPosition().length() > 0)
			position.view.setText(b.getPosition());

		if (b.getPublisher() != null && b.getPublisher().length() > 0)
			publisher.view.setText(b.getPublisher());

		if (b.getDescription() != null && b.getDescription().length() > 0)
			description.view.setText(b.getDescription());

		if (b.getAuthor() != null && b.getAuthor().length() > 0)
			author.view.setText(b.getAuthor());

		if (b.getPublicationYear() != Constants.ID_NOT_SAVED) {
			setPublicationYear(b.getPublicationYear());
		}
	}

	private void setPublicationDate(Calendar cal) {
		if (cal != null) {
			publicationDateCal = cal;
			publicationYear = null;

			publicationDate.view
					.setText(DateFormat.getMediumDateFormat(ItemAddEditActivity.this).format(cal.getTime()));
		}
	}

	private void setPublicationYear(Integer year) {
		if (year != null) {
			publicationDateCal = null;
			publicationYear = year;

			publicationDate.view.setText("" + year.intValue());
		}
	}

	public void showDateInputDialog() {
		if (type.view.getSelectedItemPosition() == INDEX_BOOK) {
			showPublicationYearInputDialog();
		} else if (type.view.getSelectedItemPosition() == INDEX_MAGAZINE) {
			showPublicationDateInputDialog();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_title_choose_date_input);

			CharSequence[] items = { getString(R.string.dialog_item_set_publication_year),
					getString(R.string.dialog_item_set_publication_date) };
			dialog.setItems(items, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0)
						showPublicationYearInputDialog();
					else
						showPublicationDateInputDialog();

				}
			});
			dialog.show();
		}
	}

	private void showErrorDialog(final int title, final int message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder dialog = new AlertDialog.Builder(ItemAddEditActivity.this);
				dialog.setTitle(title);
				dialog.setMessage(message);
				dialog.setPositiveButton(android.R.string.ok, null);
				dialog.show();
			}
		});
	}

	private void showPublicationDateInputDialog() {
		Calendar cal;

		if (publicationDateCal != null) {
			cal = publicationDateCal;
		} else {
			cal = GregorianCalendar.getInstance();

			if (publicationYear != null) {
				cal.set(Calendar.YEAR, publicationYear);
			}
		}

		DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar cal = GregorianCalendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

				setPublicationDate(cal);
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		dialog.setTitle(R.string.dialog_title_choose_publication_date);
		dialog.show();
	}

	@TargetApi(11)
	private void showPublicationYearInputDialog() {
		Calendar cal = GregorianCalendar.getInstance();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title_choose_year);
		builder.setNegativeButton(R.string.cancel, null);

		if (Constants.API_LEVEL >= 11) {
			final NumberPicker numberPicker = new NumberPicker(this);
			numberPicker.setMinValue(0);
			numberPicker.setMaxValue(9999);

			if (publicationYear != null)
				numberPicker.setValue(publicationYear);
			else if (publicationDateCal != null)
				numberPicker.setValue(publicationDateCal.get(Calendar.YEAR));
			else
				numberPicker.setValue(Calendar.getInstance().get(Calendar.YEAR));

			builder.setView(numberPicker);
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setPublicationYear(numberPicker.getValue());
				}
			});
		} else {
			final EditText editText = new EditText(this);
			editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			builder.setView(editText);
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					setPublicationYear(Integer.valueOf(editText.getText().toString()));
				}
			});
		}

		builder.show();

	}

	private class Editable<T extends View> {

		public final ViewGroup layout;

		public final View optionalIndicator;

		public final T view;

		@SuppressWarnings("unchecked")
		public Editable(int layoutId, int optionalIndicatorId, int viewId) {
			layout = (ViewGroup) findViewById(layoutId);
			Assert.assertNotNull(layout);

			if (optionalIndicatorId > 0) {
				optionalIndicator = findViewById(optionalIndicatorId);
				Assert.assertNotNull(optionalIndicatorId);
			} else {
				optionalIndicator = null;
			}

			view = (T) findViewById(viewId);
			Assert.assertNotNull(view);
		}

		public Editable<T> hide() {
			layout.setVisibility(View.GONE);
			return this;
		}

		public Editable<T> listenForChanges() {
			return this;
		}

		public Editable<T> setIsOptional(boolean optional) {
			if (optionalIndicator != null) {
				optionalIndicator.setVisibility(optional ? View.VISIBLE : View.GONE);
			}
			return this;
		}

		public Editable<T> show() {
			layout.setVisibility(View.VISIBLE);
			return this;
		}
	}

	private class EditTextEditable extends Editable<EditText> {
		public EditTextEditable(int layoutId, int optionalIndicatorId, int editTextId) {
			super(layoutId, optionalIndicatorId, editTextId);
		}

		public String getText() {
			return view.getText().toString();
		}

		@Override
		public Editable<EditText> listenForChanges() {
			view.addTextChangedListener(editTextWatcher);
			return super.listenForChanges();
		}

	}

	private class LoadCopyCountAsyncTask extends AsyncTask<Void, Void, Response<ItemCopyAdapter>> {

		@Override
		protected Response<ItemCopyAdapter> doInBackground(Void... params) {
			return PocketBibApp.getLibraryManager().getItemCopies(item.getItemId());
		}

		@Override
		protected void onPostExecute(Response<ItemCopyAdapter> result) {
			if (result.getResponseCode() == ResponseCode.OK) {
				int activeCopyCount = 0;
				int availableCopyCount = 0;

				ItemCopyAdapter adapter = result.getData();

				if (adapter != null) {
					allCopies = adapter.getCount();
					Log.i(TAG, "LoadCopyCount: Got OK and Adapter with " + allCopies + " copies.");

					for (ItemCopy itemCopy : adapter) {
						if (itemCopy != null && itemCopy.isActive()) {
							activeCopyCount++;
						}
						if (itemCopy != null && itemCopy.isActive() && itemCopy.getUser() == null) {
							availableCopyCount++;
						}
					}
				} else {
					Log.i(TAG, "LoadCopyCount: Got OK and NULL Adapter.");
				}

				item.setAvailableCopies(availableCopyCount);
				copyCount.view.setText(getString(R.string.label_available_copies_detailed, item.getAvailableCopies(),
						activeCopyCount));
			} else {
				Log.e(TAG, "LoadCopyCountTask got " + result.getResponseCode());
			}
		}

	}

	private final TextWatcher editTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(android.text.Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			valueChanged = true;
		}
	};

	private class CheckExistingItemsTask extends AsyncTask<Void, Void, Item> {

		@Override
		protected Item doInBackground(Void... params) {
			if ((foundIsbn != null && foundIsbn.equals(isbn.getText()))
					|| (foundIssn != null && foundIssn.equals(issn.getText()))) {
				// Because this is a AsyncTask, it can be called again before it managed to change it
				Log.i(TAG, "checking failed - already checked (isbn: " + isbn.getText() + ", issn: " + issn.getText() + ")");
				return Book.createNew();

			} else if (type.view.getSelectedItemPosition() == INDEX_BOOK && Isbn.initIsbn(isbn.getText()) != null) {
				// valid isbn was found
				Log.i(TAG, "checking isbn: " + isbn.getText());
				foundIsbn = isbn.getText();
				return RestClient.getInstance().getBook(new Isbn(foundIsbn));

			} else if (type.view.getSelectedItemPosition() == INDEX_MAGAZINE && Issn.initIssn(issn.getText()) != null) {
				// valid issn was found
				Log.i(TAG, "checking issn: " + issn.getText());
				foundIssn = issn.getText();
				return RestClient.getInstance().getMagazine(new Issn(foundIssn));

			} else {
				Log.i(TAG, "checking failed (isbn: " + isbn.getText() + ", issn: " + issn.getText() + ")");
				return Book.createNew();
			}
		}

		@Override
		protected void onPostExecute(final Item item) {
			if (item != null && !Constants.ID_NOT_SAVED.equals(item.getItemId())) {
				// there already exists an item with the identifier in the database
				Log.i(TAG, "Item already exists: " + item);

				AlertDialog.Builder dialog = new AlertDialog.Builder(ItemAddEditActivity.this);
				dialog.setTitle(R.string.dialog_title_item_exists);
				dialog.setMessage(R.string.dialog_message_item_exists);
				dialog.setCancelable(false);
				dialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (type.view.getSelectedItemPosition() == INDEX_BOOK) {
							isbn.view.setText("");
						} else if (type.view.getSelectedItemPosition() == INDEX_MAGAZINE) {
							issn.view.setText("");
							foundIssn = null;
						}

					}

				});
				dialog.setPositiveButton(R.string.dialog_button_item_exists, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ItemAddEditActivity.this, ItemAddEditActivity.class);
						intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE);
						intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
						startActivity(intent);
						finish();
					}
				});
				dialog.show();
			} else if (item == null && !calledInformationManager && isScanningIsbn) {

				final Isbn isbnString = Isbn.initIsbn(isbn.getText());

				Log.i(TAG, "ISBN after barcode scan: " + isbnString + " (raw: " + isbn.getText() + ")");

				if (isbnString != null) {
					calledInformationManager = true;
					AlertDialog.Builder dialog = new AlertDialog.Builder(ItemAddEditActivity.this);
					dialog.setTitle(R.string.dialog_title_load_external_information);
					dialog.setMessage(R.string.dialog_message_load_external_information);
					dialog.setNegativeButton(R.string.cancel, null);
					dialog.setPositiveButton(R.string.dialog_action_load_external_information, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DialogUtil.showProgressDialog(ItemAddEditActivity.this, R.string.loading, null,
									new Runnable() {

										@Override
										public void run() {
											final Book book = PocketBibApp.getItemInformationManager().getBook(
													isbnString);

											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													setFormValuesFromExternalSource(book);
												}
											});
										}
									});
						}
					});
					dialog.show();
				}

			}

		}

	}

	private final TextWatcher isbnOrIssnTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(android.text.Editable s) {
			if ((foundIsbn == null && foundIssn == null)
					|| (foundIsbn != null && !foundIsbn.equals(isbn.getText()))
					|| (foundIssn != null && !foundIssn.equals(issn.getText()))) {				
				new CheckExistingItemsTask().execute();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

}
