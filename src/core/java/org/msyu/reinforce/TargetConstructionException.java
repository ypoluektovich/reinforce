package org.msyu.reinforce;

public class TargetConstructionException extends TargetLoadingException {

	private TargetInvocation myTargetInvocation;

	public TargetConstructionException(String message) {
		super(message);
	}

	public TargetConstructionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetConstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


	public void setInvocation(TargetInvocation invocation) {
		myTargetInvocation = invocation;
	}


	@Override
	public String getMessage() {
		return "Could not construct target " +
				(myTargetInvocation != null ? myTargetInvocation : "(unknown invocation)") +
				": " + super.getMessage();
	}

}
