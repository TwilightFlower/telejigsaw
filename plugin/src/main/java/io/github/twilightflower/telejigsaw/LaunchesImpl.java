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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Action;

class LaunchesImpl implements Launches {
	private Map<String, LaunchProfile> profiles = new HashMap<>();
	private LaunchProfileImpl def = new LaunchProfileImpl();

	@Override
	public Map<String, LaunchProfile> getProfiles() {
		return profiles;
	}

	@Override
	public void setProfiles(Map<String, LaunchProfile> to) {
		profiles = to;
	}

	@Override
	public LaunchProfile profile(String profileName, Action<? super LaunchProfile> action) {
		LaunchProfile p = new LaunchProfileImpl();
		action.execute(p);
		if(profileName != null) {
			profiles.put(profileName, p);
		}
		return p;
	}
	
	void write(PrintStream to, String pfx) {
		def.write(to, pfx);
		System.out.println(profiles);
		for(var entry : profiles.entrySet()) {
			to.println(pfx + ":" + entry.getKey());
			var impl = (LaunchProfileImpl) entry.getValue();
			impl.write(to, pfx + "\t");
		}
	}

	@Override
	public LaunchProfile all(Action<? super LaunchProfile> action) {
		action.execute(def);
		return def;
	}
}
