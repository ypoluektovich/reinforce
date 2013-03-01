package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * <a name="regularRoot"/>
 */
public class EagerlyCachingFileTreeResourceCollection extends AbstractEagerlyCachingResourceCollection {

	private final Path myRootPath;

	protected EagerlyCachingFileTreeResourceCollection(Path rootPath) {
		myRootPath = rootPath;
	}

	@Override
	protected List<Resource> innerRebuildCache() throws ResourceEnumerationException {
		final Path rootPath = Build.getCurrent().getBasePath().resolve(myRootPath);
		if (Files.isRegularFile(rootPath)) {
			Log.verbose("Collection root is a file: %s", rootPath);
			try {
				return Collections.singletonList((Resource) new FileSystemResource(
						rootPath,
						Files.readAttributes(rootPath, BasicFileAttributes.class),
						rootPath.getFileName()
				));
			} catch (IOException e) {
				throw new ResourceEnumerationException(e);
			}
		}
		Log.verbose("Enumerating files under %s", rootPath);
		final List<Resource> resources = new ArrayList<>();
		try {
			Files.walkFileTree(
					rootPath,
					EnumSet.of(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE,
					new FileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
							Log.debug("Enumerating files in directory: %s", dir);
							resources.add(new FileSystemResource(dir, attrs, rootPath.relativize(dir)));
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
							Log.debug("Found file: %s", file);
							resources.add(new FileSystemResource(file, attrs, rootPath.relativize(file)));
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							Log.warn("Couldn't read attributes of %s", file);
							throw exc;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							if (exc == null) {
								Log.debug("Finished enumerating %s", dir);
								return FileVisitResult.CONTINUE;
							} else {
								throw exc;
							}
						}
					}
			);
		} catch (IOException e) {
			throw new ResourceEnumerationException(e);
		}
		return resources;
	}

	@Override
	public String toString() {
		return super.toString() + "{" + myRootPath + "}";
	}

	@Override
	public Resource getRoot() {
		return new FileSystemResource(myRootPath, null, myRootPath.getFileName());
	}

}
