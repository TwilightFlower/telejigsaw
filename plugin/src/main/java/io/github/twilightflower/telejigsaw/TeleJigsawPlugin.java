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
import java.util.Map;

public class TeleJigsawPlugin implements Plugin<Project> {
	public void apply(Project project) {
		project.apply(Map.of("plugin", "amalgamation-minecraft"));
		MinecraftAmalgamation amalg = project.getExtensions().getByType(MinecraftAmalgamation.class);
		
		project.getExtensions().create(TeleJigsawExtension.class, "telejigsaw", TeleJigsawExtensionImpl.class, amalg);
	}
}
