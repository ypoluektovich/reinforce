package org.msyu.reinforce.resources;

public class EmptyResourceIterator implements ResourceIterator {

	public static final EmptyResourceIterator INSTANCE = new EmptyResourceIterator();

	@Override
	public Resource next() throws ResourceEnumerationException {
		return null;
	}

}
