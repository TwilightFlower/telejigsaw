package io.github.twilightflower.telejigsaw.decomp;

import java.io.File;
import java.nio.file.Paths;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.java.decompiler.main.Fernflower;

import io.github.astrarre.amalgamation.gradle.dependencies.LibrariesDependency;
import io.github.twilightflower.telejigsaw.MinecraftDependency;
import io.github.twilightflower.telejigsaw.TeleJigsawExtension;
import io.github.twilightflower.telejigsaw.util.Util;

public abstract class GenSourcesTask extends DefaultTask {
	@Inject
    public abstract WorkerExecutor getWorkerExecutor();
	
	@TaskAction
	public void decompile() {
		Project project = getProject();
		var ext = project.getExtensions().getByType(TeleJigsawExtension.class);
		Dependency[] mcDeps = ext.getMinecraft();
		var mc = (MinecraftDependency) mcDeps[0];
		var libs = (LibrariesDependency) mcDeps[1];
		
		File fernflower = Util.sneakyGet(() -> Paths.get(Fernflower.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile());
		
		WorkQueue queue = getWorkerExecutor().processIsolation(pw -> {
			pw.getForkOptions().jvmArgs("-Xmx3G");
			pw.getClasspath().from(fernflower);
		});

		queue.submit(DecompileAction.class, params -> {
			params.getClasspath().set(libs.resolve());
			params.getCompiledPath().set(mc.getMainPath().toFile());
			params.getSourcesPath().set(mc.getSourcesPath().toFile());
		});
		queue.await();
	}
}
