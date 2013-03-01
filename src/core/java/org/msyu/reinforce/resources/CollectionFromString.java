package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CollectionFromString {

	private CollectionFromString() {
		// do not instantiate
	}


	public static ResourceCollection interpret(String defString) throws ResourceConstructionException {
		Log.debug("Interpreting a string: '%s'...", defString);
		try {
			defString = Variables.expand(defString);
		} catch (VariableSubstitutionException e) {
			throw new ResourceConstructionException("error while expanding variables in string: " + defString, e);
		}
		Log.debug("String after variable expansion: '%s'...", defString);
		if (Build.getCurrent().getExecutedTargetNames().contains(defString)) {
			return interpretTarget(defString);
		} else {
			return interpretLocation(defString);
		}
	}

	private static ResourceCollection interpretTarget(String defString) throws ResourceConstructionException {
		Log.debug("Interpreting a string as a target name...");
		Target target = Build.getCurrent().getExecutedTarget(defString);
		if (target instanceof ResourceCollection) {
			Log.debug("Target is a resource collection");
			return (ResourceCollection) target;
		} else if (target instanceof Resource) {
			ResourceCollection collection = new SingleResourceCollection((Resource) target);
			Log.debug("Target is a single resource; wrapping in a collection: %s", collection);
			return collection;
		} else {
			throw new ResourceConstructionException("can't interpret target '" + defString + "' as a resource collection");
		}
	}

	static ResourceCollection interpretLocation(String defString) throws ResourceConstructionException {
		Log.debug("Interpreting a string as a file system location...");

		Path rootPath;
		try {
			rootPath = Paths.get(defString);
		} catch (InvalidPathException e) {
			throw new ResourceConstructionException("path is invalid: " + defString);
		}

		return FileCollections.fromPath(rootPath);
	}

}
