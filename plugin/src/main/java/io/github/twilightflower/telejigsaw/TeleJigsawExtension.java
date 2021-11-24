package io.github.twilightflower.telejigsaw;

import org.gradle.api.Action;

public interface TeleJigsawExtension {
	void minecraft(String version);
	void mappings(Action<MappingInfo> act);
	void mappings(Object o);
	Object remap(Object dep);
	Object getMinecraft();
}
