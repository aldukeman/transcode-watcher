package com.antondukeman.transcoding

import com.antondukeman.transcoding.transcoder.ScriptedHandbrakeTranscoder
import com.antondukeman.transcoding.pathprocessor.TranscodingPathProcessor
import java.nio.file.Path
import kotlin.io.path.*

class App(val originalPath: Path, val processedPath: Path, val transcodeExecutablePath: Path) {
    fun start() {
        var transcoder = ScriptedHandbrakeTranscoder(transcodeExecutablePath, true)
        transcoder.startTranscodeVideoThread()

        var pathProcessor = TranscodingPathProcessor(".mkv", originalPath, processedPath, transcoder)

        val observer = DirectoryObserver(originalPath)
        observer.startObserving {
            pathProcessor.process(it)
        }
    }
}

fun main(args: Array<String>) {
    val rootPathString: String
    if(args.size >= 1) {
        rootPathString = args[0]
    } else {
        // rootPathString = "/Volumes/media/transcoding/"
        rootPathString = "/Users/aldukeman/dev/transcoding_testing"
    }

    val rootPath = Path(rootPathString)
    val originalPath = rootPath.resolve("original")
    val processedPath = rootPath.resolve("processed")
    val transcodeExecutablePath = Path("/Users/aldukeman/scripts/HandBrakeCLI")

    val app = App(originalPath, processedPath, transcodeExecutablePath)
    app.start()
}
