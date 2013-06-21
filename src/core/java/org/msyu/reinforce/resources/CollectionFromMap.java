package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.interpretation.Reinterpret;
import org.msyu.reinforce.interpretation.ReinterpretationException;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CollectionFromMap {

	public static final String ALLOW_EMPTY_KEY = "allow empty";

	private CollectionFromMap() {
		// do not instantiate
	}


	public static ResourceCollection interpret(Map defMap) throws ResourceConstructionException {
		Log.debug("Interpreting a map...");
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
			if (defMap.containsKey(ALLOW_EMPTY_KEY)) {
				Object setting = defMap.get(ALLOW_EMPTY_KEY);
				if (setting instanceof Boolean) {
					allowEmpty = (boolean) setting;
				} else {
					throw new ResourceConstructionException("'" + ALLOW_EMPTY_KEY + "' must be a boolean");
				}
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

		return ResourceCollections.asResourceCollection(matchedItems);
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
		Log.debug("Matching target names with %s", matcher);
		for (TargetInvocation executedInvocation : Build.getCurrent().getExecutedTargets()) {
			if (matcher.fits(executedInvocation.getTargetName())) {
				Log.debug("%s fits", executedInvocation);
				matchedTargets.add(Build.getCurrent().getExecutedTarget(executedInvocation));
			} else {
				Log.debug("%s does not fit", executedInvocation);
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

		@Override
		public String toString() {
			return this.getClass().getName() + "{" + myTargetString + "}";
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

		@Override
		public String toString() {
			return this.getClass().getName() + "{" + myPattern.pattern() + "}";
		}
	}

	private static List<Object> matchLocations(Object locationSetting, Map defMap) throws ResourceConstructionException {
		Log.debug("Interpreting location as base collection...");
		if (!(locationSetting instanceof String)) {
			throw new ResourceConstructionException("location must be a string (a path in file system)");
		}
		String locationString;
		try {
			locationString = Variables.expand((String) locationSetting);
		} catch (VariableSubstitutionException e) {
			throw new ResourceConstructionException("error while expanding variables in location setting", e);
		}
		List<Object> matchedItems = new ArrayList<>();
		ResourceCollection collection = CollectionFromString.interpretLocation(locationString);
		try {
			if (!collection.isEmpty()) {
				matchedItems.add(collection);
			}
		} catch (ResourceEnumerationException e) {
			throw new ResourceConstructionException("couldn't check collection emptiness", e);
		}
		return matchedItems;
	}

	private static List<Object> reinterpret(List<Object> matchedItems, Object interpretationSpec) throws ResourceConstructionException {
		Log.debug("Reinterpreting items...");
		List<Object> reinterpretedItems = new ArrayList<>(matchedItems.size());
		for (Object matchedItem : matchedItems) {
			try {
				reinterpretedItems.add(Reinterpret.reinterpret(matchedItem, interpretationSpec));
			} catch (ReinterpretationException e) {
				throw new ResourceConstructionException(
						"error while reinterpreting " + matchedItem + " as " + interpretationSpec,
						e
				);
			}
		}
		return reinterpretedItems;
	}

}
