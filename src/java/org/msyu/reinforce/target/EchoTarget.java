package org.msyu.reinforce.target;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;

import java.util.Map;

public class EchoTarget extends Target {

	private String message;

	public EchoTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		Object message = docMap.get("message");
		if (!(message instanceof String)) {
			throw new TargetInitializationException("required parameter 'message' of echo target must be a string");
		}
		this.message = (String) message;
	}

	@Override
	public void run() {
		System.out.println(message);
	}

}
