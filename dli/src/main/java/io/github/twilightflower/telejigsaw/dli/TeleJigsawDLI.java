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
 *
 * If you modify this Program, or any covered work, by linking or combining it with other software, containing parts covered by the terms of the other software's license, the licensors of this Program grant you additional permission to convey the resulting work.
 */

package io.github.twilightflower.telejigsaw.dli;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleJigsawDLI {
	public static void main(String[] args) throws Throwable { // go away checked exceptions
		
		Path dliLoc = Paths.get(System.getProperty("telejigsaw.dli.config"));
		
		Map<String, LaunchSpec> profiles = new HashMap<>();
		LaunchSpec activeProfile = new LaunchSpec();
		profiles.put("all", activeProfile);
		for(String line : Files.readAllLines(dliLoc, StandardCharsets.UTF_8)) {
			line = line.trim();
			if(!line.isEmpty()) {
				char c0 = line.charAt(0);
				line = line.substring(1).trim();
				switch(c0) {
					case ':':
						activeProfile = profiles.computeIfAbsent(line.trim(), l -> new LaunchSpec());
						break;
					case 'D':
						String[] split = line.split("=", 2);
						activeProfile.properties.put(split[0], split[1]);
						break;
					case 'A':
						activeProfile.args.add(line);
						break;
					case 'M':
						activeProfile.main = line;
						break;
				}
			}
		}
		LaunchSpec launchProfile = new LaunchSpec();
		profiles.get("all").addTo(launchProfile);
		String[] use = System.getProperty("telejigsaw.dli.profiles", "").split(",");
		for(String pName : use) {
			LaunchSpec profile = profiles.get(pName);
			if(profile != null) {
				profile.addTo(launchProfile);
			}
		}
		
		for(Map.Entry<String, String> entry : launchProfile.properties.entrySet()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}
		
		Class<?> mainClass = Class.forName(launchProfile.main);
		// note: using MethodHandles over reflection to avoid InvocationTargetException
		MethodHandle main = MethodHandles.publicLookup().findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class));
		
		String[] newArgs = new String[args.length + launchProfile.args.size()];
		System.arraycopy(args, 0, newArgs, 0, args.length);
		String[] addedArgs = launchProfile.args.toArray(new String[0]);
		System.arraycopy(addedArgs, 0, newArgs, args.length, addedArgs.length);
		main.invoke(newArgs);
	}
	
	static class LaunchSpec {
		List<String> args = new ArrayList<>();
		Map<String, String> properties = new HashMap<>();
		String main;
		
		void addTo(LaunchSpec other) {
			other.args.addAll(args);
			other.properties.putAll(properties);
			if(main != null) {
				other.main = main;
			}
		}
	}
}
