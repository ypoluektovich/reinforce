package org.msyu.reinforce.target;

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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTarget extends Target implements Resource, ResourceCollection {

	public static final String SOURCE_KEY = "source";

	public static final String DESTINATION_KEY = "destination";

	private ResourceCollection mySources;

	private Path myDestinationPath;

	public ZipTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		initializeSources(docMap, dependencyTargetByName);
		initializeDestination(docMap);
	}

	private void initializeSources(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		mySources = ResourceDefinitionYamlParser.parseAsCollection(docMap.get(SOURCE_KEY), dependencyTargetByName);
	}

	private void initializeDestination(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(DESTINATION_KEY)) {
			myDestinationPath = Paths.get("build", getName() + ".zip");
			return;
		}
		Object destinationObject = docMap.get(DESTINATION_KEY);
		if (destinationObject instanceof String) {
			myDestinationPath = Paths.get((String) destinationObject);
			return;
		}
		throw new TargetInitializationException("invalid destination: must be a string");
	}

	@Override
	public void run() throws BuildException {
		Path destinationPath = Build.getCurrent().getBasePath().resolve(myDestinationPath);
		try {
			Log.verbose("Clearing the destination path");
			FilesUtil.deleteFileTree(destinationPath);
			Log.verbose("Creating parent directories");
			Files.createDirectories(destinationPath.getParent());
		} catch (IOException e) {
			throw new BuildException("failed to prepare the destination", e);
		}

		try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(destinationPath)))) {
			ResourceIterator resourceIterator = mySources.getResourceIterator();
			Resource resource;
			Set<String> addedEntries = new HashSet<>();
			while ((resource = resourceIterator.next()) != null) {
				Path relativePath = resource.getRelativePath();
				if (FilesUtil.EMPTY_PATH.equals(relativePath)) {
					continue;
				}
				boolean entryIsFile = !resource.getAttributes().isDirectory();
				String zipPath = entryIsFile ? relativePath.toString() : (relativePath.toString() + "/");
				if (addedEntries.contains(zipPath)) {
					if (entryIsFile) {
						throw new BuildException("cannot overwrite packed file: " + zipPath);
					}
					// if duplicate dir, just silently continue
				} else {
					Path realPath = resource.getPath();
					Log.debug("%s %s as %s", entryIsFile ? "Writing file" : "Adding dir", realPath, zipPath);
					output.putNextEntry(new ZipEntry(zipPath));
					if (entryIsFile) {
						Files.copy(realPath, output);
					}
					output.closeEntry();
					addedEntries.add(zipPath);
				}
			}
			output.finish();
			output.flush();
		} catch (IOException e) {
			throw new BuildException("error while writing the destination file", e);
		}
	}

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

}
