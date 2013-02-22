package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterFromMap {

	private FilterFromMap() {
		// do not instantiate
	}


	public static ResourceFilter interpret(Map defMap) throws ResourceConstructionException {
		return interpretIncludeExclude(defMap);
	}

	public static IncludeExcludeResourceFilter interpretIncludeExclude(Map defMap) throws ResourceConstructionException {
		Set<ResourceFilter> includes = getResourceFilters(defMap, "include");
		Set<ResourceFilter> excludes = getResourceFilters(defMap, "exclude");
		return (includes.isEmpty() && excludes.isEmpty()) ?
				null :
				new IncludeExcludeResourceFilter(
						includes.isEmpty() ? null : new OrResourceFilter(includes),
						excludes.isEmpty() ? null : new OrResourceFilter(excludes)
				);
	}

	private static Set<ResourceFilter> getResourceFilters(Map defMap, String filterKey)
			throws ResourceConstructionException {
		Log.debug("Interpreting %s filter from map...", filterKey);
		Set<ResourceFilter> individualFilters = new HashSet<>();
		if (defMap.containsKey(filterKey)) {
			Object filterList = defMap.get(filterKey);
			if (filterList instanceof String) {
				individualFilters.add(getRegexResourceFilter(filterList, filterKey));
			} else if (filterList instanceof List) {
				for (Object filterDef : (List) filterList) {
					individualFilters.add(getRegexResourceFilter(filterDef, filterKey));
				}
			} else {
				throw new ResourceConstructionException(filterKey +
						" directive of a source must be a string or a list of strings");
			}
		}
		return individualFilters;
	}

	private static RegexResourceFilter getRegexResourceFilter(Object filterDef, String filterKey)
			throws ResourceConstructionException {
		if (!(filterDef instanceof String)) {
			throw new ResourceConstructionException(filterKey + " list of a source must contain only strings");
		}
		return new RegexResourceFilter((String) filterDef);
	}

}
