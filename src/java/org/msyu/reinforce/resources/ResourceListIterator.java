package org.msyu.reinforce.resources;

import java.util.Iterator;

public class ResourceListIterator implements ResourceIterator {

	private Iterator<Resource> myIterator;

	public ResourceListIterator(Iterator<Resource> resources) {
		myIterator = resources;
	}

	@Override
	public Resource next() {
		if (myIterator == null) {
			return null;
		} else {
			if (myIterator.hasNext()) {
				return myIterator.next();
			} else {
				myIterator = null;
				return null;
			}
		}
	}

}
