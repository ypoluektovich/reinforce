package org.msyu.reinforce.target;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;

public class CoreTargetFactory implements TargetFactory {

	public static final String ECHO = "echo";
	public static final String SOURCE = "source";
	public static final String JAVAC = "javac";
	public static final String ZIP = "zip";
	public static final String UNZIP = "unzip";

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
			case UNZIP:
				return new UnzipTarget(name);
			default:
				return null;
		}
	}

}
