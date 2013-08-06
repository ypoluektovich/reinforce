package org.msyu.reinforce.target.javac;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.util.FilesUtil;
import org.msyu.reinforce.util.JavaClasspath;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractJavac implements JavaCompiler {

	public static final String OPTIONS_FILE_NAME_SUFFIX = ".options";

	public static final String SOURCES_FILE_NAME_SUFFIX = ".sources";

	@Override
	public Path execute(String targetName, ResourceCollection sources, ResourceCollection classpath) throws ExecutionException {
		Path sandboxPath = Build.getCurrent().getSandboxPath();
		Path destinationDir = sandboxPath.resolve(targetName);
		Path optionsFile = sandboxPath.resolve(targetName + OPTIONS_FILE_NAME_SUFFIX);
		Path sourcesFile = sandboxPath.resolve(targetName + SOURCES_FILE_NAME_SUFFIX);

		clearDestinations(destinationDir, optionsFile, sourcesFile);

		compileOrDie(buildCompilerCommandLine(optionsFile, destinationDir, classpath, sourcesFile, sources));

		return destinationDir;
	}

	private void clearDestinations(Path destinationDir, Path optionsFile, Path sourcesFile) throws ExecutionException {
		try {
			Log.verbose("Cleaning destinations");
			FilesUtil.deleteFileTree(destinationDir);
			Files.createDirectories(destinationDir);
			FilesUtil.deleteFileTree(optionsFile);
			FilesUtil.deleteFileTree(sourcesFile);
		} catch (IOException e) {
			throw new ExecutionException("failed to clear destinations for compilation", e);
		}
	}

	private List<String> buildCompilerCommandLine(
			Path optionsFile,
			Path destinationDir,
			ResourceCollection classpath,
			Path sourcesFile,
			ResourceCollection sources
	) throws ExecutionException {
		Log.verbose("Preparing command line for compiler");
		writeOptionsFile(optionsFile, destinationDir, classpath);
		writeSourcesFile(sourcesFile, sources);
		Log.verbose("Finished preparing compiler command line");
		return Arrays.asList("@" + optionsFile, "@" + sourcesFile);
	}

	private void writeOptionsFile(Path optionsFile, Path destinationDir, ResourceCollection classpath) throws ExecutionException {
		Log.debug("Writing options file: %s", optionsFile);
		try (BufferedWriter writer = Files.newBufferedWriter(optionsFile, Charset.forName("UTF-8"))) {
			Log.debug("Writing destination dir option: -d %s", destinationDir);
			writer.write("-d ");
			writer.write(destinationDir.toString());
			writer.newLine();

			try {
				JavaClasspath.fromResourceCollection(
						classpath,
						writer,
						"-classpath ",
						System.getProperty("path.separator"),
						System.getProperty("line.separator")
				);
			} catch (ResourceEnumerationException e) {
				throw new ExecutionException("error while enumerating classpath entries", e);
			}

			Log.debug("Closing options file");
		} catch (IOException e) {
			throw new ExecutionException("IO error while writing javac options file", e);
		}
	}

	private void writeSourcesFile(Path sourcesFile, ResourceCollection sources) throws ExecutionException {
		try {
			Log.debug("Writing sources file: %s", sourcesFile);
			try (BufferedWriter writer = Files.newBufferedWriter(sourcesFile, Charset.forName("UTF-8"))) {
				ResourceIterator resourceIterator = sources.getResourceIterator();
				Path sourceElement;
				while ((sourceElement = getNextFile(resourceIterator)) != null) {
					writer.write(Build.getCurrent().getBasePath().resolve(sourceElement).toString());
					writer.newLine();
				}
				Log.debug("Closing sources file");
			} catch (IOException e) {
				throw new ExecutionException("I/O error while saving the list of files to be compiled", e);
			}
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating source files", e);
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

	protected abstract void compileOrDie(List<String> compilerParameters) throws ExecutionException;

}
