package org.msyu.reinforce;

public class InvalidTargetNameException extends Exception {

	private final String name;

	public InvalidTargetNameException(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return "Invalid target name: " + (name == null ? "null" : ("'" + name + "'"));
	}

}
