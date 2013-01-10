package org.msyu.reinforce.resources;

import org.msyu.reinforce.Log;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EagerlyCachingFileTreeResourceCollection extends AbstractEagerlyCachingResourceCollection {

	private final Path myRootPath;

	public EagerlyCachingFileTreeResourceCollection(Path rootPath) {
		myRootPath = rootPath;
	}

	@Override
	protected List<Resource> innerRebuildCache() throws ResourceEnumerationException {
		Log.verbose("Enumerating files under %s", myRootPath);
		final List<Resource> resources = new ArrayList<>();
		try {
			Files.walkFileTree(
					myRootPath,
					EnumSet.of(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE,
					new FileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
							Log.debug("Enumerating files in directory: %s", dir);
							resources.add(new FileSystemResource(dir, attrs, myRootPath.relativize(dir)));
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
							Log.debug("Found file: %s", file);
							resources.add(new FileSystemResource(file, attrs, myRootPath.relativize(file)));
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

}
