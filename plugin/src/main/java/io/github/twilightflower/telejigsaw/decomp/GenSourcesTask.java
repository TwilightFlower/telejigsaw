package io.github.twilightflower.telejigsaw.decomp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.java.decompiler.main.Fernflower;

import io.github.twilightflower.telejigsaw.TeleJigsawExtension;
import io.github.twilightflower.telejigsaw.util.Util;

public abstract class GenSourcesTask extends DefaultTask {
	@Inject
    public abstract WorkerExecutor getWorkerExecutor();
	
	@TaskAction
	public void decompile() {
		Project project = getProject();
		var ext = project.getExtensions().getByType(TeleJigsawExtension.class);
		Object[] mcDeps = (Object[]) ext.getMinecraft();
		var mc = (List<?>) mcDeps[0];
		var libs = (List<?>) mcDeps[1];
		
		Dependency[] mcAsDeps = new Dependency[mc.size()];
		for(int i = 0; i < mcAsDeps.length; i++) {
			mcAsDeps[i] = project.getDependencies().create(mc.get(i));
		}
		
		Dependency[] libsAsDeps = new Dependency[libs.size()];
		for(int i = 0; i < libsAsDeps.length; i++) {
			libsAsDeps[i] = project.getDependencies().create(mc.get(i));
		}
		
		Set<File> mcResolved = project.getConfigurations().detachedConfiguration(mcAsDeps).resolve();
		Set<File> libsResolved = project.getConfigurations().detachedConfiguration(libsAsDeps).resolve();
		
		Path mcPath = mcResolved.iterator().next().toPath();
		String mcFName = mcPath.getFileName().toString();
		String srcFName = mcFName.substring(0, mcFName.length() - ".jar".length()) + "-sources.jar";
		Path srcPath = mcPath.resolveSibling(srcFName);
		
		File fernflower = Util.sneakyGet(() -> Paths.get(Fernflower.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile());
		
		WorkQueue queue = getWorkerExecutor().processIsolation(pw -> {
			pw.getForkOptions().jvmArgs("-Xmx3G");
			pw.getClasspath().from(fernflower);
		});

		queue.submit(DecompileAction.class, params -> {
			params.getClasspath().set(libsResolved);
			params.getCompiledPath().set(mcPath.toFile());
			params.getSourcesPath().set(srcPath.toFile());
		});
		queue.await();
	}
}
