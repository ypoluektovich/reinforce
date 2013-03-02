package org.msyu.reinforce.target;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.target.archive.JarTarget;
import org.msyu.reinforce.target.archive.ZipTarget;
import org.msyu.reinforce.target.javac.JavacTarget;

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
	public Target createTargetObject(String type, TargetInvocation targetInvocation) {
		switch (type) {
			case ECHO:
				return new EchoTarget(targetInvocation);
			case SOURCE:
				return new SourceTarget(targetInvocation);
			case JAVAC:
				return new JavacTarget(targetInvocation);
			case ZIP:
				return new ZipTarget(targetInvocation);
			case JAR:
				return new JarTarget(targetInvocation);
			case UNZIP:
				return new UnzipTarget(targetInvocation);
			case EXEC:
				return new ExecTarget(targetInvocation);
			case REINFORCE:
			case REIN:
				return new ReinforceTarget(targetInvocation);
			default:
				return null;
		}
	}

}
