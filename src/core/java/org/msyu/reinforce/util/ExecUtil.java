package org.msyu.reinforce.util;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;

import java.io.IOException;
import java.util.List;

public class ExecUtil {

	public static int execute(List<String> command) throws ExecutionException {
		int exitCode;
		try {
			Log.verbose("Starting external process...");
			Process process = new ProcessBuilder(command)
					.directory(Build.getCurrent().getBasePath().toFile())
					.inheritIO()
					.start();
			while (true) {
				try {
					exitCode = process.waitFor();
					break;
				} catch (InterruptedException e) {
					Log.debug("The thread waiting for the external process got interrupted. Ignoring...");
				}
			}
		} catch (IOException e) {
			throw new ExecutionException("exception during external process invocation", e);
		}
		Log.verbose("Process exited with code %d", exitCode);
		return exitCode;
	}

}
