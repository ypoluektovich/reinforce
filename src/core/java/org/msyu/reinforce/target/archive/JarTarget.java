package org.msyu.reinforce.target.archive;

import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.resources.Resource;

import java.io.IOException;
import java.nio.file.Path;

public class JarTarget extends AbstractArchiveTarget<JarOutputStreamWrapper> {

	public JarTarget(String name) {
		super(name);
	}

	@Override
	protected JarOutputStreamWrapper openArchive(Path destinationPath) throws IOException {
		return new JarOutputStreamWrapper(destinationPath);
	}

	@Override
	protected void addResourceToArchive(Resource resource, JarOutputStreamWrapper archive) throws BuildException, IOException {
		if (JarOutputStreamWrapper.META_INF_SERVICES.equals(resource.getRelativePath().getParent()) &&
				resource.getAttributes().isRegularFile()
		) {
			archive.addServiceLoaderDescriptor(resource);
		} else {
			archive.writeNow(resource);
		}
	}

}
