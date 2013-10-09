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

		BaseCollectionMatcher baseCollectionMatcher = null;
		baseCollectionMatcher = buildMatcherOrDie(defMap, TargetMatcher.Factory.INSTANCE, baseCollectionMatcher);
		baseCollectionMatcher = buildMatcherOrDie(defMap, LocationMatcher.Factory.INSTANCE, baseCollectionMatcher);
		baseCollectionMatcher = buildMatcherOrDie(defMap, UnionMatcher.Factory.INSTANCE, baseCollectionMatcher);

		List<Object> matchedItems = baseCollectionMatcher.match();
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

	private static abstract class BaseCollectionMatcher {

		protected final String myType;

		protected BaseCollectionMatcher(String type) {
			myType = type;
		}

		protected abstract List<Object> match() throws ResourceConstructionException;

	}

	private static BaseCollectionMatcher buildMatcherOrDie(
			Map defMap,
			BaseCollectionMatcherFactory<?> factory,
			BaseCollectionMatcher previousMatcher
	) throws ResourceConstructionException {
		if (!defMap.containsKey(factory.type)) {
			return previousMatcher;
		}
		if (previousMatcher != null) {
			throw new ResourceConstructionException(
					"ambiguous type of map-defined resource collection: " +
							previousMatcher.myType + " or " + factory.type
			);
		}
		return factory.build(defMap.get(factory.type), defMap);
	}

	private static abstract class BaseCollectionMatcherFactory<M extends BaseCollectionMatcher> {

		protected final String type;

		protected BaseCollectionMatcherFactory(String type) {
			this.type = type;
		}

		protected abstract M build(Object setting, Map defMap) throws ResourceConstructionException;

	}

	private static class TargetMatcher extends BaseCollectionMatcher {

		private final InvocationMatcher invocationMatcher;

		protected static class Factory extends BaseCollectionMatcherFactory<TargetMatcher> {

			protected static final Factory INSTANCE = new Factory();

			private Factory() {
				super("target");
			}

			@Override
			protected TargetMatcher build(Object setting, Map defMap) throws ResourceConstructionException {
				return new TargetMatcher(setting, defMap);
			}

		}

		protected TargetMatcher(Object targetObject, Map defMap) throws ResourceConstructionException {
			super(Factory.INSTANCE.type);
			if (!(targetObject instanceof String)) {
				throw new ResourceConstructionException("target must be referenced by its name string");
			}
			if (defMap.containsKey("match")) {
				Object matchSetting = defMap.get("match");
				if ("exact".equals(matchSetting)) {
					invocationMatcher = new EqualsMatcher((String) targetObject);
				} else if ("regex".equals(matchSetting)) {
					invocationMatcher = new RegexMatcher((String) targetObject);
				} else {
					throw new ResourceConstructionException("unsupported target match setting: " + matchSetting);
				}
			} else {
				invocationMatcher = new EqualsMatcher((String) targetObject);
			}
		}

		@Override
		protected List<Object> match() {
			Log.debug("Matching target names with %s...", invocationMatcher);
			List<Object> matchedTargets = new ArrayList<>();
			for (TargetInvocation executedInvocation : Build.getCurrent().getExecutedTargets()) {
				if (invocationMatcher.fits(executedInvocation)) {
					Log.debug("%s fits", executedInvocation);
					matchedTargets.add(Build.getCurrent().getExecutedTarget(executedInvocation));
				} else {
					Log.debug("%s does not fit", executedInvocation);
				}
			}
			return matchedTargets;
		}

	}

	private static interface InvocationMatcher {

		boolean fits(TargetInvocation invocation);

	}

	private static class EqualsMatcher implements InvocationMatcher {

		private final TargetInvocation targetInvocation;

		private EqualsMatcher(String targetString) {
			targetInvocation = TargetInvocation.parse(targetString);
		}

		@Override
		public boolean fits(TargetInvocation invocation) {
			return Objects.equals(targetInvocation, invocation);
		}

		@Override
		public String toString() {
			return this.getClass().getName() + "{" + targetInvocation + "}";
		}

	}

	private static class RegexMatcher implements InvocationMatcher {

		private final Pattern pattern;

		private RegexMatcher(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		@Override
		public boolean fits(TargetInvocation invocation) {
			return pattern.matcher(invocation.toString()).find();
		}

		@Override
		public String toString() {
			return this.getClass().getName() + "{" + pattern.pattern() + "}";
		}
	}

	private static class LocationMatcher extends BaseCollectionMatcher {

		protected static class Factory extends BaseCollectionMatcherFactory<LocationMatcher> {

			protected static final Factory INSTANCE = new Factory();

			private Factory() {
				super("location");
			}

			@Override
			protected LocationMatcher build(Object setting, Map defMap) throws ResourceConstructionException {
				return new LocationMatcher(setting, defMap);
			}

		}

		private final String myLocationString;

		protected LocationMatcher(Object locationSetting, Map defMap) throws ResourceConstructionException {
			super(Factory.INSTANCE.type);
			if (!(locationSetting instanceof String)) {
				throw new ResourceConstructionException("location must be a string (a path in file system)");
			}
			Object expandedLocation;
			try {
				expandedLocation = Variables.expand((String) locationSetting);
			} catch (VariableSubstitutionException e) {
				throw new ResourceConstructionException("error while expanding variables in location setting", e);
			}
			if (!(expandedLocation instanceof String)) {
				throw new ResourceConstructionException("location string was variable-expanded to a non-string");
			}
			myLocationString = (String) expandedLocation;
		}

		@Override
		protected List<Object> match() throws ResourceConstructionException {
			Log.debug("Interpreting location as base collection...");
			List<Object> matchedItems = new ArrayList<>();
			ResourceCollection collection = CollectionFromString.interpretLocation(myLocationString);
			try {
				if (!collection.isEmpty()) {
					matchedItems.add(collection);
				}
			} catch (ResourceEnumerationException e) {
				throw new ResourceConstructionException("couldn't check collection emptiness", e);
			}
			return matchedItems;
		}

	}

	private static class UnionMatcher extends BaseCollectionMatcher {

		protected static class Factory extends BaseCollectionMatcherFactory<UnionMatcher> {

			protected static final Factory INSTANCE = new Factory();

			private Factory() {
				super("union");
			}

			@Override
			protected UnionMatcher build(Object setting, Map defMap) throws ResourceConstructionException {
				return new UnionMatcher(setting);
			}
		}

		private final List myElementDefinitions;

		protected UnionMatcher(Object unionSetting) throws ResourceConstructionException {
			super(Factory.INSTANCE.type);
			if (!(unionSetting instanceof List)) {
				throw new ResourceConstructionException("union resource collection must be a defined by a list");
			}
			myElementDefinitions = (List) unionSetting;
		}

		@Override
		protected List<Object> match() throws ResourceConstructionException {
			Log.debug("Interpreting union as base collection...");
			List<Object> matchedItems = new ArrayList<>();
			for (Object elementDefinition : myElementDefinitions) {
				matchedItems.add(ResourceCollections.interpret(elementDefinition));
			}
			return matchedItems;
		}

	}

	private static List<Object> reinterpret(List<Object> matchedItems, Object interpretationSpec) throws ResourceConstructionException {
		Log.debug("Reinterpreting items (as %s)...", interpretationSpec);
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
