package org.quiltmc.gradle.eclipse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.Project;
import org.gradle.plugins.ide.eclipse.model.Classpath;
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.eclipse.model.Library;
import org.quiltmc.gradle.MinecraftDependency;
import org.quiltmc.gradle.QuiltGradleExtension;

public class EclipseConfig {
	public static void configEclipse(Project proj) {
		System.out.println("yeeee");
		var eclipse = proj.getExtensions().getByType(EclipseModel.class);
		var quilt = proj.getExtensions().getByType(QuiltGradleExtension.class);
		// why is this necessary? something calls setWhenMerged! Thanks gradle
		proj.afterEvaluate(p -> {
			eclipse.getClasspath().getFile().whenMerged(c -> {
				System.out.println("yee WHENMERGED");
				var classpath = (Classpath) c;
				var mc = (MinecraftDependency) quilt.getMinecraft()[0];
				Path sources = mc.getSourcesPath().toAbsolutePath();
				Path main = mc.getMainPath().toAbsolutePath();
				if(Files.exists(sources)) {
					System.out.println("mcsrc");
					for(ClasspathEntry entry : classpath.getEntries()) {
						if(entry instanceof Library lib) {
							Path libP = Paths.get(lib.getPath()).toAbsolutePath();
							System.out.println(libP);
							if(libP.equals(main)) {
								System.out.println("Setting srcpath");
								lib.setSourcePath(classpath.fileReference(sources.toFile()));
							}
						}
					}
				}
			});
		});
	}
}
