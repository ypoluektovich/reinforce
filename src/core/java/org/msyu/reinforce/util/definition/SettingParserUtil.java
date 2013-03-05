package org.msyu.reinforce.util.definition;

import org.msyu.reinforce.TargetInitializationException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SettingParserUtil {

	private SettingParserUtil() {
		// do not instantiate
	}


	public static <T> T parseSingle(Map defMap, Iterable<? extends SettingParser<? extends T>> parsers) throws TargetInitializationException {
		Set<?> settingKeys = Collections.unmodifiableSet(((Set<?>) defMap.keySet()));
		SettingParser<? extends T> parserToUse = null;
		for (SettingParser<? extends T> parser : parsers) {
			if (parser.isApplicable(settingKeys)) {
				if (parserToUse == null) {
					parserToUse = parser;
				} else {
					throw new AmbiguousDefinitionException();
				}
			}
		}
		if (parserToUse == null) {
			throw new UnparseableDefinitionException();
		}
		return parserToUse.parse(defMap);
	}

}
