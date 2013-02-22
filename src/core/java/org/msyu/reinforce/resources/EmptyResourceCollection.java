package org.msyu.reinforce.resources;

import java.util.List;

public class EmptyResourceCollection implements ResourceCollection {

	public static final EmptyResourceCollection INSTANCE = new EmptyResourceCollection();

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return EmptyResourceIterator.INSTANCE;
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return null;
	}

	@Override
	public Resource getRoot() {
		return null;
	}

}
