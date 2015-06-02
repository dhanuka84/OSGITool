package org.wso2.carbon.apim.datapumper;


public class APIMRuntimException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public APIMRuntimException() {
		super();
	}

	/**
	 * @param message
	 */
	public APIMRuntimException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public APIMRuntimException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public APIMRuntimException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
