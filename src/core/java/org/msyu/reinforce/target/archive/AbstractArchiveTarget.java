package org.msyu.reinforce.target.archive;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceDefinitionYamlParser;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.resources.SingleResourceIterator;
import org.msyu.reinforce.util.FilesUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public abstract class AbstractArchiveTarget<T extends Closeable> extends Target implements Resource, ResourceCollection {

	public static final String SOURCE_KEY = "source";

	public static final String DESTINATION_KEY = "destination";


	private ResourceCollection mySources;

	private Path myDestinationPath;


	protected AbstractArchiveTarget(String name) {
		super(name);
	}


	@Override
	protected final void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		initializeSources(docMap, dependencyTargetByName);
		initializeDestination(docMap);
		customInitTarget();
	}

	private void initializeSources(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		mySources = ResourceDefinitionYamlParser.parseAsCollection(docMap.get(SOURCE_KEY), dependencyTargetByName);
	}

	private void initializeDestination(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(DESTINATION_KEY)) {
			myDestinationPath = null;
			return;
		}
		Object destinationObject = docMap.get(DESTINATION_KEY);
		if (destinationObject instanceof String) {
			myDestinationPath = Paths.get((String) destinationObject);
			return;
		}
		throw new TargetInitializationException("invalid destination: must be a string");
	}

	protected void customInitTarget() {
		// inheritors may override
	}


	@Override
	public void run() throws BuildException {
		Path destinationPath = myDestinationPath == null ?
				Build.getCurrent().getSandboxPath().resolve(getName() + ".zip") :
				Build.getCurrent().getBasePath().resolve(myDestinationPath);
		try {
			Log.verbose("Clearing the destination path");
			FilesUtil.deleteFileTree(destinationPath);
			Log.verbose("Creating parent directories");
			Files.createDirectories(destinationPath.getParent());
		} catch (IOException e) {
			throw new BuildException("failed to prepare the destination", e);
		}

		try (T archive = openArchive(destinationPath)) {
			ResourceIterator resourceIterator = mySources.getResourceIterator();
			Resource resource;
			while ((resource = resourceIterator.next()) != null) {
				if (FilesUtil.EMPTY_PATH.equals(resource.getRelativePath())) {
					continue;
				}
				addResourceToArchive(resource, archive);
			}
		} catch (IOException e) {
			throw new BuildException("error while writing into the archive", e);
		}
	}

	protected abstract T openArchive(Path destinationPath) throws IOException;

	protected abstract void addResourceToArchive(Resource resource, T archive) throws BuildException, IOException;

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return new SingleResourceIterator(this);
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return null;
	}

	@Override
	public Path getPath() {
		return myDestinationPath;
	}

	@Override
	public BasicFileAttributes getAttributes() throws ResourceAccessException {
		try {
			return Files.readAttributes(myDestinationPath, BasicFileAttributes.class);
		} catch (IOException e) {
			throw new ResourceAccessException("failed to read attributes of the destination archive", e);
		}
	}

	@Override
	public Path getRelativePath() {
		return myDestinationPath.getFileName();
	}

	@Override
	public Resource getRoot() {
		return this;
	}

}
