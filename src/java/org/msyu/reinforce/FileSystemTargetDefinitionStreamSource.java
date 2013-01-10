package org.msyu.reinforce;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemTargetDefinitionStreamSource implements TargetDefinitionStreamSource {

	private final Path targetDir;

	public FileSystemTargetDefinitionStreamSource(Path targetDir) {
		this.targetDir = targetDir;
	}

	@Override
	public InputStream getStreamForTarget(String name) throws TargetDefinitionLoadingException {
		Path resolvedTargetPath = targetDir.resolve(name);
		if (!Files.isRegularFile(resolvedTargetPath)) {
			return null;
		}
		try {
			return Files.newInputStream(resolvedTargetPath);
		} catch (IOException e) {
			throw new TargetDefinitionLoadingException(e);
		}
	}

}
