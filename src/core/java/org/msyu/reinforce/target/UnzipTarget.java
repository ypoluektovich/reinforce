package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Collections;
import org.msyu.reinforce.resources.EagerlyCachingFileTreeResourceCollection;
import org.msyu.reinforce.resources.FilterFromMap;
import org.msyu.reinforce.resources.IncludeExcludeResourceFilter;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.util.FilesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
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

	private IncludeExcludeResourceFilter myFilter;

	private ResourceCollection myUnpackedFiles;

	public UnzipTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		initializeSources(docMap);
		initializeDestination(docMap);
		myFilter = FilterFromMap.interpretIncludeExclude(docMap);
	}

	private void initializeSources(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(SOURCE_KEY)) {
			throw new TargetInitializationException("missing required parameter '" + SOURCE_KEY + "'");
		}
		mySources = Collections.interpret(docMap.get(SOURCE_KEY));
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
	public void run() throws ExecutionException {
		Path destinationPath = myDestinationPath == null ?
				Build.getCurrent().getSandboxPath().resolve(getName()) :
				Build.getCurrent().getBasePath().resolve(myDestinationPath);
		try {
			FilesUtil.deleteFileTree(destinationPath);
			Files.createDirectories(destinationPath);
		} catch (IOException e) {
			throw new ExecutionException("failed to prepare directory for unpacked files", e);
		}
		try {
			ResourceIterator iterator = mySources.getResourceIterator();
			Resource resource;
			while ((resource = iterator.next()) != null) {
				unpack(resource, destinationPath);
			}
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating files to unpack", e);
		}
		myUnpackedFiles = new EagerlyCachingFileTreeResourceCollection(destinationPath);
	}

	private void unpack(Resource resource, Path destinationPath) throws ExecutionException {
		Path zipPath = Build.getCurrent().getBasePath().resolve(resource.getPath());
		if (zipPath == null) {
			throw new ExecutionException("cannot unpack something that does not exist: " + zipPath);
		}
		try {
			if (!resource.getAttributes().isRegularFile()) {
				throw new ExecutionException("cannot unpack something that is not a regular file: " + zipPath);
			}
		} catch (ResourceAccessException e) {
			throw new ExecutionException("failed to access attributes of resource " + resource, e);
		}
		try (ZipFile zipFile = new ZipFile(zipPath.toString())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				ZipEntryResource entryResource = new ZipEntryResource(entry);
				try {
					if (!myFilter.fits(entryResource)) {
						continue;
					}
				} catch (ResourceEnumerationException e) {
					throw new ExecutionException("error while filtering entry '" + entry.getName() + "' of " + zipPath, e);
				}
				if (entry.isDirectory()) {
					Files.createDirectories(destinationPath.resolve(entryResource.getRelativePath()));
				} else {
					Path entryDestinationPath = destinationPath.resolve(entryResource.getRelativePath());
					Files.createDirectories(entryDestinationPath.getParent());
					try (InputStream entryStream = zipFile.getInputStream(entry)) {
						Files.copy(entryStream, entryDestinationPath);
					}
				}
			}
		} catch (IOException e) {
			throw new ExecutionException("error while unpacking " + zipPath, e);
		}
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myUnpackedFiles.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myUnpackedFiles.rebuildCache();
	}

	@Override
	public Resource getRoot() {
		return new Resource() {
			@Override
			public Path getPath() {
				return myUnpackedFiles.getRoot().getPath();
			}

			@Override
			public BasicFileAttributes getAttributes() throws ResourceAccessException {
				return myUnpackedFiles.getRoot().getAttributes();
			}

			@Override
			public Path getRelativePath() {
				return myUnpackedFiles.getRoot().getRelativePath();
			}
		};
	}

	private static class ZipEntryResource implements Resource {

		private final ZipEntry myEntry;

		private Path myRelativePath;

		protected ZipEntryResource(ZipEntry entry) {
			myEntry = entry;
			String entryName = entry.getName();
			myRelativePath = Paths.get(
					myEntry.isDirectory() ?
							entryName.substring(0, entryName.length() - 1) :
							entryName
			);
		}

		@Override
		public Path getPath() {
			return null;
		}

		@Override
		public BasicFileAttributes getAttributes() throws ResourceAccessException {
			return new BasicFileAttributes() {
				@Override
				public FileTime lastModifiedTime() {
					return FileTime.fromMillis(myEntry.getTime());
				}

				@Override
				public FileTime lastAccessTime() {
					return FileTime.fromMillis(myEntry.getTime());
				}

				@Override
				public FileTime creationTime() {
					return FileTime.fromMillis(myEntry.getTime());
				}

				@Override
				public boolean isRegularFile() {
					return !myEntry.isDirectory();
				}

				@Override
				public boolean isDirectory() {
					return myEntry.isDirectory();
				}

				@Override
				public boolean isSymbolicLink() {
					return false;
				}

				@Override
				public boolean isOther() {
					return false;
				}

				@Override
				public long size() {
					return myEntry.getSize();
				}

				@Override
				public Object fileKey() {
					return null;
				}
			};
		}

		@Override
		public Path getRelativePath() {
			return myRelativePath;
		}

	}

}
