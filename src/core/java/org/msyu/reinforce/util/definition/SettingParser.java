package org.msyu.reinforce.util.definition;

import org.msyu.reinforce.TargetInitializationException;

import java.util.Map;
import java.util.Set;

public interface SettingParser<T> {

	boolean isApplicable(Set<?> settingKeys);

	T parse(Map settings) throws TargetInitializationException;

}
