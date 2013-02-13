package org.msyu.reinforce.target.archive;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.resources.FileSystemResource;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.util.FilesUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;

public class JarOutputStreamWrapper extends ZipOutputStreamWrapper {

	public static final Path META_INF_SERVICES = Paths.get("META-INF", "services");

	private final Map<String, List<Resource>> myServiceLoaderDescriptors = new HashMap<>();

	public JarOutputStreamWrapper(Path destinationPath) throws IOException {
		super(new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(destinationPath))));
	}

	public void addServiceLoaderDescriptor(Resource resource) {
		Log.debug("Deferring addition of ServiceLoader config %s", resource.getRelativePath());
		String serviceName = resource.getRelativePath().getFileName().toString();
		List<Resource> parts;
		if (myServiceLoaderDescriptors.containsKey(serviceName)) {
			parts = myServiceLoaderDescriptors.get(serviceName);
		} else {
			myServiceLoaderDescriptors.put(serviceName, parts = new ArrayList<>());
		}
		parts.add(resource);
	}

	@Override
	public void close() throws IOException {
		flushServiceLoaderDescriptors();
		super.close();
	}

	private void flushServiceLoaderDescriptors() throws IOException {
		if (myServiceLoaderDescriptors.size() == 0) {
			return;
		}

		Log.debug(
				"Flushing %d deferred ServiceLoader config%s",
				myServiceLoaderDescriptors.size(),
				myServiceLoaderDescriptors.size() > 1 ? "s" : ""
		);

		Path tempDir = Build.getCurrent().getSandboxPath()
				.resolve(Build.getCurrent().getCurrentTargetName() + ".tmp")
				.resolve(META_INF_SERVICES);

		FilesUtil.deleteFileTree(tempDir);
		Files.createDirectories(tempDir);

		for (String serviceName : myServiceLoaderDescriptors.keySet()) {
			flushServiceLoaderDescriptor(serviceName, tempDir);
		}

		myServiceLoaderDescriptors.clear();
	}

	private void flushServiceLoaderDescriptor(String serviceName, Path tempDir) throws IOException {
		Path implementationListFile = tempDir.resolve(serviceName);
		Log.debug("Writing temporary file %s", implementationListFile);
		try (Writer out = Files.newBufferedWriter(implementationListFile, Charset.forName("UTF-8"))) {
			for (Resource part : myServiceLoaderDescriptors.get(serviceName)) {
				Path partPath = Build.getCurrent().getBasePath().resolve(part.getPath());
				Log.debug("Reading lines from %s", partPath);
				try (BufferedReader in = Files.newBufferedReader(partPath, Charset.forName("UTF-8"))) {
					String line;
					while ((line = in.readLine()) != null) {
						out.write(line);
						out.write("\n");
					}
				}
			}
		}
		writeNow(new FileSystemResource(
				implementationListFile,
				Files.readAttributes(implementationListFile, BasicFileAttributes.class),
				META_INF_SERVICES.resolve(serviceName)
		));
	}
}
