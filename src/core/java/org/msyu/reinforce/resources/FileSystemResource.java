package org.msyu.reinforce.resources;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileSystemResource implements Resource {

	private final Path myPath;

	private final BasicFileAttributes myAttributes;

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
	public BasicFileAttributes getAttributes() {
		return myAttributes;
	}

	@Override
	public Path getRelativePath() {
		return myRelativePath;
	}

}
