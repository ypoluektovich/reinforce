package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceAccessException;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceDefinitionYamlParser;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class SourceTarget extends Target implements ResourceCollection {

	private ResourceCollection myResourceCollection;

	public SourceTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		myResourceCollection = ResourceDefinitionYamlParser.parseAsCollection(docMap, dependencyTargetByName);
	}

	@Override
	public void run() throws ExecutionException {
		try {
			rebuildCache();
		} catch (ResourceEnumerationException e) {
			throw new ExecutionException("error while enumerating source files", e);
		}
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myResourceCollection.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myResourceCollection.rebuildCache();
	}

	@Override
	public Resource getRoot() {
		return new Resource() {
			@Override
			public Path getPath() {
				return myResourceCollection.getRoot().getPath();
			}

			@Override
			public BasicFileAttributes getAttributes() throws ResourceAccessException {
				return myResourceCollection.getRoot().getAttributes();
			}

			@Override
			public Path getRelativePath() {
				return myResourceCollection.getRoot().getRelativePath();
			}
		};
	}

}
