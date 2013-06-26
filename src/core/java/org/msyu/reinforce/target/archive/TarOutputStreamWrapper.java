package org.msyu.reinforce.target.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.CharsetNames;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.resources.Resource;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class TarOutputStreamWrapper implements Closeable {

	private final TarArchiveOutputStream myOutputStream;

	private final Set<String> myAddedEntries = new HashSet<>();

	public TarOutputStreamWrapper(Path destinationPath) throws IOException {
		myOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(destinationPath)), CharsetNames.UTF_8);
	}

	public void writeNow(Resource resource) throws IOException {
		String entryPath = resource.getRelativePath().toString();
		BasicFileAttributes resourceAttributes = resource.getAttributes();
		boolean entryIsFile = resourceAttributes.isRegularFile();
		if (!entryIsFile) {
			if (resourceAttributes.isDirectory()) {
				entryPath += "/";
			} else {
				throw new IOException("resource " + resource + " is neither a file nor a directory; can't add to archive");
			}
		}
		if (myAddedEntries.contains(entryPath)) {
			if (entryIsFile) {
				throw new DuplicateEntryException("cannot overwrite packed file: " + entryPath);
			}
			// if duplicate dir, just silently continue
			return;
		}

		Log.debug("Adding entry: %s", entryPath);
		TarArchiveEntry archiveEntry = new TarArchiveEntry(entryPath);
		archiveEntry.setSize(resourceAttributes.size());
		archiveEntry.setModTime(resourceAttributes.lastModifiedTime().toMillis());
		myOutputStream.putArchiveEntry(archiveEntry);
		if (entryIsFile) {
			Files.copy(Build.getCurrent().getBasePath().resolve(resource.getPath()), myOutputStream);
		}
		myOutputStream.closeArchiveEntry();
		myAddedEntries.add(entryPath);
	}

	@Override
	public void close() throws IOException {
		Log.debug("Finishing writing the archive");
		myOutputStream.finish();
		myOutputStream.close();
	}

}
