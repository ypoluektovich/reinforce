package org.msyu.reinforce;

public interface TargetRepository {

	Target getTarget(String name) throws NoSuchTargetException;

}
