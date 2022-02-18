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

import org.gradle.api.Action;
import io.github.astrarre.amalgamation.gradle.tasks.remap.RemapJar;
import io.github.astrarre.amalgamation.gradle.tasks.remap.RemapSourcesJar;

public interface TeleJigsawExtension {
	void minecraft(String version);
	void mappings(Action<MappingInfo> act);
	void mappings(Object o);
	void fernflower(Object dep);
	Object remap(Object dep);
	Object getMinecraft();
	RemapJar remapJar(String name, Action<? super RemapJar> action);
	RemapSourcesJar remapSourcesJar(String name, Action<? super RemapSourcesJar> action);
	void launches(Action<? super Launches> launches);
	
	default RemapJar remapJar(Action<? super RemapJar> action) {
		return remapJar("remapJar", action);
	}
	default RemapJar remapJar() {
		return remapJar("remapJar", r -> {});
	}
	default RemapJar remapJar(String name) {
		return remapJar(name, r -> {});
	}
	
	default RemapSourcesJar remapSourcesJar(Action<? super RemapSourcesJar> action) {
		return remapSourcesJar("remapSourcesJar", action);
	}
	default RemapSourcesJar remapSourcesJar(String name) {
		return remapSourcesJar(name, r -> {});
	}
	default RemapSourcesJar remapSourcesJar() {
		return remapSourcesJar("remapSourcesJar", r -> {});
	}
}
