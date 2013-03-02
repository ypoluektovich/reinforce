package org.msyu.reinforce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class TargetFactories {

	private static final List<TargetFactory> ourFactories = new ArrayList<>();

	static {
		Log.verbose("Loading target factories");
		Iterator<TargetFactory> factoryLoader = ServiceLoader.load(TargetFactory.class).iterator();
		ServiceConfigurationError firstError = null;
		boolean hasNext = true;
		while (hasNext) {
			try {
				hasNext = factoryLoader.hasNext();
				if (hasNext) {
					TargetFactory factory = factoryLoader.next();
					Log.debug("Loaded factory: %s", factory);
					ourFactories.add(factory);
				}
			} catch (ServiceConfigurationError e) {
				if (firstError == null) {
					firstError = e;
				}
				Log.error("Error while loading target factories");
				Log.stackTrace(e);
			}
		}
		if (firstError != null) {
			throw firstError;
		}
	}

	public static Target createTargetObject(String targetType, TargetInvocation targetInvocation) throws TargetConstructionException {
		for (TargetFactory factory : ourFactories) {
			Target targetObject = factory.createTargetObject(targetType, targetInvocation);
			if (targetObject != null) {
				return targetObject;
			}
		}
		throw new TargetConstructionException("target type '" + targetType + "' is unknown");
	}

}
