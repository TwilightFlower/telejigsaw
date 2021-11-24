package io.github.twilightflower.telejigsaw;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.gradle.api.Action;
import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;

public class TeleJigsawExtensionImpl implements TeleJigsawExtension {
	private final MinecraftAmalgamation amalg;
	private String mcVers;
	private MappingInfo mappings = new MappingInfo();
	
	public TeleJigsawExtensionImpl(MinecraftAmalgamation amalg) {
		this.amalg = amalg;
	}
	
	@Override
	public void mappings(Action<MappingInfo> act) {
		act.execute(mappings);
	}
	
	@Override
	public void mappings(Object o) {
		mappings = new MappingInfo();
		mappings.dependency = o;
	}
	
	@Override
	public Object remap(Object dep) {
		Objects.requireNonNull(mappings.dependency, "Attempt to remap without setting mappings first");
		try {
			return amalg.map(rd -> {
				rd.mappings(mappings.dependency, mappings.intermediaryNamespace, mappings.mappedNamespace);
				rd.inputGlobal(dep);
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void minecraft(String version) {
		mcVers = version;
	}

	@Override
	public Object getMinecraft() {
		Objects.requireNonNull(mcVers, "Must set a Minecraft version to depend on Minecraft");
		Objects.requireNonNull(mappings.dependency, "Must set mappings to depend on Minecraft");
		
		try {
			List<?> mc = (List<?>) amalg.map(rd -> {
				var mt = amalg.mappings(mappings.dependency, mappings.obfNamespace, mappings.mappedNamespace);
				rd.mappings(mt);
				rd.inputGlobal(amalg.mojmerged(mcVers, mt));
			});
			
			return new Object[] {mc, amalg.libraries(mcVers)};
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
