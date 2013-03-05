package org.msyu.reinforce.target.javac;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.ReinterpretationException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.EmptyResourceCollection;
import org.msyu.reinforce.resources.FileCollections;
import org.msyu.reinforce.resources.FilteringResourceCollection;
import org.msyu.reinforce.resources.RegexResourceFilter;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceCollections;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JavacTarget extends Target implements ResourceCollection {

	public static final String COMPILER_KEY = "compiler";

	public static final String COMPILER_TYPE_SUN = "sun";

	public static final String COMPILER_TYPE_EXTERNAL = "external";


	public static final String SOURCE_KEY = "source";

	public static final String ON_EMPTY_SOURCE_KEY = "on empty source";

	public static enum ActionOnEmptySource {
		DIE, WARN, SKIP
	}

	public static final String EMPTY_SOURCE_MESSAGE = "Attempted compiling empty source collection, result will be empty too";


	public static final String CLASSPATH_KEY = "classpath";


	private JavaCompiler myJavaCompiler;

	private ResourceCollection mySources;

	private ActionOnEmptySource myActionOnEmptySource = ActionOnEmptySource.DIE;

	private ResourceCollection myClasspath;

	private ResourceCollection myClassFiles;


	public JavacTarget(TargetInvocation invocation) {
		super(invocation);
	}


	@Override
	protected void initTarget(Map docMap)
			throws TargetInitializationException
	{
		myJavaCompiler = prepareCompiler(docMap);
		prepareSource(docMap);
		myClasspath = prepareClasspath(docMap);
	}

	private void prepareSource(Map docMap) throws TargetInitializationException {
		Log.debug("Parsing source setting");
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		mySources = new FilteringResourceCollection(
				ResourceCollections.interpret(docMap.get(SOURCE_KEY)),
				new RegexResourceFilter("\\.java$")
		);

		if (docMap.containsKey(ON_EMPTY_SOURCE_KEY)) {
			Object setting = docMap.get(ON_EMPTY_SOURCE_KEY);
			for (ActionOnEmptySource action : ActionOnEmptySource.values()) {
				if (action.name().toLowerCase(Locale.ENGLISH).equals(setting)) {
					myActionOnEmptySource = action;
				}
			}
			throw new TargetInitializationException(
					"value of '" + ON_EMPTY_SOURCE_KEY + "' must be one of: " +
							Arrays.toString(ActionOnEmptySource.values())
			);
		}
	}

	private ResourceCollection prepareClasspath(Map docMap) throws TargetInitializationException {
		if (docMap.containsKey(CLASSPATH_KEY)) {
			Log.debug("Parsing classpath setting");
			return ResourceCollections.interpret(docMap.get(CLASSPATH_KEY));
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
		checkEmptySource();
		Path destinationPath = myJavaCompiler.execute(getInvocation().getTargetName(), mySources, myClasspath);
		myClassFiles = FileCollections.fromPath(destinationPath);
		try {
			myClassFiles.rebuildCache();
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating compiled class files", e);
		}
	}

	private void checkEmptySource() throws ExecutionException {
		try {
			Log.debug("Checking that source collection is not empty...");
			if (!mySources.isEmpty()) {
				return;
			}
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while checking presence of sources", e);
		}
		if (myActionOnEmptySource == ActionOnEmptySource.DIE) {
			throw new ExecutionException("source list is empty");
		}
		if (myActionOnEmptySource == ActionOnEmptySource.WARN) {
			Log.warn(EMPTY_SOURCE_MESSAGE);
		} else {
			Log.info(EMPTY_SOURCE_MESSAGE);
		}
		myClassFiles = EmptyResourceCollection.INSTANCE;
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

	@Override
	public boolean isEmpty() throws ResourceEnumerationException {
		return myClassFiles.isEmpty();
	}

}
