package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.ReinterpretationException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Collections;
import org.msyu.reinforce.resources.EagerlyCachingFileTreeResourceCollection;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class JavacTarget extends Target implements ResourceCollection {

	public static final String COMPILER_KEY = "compiler";

	public static final String COMPILER_TYPE_SUN = "sun";

	public static final String COMPILER_TYPE_EXTERNAL = "external";

	public static final String SOURCE_KEY = "source";

	public static final String CLASSPATH_KEY = "classpath";

	private JavaCompiler myJavaCompiler;

	private ResourceCollection mySources;

	private ResourceCollection myClasspath;

	private ResourceCollection myClassFiles;

	public JavacTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName)
			throws TargetInitializationException
	{
		myJavaCompiler = prepareCompiler(docMap);
		mySources = prepareSource(docMap);
		myClasspath = prepareClasspath(docMap);
	}

	private ResourceCollection prepareSource(Map docMap) throws TargetInitializationException {
		Log.debug("Parsing source setting");
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		return Collections.interpret(docMap.get(SOURCE_KEY));
	}

	private ResourceCollection prepareClasspath(Map docMap) throws TargetInitializationException {
		if (docMap.containsKey(CLASSPATH_KEY)) {
			Log.debug("Parsing classpath setting");
			return Collections.interpret(docMap.get(CLASSPATH_KEY));
		} else {
			Log.debug("No classpath has been set");
			return null;
		}
	}

	private JavaCompiler prepareCompiler(Map docMap) throws TargetInitializationException {
		Object compilerType = docMap.containsKey(COMPILER_KEY) ? docMap.get(COMPILER_KEY) : COMPILER_TYPE_EXTERNAL;
		if (!(compilerType instanceof String)) {
			throw new TargetInitializationException("compiler type must be a string");
		}
		switch ((String) compilerType) {
			case COMPILER_TYPE_SUN:
				return new InternalSunJavac();
			case COMPILER_TYPE_EXTERNAL:
				return new ExternalJavac();
			default:
				throw new TargetInitializationException("unsupported compiler type: " + compilerType);
		}
	}

	@Override
	public void run() throws ExecutionException {
		Path destinationPath = myJavaCompiler.execute(getName(), mySources, myClasspath);

		myClassFiles = new EagerlyCachingFileTreeResourceCollection(destinationPath);
		try {
			myClassFiles.rebuildCache();
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating compiled class files", e);
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

	@Override
	public Resource getRoot() {
		return new Resource() {
			@Override
			public Path getPath() {
				return myClassFiles.getRoot().getPath();
			}

			@Override
			public BasicFileAttributes getAttributes() throws ResourceAccessException {
				return myClassFiles.getRoot().getAttributes();
			}

			@Override
			public Path getRelativePath() {
				return myClassFiles.getRoot().getRelativePath();
			}
		};
	}

	@Override
	public Object reinterpret(String interpretationSpec) throws ReinterpretationException {
		if ("root".equals(interpretationSpec)) {
			return getRoot();
		} else {
			return super.reinterpret(interpretationSpec);
		}
	}

}
