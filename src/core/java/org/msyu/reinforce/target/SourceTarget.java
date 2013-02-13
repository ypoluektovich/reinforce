package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.EagerlyCachingFileTreeResourceCollection;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class SourceTarget extends Target implements ResourceCollection {

	public static final String LOCATION_KEY = "location";

	private ResourceCollection myResourceCollection;

	public SourceTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!docMap.containsKey(LOCATION_KEY)) {
			throw new TargetInitializationException("missing parameter '" + LOCATION_KEY + "' of source target");
		}
		Object source = docMap.get(LOCATION_KEY);
		if (!(source instanceof String)) {
			throw new TargetInitializationException("parameter '" + LOCATION_KEY + "' of source target must be a string");
		}
		myResourceCollection = new EagerlyCachingFileTreeResourceCollection(Paths.get((String) source));
	}

	@Override
	public void run() throws ExecutionException {
		try {
			rebuildCache();
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating source files", e);
		}
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myResourceCollection.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myResourceCollection.rebuildCache();
	}

	@Override
	public Resource getRoot() {
		return new Resource() {
			@Override
			public Path getPath() {
				return myResourceCollection.getRoot().getPath();
			}

			@Override
			public BasicFileAttributes getAttributes() throws ResourceAccessException {
				return myResourceCollection.getRoot().getAttributes();
			}

			@Override
			public Path getRelativePath() {
				return myResourceCollection.getRoot().getRelativePath();
			}
		};
	}

}
