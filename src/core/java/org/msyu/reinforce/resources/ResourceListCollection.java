package org.msyu.reinforce.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceListCollection implements ResourceCollection {

	private final List<Resource> myResources;

	public ResourceListCollection(List<Resource> resources) throws NullPointerException {
		if (resources == null) {
			throw new NullPointerException("resource list must be nonnull");
		}
		for (Resource resource : resources) {
			if (resource == null) {
				throw new NullPointerException("resources in the resource list must be nonnull");
			}
		}
		myResources = new ArrayList<>(resources);
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return new ResourceListIterator(myResources.iterator());
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return Collections.unmodifiableList(myResources);
	}

	@Override
	public Resource getRoot() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return myResources.isEmpty();
	}

}
