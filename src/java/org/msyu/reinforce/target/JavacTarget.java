package org.msyu.reinforce.target;

import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.EagerlyCachingFileTreeResourceCollection;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceDefinitionYamlParser;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.util.FilesUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavacTarget extends Target implements ResourceCollection {

	public static final String SOURCE_KEY = "source";

	private ResourceCollection mySources;

	private Object compiler;

	private Method compile;

	private ResourceCollection myClassFiles;

	public JavacTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName)
			throws TargetInitializationException
	{
		assignSourceTarget(docMap, dependencyTargetByName);
		prepareCompiler();
	}

	private void assignSourceTarget(Map docMap, Map<String, Target> dependencyTargetByName)
			throws TargetInitializationException
	{
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		mySources = ResourceDefinitionYamlParser.parseAsCollection(docMap.get(SOURCE_KEY), dependencyTargetByName);
	}

	@SuppressWarnings("unchecked")
	private void prepareCompiler() throws TargetInitializationException {
		try {
			Class c = Class.forName("com.sun.tools.javac.Main");
			compiler = c.newInstance();
			compile = c.getMethod("compile", new Class[]{(new String[]{}).getClass()});
		} catch (ClassNotFoundException e) {
			throw new TargetInitializationException("failed to access compiler class", e);
		} catch (InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
			throw new TargetInitializationException("failed to acquire compiler instance", e);
		} catch (NoSuchMethodException e) {
			throw new TargetInitializationException("available compiler is incompatible", e);
		}
	}

	@Override
	public void run() throws BuildException {
		String destinationDir = "build/" + getName();
		Log.verbose("Working in directory %s", destinationDir);
		Path destinationPath;
		try {
			destinationPath = Paths.get(destinationDir);
		} catch (InvalidPathException e) {
			throw new BuildException(e);
		}

		clearDestinationDir(destinationPath);

		compileOrDie(buildCompilerCommandLine(destinationDir));

		myClassFiles = new EagerlyCachingFileTreeResourceCollection(destinationPath);
		try {
			myClassFiles.rebuildCache();
		} catch (ResourceEnumerationException e) {
			throw new BuildException("error while enumerating compiled class files", e);
		}
	}

	private void clearDestinationDir(Path destinationPath) throws BuildException {
		try {
			Log.verbose("Clearing destination directory");
			FilesUtil.deleteFileTree(destinationPath);
			Files.createDirectories(destinationPath);
		} catch (IOException e) {
			throw new BuildException("failed to prepare directory for compiled classes", e);
		}
	}

	private List<String> buildCompilerCommandLine(String destinationDir) throws ResourceEnumerationException {
		Log.debug("Preparing command line for compiler");
		List<String> compilerParameters = new ArrayList<>();

		compilerParameters.add("-d");
		compilerParameters.add(destinationDir);

		addJavaSourceFiles(compilerParameters);

		return compilerParameters;
	}

	private void addJavaSourceFiles(List<String> compilerParameters) throws ResourceEnumerationException {
		ResourceIterator resourceIterator = mySources.getResourceIterator();
		Resource resource;
		while ((resource = resourceIterator.next()) != null) {
			try {
				if (!resource.getAttributes().isRegularFile()) {
					continue;
				}
			} catch (ResourceAccessException e) {
				throw new ResourceEnumerationException(e);
			}
			Path path = resource.getPath();
			if (!path.getFileName().toString().endsWith(".java")) {
				continue;
			}
			compilerParameters.add(path.toString());
		}
	}

	private void compileOrDie(List<String> compilerParameters) throws BuildException {
		int result;
		try {
			Log.verbose("Invoking compiler");
			result = (Integer) compile.invoke(
					compiler,
					new Object[]{ compilerParameters.toArray(new String[compilerParameters.size()]) }
			);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new BuildException("failed to run compiler", e);
		}
		if (result != 0) {
			throw new BuildException("compiler exited with status code: " + result);
		}
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myClassFiles.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myClassFiles.rebuildCache();
	}

}
