package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;
import org.msyu.reinforce.interpretation.Reinterpretable;
import org.msyu.reinforce.interpretation.ReinterpretationException;

import java.util.ArrayList;
import java.util.Collection;
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


	public static ResourceCollection asResourceCollection(Object object) throws ResourceConstructionException {
		if (object instanceof Collection) {
			return asResourceCollection((Collection) object);
		}
		ResourceCollection cow = castOrWrap(object);
		if (cow != null) {
			return cow;
		}
		if (object instanceof Reinterpretable) {
			Log.debug("As a last resort, trying to descend the default interpretation chain of %s", object);
			do {
				Object prevObject = object;
				try {
					object = ((Reinterpretable) object).reinterpret(Reinterpretable.DEFAULT_INTERPRETATION_SPEC);
				} catch (ReinterpretationException e) {
					throw new ResourceConstructionException("error while descending default interpretation chain", e);
				}
				if (prevObject == object) {
					break;
				}
				cow = castOrWrap(object);
				if (cow != null) {
					return cow;
				}
			} while (object instanceof Reinterpretable);
		}
		throw new ResourceConstructionException("unable to cast, wrap or interpret " + object + " as a ResourceCollection");
	}

	private static ResourceCollection castOrWrap(Object object) throws ResourceConstructionException {
		Log.debug("Trying to cast %s to a resource or collection...", object);
		if (object instanceof ResourceCollection) {
			Log.debug("%s is a ResourceCollection", object);
			return (ResourceCollection) object;
		}
		if (object instanceof Resource) {
			ResourceCollection rc = new SingleResourceCollection((Resource) object);
			Log.debug("%s is a Resource; wrapped in collection %s", object, rc);
			return rc;
		}
		return null;
	}

	public static ResourceCollection asResourceCollection(Collection<?> items) throws ResourceConstructionException {
		Log.debug("Casting a POJO collection to a resource collection...");
		if (items.isEmpty()) {
			Log.debug("Casting empty list to empty collection");
			return EmptyResourceCollection.INSTANCE;
		}
		List<ResourceCollection> collections = new ArrayList<>();
		for (Object item : items) {
			collections.add(asResourceCollection(item));
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
