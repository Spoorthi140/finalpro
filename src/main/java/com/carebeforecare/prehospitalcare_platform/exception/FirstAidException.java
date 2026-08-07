package com.carebeforecare.prehospitalcare_platform.exception;

public class FirstAidException extends RuntimeException {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public FirstAidException(String message) {
        super(message);
    }

    public FirstAidException(String message, Throwable cause) {
        super(message, cause);
    }
}