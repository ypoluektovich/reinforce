package org.msyu.reinforce.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileSystemResource implements Resource {

	private final Path myPath;

	private BasicFileAttributes myAttributes;

	private final Path myRelativePath;

	public FileSystemResource(Path path, BasicFileAttributes attributes, Path relativePath) {
		myPath = path;
		myAttributes = attributes;
		myRelativePath = relativePath;
	}

	@Override
	public Path getPath() {
		return myPath;
	}

	@Override
	public BasicFileAttributes getAttributes() throws ResourceAccessException {
		if (myAttributes == null) {
			try {
				myAttributes = Files.readAttributes(myPath, BasicFileAttributes.class);
			} catch (IOException e) {
				throw new ResourceAccessException("couldn't read attributes of " + myPath, e);
			}
		}
		return myAttributes;
	}

	@Override
	public Path getRelativePath() {
		return myRelativePath;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + String.format("{%s -> %s}", myPath, myRelativePath);
	}

}
