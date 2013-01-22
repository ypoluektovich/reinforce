package org.msyu.reinforce.resources;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface Resource {

	Path getPath();

	BasicFileAttributes getAttributes() throws ResourceAccessException;

	Path getRelativePath();

}
