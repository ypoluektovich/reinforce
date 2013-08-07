package org.msyu.reinforce.target;

import org.msyu.reinforce.TargetInitializationException;

import java.util.Map;

public enum ActionOnEmptySource {

	DIE("die"), WARN("warn"), SKIP("skip");

	private final String mySetting;

	ActionOnEmptySource(String setting) {
		mySetting = setting;
	}

	@Override
	public String toString() {
		return mySetting;
	}

	public static ActionOnEmptySource parse(Map docMap, String key) throws TargetInitializationException {
		ActionOnEmptySource result = null;
		if (docMap.containsKey(key)) {
			Object setting = docMap.get(key);
			for (ActionOnEmptySource action : values()) {
				if (action.toString().equals(setting)) {
					result = action;
				}
			}
			if (result == null) {
				throw new TargetInitializationException("unsupported value of '" + key + "'");
			}
		} else {
			result = DIE;
		}
		return result;
	}

}
