package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionFromList {

	private CollectionFromList() {
		// do not instantiate
	}


	public static ResourceCollection interpret(List defList) throws ResourceConstructionException {
		Log.debug("Interpreting a list...");
		Map<ResourceCollection, ResourceTranslation> translationByCollection = new LinkedHashMap<>();
		Log.debug("List has %d items, iterating...", defList.size());
		for (int i = 0; i < defList.size(); i++) {
			Log.debug("Interpreting item %d...", i + 1);
			translationByCollection.put(Collections.interpret(defList.get(i)), null);
		}
		ResourceCollection collection = new EagerlyCachingUnionResourceCollection(translationByCollection);
		Log.debug("Interpreted all items, creating a union collection: %s", collection);
		return collection;
	}

}
