package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.util.List;
import java.util.Map;

public class Collections {

	public static ResourceCollection interpret(Object defObject) throws ResourceConstructionException {
		if (defObject == null) {
			Log.debug("Interpreting a null as an empty collection");
			return EmptyResourceCollection.INSTANCE;
		} else if (defObject instanceof String) {
			Log.debug("Interpreting a string: '%s'...", defObject);
			return CollectionFromString.interpret(((String) defObject));
		} else if (defObject instanceof List) {
			Log.debug("Interpreting a list...");
			return CollectionFromList.interpret(((List) defObject));
		} else if (defObject instanceof Map) {
			Log.debug("Interpreting a map...");
			return CollectionFromMap.interpret(((Map) defObject));
		} else {
			throw new ResourceConstructionException("can't parse a resource collection definition out of " +
					defObject.getClass().getName());
		}
	}

}
