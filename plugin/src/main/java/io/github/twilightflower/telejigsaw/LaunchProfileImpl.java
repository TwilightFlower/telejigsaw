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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LaunchProfileImpl implements LaunchProfile {
	private Map<String, String> properties = new HashMap<>();
	private List<String> args = new ArrayList<>();
	private String main = null;
	
	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> to) {
		properties = to;
	}

	@Override
	public List<String> getArgs() {
		return args;
	}

	@Override
	public void setArgs(List<String> to) {
		args = to;
	}

	@Override
	public String getMain() {
		return main;
	}

	@Override
	public void setMain(String mainClass) {
		main = mainClass;
	}
	
	void write(PrintStream to, String pfx) {
		if(main != null) {
			to.println(pfx + "M" + main);
		}
		for(String arg : args) {
			to.println(pfx + "A" + arg);
		}
		for(var entry : properties.entrySet()) {
			var out = String.format("%sD%s=%s", pfx, entry.getKey(), entry.getValue());
			to.println(out);
		}
	}
}
