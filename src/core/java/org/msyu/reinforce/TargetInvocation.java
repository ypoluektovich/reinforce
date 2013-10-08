package org.msyu.reinforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TargetInvocation {

	private static final Pattern TARGET_SPEC_PATTERN = Pattern.compile("^([-\\w]++)(?:@(.*+))?$");

	public static List<TargetInvocation> parse(Iterable<String> targetSpecs) {
		List<TargetInvocation> invocations = new ArrayList<>();
		for (String targetSpec : targetSpecs) {
			invocations.add(parse(targetSpec));
		}
		return invocations;
	}

	public static TargetInvocation parse(String targetSpec) {
		Matcher matcher = TARGET_SPEC_PATTERN.matcher(targetSpec);
		if (!matcher.matches()) {
			return null;
		}
		String targetName = matcher.group(1);
		String paramString = matcher.group(2);
		if (paramString == null) {
			return new TargetInvocation(targetName);
		} else {
			return new TargetInvocation(targetName + "@", paramString);
		}
	}


	private final String targetName;

	private final Map<String, Object> parameters;

	public TargetInvocation(String targetName) {
		this(targetName, (Map<String, String>) null);
	}

	public TargetInvocation(String targetName, String singleParameter) {
		this(targetName, Collections.singletonMap("", singleParameter));
	}

	private TargetInvocation(String targetName, Map<String, String> parameters) {
		this.targetName = Objects.requireNonNull(targetName, "target name cannot be null");
		this.parameters = (parameters == null) ? null : new LinkedHashMap<String, Object>(parameters);
	}

	public final String getTargetName() {
		return targetName;
	}

	public final Map<String, Object> getParameters() {
		return parameters == null ? null : Collections.unmodifiableMap(parameters);
	}


	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder(targetName);
		if (parameters != null) {
			if (parameters.size() == 1 && parameters.containsKey("")) {
				sb.append(parameters.get(""));
			} else {
				sb.append(parameters.toString());
			}
		}
		return sb.toString();
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TargetInvocation)) {
			return false;
		}
		TargetInvocation other = (TargetInvocation) o;
		return targetName.equals(other.targetName) && Objects.equals(parameters, other.parameters);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(targetName, parameters);
	}

}
