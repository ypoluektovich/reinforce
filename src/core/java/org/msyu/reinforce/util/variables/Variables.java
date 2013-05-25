package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.Build;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
	public static VariableSource sourceFromMap(final Map<String, Object> map) {
		if (map == null) {
			return EmptyVariableSource.INSTANCE;
		}
		return new VariableSource() {
			@Override
			public boolean isDefined(String variableName) {
				return map.containsKey(variableName);
			}

			@Override
			public Object getValueOf(String variableName) throws UndefinedVariableException {
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
			public Object getValueOf(String variableName) throws UndefinedVariableException {
				for (VariableSource source : sources) {
					if (source != null && source.isDefined(variableName)) {
						return source.getValueOf(variableName);
					}
				}
				throw new UndefinedVariableException(variableName);
			}
		};
	}

	public static Object expand(String source) throws VariableSubstitutionException {
		return expand(source, Build.getCurrent().getContextVariables());
	}

	public static Object expand(String source, VariableSource variables)
			throws VariableSubstitutionException, ImpossibleIOException
	{
		try (Reader input = new StringReader(source)) {
			Sink sink = new Sink();
			expand(input, variables, true, sink);
			return sink.getResult();
		} catch (IOException e) {
			throw new ImpossibleIOException(e);
		}
	}

	private static final class Sink {

		private Object nonStringResult = null;

		private StringBuilder stringResult = null;

		public final void writeChar(int c) throws ConcatException {
			if (nonStringResult != null) {
				throw new ConcatException(false, true);
			}
			getStringBuilder().append((char) c);
		}

		public final void writeObject(Object o) throws ConcatException {
			if (o instanceof String) {
				if (nonStringResult != null) {
					throw new ConcatException(false, true);
				}
				getStringBuilder().append((String) o);
			} else {
				if (stringResult != null) {
					throw new ConcatException(true, false);
				}
				if (nonStringResult != null) {
					throw new ConcatException(false, false);
				}
				nonStringResult = o;
			}
		}

		private StringBuilder getStringBuilder() {
			return stringResult == null ?
					(stringResult = new StringBuilder()) :
					stringResult;
		}

		public final Object getResult() {
			return (nonStringResult != null) ?
					nonStringResult :
					(stringResult != null) ?
							stringResult.toString() :
							"";
		}

	}

	private static void expand(Reader input, VariableSource variables, boolean noEofOnBrace, Sink sink)
			throws IOException, VariableSubstitutionException
	{
		int c;
		while (isNotEof(c = markAndRead(input), noEofOnBrace)) {
			if (c == '\\') {
				c = input.read();
				if (c == -1) {
					throw new TrailingEscapeException();
				}
				sink.writeChar(c);
				input.mark(0);
			} else if (c == '$') {
				c = markAndRead(input);
				if (isEof(c, noEofOnBrace)) {
					sink.writeChar('$');
				} else if (c == '{') {
					Sink innerSink = new Sink();
					expand(input, variables, false, innerSink);
					c = input.read();
					if (c != '}') {
						throw new UnclosedVariableException();
					}
					input.mark(0);
					Object innerResult = innerSink.getResult();
					if (innerResult instanceof String) {
						sink.writeObject(variables.getValueOf((String) innerResult));
					} else {
						throw new NonStringVariableNameException();
					}
				} else {
					input.reset();
					sink.writeChar('$');
				}
			} else {
				sink.writeChar(c);
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
