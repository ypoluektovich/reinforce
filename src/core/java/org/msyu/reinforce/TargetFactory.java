package org.msyu.reinforce;

public interface TargetFactory {

	Target createTargetObject(String type, TargetInvocation invocation);

}
