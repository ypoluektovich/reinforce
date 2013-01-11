package org.msyu.reinforce.resources;

public class IncludeExcludeResourceFilter implements ResourceFilter {

	private final ResourceFilter myIncludeFilter;

	private final ResourceFilter myExcludeFilter;

	public IncludeExcludeResourceFilter(ResourceFilter includeFilter, ResourceFilter excludeFilter) {
		myIncludeFilter = includeFilter;
		myExcludeFilter = excludeFilter;
	}

	@Override
	public boolean fits(Resource resource) throws ResourceEnumerationException {
		if (myExcludeFilter == null) {
			return myIncludeFilter == null || myIncludeFilter.fits(resource);
		} else {
			return !myExcludeFilter.fits(resource) || myIncludeFilter != null && myIncludeFilter.fits(resource);
		}
	}

}
