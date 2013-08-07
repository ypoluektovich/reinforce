package org.msyu.reinforce.util;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;

import java.io.IOException;
import java.nio.file.Path;

public class JavaClasspath {

	public static String stringFromResourceCollection(ResourceCollection classpath, String separator) throws ResourceEnumerationException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			fromResourceCollection(classpath, stringBuilder, null, separator, null);
		} catch (IOException impossible) {
			// StringBuilder doesn't throw IOException
		}
		return stringBuilder.toString();
	}

	public static void fromResourceCollection(
			ResourceCollection classpath,
			Appendable sink,
			CharSequence beforeFirst,
			CharSequence separator,
			CharSequence afterLast
	) throws NullPointerException, ResourceEnumerationException, IOException {
		if (classpath == null) {
			Log.debug("No classpath");
			return;
		}
		Log.debug("Enumerating classpath entries...");
		ResourceIterator cpIterator = classpath.getResourceIterator();
		Resource cpElement = cpIterator.next();
		if (cpElement == null) {
			Log.debug("Classpath is empty, skipping");
		} else {
			Path cpElementPath = Build.getCurrent().getBasePath().resolve(cpElement.getPath());

			Log.debug("First classpath entry: %s", cpElementPath);
			sink.append(beforeFirst);
			sink.append(cpElementPath.toString());

			while ((cpElement = cpIterator.next()) != null) {
				cpElementPath = Build.getCurrent().getBasePath().resolve(cpElement.getPath());
				Log.debug("Next classpath entry: %s", cpElementPath);
				sink.append(separator);
				sink.append(cpElementPath.toString());
			}

			Log.debug("Classpath enumerated");
			sink.append(afterLast);
		}
	}

}
