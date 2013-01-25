package org.msyu.reinforce.target;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.target.archive.JarTarget;
import org.msyu.reinforce.target.archive.ZipTarget;

public class CoreTargetFactory implements TargetFactory {

	public static final String ECHO = "echo";
	public static final String SOURCE = "source";
	public static final String JAVAC = "javac";
	public static final String ZIP = "zip";
	public static final String JAR = "jar";
	public static final String UNZIP = "unzip";
	public static final String EXEC = "exec";
	public static final String REINFORCE = "reinforce";
	public static final String REIN = "rein";

	@Override
	public Target createTargetObject(String type, String name) {
		switch (type) {
			case ECHO:
				return new EchoTarget(name);
			case SOURCE:
				return new SourceTarget(name);
			case JAVAC:
				return new JavacTarget(name);
			case ZIP:
				return new ZipTarget(name);
			case JAR:
				return new JarTarget(name);
			case UNZIP:
				return new UnzipTarget(name);
			case EXEC:
				return new ExecTarget(name);
			case REINFORCE:
			case REIN:
				return new ReinforceTarget(name);
			default:
				return null;
		}
	}

}
