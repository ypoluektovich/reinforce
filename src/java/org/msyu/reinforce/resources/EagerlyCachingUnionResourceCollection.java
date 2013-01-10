package org.msyu.reinforce.resources;

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
		for (ResourceCollection innerCollection : myInnerCollections.keySet()) {
			ResourceTranslation translation = myInnerCollections.get(innerCollection);
			ResourceIterator resourceIterator = innerCollection.getResourceIterator();
			Resource innerResource;
			while ((innerResource = resourceIterator.next()) != null) {
				Resource translatedResource = (translation != null) ?
						translation.translate(innerResource) :
						innerResource;
				if (translatedResource != null && !FilesUtil.EMPTY_PATH.equals(translatedResource.getRelativePath())) {
					cache.add(translatedResource);
				}
			}
		}
		return cache;
	}

}
