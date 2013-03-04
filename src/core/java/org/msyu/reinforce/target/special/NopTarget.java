package org.msyu.reinforce.target.special;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInvocation;

import java.util.Map;

public class NopTarget extends Target {

	public NopTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected void initTarget(Map docMap) {
		Log.debug("Nothing to initialize");
	}

	@Override
	public void run() throws ExecutionException {
		Log.verbose("This is a no-op target, nothing to do");
	}

}
