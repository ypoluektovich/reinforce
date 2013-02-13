package org.msyu.reinforce.target;

import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.TargetInitializationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class InternalSunJavac extends AbstractJavac {

	private final Object compiler;

	private final Method compile;

	@SuppressWarnings("unchecked")
	public InternalSunJavac() throws TargetInitializationException{
		try {
			Class c = Class.forName("com.sun.tools.javac.Main");
			compiler = c.newInstance();
			compile = c.getMethod("compile", new Class[]{(new String[]{}).getClass()});
		} catch (ClassNotFoundException e) {
			throw new TargetInitializationException("failed to access compiler class", e);
		} catch (InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
			throw new TargetInitializationException("failed to acquire compiler instance", e);
		} catch (NoSuchMethodException e) {
			throw new TargetInitializationException("available compiler is incompatible", e);
		}
	}

	@Override
	protected void compileOrDie(List<String> compilerParameters) throws ExecutionException {
		int result;
		try {
			Log.verbose("Invoking compiler");
			result = (Integer) compile.invoke(
					compiler,
					new Object[]{ compilerParameters.toArray(new String[compilerParameters.size()]) }
			);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ExecutionException("failed to run compiler", e);
		}
		Log.verbose("Compiler returned exit code %d", result);
		if (result != 0) {
			throw new ExecutionException("compiler exited with status code: " + result);
		}
	}

}
