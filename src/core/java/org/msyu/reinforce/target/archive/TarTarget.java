package org.msyu.reinforce.target.archive;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.Resource;

import java.io.IOException;
import java.nio.file.Path;

public class TarTarget extends AbstractArchiveTarget<TarOutputStreamWrapper> {

	public TarTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected final String getDefaultFileExtension() {
		return "tar";
	}

	@Override
	protected TarOutputStreamWrapper openArchive(Path destinationPath) throws IOException {
		return new TarOutputStreamWrapper(destinationPath);
	}

	@Override
	protected void addResourceToArchive(Resource resource, TarOutputStreamWrapper archive) throws ExecutionException, IOException {
		archive.writeNow(resource);
	}

}
