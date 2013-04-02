package org.msyu.reinforce.target.archive;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.Resource;

import java.io.IOException;
import java.nio.file.Path;

public class ZipTarget extends AbstractArchiveTarget<ZipOutputStreamWrapper> {

	public ZipTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected final String getDefaultFileExtension() {
		return "zip";
	}

	@Override
	protected ZipOutputStreamWrapper openArchive(Path destinationPath) throws IOException {
		return new ZipOutputStreamWrapper(destinationPath);
	}

	@Override
	protected void addResourceToArchive(Resource resource, ZipOutputStreamWrapper archive) throws ExecutionException, IOException {
		archive.writeNow(resource);
	}

}
