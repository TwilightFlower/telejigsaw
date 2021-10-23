package org.quiltmc.gradle.decomp;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Manifest;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.quiltmc.gradle.util.Util;
import org.quiltmc.gradle.util.Util.ExceptionRunnable;

public class FernflowerIO implements IResultSaver, IBytecodeProvider, Runnable, Closeable, AutoCloseable {
	private static final ExceptionRunnable<IOException> SIGNAL = () -> {}; // used to signal termination.
	
	private final BlockingQueue<ExceptionRunnable<IOException>> execQueue = new LinkedBlockingQueue<>();
	private final Map<Path, FileSystem> archives = new ConcurrentHashMap<>();
	private final Object awaiter = new Object();
	private final AtomicBoolean done = new AtomicBoolean(false);
	private Path output;
	
	public FernflowerIO() {
		new Thread(this).start();
	}

	@Override
	public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
		FileSystem zipfs = zipFs(Paths.get(externalPath));
		return Files.readAllBytes(zipfs.getPath(internalPath));
	}

	@Override
	public void saveFolder(String path) {
		execQueue.add(() -> {
			Files.createDirectories(Paths.get(path));
		});
	}

	@Override
	public void copyFile(String source, String path, String entryName) {
		execQueue.add(() -> {
			Files.copy(Paths.get(source), Paths.get(path, entryName));
		});
	}

	@Override
	public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
		execQueue.add(() -> {
			Files.writeString(Paths.get(path, entryName), content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		});
	}

	@Override
	public void createArchive(String path, String archiveName, Manifest manifest) {
		execQueue.add(() -> {
			Path archivePath = Paths.get(path, archiveName);
			Files.deleteIfExists(archivePath);
			FileSystem zipfs = zipFs(archivePath);
			if(manifest != null) {
				Path metaInf = zipfs.getPath("META-INF");
				Files.createDirectory(metaInf);
				Path manifestPath = metaInf.resolve("MANIFEST.MF");
				try(OutputStream out = new BufferedOutputStream(Files.newOutputStream(manifestPath, StandardOpenOption.CREATE))) {
					manifest.write(out);
				}
			}
		});
	}

	@Override
	public void saveDirEntry(String path, String archiveName, String entryName) {
		execQueue.add(() -> {
			Path archivePath = Paths.get(path, archiveName);
			FileSystem zipfs = zipFs(archivePath);
			Files.createDirectory(zipfs.getPath(entryName));
		});
	}

	@Override
	public void copyEntry(String source, String path, String archiveName, String entry) {
		execQueue.add(() -> {
			Path archivePath = Paths.get(path, archiveName);
			FileSystem destFs = zipFs(archivePath);
			FileSystem srcFs = zipFs(Paths.get(source));
			Path destPath = destFs.getPath(entry);
			Path srcPath = srcFs.getPath(entry);
			Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
		});
	}

	@Override
	public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
		execQueue.add(() -> {
			Path archivePath = Paths.get(path, archiveName);
			FileSystem zipfs = zipFs(archivePath);
			Files.writeString(zipfs.getPath(entryName), content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		});
	}

	@Override
	public void closeArchive(String path, String archiveName) {
		output = Paths.get(path, archiveName).toAbsolutePath();
		execQueue.add(SIGNAL);
	}
	
	private FileSystem zipFs(Path path) throws IOException {
		return archives.computeIfAbsent(path.toAbsolutePath(), Util.makeSneakyFunction(p -> {
			URI asUri = URI.create("jar:file:" + p.toAbsolutePath().toString());
			try {
				FileSystems.getFileSystem(asUri).close(); // this is a thing bc gradle daemons.
			} catch(FileSystemNotFoundException | IOException e) { }
			FileSystem zipfs = FileSystems.newFileSystem(asUri, Collections.singletonMap("create", true));
			return zipfs;
		}));
	}

	@Override
	public void run() {
		try {
			ExceptionRunnable<IOException> r;
			while((r = execQueue.take()) != SIGNAL) {
				try {
					r.run();
				} catch(IOException e) {
					System.err.println("Error during decompilation: " + e.getMessage());
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			IOException exc = null;
			for(FileSystem fs : archives.values()) {
				try {
					fs.close();
				} catch (IOException e) {
					if(exc == null) {
						exc = e;
					} else {
						exc.addSuppressed(e);
					}
				}
			}
			if(exc != null) {
				throw new RuntimeException("Error(s) closing zip file systems", exc);
			}
		}
		synchronized(awaiter) {
			done.set(true);
			awaiter.notifyAll();
		}
	}
	
	public Path await() {
		synchronized(awaiter) {
			if(!done.get()) {
				try {
					awaiter.wait();
				} catch (InterruptedException e) { }
			}
		}
		return output;
	}

	@Override
	public void close() {
		await();
	}
}
