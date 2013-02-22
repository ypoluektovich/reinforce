package org.msyu.reinforce.target.javac;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;

import java.io.IOException;
import java.util.List;

public class ExternalJavac extends AbstractJavac {

	@Override
	protected void compileOrDie(List<String> compilerParameters) throws ExecutionException {
		int exitCode;
		try {
			Log.verbose("Starting external process...");
			Process process = new ProcessBuilder(getCommandLineArray(compilerParameters))
					.directory(Build.getCurrent().getBasePath().toFile())
					.inheritIO()
					.start();
			while (true) {
				try {
					exitCode = process.waitFor();
					break;
				} catch (InterruptedException e) {
					Log.debug("The thread waiting for the external compiler got interrupted. Ignoring...");
				}
			}
		} catch (IOException e) {
			throw new ExecutionException("exception during external compiler invocation", e);
		}
		Log.verbose("Process exited with code %d", exitCode);
		if (exitCode != 0) {
			throw new ExecutionException("compiler exited with status code: " + exitCode);
		}
	}

	private static String[] getCommandLineArray(List<String> compilerParameters) {
		String[] array = new String[compilerParameters.size() + 1];
		compilerParameters.toArray(array);
		System.arraycopy(array, 0, array, 1, compilerParameters.size());
		array[0] = "javac";
		return array;
	}

}
