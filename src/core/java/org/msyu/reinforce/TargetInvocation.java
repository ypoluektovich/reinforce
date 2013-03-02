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

	private static final Pattern TARGET_SPEC_PATTERN = Pattern.compile("^([-\\w]++)(?:@(.++))?$");

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

	private static void serializeParameters(Map<String, String> parameters, StringBuilder sb) {
		sb.append(parameters.toString());
	}


	private final String myTargetName;

	private final Map<String, String> myParameters;

	public TargetInvocation(String targetName) {
		this(targetName, (Map<String, String>) null);
	}

	public TargetInvocation(String targetName, String singleParameter) {
		this(targetName, Collections.singletonMap("", singleParameter));
	}

	private TargetInvocation(String targetName, Map<String, String> parameters) {
		myTargetName = Objects.requireNonNull(targetName, "target name cannot be null");
		myParameters = (parameters == null) ? null : new LinkedHashMap<>(parameters);
	}

	public final String getTargetName() {
		return myTargetName;
	}

	public final Map<String, String> getParameters() {
		return myParameters == null ? null : Collections.unmodifiableMap(myParameters);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(myTargetName);
		if (myParameters != null) {
			if (myParameters.size() == 1 && myParameters.containsKey("")) {
				sb.append(myParameters.get(""));
			} else {
				serializeParameters(myParameters, sb);
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TargetInvocation)) {
			return false;
		}
		TargetInvocation other = (TargetInvocation) o;
		return myTargetName.equals(other.myTargetName) && Objects.equals(myParameters, other.myParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(myTargetName, myParameters);
	}

}
