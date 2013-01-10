package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public abstract class AbstractEagerlyCachingResourceCollection implements ResourceCollection {

	private final WeakHashMap<Build, List<Resource>> myCachedResources = new WeakHashMap<>();

	@Override
	public final ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		Build currentBuild = Build.getCurrent();
		return new ResourceListIterator(
				(myCachedResources.containsKey(currentBuild) ?
						myCachedResources.get(currentBuild) :
						rebuildCache()
				).iterator()
		);
	}

	@Override
	public final List<Resource> rebuildCache() throws ResourceEnumerationException {
		List<Resource> resources = Collections.unmodifiableList(new ArrayList<>(innerRebuildCache()));
		myCachedResources.put(Build.getCurrent(), resources);
		return resources;
	}

	protected abstract List<Resource> innerRebuildCache() throws ResourceEnumerationException;

}
