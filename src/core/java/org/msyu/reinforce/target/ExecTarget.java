package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExecTarget extends Target {

	private static final String COMMAND_KEY = "command";

	private List<String> myCommand;

	public ExecTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!docMap.containsKey(COMMAND_KEY)) {
			throw new TargetInitializationException("missing required parameter: " + COMMAND_KEY);
		}
		Object commandObject = docMap.get(COMMAND_KEY);
		if (commandObject instanceof String) {
			myCommand = Arrays.asList((String) commandObject);
		} else if (commandObject instanceof List) {
			List commandList = (List) commandObject;
			myCommand = new ArrayList<>(commandList.size());
			for (int i = 0; i < commandList.size(); i++) {
				Object commandArgument = commandList.get(i);
				if (commandArgument instanceof String) {
					myCommand.add((String) commandArgument);
				} else {
					throw new TargetInitializationException("a non-string in command argument list at position " + i);
				}
			}
		}
	}

	@Override
	public void run() throws BuildException {
		Log.verbose("Running external command...");
		int exitCode;
		try {
			Process process = new ProcessBuilder(myCommand)
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
			throw new BuildException("exception during external compiler invocation", e);
		}
		Log.verbose("Process exited with code %d", exitCode);
	}
}
