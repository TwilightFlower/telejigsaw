package io.github.twilightflower.telejigsaw.decomp;

import java.io.File;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;

public interface DecompileWorkParameters extends WorkParameters {
	Property<File> getSourcesPath();
	RegularFileProperty getCompiledPath();
	SetProperty<File> getClasspath();
}
