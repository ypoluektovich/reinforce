package org.msyu.reinforce.resources;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexResourceFilter implements ResourceFilter {

	private final Pattern myPattern;

	public RegexResourceFilter(String regex) throws ResourceConstructionException {
		try {
			myPattern = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			throw new ResourceConstructionException("can't filter resources with specified regex", e);
		}
	}

	@Override
	public boolean fits(Resource resource) throws ResourceEnumerationException {
		return myPattern.matcher(resource.getRelativePath().toString()).find();
	}

}
