package io.github.twilightflower.telejigsaw;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gradle.api.artifacts.Dependency;

import io.github.astrarre.amalgamation.gradle.dependencies.AbstractSelfResolvingDependency;

public class MinecraftDependency extends AbstractSelfResolvingDependency {
	private final AbstractSelfResolvingDependency of;
	
	public MinecraftDependency(AbstractSelfResolvingDependency of) {
		super(of.project, of.getGroup(), of.getName(), of.getVersion());
		this.of = of;
	}

	@Override
	public Dependency copy() {
		return this;
	}

	@Override
	protected Iterable<Path> resolvePaths() throws IOException {
		
		Set<File> base = of.resolve();
		List<Path> paths = new ArrayList<>();
		base.stream().map(File::toPath).forEach(paths::add);
		
		return paths;
	}

	private static String truncateFileName(String name) {
		int idx = name.lastIndexOf('_');
		if(idx != -1) {
			return name.substring(0, idx);
		} else {
			return name.substring(0, name.length() - 4); // cut off .jar
		}
	}
	
	public Path getMainPath() {
		Set<File> base = of.resolve();
		for(File f : base) {
			if(f.toPath().getFileName().toString().endsWith("_0.jar")) {
				return f.toPath();
			}
		}
		
		return null;
	}
	
	public Path getSourcesPath() {
		Path mc = getMainPath();
		String mcName = mc.getFileName().toString();
		String sourcesName = truncateFileName(mcName) + "-sources_qghack.jar";
		return mc.resolveSibling(sourcesName);
	}
}
