package org.msyu.reinforce.target;

import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.util.FilesUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractJavac implements JavaCompiler {

	public static final String OPTIONS_FILE_NAME_SUFFIX = ".options";

	public static final String SOURCES_FILE_NAME_SUFFIX = ".sources";

	@Override
	public Path execute(String targetName, ResourceCollection sources, ResourceCollection classpath) throws BuildException {
		Path buildHome = Paths.get("build");
		Path destinationDir = buildHome.resolve(targetName);
		Path optionsFile = buildHome.resolve(targetName + OPTIONS_FILE_NAME_SUFFIX);
		Path sourcesFile = buildHome.resolve(targetName + SOURCES_FILE_NAME_SUFFIX);

		clearDestinations(destinationDir, optionsFile, sourcesFile);

		compileOrDie(buildCompilerCommandLine(optionsFile, destinationDir, classpath, sourcesFile, sources));

		return destinationDir;
	}

	private void clearDestinations(Path destinationDir, Path optionsFile, Path sourcesFile) throws BuildException {
		try {
			Log.verbose("Cleaning destinations");
			FilesUtil.deleteFileTree(destinationDir);
			Files.createDirectories(destinationDir);
			FilesUtil.deleteFileTree(optionsFile);
			FilesUtil.deleteFileTree(sourcesFile);
		} catch (IOException e) {
			throw new BuildException("failed to clear destinations for compilation", e);
		}
	}

	private List<String> buildCompilerCommandLine(
			Path optionsFile,
			Path destinationDir,
			ResourceCollection classpath,
			Path sourcesFile,
			ResourceCollection sources
	) throws BuildException {
		Log.verbose("Preparing command line for compiler");
		writeOptionsFile(optionsFile, destinationDir, classpath);
		writeSourcesFile(sourcesFile, sources);
		return Arrays.asList("@" + optionsFile, "@" + sourcesFile);
	}

	private void writeOptionsFile(Path optionsFile, Path destinationDir, ResourceCollection classpath) throws BuildException {
		try (BufferedWriter writer = Files.newBufferedWriter(optionsFile, Charset.forName("UTF-8"))) {
			writer.write("-d ");
			writer.write(destinationDir.toString());
			writer.newLine();

			ResourceIterator cpIterator = classpath.getResourceIterator();
			Resource cpElement = cpIterator.next();
			if (cpElement != null) {
				writer.write("-classpath ");
				writer.write(cpElement.getPath().toString());
				while ((cpElement = cpIterator.next()) != null) {
					writer.write(":");
					writer.write(cpElement.getPath().toString());
				}
				writer.newLine();
			}
		} catch (IOException e) {
			throw new BuildException("IO error while writing javac options file", e);
		}
	}

	private void writeSourcesFile(Path sourcesFile, ResourceCollection sources) throws BuildException {
		ResourceIterator resourceIterator = sources.getResourceIterator();
		Path sourceElement = getNextFile(resourceIterator);
		if (sourceElement == null) {
			throw new BuildException("source list is empty");
		}
		try (BufferedWriter writer = Files.newBufferedWriter(sourcesFile, Charset.forName("UTF-8"))) {
			do {
				writer.write(sourceElement.toString());
				writer.newLine();
			} while ((sourceElement = getNextFile(resourceIterator)) != null);
		} catch (IOException e) {
			throw new BuildException("I/O error while saving the list of files to be compiled", e);
		}
	}

	private Path getNextFile(ResourceIterator iterator) throws ResourceEnumerationException {
		Resource resource;
		while ((resource = iterator.next()) != null) {
			try {
				if (!resource.getAttributes().isRegularFile()) {
					continue;
				}
			} catch (ResourceAccessException e) {
				throw new ResourceEnumerationException(e);
			}
			Path path = resource.getPath();
			if (!path.getFileName().toString().endsWith(".java")) {
				continue;
			}
			return path;
		}
		return null;
	}

	protected abstract void compileOrDie(List<String> compilerParameters) throws BuildException;

}
