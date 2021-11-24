package io.github.twilightflower.telejigsaw;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;

public interface TeleJigsawExtension {
	void minecraft(String version);
	void mappings(Action<MappingInfo> act);
	void mappings(Object o);
	Dependency remap(Object dep);
	Dependency[] getMinecraft();
}
