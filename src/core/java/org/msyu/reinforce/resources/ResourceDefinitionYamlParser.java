package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;
import org.msyu.reinforce.ReinterpretationException;
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
			Log.debug("Interpreting a string: '%s'...", defObject);
			return parseStringAsCollection(((String) defObject), targetByName);
		} else if (defObject instanceof List) {
			Log.debug("Interpreting a list...");
			return parseListAsCollection(((List) defObject), targetByName);
		} else if (defObject instanceof Map) {
			Log.debug("Interpreting a map...");
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
			Log.debug("Interpreting a string as a target...");
			return parseTargetAsCollection(defString, targetByName);
		} else {
			Log.debug("Interpreting a string as a file collection...");
			return parseStringAsFileCollection(defString);
		}
	}

	private static ResourceCollection parseTargetAsCollection(String defString, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		if (!targetByName.containsKey(defString)) {
			throw new ResourceConstructionException("target '" + defString + "' is unavailable");
		}
		Target target = targetByName.get(defString);
		if (target instanceof ResourceCollection) {
			Log.debug("Target is a resource collection");
			return (ResourceCollection) target;
		} else if (target instanceof Resource) {
			ResourceCollection collection = new SingleResourceCollection((Resource) target);
			Log.debug("Target is a single resource; wrapping in a collection: %s", collection);
			return collection;
		} else {
			throw new ResourceConstructionException("can't interpret target '" + defString +
					"' as a resource collection");
		}
	}

	private static ResourceCollection parseStringAsFileCollection(String defString)
			throws ResourceConstructionException
	{
		try {
			ResourceCollection collection = new EagerlyCachingFileTreeResourceCollection(Paths.get(defString));
			Log.debug("Creating a file tree resource collection: %s", collection);
			return collection;
		} catch (InvalidPathException e) {
			throw new ResourceConstructionException("path is invalid: " + defString);
		}
	}

	private static ResourceCollection parseListAsCollection(List defList, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		Map<ResourceCollection, ResourceTranslation> translationByCollection = new LinkedHashMap<>();
		Log.debug("List has %d items, iterating...", defList.size());
		for (int i = 0; i < defList.size(); i++) {
			Log.debug("Interpreting item %d...", i + 1);
			translationByCollection.put(parseAsCollection(defList.get(i), targetByName), null);
		}
		ResourceCollection collection = new EagerlyCachingUnionResourceCollection(translationByCollection);
		Log.debug("Interpreted all items, creating a union collection: %s", collection);
		return collection;
	}


	private static ResourceCollection parseMapAsCollection(Map defMap, Map<String, Target> targetByName)
			throws ResourceConstructionException {
		ResourceCollection baseCollection = getBaseCollection(defMap, targetByName);
		IncludeExcludeResourceFilter resourceFilter = getIncludeExcludeFilter(defMap);
		if (resourceFilter == null) {
			Log.debug("No filters, returning base collection");
			return baseCollection;
		} else {
			ResourceCollection collection = new FilteringResourceCollection(baseCollection, resourceFilter);
			Log.debug("Creating filtering collection: %s", collection);
			return collection;
		}
	}

	private static ResourceCollection getBaseCollection(Map defMap, Map<String, Target> targetByName)
			throws ResourceConstructionException
	{
		Log.debug("Interpreting base collection from map...");
		Object targetObject = defMap.containsKey("target") ? defMap.get("target") : null;
		Object locationObject = defMap.containsKey("location") ? defMap.get("location") : null;
		if ((targetObject == null) == (locationObject == null)) {
			throw new ResourceConstructionException(
					"must specify either target or location in a resource collection map-definition");
		}
		if (targetObject != null) {
			if (!(targetObject instanceof String)) {
				throw new ResourceConstructionException("target must be referenced by its name string");
			}

			if (!defMap.containsKey("as")) {
				Log.debug("Interpreting target as base collection...");
				return parseTargetAsCollection((String) targetObject, targetByName);
			}

			Log.debug("Reinterpreting target as base collection...");
			if (!targetByName.containsKey(targetObject)) {
				throw new ResourceConstructionException("target '" + targetObject + "' is unavailable");
			}
			Target target = targetByName.get(targetObject);

			Object interpretationSpec = defMap.get("as");
			if (!(interpretationSpec instanceof String)) {
				throw new ResourceConstructionException("target translation spec must be a string");
			}

			Object reinterpreted;
			try {
				reinterpreted = target.reinterpret((String) interpretationSpec);
			} catch (ReinterpretationException e) {
				throw new ResourceConstructionException(
						"error while translating target " + target + " as " + interpretationSpec,
						e
				);
			}
			if (reinterpreted instanceof ResourceCollection) {
				Log.debug("Reinterpreted as a resource collection");
				return (ResourceCollection) reinterpreted;
			} else if (reinterpreted instanceof Resource) {
				SingleResourceCollection collection = new SingleResourceCollection((Resource) reinterpreted);
				Log.debug("Reinterpreted as a single resource; wrapping in a collection: %s", collection);
				return collection;
			} else {
				throw new ResourceConstructionException("can't interpret " + reinterpreted +
						" as a resource collection");
			}
		} else /* locationObject != null */ {
			if (locationObject instanceof String) {
				Log.debug("Interpreting location as base collection...");
				return parseStringAsFileCollection((String) locationObject);
			} else {
				throw new ResourceConstructionException("location must be a string (a path in file system)");
			}
		}
	}

	public static IncludeExcludeResourceFilter getIncludeExcludeFilter(Map defMap) throws ResourceConstructionException {
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
			throws ResourceConstructionException
	{
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
			throws ResourceConstructionException
	{
		if (!(filterDef instanceof String)) {
			throw new ResourceConstructionException(filterKey + " list of a source must contain only strings");
		}
		return new RegexResourceFilter((String) filterDef);
	}

}
