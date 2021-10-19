package org.quiltmc.gradle;

import java.util.Objects;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;

import io.github.astrarre.amalgamation.gradle.dependencies.AbstractSelfResolvingDependency;
import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;

public class QuiltGradleExtensionImpl implements QuiltGradleExtension {
	private final MinecraftAmalgamation amalg;
	private String mcVers;
	private MappingInfo mappings = new MappingInfo();
	
	public QuiltGradleExtensionImpl(MinecraftAmalgamation amalg) {
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
	public Dependency remap(Object dep) {
		Objects.requireNonNull(mappings.dependency, "Attempt to remap without setting mappings first");
		Dependency d = amalg.map(rd -> {
			rd.mappings(mappings.dependency, mappings.intermediaryNamespace, mappings.mappedNamespace);
			rd.remap(dep, true);
		});
		System.out.println(((AbstractSelfResolvingDependency) d).resolve());
		return d; 
	}
	
	@Override
	public void minecraft(String version) {
		mcVers = version;
	}

	@Override
	public Dependency[] getMinecraft() {
		Objects.requireNonNull(mcVers, "Must set a Minecraft version to depend on Minecraft");
		Objects.requireNonNull(mappings.dependency, "Must set mappings to depend on Minecraft");
		
		Dependency mc = amalg.map(rd -> {
			rd.mappings(mappings.dependency, mappings.obfNamespace, mappings.mappedNamespace);
			rd.remap(amalg.mojmerged(mcVers), true);
		});
		
		return new Dependency[] {mc, amalg.libraries(mcVers)};
	}
}
