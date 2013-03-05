package org.msyu.reinforce.target.junit;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.io.IOException;
import java.io.ObjectOutput;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class TestDescription {

	private final Set<URL> myClasspathEntries = new LinkedHashSet<>();

	private final Set<String> myClassNames = new LinkedHashSet<>();

	public void addToClasspath(URL entry) {
		myClasspathEntries.add(entry);
	}

	public void addToClasspath(Resource resource) throws TargetInitializationException {
		Path path = resource.getPath();
		if (path == null) {
			throw new TargetInitializationException("can't add virtual resource to classpath");
		}
		try {
			addToClasspath(path.toAbsolutePath().toUri().toURL());
		} catch (MalformedURLException e) {
			throw new TargetInitializationException("can't convert the resource location to URL", e);
		}
	}

	public void addAllToClasspath(ResourceCollection resourceCollection) throws TargetInitializationException {
		try {
			ResourceIterator iterator = resourceCollection.getResourceIterator();
			Resource resource;
			while ((resource = iterator.next()) != null) {
				addToClasspath(resource);
			}
		} catch (ResourceEnumerationException e) {
			throw new TargetInitializationException("error while enumerating classpath entries", e);
		}
	}

	public void addClassName(String name) {
		myClassNames.add(name);
	}


	public void serialize(ObjectOutput output) throws ExecutionException, IOException {
		output.writeObject(myClasspathEntries);
		output.writeObject(myClassNames);
	}

}
