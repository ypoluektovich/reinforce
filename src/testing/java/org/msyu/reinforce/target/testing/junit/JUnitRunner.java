package org.msyu.reinforce.target.testing.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JUnitRunner {

	public static void main(String[] args) {
		for (String arg : args) {
			try {
				processTestDescriptionFile(arg);
				System.out.println("All tests passed");
			} catch (TestsFailedException e) {
				System.out.println("Some tests failed! Listing:");
				for (Failure failure : e.getResult().getFailures()) {
					System.out.println(failure);
				}
				System.exit(1);
			} catch (JUnitRunnerException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
	}

	private static void processTestDescriptionFile(String arg) throws JUnitRunnerException {
		System.out.printf("Processing: %s%n", arg);

		Set<URL> classpath;
		Set<String> tests;
		try (ObjectInput input = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(arg))))) {
			classpath = readObjectAndCast(input);
			tests = readObjectAndCast(input);
		} catch (IOException | ClassNotFoundException e) {
			throw new JUnitRunnerException("error while reading test description", e);
		}

		Result result;
		try (URLClassLoader classLoader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]))) {
			List<Class<?>> testClasses = new ArrayList<>();
			for (String testClassName : tests) {
				try {
					System.out.println("Loading class " + testClassName);
					Class<?> testClass = classLoader.loadClass(testClassName);
					testClasses.add(testClass);
				} catch (ClassNotFoundException e) {
					throw new JUnitRunnerException("a test class is missing from classpath: " + testClassName);
				}
			}
			result = JUnitCore.runClasses(testClasses.toArray(new Class[testClasses.size()]));
		} catch (IOException e) {
			throw new JUnitRunnerException("error while closing the class loader", e);
		}

		if (!result.wasSuccessful()) {
			throw new TestsFailedException(result);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T readObjectAndCast(ObjectInput input) throws IOException, ClassNotFoundException {
		return (T) input.readObject();
	}

}
