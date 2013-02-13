package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.resources.ResourceCollection;

import java.nio.file.Path;

public interface JavaCompiler {

	Path execute(String targetName, ResourceCollection sources, ResourceCollection classpath) throws ExecutionException;

}
