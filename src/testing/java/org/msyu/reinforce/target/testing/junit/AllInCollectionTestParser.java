package org.msyu.reinforce.target.testing.junit;

import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceCollections;
import org.msyu.reinforce.resources.ResourceConstructionException;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.util.definition.FixedKeyParser;

import java.nio.file.Path;

class AllInCollectionTestParser extends FixedKeyParser<Void> {

	private static final String DOT_CLASS = ".class";

	private final TestDescription myTestDescription;

	AllInCollectionTestParser(TestDescription testDescription) {
		super("all in");
		myTestDescription = testDescription;
	}

	@Override
	protected Void parseSetting(Object setting) throws TargetInitializationException {
		ResourceCollection resourceCollection;
		try {
			resourceCollection = ResourceCollections.interpret(setting);
		} catch (ResourceConstructionException e) {
			throw new TargetInitializationException("failed to interpret the value of the 'all in' setting as a resource collection");
		}

		boolean addedTests = iterateAndAddTests(resourceCollection);

		if (addedTests) {
			addClasspathEntry(resourceCollection);
		}
		return null;
	}

	private boolean iterateAndAddTests(ResourceCollection resourceCollection) throws TargetInitializationException {
		boolean addedTests = false;
		try {
			ResourceIterator iterator = resourceCollection.getResourceIterator();
			Resource resource;
			while ((resource = iterator.next()) != null) {
				if (!resource.getAttributes().isRegularFile()) {
					continue;
				}
				Path relativePath = resource.getRelativePath();
				if (!relativePath.getFileName().toString().endsWith(DOT_CLASS)) {
					continue;
				}
				String className = relativePath.toString().replace(relativePath.getFileSystem().getSeparator(), ".");
				className = className.substring(0, className.length() - DOT_CLASS.length());
				myTestDescription.addClassName(className);
				addedTests = true;
			}
		} catch (ResourceEnumerationException | ResourceAccessException e) {
			throw new TargetInitializationException("error while enumerating potential classes", e);
		}
		return addedTests;
	}

	private void addClasspathEntry(ResourceCollection resourceCollection) throws TargetInitializationException {
		Resource root = resourceCollection.getRoot();
		if (root == null) {
			throw new TargetInitializationException("can't add virtual resource collection to classpath");
		}
		myTestDescription.addToClasspath(root);
	}

}
