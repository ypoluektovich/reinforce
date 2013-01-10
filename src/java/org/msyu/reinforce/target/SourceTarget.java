package org.msyu.reinforce.target;

import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.EagerlyCachingFileTreeResourceCollection;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.nio.file.Paths;
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
	public void run() throws BuildException {
		rebuildCache();
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myResourceCollection.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myResourceCollection.rebuildCache();
	}

}
