package org.msyu.reinforce.target.testing.junit;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.target.ActionOnEmptySource;
import org.msyu.reinforce.target.testing.ATestDescription;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.LinkedHashSet;
import java.util.Set;

public class TestDescription extends ATestDescription {

	private final Set<String> myClassNames = new LinkedHashSet<>();

	public TestDescription(ActionOnEmptySource onMissing) {
		super(onMissing);
	}

	public void addClassName(String name) {
		myClassNames.add(name);
	}


	public void serialize(ObjectOutput output) throws ExecutionException, IOException {
		output.writeObject(getClasspathEntries());
		output.writeObject(myClassNames);
	}

}
