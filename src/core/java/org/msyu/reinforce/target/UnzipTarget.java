package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipTarget extends Target implements ResourceCollection {

	public static final String SOURCE_KEY = "source";

	public static final String DESTINATION_KEY = "destination";

	private ResourceCollection mySources;

	private Path myDestinationPath;

	private ResourceCollection myUnpackedFiles;

	public UnzipTarget(String name) {
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

	@Override
	public void run() throws BuildException {
		Path destinationPath = myDestinationPath == null ?
				Build.getCurrent().getSandboxPath().resolve(getName()) :
				Build.getCurrent().getBasePath().resolve(myDestinationPath);
		try {
			FilesUtil.deleteFileTree(destinationPath);
			Files.createDirectories(destinationPath);
		} catch (IOException e) {
			throw new BuildException("failed to prepare directory for unpacked files", e);
		}
		ResourceIterator iterator = mySources.getResourceIterator();
		Resource resource;
		while ((resource = iterator.next()) != null) {
			Path zipPath = resource.getPath();
			if (zipPath == null) {
				throw new BuildException("cannot unpack something that does not exist: " + zipPath);
			}
			try {
				if (!resource.getAttributes().isRegularFile()) {
					throw new BuildException("cannot unpack something that is not a regular file: " + zipPath);
				}
			} catch (ResourceAccessException e) {
				throw new BuildException("failed to access attributes of resource " + resource, e);
			}
			try (ZipFile zipFile = new ZipFile(zipPath.toString())) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();
					boolean entryIsDirectory = entryName.endsWith("/");
					if (entryIsDirectory) {
						Files.createDirectories(destinationPath.resolve(entryName.substring(0, entryName.length() - 1)));
					} else {
						Path entryDestinationPath = destinationPath.resolve(entryName);
						Files.createDirectories(entryDestinationPath.getParent());
						try (InputStream entryStream = zipFile.getInputStream(entry)) {
							Files.copy(entryStream, entryDestinationPath);
						}
					}
				}
			} catch (IOException e) {
				throw new BuildException("error while unpacking " + zipPath, e);
			}
		}
		myUnpackedFiles = new EagerlyCachingFileTreeResourceCollection(destinationPath);
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myUnpackedFiles.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myUnpackedFiles.rebuildCache();
	}

}
