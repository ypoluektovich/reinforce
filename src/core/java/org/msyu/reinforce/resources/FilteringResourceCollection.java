package org.msyu.reinforce.resources;

import java.util.List;

public class FilteringResourceCollection implements ResourceCollection {

	private final ResourceCollection myInnerCollection;

	private final ResourceFilter myResourceFilter;

	public FilteringResourceCollection(
			ResourceCollection innerCollection,
			ResourceFilter resourceFilter
	) {
		myInnerCollection = innerCollection;
		myResourceFilter = resourceFilter;
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return new ResourceIterator() {

			private final ResourceIterator innerIterator = myInnerCollection.getResourceIterator();

			@Override
			public Resource next() throws ResourceEnumerationException {
				Resource nextResource;
				while ((nextResource = innerIterator.next()) != null) {
					if (myResourceFilter.fits(nextResource)) {
						return nextResource;
					}
				}
				return null;
			}

		};
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return null;
	}

}
