package org.msyu.reinforce.target.javac;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.util.ExecUtil;

import java.util.ArrayList;
import java.util.List;

public class ExternalJavac extends AbstractJavac {

	@Override
	protected void compileOrDie(List<String> compilerParameters) throws ExecutionException {
		int exitCode = ExecUtil.execute(getCommandLine(compilerParameters));
		if (exitCode != 0) {
			throw new ExecutionException("compiler exited with status code: " + exitCode);
		}
	}

	private static List<String> getCommandLine(List<String> compilerParameters) {
		List<String> cl = new ArrayList<>(compilerParameters.size() + 1);
		cl.add("javac");
		cl.addAll(compilerParameters);
		return cl;
	}

}
