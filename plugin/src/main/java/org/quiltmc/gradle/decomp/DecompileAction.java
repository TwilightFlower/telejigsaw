package org.quiltmc.gradle.decomp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import org.gradle.workers.WorkAction;
import org.jetbrains.java.decompiler.main.Fernflower;

public abstract class DecompileAction implements WorkAction<DecompileWorkParameters> {
	@Override
	public void execute() {
		Path sources = getParameters().getSourcesPath().get().toPath();
		Path compiled = getParameters().getCompiledPath().get().getAsFile().toPath();
		Set<File> classpath = getParameters().getClasspath().get();
		Path output;
		try(FernflowerIO ffio = new FernflowerIO()) {
			Fernflower ff = new Fernflower(ffio, ffio, Collections.emptyMap(), new FernflowerLogger());
			ff.addSource(compiled.toFile());
			classpath.stream().forEach(ff::addLibrary);
			ff.decompileContext();
			output = ffio.await();
		}
		try {
			Files.copy(output, sources, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
