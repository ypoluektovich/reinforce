package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.Build;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class Variables {

	private Variables() {
		// do not instantiate
	}

	public static String expand(String source) throws VariableSubstitutionException {
		return expand(source, Build.getCurrent().getVariables());
	}

	public static String expand(String source, Map<String, String> variables) throws VariableSubstitutionException {
		try (Reader input = new StringReader(source); StringWriter output = new StringWriter()) {
			expand(input, variables, output, true);
			return output.toString();
		} catch (IOException e) {
			throw new VariableSubstitutionException("impossible IO error while expanding variables", e);
		}
	}

	private static void expand(Reader input, Map<String, String> variables, Writer output, boolean noEofOnBrace)
			throws IOException, VariableSubstitutionException
	{
		int c;
		while (isNotEof(c = markAndRead(input), noEofOnBrace)) {
			if (c == '\\') {
				c = input.read();
				if (c == -1) {
					throw new TrailingEscapeException();
				}
				output.write(c);
				input.mark(0);
			} else if (c == '$') {
				c = markAndRead(input);
				if (isEof(c, noEofOnBrace)) {
					output.write('$');
				} else if (c == '{') {
					StringWriter innerOutput = new StringWriter();
					expand(input, variables, innerOutput, false);
					c = input.read();
					if (c != '}') {
						throw new UnclosedVariableException();
					}
					input.mark(0);
					String variableName = innerOutput.toString();
					if (variables.containsKey(variableName)) {
						output.write(variables.get(variableName));
					} else {
						throw new UndefinedVariableException(variableName);
					}
				} else {
					input.reset();
					output.write('$');
				}
			} else {
				output.write(c);
			}
		}
		input.reset();
	}

	private static int markAndRead(Reader input) throws IOException {
		input.mark(1);
		return input.read();
	}

	private static boolean isEof(int c, boolean noEofOnBrace) {
		return (c == -1) || (!noEofOnBrace && (c == '}'));
	}

	private static boolean isNotEof(int c, boolean noEofOnBrace) {
		return (c != -1) && ((c != '}') || noEofOnBrace);
	}

}
