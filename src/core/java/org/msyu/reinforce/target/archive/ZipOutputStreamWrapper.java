package org.msyu.reinforce.target.archive;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWrapper implements Closeable {

	private final ZipOutputStream myZipOutputStream;

	private final Set<String> myAddedEntries = new HashSet<>();

	public ZipOutputStreamWrapper(Path destinationPath) throws IOException {
		myZipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(destinationPath)));
	}

	protected ZipOutputStreamWrapper(ZipOutputStream zipOutputStream) {
		myZipOutputStream = zipOutputStream;
	}

	public void writeNow(Resource resource) throws IOException {
		String zipPath = resource.getRelativePath().toString();
		BasicFileAttributes resourceAttributes = resource.getAttributes();
		boolean entryIsFile = resourceAttributes.isRegularFile();
		if (!entryIsFile) {
			if (resourceAttributes.isDirectory()) {
				zipPath += "/";
			} else {
				throw new IOException("resource " + resource + " is neither a file nor a directory; can't add to archive");
			}
		}
		if (myAddedEntries.contains(zipPath)) {
			if (entryIsFile) {
				throw new DuplicateEntryException("cannot overwrite packed file: " + zipPath);
			}
			// if duplicate dir, just silently continue
			return;
		}

		Log.debug("Adding entry: %s", zipPath);
		myZipOutputStream.putNextEntry(new ZipEntry(zipPath));
		if (entryIsFile) {
			Files.copy(Build.getCurrent().getBasePath().resolve(resource.getPath()), myZipOutputStream);
		}
		myZipOutputStream.closeEntry();
		myAddedEntries.add(zipPath);
	}

	@Override
	public void close() throws IOException {
		Log.debug("Finishing writing the archive");
		myZipOutputStream.finish();
		myZipOutputStream.close();
	}

}
