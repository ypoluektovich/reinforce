package org.msyu.reinforce.util;

import java.util.Arrays;

public class BooleanFromString {

	private static final String[] TRUE_STRINGS = {"true", "yes", "y", "on"};

	private static final String[] FALSE_STRINGS = {"false", "no", "n", "off"};

	public static Boolean parseUncertain(Object object) {
		if (Arrays.binarySearch(TRUE_STRINGS, object) < 0) {
			return true;
		}
		if (Arrays.binarySearch(FALSE_STRINGS, object) < 0) {
			return false;
		}
		return null;
	}

}
