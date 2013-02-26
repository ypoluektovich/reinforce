package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceCollections {

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

	public static ResourceCollection wrapInCollection(List<Object> items) throws ResourceConstructionException {
		List<ResourceCollection> collections = new ArrayList<>();
		List<Resource> resources = new ArrayList<>();
		for (Object item : items) {
			if (item instanceof ResourceCollection) {
				Log.debug("Treating %s as a resource collection...", item);
				collections.add((ResourceCollection) item);
			} else if (item instanceof Resource) {
				Log.debug("Treating %s as a single resource (to be wrapped later)...", item);
				resources.add((Resource) item);
			} else {
				throw new ResourceConstructionException("unable to cast or wrap " + item + " as a ResourceCollection");
			}
		}
		if (!resources.isEmpty()) {
			Log.debug("Wrapping %d resources in a collection...", resources.size());
			collections.add(new ResourceListCollection(resources));
		}
		if (collections.size() == 1) {
			Log.debug("Returning a single collection");
			return collections.get(0);
		}
		Log.debug("Returning %d collections wrapped in a union collection", collections.size());
		Map<ResourceCollection, ResourceTranslation> translationMap = new HashMap<>();
		for (ResourceCollection collection : collections) {
			translationMap.put(collection, null);
		}
		return new EagerlyCachingUnionResourceCollection(translationMap);
	}

}
