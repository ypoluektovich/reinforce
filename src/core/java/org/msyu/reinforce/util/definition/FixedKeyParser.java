package org.msyu.reinforce.util.definition;

import org.msyu.reinforce.TargetInitializationException;

import java.util.Map;
import java.util.Set;

public abstract class FixedKeyParser<T> implements SettingParser<T> {

	private final Object myKey;

	public FixedKeyParser(Object key) {
		myKey = key;
	}

	@Override
	public final boolean isApplicable(Set<?> settingKeys) {
		return settingKeys.contains(myKey);
	}

	@Override
	public final T parse(Map settings) throws TargetInitializationException {
		return parseSetting(settings.get(myKey));
	}

	protected abstract T parseSetting(Object setting) throws TargetInitializationException;

}
