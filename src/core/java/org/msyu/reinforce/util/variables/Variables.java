package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.Build;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

public class Variables {

	private Variables() {
		// do not instantiate
	}

	/**
	 * Construct a {@code VariableSource} from a map.
	 *
	 * @param map    the map of variable names to values.
	 *
	 * @return a new {@code VariableSource} that takes the variable values from the specified map.
	 * If {@code map == null}, {@link EmptyVariableSource#INSTANCE an empty VariableSource} is returned.
	 */
	public static VariableSource sourceFromMap(final Map<String, String> map) {
		if (map == null) {
			return EmptyVariableSource.INSTANCE;
		}
		return new VariableSource() {
			@Override
			public boolean isDefined(String variableName) {
				return map.containsKey(variableName);
			}

			@Override
			public String getValueOf(String variableName) throws UndefinedVariableException {
				if (map.containsKey(variableName)) {
					return map.get(variableName);
				}
				throw new UndefinedVariableException(variableName);
			}
		};
	}

	public static VariableSource sourceFromChain(VariableSource... sources) {
		return sourceFromChain((sources == null) ? null : Arrays.asList(sources));
	}

	/**
	 * Construct a {@code VariableSource} that searches for variables in a sequence of other sources.
	 * <p/>
	 * The {@code VariableSource} returned by this method iterates over the specified sources and tests for
	 * the presence of the required variable. If the variable is found, the iteration is aborted and the value
	 * of the found variable returned. Thus, variables in sources that appear first in the specified {@link Iterable}
	 * override the variables in the latter sources.
	 * <p/>
	 * {@code null} sources within the {@code Iterable} are ignored. If {@code sources == null},
	 * {@link EmptyVariableSource#INSTANCE an empty VariableSource} is returned by this method.
	 *
	 * @param sources    where to search for the variables.
	 *
	 * @return a new chained {@code VariableSource}, or an empty one if {@code sources == null}.
	 */
	public static VariableSource sourceFromChain(final Iterable<VariableSource> sources) {
		if (sources == null) {
			return EmptyVariableSource.INSTANCE;
		}
		return new VariableSource() {
			@Override
			public boolean isDefined(String variableName) {
				for (VariableSource source : sources) {
					if (source != null && source.isDefined(variableName)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getValueOf(String variableName) throws UndefinedVariableException {
				for (VariableSource source : sources) {
					if (source != null && source.isDefined(variableName)) {
						return source.getValueOf(variableName);
					}
				}
				throw new UndefinedVariableException(variableName);
			}
		};
	}

	public static String expand(String source) throws VariableSubstitutionException {
		return expand(source, Build.getCurrent().getContextVariables());
	}

	public static String expand(String source, VariableSource variables) throws VariableSubstitutionException {
		try (Reader input = new StringReader(source); StringWriter output = new StringWriter()) {
			expand(input, variables, output, true);
			return output.toString();
		} catch (IOException e) {
			throw new VariableSubstitutionException("impossible IO error while expanding variables", e);
		}
	}

	private static void expand(Reader input, VariableSource variables, Writer output, boolean noEofOnBrace)
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
					output.write(variables.getValueOf(innerOutput.toString()));
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
