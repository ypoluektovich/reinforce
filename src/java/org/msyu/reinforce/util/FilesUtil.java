package org.msyu.reinforce.util;

import org.msyu.reinforce.Log;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FilesUtil {

	public static final Path EMPTY_PATH = Paths.get("");

	public static void deleteFileTree(Path root) throws IOException {
		Files.walkFileTree(
				root,
				new FileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						Log.debug("Iterating dir: %s", dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Log.debug("Deleting file: %s", file);
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						if (exc instanceof NoSuchFileException) {
							Log.debug("Ninjas disappeared file: %s", file);
						} else {
							Log.debug("Despite failing to read attributes, deleting %s", file);
							Files.deleteIfExists(file);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						if (exc == null) {
							Log.debug("Deleting dir: %s", dir);
							Files.deleteIfExists(dir);
							return FileVisitResult.CONTINUE;
						} else {
							throw exc;
						}
					}
				}
		);
	}

}
