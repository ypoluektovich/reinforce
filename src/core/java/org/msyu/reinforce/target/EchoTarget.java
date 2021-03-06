package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;

import java.util.Map;

public class EchoTarget extends Target {

	private String message;

	public EchoTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected void initTarget(Map docMap) throws TargetInitializationException {
		Object message = docMap.get("message");
		if (!(message instanceof String)) {
			throw new TargetInitializationException("required parameter 'message' of echo target must be a string");
		}
		this.message = (String) message;
	}

	@Override
	public void run() throws ExecutionException {
		System.out.println(message);
	}

}
