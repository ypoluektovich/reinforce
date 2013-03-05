package org.msyu.reinforce;

public class Log {

	public static final String LOG_LEVEL_PROPERTY = "reinforce.logging";

	public static enum Level {
		ANY(Integer.MIN_VALUE, ""),
		DEBUG(-2, "dd"),
		VERBOSE(-1, "vv"),
		INFO(0, "II"),
		WARN(1, "WW"),
		ERROR(1, "EE");

		private final int myLevelCode;

		private final String myPrefix;

		private Level(int levelCode, String prefix) {
			this.myLevelCode = levelCode;
			this.myPrefix = prefix;
		}

		public boolean acceptMessageOfLevel(Level other) {
			return this.myLevelCode <= other.myLevelCode;
		}

		public String getPrefix() {
			return myPrefix;
		}
	}

	private static Level ourLevel = Level.INFO;

	static {
		String loggingLevel = System.getProperty(LOG_LEVEL_PROPERTY);
		if (loggingLevel != null) {
			setLevel(loggingLevel);
		}
	}

	public static boolean setLevel(String levelCode) {
		for (Level level : Level.values()) {
			if (level.name().equalsIgnoreCase(levelCode)) {
				ourLevel = level;
				log(Level.ANY, "Set logging level to %s", level);
				return true;
			}
		}
		return false;
	}

	public static Level getLevel() {
		return ourLevel;
	}


	public static void error(String message, Object... parameters) {
		if (ourLevel.acceptMessageOfLevel(Level.ERROR)) {
			log(Level.ERROR, message, parameters);
		}
	}

	public static void warn(String message, Object... parameters) {
		if (ourLevel.acceptMessageOfLevel(Level.WARN)) {
			log(Level.WARN, message, parameters);
		}
	}

	public static void info(String message, Object... parameters) {
		if (ourLevel.acceptMessageOfLevel(Level.INFO)) {
			log(Level.INFO, message, parameters);
		}
	}

	public static void verbose(String message, Object... parameters) {
		if (ourLevel.acceptMessageOfLevel(Level.VERBOSE)) {
			log(Level.VERBOSE, message, parameters);
		}
	}

	public static void debug(String message, Object... parameters) {
		if (ourLevel.acceptMessageOfLevel(Level.DEBUG)) {
			log(Level.DEBUG, message, parameters);
		}
	}

	private static void log(Level level, String message, Object... parameters) {
		System.out.printf("%-4s", level.getPrefix());
		System.out.printf(message, parameters);
		System.out.println();
	}

	public static void stackTrace(Throwable t) {
		if (ourLevel.acceptMessageOfLevel(Level.DEBUG)) {
			t.printStackTrace();
		}
	}

}
