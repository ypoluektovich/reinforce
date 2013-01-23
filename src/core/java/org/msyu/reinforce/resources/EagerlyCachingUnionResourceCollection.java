package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;
import org.msyu.reinforce.util.FilesUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EagerlyCachingUnionResourceCollection extends AbstractEagerlyCachingResourceCollection {

	private final Map<ResourceCollection, ResourceTranslation> myInnerCollections;

	public EagerlyCachingUnionResourceCollection(Map<ResourceCollection, ResourceTranslation> innerCollections) {
		myInnerCollections = new LinkedHashMap<>(innerCollections);
	}

	@Override
	protected List<Resource> innerRebuildCache() throws ResourceEnumerationException {
		List<Resource> cache = new ArrayList<>();
		Log.debug("Iterating over %d collections in a union", myInnerCollections.keySet().size());
		int collectionIndex = 0;
		for (ResourceCollection innerCollection : myInnerCollections.keySet()) {
			Log.debug("Processing collection #%d: %s", ++collectionIndex, innerCollection);
			ResourceTranslation translation = myInnerCollections.get(innerCollection);
			if (translation == null) {
				Log.debug("No translation will be applied to this collection");
			} else {
				Log.debug("Applying translation: %s", translation);
			}
			ResourceIterator resourceIterator = innerCollection.getResourceIterator();
			Resource innerResource;
			while ((innerResource = resourceIterator.next()) != null) {
				Log.debug("Resource: %s", innerResource);
				Resource translatedResource;
				if (translation != null) {
					translatedResource = translation.translate(innerResource);
					Log.debug("Translated resource: %s", translatedResource);
				} else {
					translatedResource = innerResource;
				}

				if (translatedResource != null && !FilesUtil.EMPTY_PATH.equals(translatedResource.getRelativePath())) {
					cache.add(translatedResource);
				}
			}
		}
		return cache;
	}

}
