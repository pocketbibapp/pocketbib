package edu.kit.tm.telematics.pocketbib.model;

/**
 * A server response.
 * @param <T> type for the actual data
 */
public class Response<T> {
	
	/** the server response code */
	private ResponseCode code;
	
	/** the actual data */
	private T data;
	
	/**
	 * Creates a new Response.
	 * @param code the response code
	 * @param data the actual data
	 */
	public Response(ResponseCode code, T data) {
		this.code = code;
		this.data = data;
	}
	
	/**
	 * Returns the response code
	 * @return the response code
	 */
	public ResponseCode getResponseCode() {
		return code;
	}
	
	/**
	 * Returns the actual data
	 * @return the actual data
	 */
	public T getData() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Response) {
			Response<?> r = (Response<?>) o;
			return r.code == code && r.data.equals(data);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 13 * code.getErrorCode() + data.hashCode();
	}

	@Override
	public String toString() {
		String dataString = data.toString();
		
		if(dataString.length() > 33) 
			dataString = dataString.substring(0, 30) + "...";
		
		return "Response [code=" + code + " data=" + dataString + "]";
	}
	
	

}
