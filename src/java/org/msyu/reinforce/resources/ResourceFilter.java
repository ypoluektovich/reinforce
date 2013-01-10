package org.msyu.reinforce.resources;

public interface ResourceFilter {

	boolean fits(Resource resource) throws ResourceEnumerationException;

}
