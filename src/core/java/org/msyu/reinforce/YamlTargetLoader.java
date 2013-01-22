package org.msyu.reinforce;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class YamlTargetLoader implements TargetRepository {

	public static final Pattern NAME_PATTERN = Pattern.compile("[-\\w]+");

	protected static final String TARGET_DEPENDENCY_KEY = "depends-on";

	private final Map<String, Target> targets = new HashMap<>();

	private final TargetDefinitionStreamSource myTargetDefinitionStreamSource;

	public YamlTargetLoader(TargetDefinitionStreamSource targetDefinitionStreamSource) {
		this.myTargetDefinitionStreamSource = targetDefinitionStreamSource;
	}

	public Map<String, Target> load(Iterable<String> targetNames)
			throws InvalidTargetNameException, TargetLoadingException
	{
		HashMap<String, Target> targetByName = new HashMap<>();
		for (String targetName : targetNames) {
			targetByName.put(targetName, load(targetName));
		}
		return targetByName;
	}

	public Target load(String targetName) throws InvalidTargetNameException, TargetLoadingException {
		Log.verbose("== Loading target: %s", targetName);
		checkName(targetName);
		return targets.containsKey(targetName) ?
				getPreloadedTarget(targetName) :
				getNewTarget(targetName);
	}

	private static void checkName(String name) throws InvalidTargetNameException {
		Log.debug("Checking target name");
		if (name == null || !NAME_PATTERN.matcher(name).matches()) {
			Log.debug("Target name invalid; throwing");
			throw new InvalidTargetNameException(name);
		}
	}

	private Target getPreloadedTarget(String targetName) throws CyclicTargetDependencyException {
		Target target = targets.get(targetName);
		if (target == null) {
			Log.verbose("Detected cyclic dependency; throwing");
			throw new CyclicTargetDependencyException(targetName);
		}
		Log.verbose("== Target has already been loaded");
		return target;
	}

	private Target getNewTarget(String targetName) throws InvalidTargetNameException, TargetLoadingException {
		Log.verbose("Loading target from YAML definition");
		Object targetDefinition = loadYamlDefinition(targetName);
		if (!(targetDefinition instanceof Map)) {
			throw new TargetConstructionException("target definition document must be a map");
		}
		Target target = constructTarget(targetName, (Map) targetDefinition);
		Log.verbose("== Target successfully loaded: %s", targetName);
		return target;
	}

	private Object loadYamlDefinition(String targetName) throws TargetDefinitionLoadingException {
		Log.debug("Requesting definition stream from source");
		try (InputStream targetDefStream = myTargetDefinitionStreamSource.getStreamForTarget(targetName + ".yaml")) {
			if (targetDefStream == null) {
				Log.verbose("Target definition not found; throwing");
				throw new TargetDefinitionNotFoundException(targetName);
			}
			Log.debug("Parsing YAML stream");
			return new Yaml(new SafeConstructor()).load(targetDefStream);
		} catch (IOException | YAMLException e) {
			Log.debug("Error while loading definition; throwing");
			throw new TargetDefinitionLoadingException(e);
		}
	}

	private Target constructTarget(String targetName, Map docMap)
			throws InvalidTargetNameException, TargetLoadingException
	{
		Log.debug("Starting target object construction");
		targets.put(targetName, null);

		Target target;
		try {
			Set<String> dependencyTargetNames = docMap.containsKey(TARGET_DEPENDENCY_KEY) ?
					parseAndLoadTargetDependencies(docMap.get(TARGET_DEPENDENCY_KEY)) :
					Collections.<String>emptySet();

			target = createTargetObject(docMap, targetName);

			target.setDependencyTargetNames(dependencyTargetNames);

			target.initTarget(docMap, createDependencyTargetMap(target));
		} catch (TargetConstructionException e) {
			e.setTargetName(targetName);
			throw e;
		}

		targets.put(targetName, target);
		return target;
	}

	private Target createTargetObject(Map docMap, String targetName) throws TargetConstructionException {
		Object typeObject = docMap.get("type");
		if (!(typeObject instanceof String)) {
			throw new TargetConstructionException("target must declare its type as a string");
		}
		return TargetFactories.createTargetObject((String) typeObject, targetName);
	}

	private Set<String> parseAndLoadTargetDependencies(Object depList) throws TargetConstructionException {
		if (depList == null) {
			return Collections.emptySet();
		}
		Log.verbose("Loading dependency targets");
		HashSet<String> dependencyNames = new HashSet<>();

		if (depList instanceof String) {
			parseAndLoadDependency(dependencyNames, depList);
		} else if (depList instanceof List) {
			for (Object dependencyObject : (List) depList) {
				parseAndLoadDependency(dependencyNames, dependencyObject);
			}
		} else {
			throw new TargetConstructionException("dependency list is invalid");
		}

		Log.verbose("Finished loading dependencies");
		return dependencyNames;
	}

	private void parseAndLoadDependency(HashSet<String> dependencyNames, Object dependencyObject) throws TargetConstructionException {
		if (!(dependencyObject instanceof String)) {
			throw new TargetConstructionException("dependencies must be referenced by their string names");
		}
		String dependencyString = (String) dependencyObject;
		try {
			load(dependencyString);
		} catch (InvalidTargetNameException | TargetLoadingException e) {
			throw new TargetConstructionException("failed to load dependency targets", e);
		}
		dependencyNames.add(dependencyString);
	}

	private Map<String, Target> createDependencyTargetMap(Target target) {
		Set<String> dependencyTargetNames = target.getDependencyTargetNames();
		if (dependencyTargetNames.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Target> depTargetByName = new HashMap<>();
		for (String dependencyTargetName : dependencyTargetNames) {
			depTargetByName.put(dependencyTargetName, targets.get(dependencyTargetName));
		}
		return Collections.unmodifiableMap(depTargetByName);
	}

	@Override
	public Target getTarget(String name) throws NoSuchTargetException {
		if (!targets.containsKey(name)) {
			throw new NoSuchTargetException(name);
		}
		return targets.get(name);
	}

}
