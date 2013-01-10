package org.msyu.reinforce;

import java.io.InputStream;

public interface TargetDefinitionStreamSource {
	InputStream getStreamForTarget(String name) throws TargetDefinitionLoadingException;
}
