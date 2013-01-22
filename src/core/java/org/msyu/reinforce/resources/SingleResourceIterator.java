package org.msyu.reinforce.resources;

import java.util.concurrent.atomic.AtomicReference;

public class SingleResourceIterator implements ResourceIterator {

	private final AtomicReference<Resource> myResource;

	public SingleResourceIterator(Resource resource) {
		myResource = new AtomicReference<>(resource);
	}

	@Override
	public Resource next() throws ResourceEnumerationException {
		return myResource.getAndSet(null);
	}

}
