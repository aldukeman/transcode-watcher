package com.antondukeman.transcoding

import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

// Watch the directory at root path for new files in the tree
class DirectoryObserver(val rootPath: Path) {
    private val watcherService = FileSystems.getDefault().newWatchService()
    private val keys = hashMapOf<WatchKey, Path>()

    private var found = mutableListOf<Path>()

    // Start observing the directory tree and execute callback with path relative to root when new
    // files are found
    fun startObserving(callback: (path: Path) -> Unit) {
        val relativeCallback: (Path) -> Unit = { path -> 
            val relativePath = rootPath.relativize(path)
            callback(relativePath)
        }
        walkAndRegisterDirectories(rootPath)
        pushFileChanges(relativeCallback)

        while (true) {
            var key: WatchKey?
            try {
                key = watcherService.poll(12, TimeUnit.SECONDS)
            } catch (x: InterruptedException) {
                System.err.println("Can't take " + x)
                return
            }

            if (key != null) {
                val dir = keys.get(key)
                if (dir == null) {
                    System.err.println("WatchKey not recognized!")
                    continue
                }
                processWatchKey(key, dir)
            } else {
                System.out.println("Pushing changes")
                pushFileChanges(relativeCallback)
            }
        }
    }

    private fun walkAndRegisterDirectories(start: Path, ) {
        Files.walk(start).forEach { path ->
            if (path.isDirectory()) {
                registerDirectory(path)
            } else if (path.isRegularFile()) {
                found.add(path)
            }
        }
    }

    private fun registerDirectory(dir: Path) {
        System.out.println("Registering " + dir)
        val key = dir.register(watcherService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        keys.put(key, dir)
    }

    private fun processWatchKey(key: WatchKey, dir: Path) {
        key.pollEvents().forEach { event ->
            @Suppress("UNCHECKED_CAST") var name = (event as WatchEvent<Path>).context()
            var child = dir.resolve(name)

            System.out.format("%s: %s\n", event.kind().name(), child)

            var kind = event.kind()
            if (kind == ENTRY_CREATE) {
                try {
                    if (child.isDirectory()) {
                        walkAndRegisterDirectories(child)
                    } else if (child.isRegularFile()) {
                        found.add(child)
                    }
                } catch (x: IOException) {
                    System.out.println(x.toString())
                }
            }
        }

        var valid = key.reset()
        if (!valid) {
            keys.remove(key)
        }
    }

    private fun pushFileChanges(callback: (path: Path) -> Unit) {
        for(path in found) {
            callback(path)
        }
        found = mutableListOf<Path>()
    }
}
