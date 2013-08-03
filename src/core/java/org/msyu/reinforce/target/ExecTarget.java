package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.util.ExecUtil;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExecTarget extends Target {

	private static final String COMMAND_KEY = "command";

	private List<String> myCommand;

	public ExecTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected void initTarget(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(COMMAND_KEY)) {
			throw new TargetInitializationException("missing required parameter: " + COMMAND_KEY);
		}
		Object commandObject = docMap.get(COMMAND_KEY);
		if (commandObject instanceof List) {
			List commandList = (List) commandObject;
			myCommand = new ArrayList<>(commandList.size());
			for (int i = 0; i < commandList.size(); i++) {
				myCommand.add(expandCommandArgument(commandList.get(i), i));
			}
		} else {
			myCommand = Arrays.asList(expandCommandArgument(commandObject, 0));
		}
	}

	private String expandCommandArgument(Object argumentSetting, int index) throws TargetInitializationException {
		if (!(argumentSetting instanceof String)) {
			throw new TargetInitializationException("a non-string in command argument list at position " + index);
		}
		String expandedArgument;
		try {
			Object expandedArgumentObject = Variables.expand((String) argumentSetting);
			if (!(expandedArgumentObject instanceof String)) {
				throw new TargetInitializationException("argument at position " + index +
						" was variable-expanded to a non-string");
			}
			expandedArgument = (String) expandedArgumentObject;
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding variables in command argument #" + index, e);
		}
		return expandedArgument;
	}

	@Override
	public void run() throws ExecutionException {
		int exitCode = ExecUtil.execute(myCommand);
		if (exitCode != 0) {
			throw new ExecutionException("external process exited with non-zero code: " + exitCode);
		}
	}

}
