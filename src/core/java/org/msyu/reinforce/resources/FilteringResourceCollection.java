package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

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
		Log.debug("Acquiring iterator for inner (filtered) collection: %s", myInnerCollection);
		final ResourceIterator innerIterator = myInnerCollection.getResourceIterator();
		Log.debug("Applying filter %s", myResourceFilter);
		return new ResourceIterator() {
			@Override
			public Resource next() throws ResourceEnumerationException {
				Resource nextResource;
				while ((nextResource = innerIterator.next()) != null) {
					Log.debug("Applying filter to resource %s...", nextResource);
					if (myResourceFilter.fits(nextResource)) {
						Log.debug("Resource fits");
						return nextResource;
					}
					Log.debug("Resource does not fit");
				}
				return null;
			}
		};
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return null;
	}

	@Override
	public Resource getRoot() {
		return null;
	}

	@Override
	public boolean isEmpty() throws ResourceEnumerationException {
		return getResourceIterator().next() == null;
	}

}
