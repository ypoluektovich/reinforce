package org.msyu.reinforce.resources;

import org.msyu.reinforce.Target;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceDefinitionYamlParser {

	public static ResourceCollection parseAsCollection(Object defObject, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		if (defObject == null) {
			throw new ResourceConstructionException("null is not a resource collection");
		} else if (defObject instanceof String) {
			return parseStringAsCollection(((String) defObject), targetByName);
		} else if (defObject instanceof List) {
			return parseListAsCollection(((List) defObject), targetByName);
		} else if (defObject instanceof Map) {
			return parseMapAsCollection(((Map) defObject), targetByName);
		} else {
			throw new ResourceConstructionException("can't parse a resource collection definition out of " +
					defObject.getClass().getName());
		}
	}


	private static ResourceCollection parseStringAsCollection(String defString, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		if (targetByName.containsKey(defString)) {
			return parseTargetAsCollection(defString, targetByName);
		} else {
			return parseStringAsFileCollection(defString);
		}
	}

	private static ResourceCollection parseTargetAsCollection(String defString, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		Target target = targetByName.get(defString);
		if (target instanceof ResourceCollection) {
			return (ResourceCollection) target;
		} else if (target instanceof Resource) {
			return new SingleResourceCollection((Resource) target);
		} else {
			throw new ResourceConstructionException("can't construct a resource collection from target '" +
					defString + "'");
		}
	}

	private static ResourceCollection parseStringAsFileCollection(String defString)
			throws ResourceConstructionException
	{
		try {
			return new EagerlyCachingFileTreeResourceCollection(Paths.get(defString));
		} catch (InvalidPathException e) {
			throw new ResourceConstructionException("path is invalid: " + defString);
		}
	}

	private static ResourceCollection parseListAsCollection(List defList, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		Map<ResourceCollection, ResourceTranslation> translationByCollection = new LinkedHashMap<>();
		for (Object defObject : defList) {
			translationByCollection.put(parseAsCollection(defObject, targetByName), null);
		}
		return new EagerlyCachingUnionResourceCollection(translationByCollection);
	}


	private static ResourceCollection parseMapAsCollection(Map defMap, Map<String, Target> targetByName)
			throws ResourceConstructionException {
		ResourceCollection collection = getBaseCollection(defMap, targetByName);
		Set<ResourceFilter> includes = getResourceFilters(defMap, "include");
		Set<ResourceFilter> excludes = getResourceFilters(defMap, "exclude");
		if (includes.isEmpty() && excludes.isEmpty()) {
			return collection;
		} else {
			return new FilteringResourceCollection(
					collection,
					new IncludeExcludeResourceFilter(
							includes.isEmpty() ? null : new OrResourceFilter(includes),
							excludes.isEmpty() ? null : new OrResourceFilter(excludes)
					)
			);
		}
	}

	private static ResourceCollection getBaseCollection(Map defMap, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		Object targetObject = defMap.containsKey("target") ? defMap.get("target") : null;
		Object locationObject = defMap.containsKey("location") ? defMap.get("location") : null;
		if ((targetObject == null) == (locationObject == null)) {
			throw new ResourceConstructionException(
					"must specify either target or location in a resource collection map-definition");
		}
		if (targetObject != null) {
			if (targetObject instanceof String) {
				return parseTargetAsCollection((String) targetObject, targetByName);
			} else {
				throw new ResourceConstructionException("target must be referenced by its name string");
			}
		} else /* locationObject != null */ {
			if (locationObject instanceof String) {
				return parseStringAsFileCollection((String) locationObject);
			} else {
				throw new ResourceConstructionException("location must be a string (a path in file system)");
			}
		}
	}

	private static Set<ResourceFilter> getResourceFilters(Map defMap, String filterKey)
			throws ResourceConstructionException
	{
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
			throws ResourceConstructionException
	{
		if (!(filterDef instanceof String)) {
			throw new ResourceConstructionException(filterKey + " list of a source must contain only strings");
		}
		return new RegexResourceFilter((String) filterDef);
	}

}
