package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.util.List;
import java.util.Map;

public class Collections {

	public static ResourceCollection interpret(Object defObject) throws ResourceConstructionException {
		if (defObject == null) {
			return interpretNull();
		} else if (defObject instanceof String) {
			return CollectionFromString.interpret(((String) defObject));
		} else if (defObject instanceof List) {
			return CollectionFromList.interpret(((List) defObject));
		} else if (defObject instanceof Map) {
			return CollectionFromMap.interpret(((Map) defObject));
		} else {
			throw new ResourceConstructionException("can't parse a resource collection definition out of " +
					defObject.getClass().getName());
		}
	}

	private static ResourceCollection interpretNull() {
		Log.debug("Interpreting a null as an empty collection");
		return EmptyResourceCollection.INSTANCE;
	}

}
