package soba.core;

import java.io.IOException;

public class ClassReadFailureException extends IOException {

	private static final long serialVersionUID = -5653771195879228138L;

	public ClassReadFailureException(String message) {
		super(message);
	}
	
}
