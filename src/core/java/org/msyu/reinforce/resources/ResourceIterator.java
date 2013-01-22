package org.msyu.reinforce.resources;

public interface ResourceIterator {

	Resource next() throws ResourceEnumerationException;

}
