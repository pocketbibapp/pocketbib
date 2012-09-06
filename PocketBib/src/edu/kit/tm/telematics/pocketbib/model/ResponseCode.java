package edu.kit.tm.telematics.pocketbib.model;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;

/**
 * A server response code with an associated error message.
 */
public enum ResponseCode {
	OK(200, android.R.string.ok),
	
	ERROR_UNKNOWN(400, R.string.error_unknown),
	ERROR_LOGIN_FAILED(401, R.string.error_login_failed), 
	ERROR_NO_RIGHTS(403, R.string.error_no_rights),
	ERROR_ITEM_ALREADY_LENT(410, R.string.error_item_already_lent),
	ERROR_ACCOUNT_DEACTIVATED(418, R.string.error_account_deactivated),
	ERROR_SERVER_PROBLEM(500, R.string.error_server_problem),
	ERROR_SERVER_NOT_AVAILABLE(503,	R.string.error_server_not_available);

	/** the error code (received from the server) */
	private final int errorCode;

	/** the String resource of the error message */
	private final int errorStringRes;

	/**
	 * Creates a new ResponseCode.
	 * @param errorCode the server error code
	 * @param errorStringRes the error message String resource
	 */
	private ResponseCode(int errorCode, int errorStringRes) {
		this.errorCode = errorCode;
		this.errorStringRes = errorStringRes;
	}

	/**
	 * Returns the server error code
	 * @return the server error code
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the error String resource
	 * @return the error String resource
	 */
	public int getErrorStringRes() {
		return errorStringRes;
	}

	/**
	 * Returns the error message.
	 * @return the error message.
	 */
	public String getErrorString() {
		return PocketBibApp.getAppContext().getString(errorStringRes);
	}

	@Override
	public String toString() {
		return getErrorString() + "(#" + errorCode + ")";
	}
	
	/**
	 * Returns the ResponseCode associated with the given server error code.
	 * @param errorCode the server error code
	 * @return the associated ResponseCode
	 */
	public static ResponseCode getFromErrorCode(int errorCode) {
		// check if there's a matching ResponseCode
		for (ResponseCode responseCode : values()) {
			if(responseCode.errorCode == errorCode) {
				return responseCode;
			}
		}
		
		// return ERROR_UNKNOWN by default
		return ERROR_UNKNOWN;
	}
	
}
