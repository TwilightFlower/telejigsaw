/*
 * Telejigsaw
 * Copyright (C) 2021-2022  TwilightFlower
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.twilightflower.telejigsaw;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;
import io.github.astrarre.amalgamation.gradle.tasks.remap.RemapJar;
import io.github.astrarre.amalgamation.gradle.tasks.remap.RemapSourcesJar;

public class TeleJigsawExtensionImpl implements TeleJigsawExtension {
	private final MinecraftAmalgamation amalg;
	private final Project project;
	final LaunchesImpl launches = new LaunchesImpl();
	private String mcVers;
	private MappingInfo mappings = new MappingInfo();
	private Object fernflower = "org.quiltmc:quiltflower:1.7.0";
	private boolean addedDli = false;
	
	public TeleJigsawExtensionImpl(MinecraftAmalgamation amalg, Project project) {
		this.amalg = amalg;
		this.project = project;
	}
	
	@Override
	public void mappings(Action<MappingInfo> act) {
		act.execute(mappings);
		if(mappings.classpath) {
			project.getDependencies().add("runtimeClasspath", mappings.dependency);
		}
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
	public void fernflower(Object dep) {
		fernflower = dep;
	}
	
	@Override
	public Object getMinecraft() {
		Objects.requireNonNull(mcVers, "Must set a Minecraft version to depend on Minecraft");
		Objects.requireNonNull(mappings.dependency, "Must set mappings to depend on Minecraft");
		
		try {
			var mt = amalg.mappings(mappings.dependency, mappings.obfNamespace, mappings.mappedNamespace);
			var libs = amalg.libraries(mcVers);
			var mc = amalg.map(rd -> {
				rd.mappings(mt);
				rd.inputGlobal(amalg.mojmerged(mcVers, mt));
			});
			var decompMc = amalg.decompile(dd -> {
				dd.fernflower(fernflower);
				dd.inputGlobal(mc);
				dd.classpath(libs);
			});
			return new Object[] {decompMc, libs, project.files(amalg.natives(mcVers))};
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RemapJar remapJar(String name, Action<? super RemapJar> action) {
		RemapJar task = project.getTasks().create(name, RemapJar.class);
		task.setGroup("build");
		task.getClasspath().set(project.getConfigurations().getAt("compileClasspath"));
		task.mappings(mappings.dependency, mappings.mappedNamespace, mappings.intermediaryNamespace);
		Task jarTask = project.getTasks().getAt("jar");
		CopySpec copySpec = project.copySpec().from(jarTask);
		task.with(copySpec);
		task.dependsOn(jarTask);
		task.useExperimentalMixinRemapper();
		action.execute(task);
		return task;
	}
	
	@Override
	public RemapSourcesJar remapSourcesJar(String name, Action<? super RemapSourcesJar> action) {
		Task sourcesJarTask = project.getTasks().getAt("sourcesJar");
		RemapSourcesJar task = project.getTasks().create(name, RemapSourcesJar.class);
		task.setGroup("build");
		task.getClasspath().set(project.getConfigurations().getAt("compileClasspath"));
		task.mappings(mappings.dependency, mappings.mappedNamespace, mappings.intermediaryNamespace);
		CopySpec copySpec = project.copySpec().from(sourcesJarTask);
		task.with(copySpec);
		task.dependsOn(sourcesJarTask);
		action.execute(task);
		return task;
	}
	
	@Override
	public void launches(Action<? super Launches> action) {
		action.execute(launches);
		Path launchesLoc = project.getProjectDir().toPath().resolve(".gradle").resolve("telejigsaw").resolve("dli.txt");
		Path runDir = project.getProjectDir().toPath().resolve("run");
		try {
			Files.createDirectories(runDir);
			Files.createDirectories(launchesLoc.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try(PrintStream out = new PrintStream(Files.newOutputStream(launchesLoc))) {
			launches.write(out, "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if(!addedDli) {
			addedDli = true;
			project.getDependencies().add("runtimeClasspath", "io.github.twilightflower.telejigsaw:dli:1.0.0");
		}
	}
	
	@Override
	public void fumo() {
		launches(l -> {
			l.all(p -> {
				p.mainClass("io.github.twilightflower.fumo.bootstrap.Main");
				p.property("fumo.minecraft.dev", "true");
				p.property("fumo.minecraft.runtimemappings", mappings.mappedNamespace);
			});
			l.profile("client", p -> {
				p.property("fumo.minecraft.side", "client");
				p.arg("--version");
				p.arg(mcVers);
				p.arg("--accessToken");
				p.arg("nocrash");
			});
			l.profile("server", p -> {
				p.property("fumo.minecraft.version", mcVers);
			});
		});
	}
}