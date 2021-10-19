package org.quiltmc.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;

import java.util.Map;

public class QuiltGradlePlugin implements Plugin<Project> {
	public void apply(Project project) {
		project.apply(Map.of("plugin", "amalgamation-minecraft"));
		MinecraftAmalgamation amalg = project.getExtensions().getByType(MinecraftAmalgamation.class);
		
		project.getExtensions().create(QuiltGradleExtension.class, "quilt", QuiltGradleExtensionImpl.class, amalg);
	}
}
