package io.github.twilightflower.telejigsaw.util;

import java.util.function.Function;

public class Util {
	public static <T> T sneakyGet(ExceptionSupplier<T, ?> supplier) {
		@SuppressWarnings("unchecked")
		var sneak = (ExceptionSupplier<T, RuntimeException>) supplier;
		return sneak.get();
	}
	
	public static void sneakyRun(ExceptionRunnable<?> runnable) {
		@SuppressWarnings("unchecked")
		var sneak = (ExceptionRunnable<RuntimeException>) runnable;
		sneak.run();
	}
	
	public static Runnable makeSneakyRunnable(ExceptionRunnable<?> runnable) {
		@SuppressWarnings("unchecked")
		var sneak = (ExceptionRunnable<RuntimeException>) runnable;
		return sneak::run;
	}
	
	public static <T, U> Function<T, U> makeSneakyFunction(ExceptionFunction<T, U, ?> consumer) {
		@SuppressWarnings("unchecked")
		var sneak = (ExceptionFunction<T, U, RuntimeException>) consumer;
		return sneak::apply;
	}
	
	public interface ExceptionSupplier<T, E extends Throwable> {
		T get() throws E;
	}
	
	public interface ExceptionRunnable<E extends Throwable> {
		void run() throws E;
	}
	
	public interface ExceptionFunction<T, U, E extends Throwable> {
		U apply(T t) throws E;
	}
}
