package org.msyu.reinforce.resources;

import java.util.List;

public interface ResourceCollection {

	ResourceIterator getResourceIterator() throws ResourceEnumerationException;

	List<Resource> rebuildCache() throws ResourceEnumerationException;

	Resource getRoot();

	boolean isEmpty() throws ResourceEnumerationException;

}
