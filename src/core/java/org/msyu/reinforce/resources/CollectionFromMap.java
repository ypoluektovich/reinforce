package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Reinterpretable;
import org.msyu.reinforce.ReinterpretationException;
import org.msyu.reinforce.util.BooleanFromString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CollectionFromMap {

	private CollectionFromMap() {
		// do not instantiate
	}


	public static ResourceCollection interpret(Map defMap) throws ResourceConstructionException {
		ResourceCollection baseCollection = interpretBase(defMap);
		ResourceFilter resourceFilter = FilterFromMap.interpret(defMap);
		if (resourceFilter == null) {
			Log.debug("No filters, returning base collection");
			return baseCollection;
		} else {
			ResourceCollection collection = new FilteringResourceCollection(baseCollection, resourceFilter);
			Log.debug("Created filtering collection: %s", collection);
			return collection;
		}
	}

	private static ResourceCollection interpretBase(Map defMap) throws ResourceConstructionException {
		Log.debug("Interpreting base collection from map...");
		Object targetObject = defMap.containsKey("target") ? defMap.get("target") : null;
		Object locationObject = defMap.containsKey("location") ? defMap.get("location") : null;
		if ((targetObject == null) == (locationObject == null)) {
			throw new ResourceConstructionException(
					"must specify either target or location in a resource collection map-definition");
		}

		List<Object> matchedItems;
		if (targetObject != null) {
			matchedItems = matchTargets(targetObject, defMap);
		} else /* locationObject != null */ {
			matchedItems = matchLocations(locationObject, defMap);
		}
		Log.debug("Matched %d items...", matchedItems.size());

		if (matchedItems.isEmpty()) {
			boolean allowEmpty = false;
			if (defMap.containsKey("allow empty")) {
				Boolean setting = BooleanFromString.parseUncertain(defMap.get("allow empty"));
				if (setting == null) {
					throw new ResourceConstructionException("'allow empty' must be a boolean");
				}
				allowEmpty = setting;
			}
			if (allowEmpty) {
				Log.debug("Returning an empty collection");
				return EmptyResourceCollection.INSTANCE;
			} else {
				throw new ResourceConstructionException("nothing was matched, and empty result is not allowed");
			}
		}

		if (!matchedItems.isEmpty() && defMap.containsKey("as")) {
			matchedItems = reinterpret(matchedItems, defMap.get("as"));
		}

		return wrapInCollection(matchedItems);
	}

	private static List<Object> matchTargets(Object targetObject, Map defMap) throws ResourceConstructionException {
		Log.debug("Matching targets...");
		if (!(targetObject instanceof String)) {
			throw new ResourceConstructionException("target must be referenced by its name string");
		}

		StringMatcher matcher;
		if (defMap.containsKey("match")) {
			Object matchSetting = defMap.get("match");
			if ("exact".equals(matchSetting)) {
				matcher = new EqualsMatcher((String) targetObject);
			} else if ("regex".equals(matchSetting)) {
				matcher = new RegexMatcher((String) targetObject);
			} else {
				throw new ResourceConstructionException("unsupported target match setting: " + matchSetting);
			}
		} else {
			matcher = new EqualsMatcher((String) targetObject);
		}

		List<Object> matchedTargets = new ArrayList<>();
		for (String targetName : Build.getCurrent().getExecutedTargetNames()) {
			if (matcher.fits(targetName)) {
				matchedTargets.add(Build.getCurrent().getExecutedTarget(targetName));
			}
		}
		return matchedTargets;
	}

	private static interface StringMatcher {

		boolean fits(String string);

	}

	private static class EqualsMatcher implements StringMatcher {

		private final String myTargetString;

		private EqualsMatcher(String targetString) {
			myTargetString = targetString;
		}

		@Override
		public boolean fits(String string) {
			return Objects.equals(myTargetString, string);
		}

	}

	private static class RegexMatcher implements StringMatcher {

		private final Pattern myPattern;

		private RegexMatcher(String pattern) {
			myPattern = Pattern.compile(pattern);
		}

		@Override
		public boolean fits(String string) {
			return myPattern.matcher(string).find();
		}

	}

	private static List<Object> matchLocations(Object locationObject, Map defMap) throws ResourceConstructionException {
		if (locationObject instanceof String) {
			Log.debug("Interpreting location as base collection...");
			return Collections.<Object>singletonList(
					CollectionFromString.interpretLocation((String) locationObject)
			);
		} else {
			throw new ResourceConstructionException("location must be a string (a path in file system)");
		}
	}

	private static List<Object> reinterpret(List<Object> matchedItems, Object interpretationSpec) throws ResourceConstructionException {
		Log.debug("Reinterpreting items...");
		if (!(interpretationSpec instanceof String)) {
			throw new ResourceConstructionException("reinterpretation specification must be a string");
		}
		List<Object> reinterpretedItems = new ArrayList<>(matchedItems.size());
		for (Object matchedItem : matchedItems) {
			reinterpretedItems.add(reinterpret(matchedItem, (String) interpretationSpec));
		}
		return reinterpretedItems;
	}

	private static Object reinterpret(Object matchedItem, String interpretationSpec) throws ResourceConstructionException {
		if (matchedItem instanceof Reinterpretable) {
			try {
				return ((Reinterpretable) matchedItem).reinterpret(interpretationSpec);
			} catch (ReinterpretationException e) {
				throw new ResourceConstructionException(
						"error while reinterpreting " + matchedItem + " as " + interpretationSpec,
						e
				);
			}
		} else {
			throw new ResourceConstructionException("can't reinterpret non-Reinterpretable item: " + matchedItem);
		}
	}

	private static ResourceCollection wrapInCollection(List<Object> items) throws ResourceConstructionException {
		List<ResourceCollection> collections = new ArrayList<>();
		List<Resource> resources = new ArrayList<>();
		for (Object item : items) {
			if (item instanceof ResourceCollection) {
				Log.debug("Treating %s as a resource collection...", item);
				collections.add((ResourceCollection) item);
			} else if (item instanceof Resource) {
				Log.debug("Treating %s as a single resource (to be wrapped later)...", item);
				resources.add((Resource) item);
			} else {
				throw new ResourceConstructionException("unable to cast or wrap " + item + " as a ResourceCollection");
			}
		}
		if (!resources.isEmpty()) {
			Log.debug("Wrapping %d resources in a collection...");
			collections.add(new ResourceListCollection(resources));
		}
		if (collections.size() == 1) {
			Log.debug("Returning a single collection");
			return collections.get(0);
		}
		Log.debug("Returning %d collections wrapped in a union collection", collections.size());
		Map<ResourceCollection, ResourceTranslation> translationMap = new HashMap<>();
		for (ResourceCollection collection : collections) {
			translationMap.put(collection, null);
		}
		return new EagerlyCachingUnionResourceCollection(translationMap);
	}

}
