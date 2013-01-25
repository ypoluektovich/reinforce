package org.msyu.reinforce.target.archive;

import java.io.IOException;

public class DuplicateEntryException extends IOException {

	public DuplicateEntryException(String message) {
		super(message);
	}

	@Override
	public String getMessage() {
		return "can't overwrite archive entry: " + super.getMessage();
	}

}
