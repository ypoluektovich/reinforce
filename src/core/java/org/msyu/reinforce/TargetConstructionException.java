package org.msyu.reinforce;

public class TargetConstructionException extends TargetLoadingException {

	private String targetName = "(unknown name)";

	public TargetConstructionException(String message) {
		super(message);
	}

	public TargetConstructionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetConstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}


	@Override
	public String getMessage() {
		return "Could not construct target " + targetName + ": " + super.getMessage();
	}

}
