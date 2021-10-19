package org.quiltmc.gradle;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;

public interface QuiltGradleExtension {
	void minecraft(String version);
	void mappings(Action<MappingInfo> act);
	void mappings(Object o);
	Dependency remap(Object dep);
	Dependency[] getMinecraft();
}
