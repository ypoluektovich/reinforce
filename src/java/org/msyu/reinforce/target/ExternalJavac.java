package org.msyu.reinforce.target;

import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;

import java.io.IOException;
import java.util.List;

public class ExternalJavac extends AbstractJavac {

	@Override
	protected void compileOrDie(List<String> compilerParameters) throws BuildException {
		int exitCode;
		try {
			Process process = new ProcessBuilder(getCommandLineArray(compilerParameters)).inheritIO().start();
			while (true) {
				try {
					exitCode = process.waitFor();
					break;
				} catch (InterruptedException e) {
					Log.debug("The thread waiting for the external compiler got interrupted. Ignoring...");
				}
			}
		} catch (IOException e) {
			throw new BuildException("exception during external compiler invocation", e);
		}
		if (exitCode != 0) {
			throw new BuildException("compiler exited with status code: " + exitCode);
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
