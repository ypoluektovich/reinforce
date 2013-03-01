package org.msyu.reinforce.resources;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileCollections {

	private FileCollections() {
		// do not instantiate
	}

	public static ResourceCollection fromPath(Path rootPath) {
		if (!Files.exists(Build.getCurrent().getBasePath().resolve(rootPath))) {
			Log.warn("Returning empty collection for nonexistent path: %s", rootPath);
			return EmptyResourceCollection.INSTANCE;
		}

		ResourceCollection collection = new EagerlyCachingFileTreeResourceCollection(rootPath);
		Log.debug("Creating a file tree resource collection: %s", collection);
		return collection;
	}

}
