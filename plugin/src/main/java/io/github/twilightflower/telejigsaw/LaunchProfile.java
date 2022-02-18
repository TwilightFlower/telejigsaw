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

import java.util.List;
import java.util.Map;

public interface LaunchProfile {
	default void properties(Map<String, String> props) {
		getProperties().putAll(props);
	}
	default void property(String name, String val) {
		getProperties().put(name, val);
	}
	Map<String, String> getProperties();
	void setProperties(Map<String, String> to);
	
	default void args(List<String> args) {
		getArgs().addAll(args);
	}
	default void arg(String arg) {
		getArgs().add(arg);
	}
	List<String> getArgs();
	void setArgs(List<String> to);
	
	default void mainClass(String mainClass) {
		setMain(mainClass);
	}
	String getMain();
	void setMain(String mainClass);
}
