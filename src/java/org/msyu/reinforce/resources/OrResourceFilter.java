package org.msyu.reinforce.resources;

public class OrResourceFilter implements ResourceFilter {

	private final Iterable<ResourceFilter> myResourceFilters;

	public OrResourceFilter(Iterable<ResourceFilter> resourceFilters) {
		myResourceFilters = resourceFilters;
	}

	@Override
	public boolean fits(Resource resource) throws ResourceEnumerationException {
		for (ResourceFilter resourceFilter : myResourceFilters) {
			if (resourceFilter.fits(resource)) {
				return true;
			}
		}
		return false;
	}

}
