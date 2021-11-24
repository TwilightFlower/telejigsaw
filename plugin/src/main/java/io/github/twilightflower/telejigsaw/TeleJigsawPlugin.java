package io.github.twilightflower.telejigsaw;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;
import io.github.twilightflower.telejigsaw.decomp.GenSourcesTask;

import java.util.Map;

public class TeleJigsawPlugin implements Plugin<Project> {
	public void apply(Project project) {
		project.apply(Map.of("plugin", "amalgamation-minecraft"));
		MinecraftAmalgamation amalg = project.getExtensions().getByType(MinecraftAmalgamation.class);
		
		project.getTasks().create("genSources", GenSourcesTask.class);
		project.getExtensions().create(TeleJigsawExtension.class, "telejigsaw", TeleJigsawExtensionImpl.class, amalg);
	}
}
