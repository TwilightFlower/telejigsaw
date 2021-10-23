package org.quiltmc.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.quiltmc.gradle.decomp.GenSourcesTask;
import org.quiltmc.gradle.eclipse.EclipseConfig;

import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;

import java.util.Map;

public class QuiltGradlePlugin implements Plugin<Project> {
	public void apply(Project project) {
		project.apply(Map.of("plugin", "amalgamation-minecraft"));
		MinecraftAmalgamation amalg = project.getExtensions().getByType(MinecraftAmalgamation.class);
		
		project.getTasks().create("genSources", GenSourcesTask.class);
		project.getExtensions().create(QuiltGradleExtension.class, "quilt", QuiltGradleExtensionImpl.class, amalg);
		
		project.getPlugins().withId("eclipse", e -> EclipseConfig.configEclipse(project));
	}
}
