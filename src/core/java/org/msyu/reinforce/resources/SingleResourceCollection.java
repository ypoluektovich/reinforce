package org.msyu.reinforce.resources;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

public class SingleResourceCollection implements Resource, ResourceCollection {

	private final Resource myResource;

	public SingleResourceCollection(Resource resource) {
		myResource = resource;
	}

	@Override
	public Path getPath() {
		return myResource.getPath();
	}

	@Override
	public BasicFileAttributes getAttributes() throws ResourceAccessException {
		return myResource.getAttributes();
	}

	@Override
	public Path getRelativePath() {
		return myResource.getRelativePath();
	}

	@Override
	public ResourceIterator getResourceIterator() {
		return new SingleResourceIterator(myResource);
	}

	@Override
	public List<Resource> rebuildCache() {
		return Collections.singletonList(myResource);
	}

}
