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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.github.astrarre.amalgamation.gradle.plugin.minecraft.MinecraftAmalgamation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TeleJigsawPlugin implements Plugin<Project> {
	private static final String ECLIPSE_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n"
			+ "        <listEntry value=\"/%1$s\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n"
			+ "        <listEntry value=\"4\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_ATTR_USE_ARGFILE\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES\" value=\"true\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_CLASSPATH_ONLY_JAR\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"io.github.twilightflower.telejigsaw.dli.TeleJigsawDLI\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MODULE_NAME\" value=\"%1$s\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"%1$s\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Dtelejigsaw.dli.config=%2$s -Dtelejigsaw.dli.profiles=%3$s\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.WORKING_DIRECTORY\" value=\"${workspace_loc:%1$s}/run\"/>\n"
			+ "</launchConfiguration>\n";
	
	public void apply(Project project) {
		project.apply(Map.of("plugin", "amalgamation-minecraft"));
		MinecraftAmalgamation amalg = project.getExtensions().getByType(MinecraftAmalgamation.class);
		
		TeleJigsawExtensionImpl extension = (TeleJigsawExtensionImpl) project.getExtensions().create(TeleJigsawExtension.class, "telejigsaw", TeleJigsawExtensionImpl.class, amalg);
		//TeleJigsawExtensionImpl extension = null;
		project.getTasks().create("genEclipseRuns", t -> {
			t.doLast(t2 -> {
				Path projDir = project.getProjectDir().toPath();
				String launchesLoc = project.getProjectDir().toPath().resolve(".gradle").resolve("telejigsaw").resolve("dli.txt").toAbsolutePath().toString();
				String projDirName = project.getProjectDir().toPath().getFileName().toString();
				for(String profile : extension.launches.getProfiles().keySet()) {
					String launch = String.format(ECLIPSE_TEMPLATE, projDirName, launchesLoc, profile);
					String fname = projDirName + "_" + profile + ".launch";
					try {
						Files.writeString(projDir.resolve(fname), launch, StandardCharsets.UTF_8);
					} catch (IOException e) {
						throw new RuntimeException("exception writing run config", e);
					}
				}
			});
		});
	}
}
