package org.quiltmc.gradle.decomp;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

public class FernflowerLogger extends IFernflowerLogger {

	@Override
	public void writeMessage(String message, Severity severity) {
		if(severity == Severity.ERROR)	{
			System.err.println(message);
		}
	}

	@Override
	public void writeMessage(String message, Severity severity, Throwable t) {
		if(severity == Severity.ERROR)	{
			System.err.println(message);
			t.printStackTrace();
		}
	}
}
