package edu.kit.tm.telematics.pocketbib.model.user;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;

public class InformationKey {

	/** the possible categories */
	public enum InformationCategory {
		LOCATION, TELEPHONE_NUMBER, OTHER;
	}


	/** the name, used for serialization / parcelation */
	private final String name;
	
	/** the category */
	private final InformationCategory category;

	/** the label String resource */
	private final int labelStringRes;

	/** the input type which will be assigned to EditText views */
	private final int editTextInputType;

	/** the ime options which will be assigned to EditText views */
	private final int editTextImeOptions;

	/**
	 * Creates an InformationKey.
	 * 
	 * @param category
	 *            the category
	 * @param labelStringRes
	 *            the label String resource
	 */
	public InformationKey(String name, InformationCategory category, int labelStringRes) {
		this(name, category, labelStringRes, InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_NEXT);
	}

	/**
	 * Creates an InformationKey.
	 * 
	 * @param category
	 *            the category
	 * @param labelStringRes
	 *            the label String resource
	 * @param editTextInputType
	 *            the input type for EditText views (refer to {@link #InputType}
	 *            )
	 */
	public InformationKey(String name, InformationCategory category, int labelStringRes, int editTextInputType) {
		this(name, category, labelStringRes, editTextInputType, EditorInfo.IME_ACTION_NEXT);
	}

	/**
	 * Creates an InformationKey.
	 * 
	 * @param category
	 *            the category
	 * @param labelStringRes
	 *            the label String resource
	 * @param editTextInputType
	 *            the input type for EditText views (refer to {@link #InputType}
	 *            )
	 * @param editTextImeOptions
	 *            the ime options for EditText views (refer to
	 *            {@link #EditorInfo})
	 */
	public InformationKey(String name, InformationCategory category, int labelStringRes, int editTextInputType,
			int editTextImeOptions) {
		this.name = name;
		this.category = category;
		this.labelStringRes = labelStringRes;
		this.editTextInputType = editTextInputType;
		this.editTextImeOptions = editTextImeOptions;
	}

	/**
	 * Returns the category
	 * 
	 * @return the category
	 */
	public InformationCategory getCategory() {
		return this.category;
	}

	/**
	 * Returns the label as localized String
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return PocketBibApp.getAppContext().getString(labelStringRes);
	}

	/**
	 * Returns the label as String resource
	 * 
	 * @return the label
	 */
	public int getLabelStringRes() {
		return labelStringRes;
	}

	/**
	 * Returns the input type for this InformationKey.
	 * 
	 * @return the input type for EditText views
	 */
	public int getInputType() {
		return editTextInputType;
	}

	/**
	 * Returns the ime options for this InformationKey.
	 * @return the ime options for EditText views
	 */
	public int getImeOptions() {
		return editTextImeOptions;
	}
	
	/**
	 * Returns the unique name.
	 * @return the unique name.
	 */
	public String getName() {
		return name;
	}
	
}
