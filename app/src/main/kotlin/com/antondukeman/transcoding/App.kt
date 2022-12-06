package com.antondukeman.transcoding

import com.antondukeman.transcoding.transcoder.ForkedHandbrakeTranscoder
import com.antondukeman.transcoding.pathprocessor.TranscodingPathProcessor
import java.nio.file.Path
import kotlin.io.path.*

class App(val originalPath: Path, val processedPath: Path, val transcodeExecutablePath: Path) {
    fun start() {
        var transcoder = ForkedHandbrakeTranscoder(transcodeExecutablePath, true)
        transcoder.startTranscodeVideoThread()

        var pathProcessor = TranscodingPathProcessor(".mkv", originalPath, processedPath, transcoder)

        val observer = DirectoryObserver(originalPath, pathProcessor)
        observer.startObserving()
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

    val transcodeExecutablePath: Path
    if(args.size >= 2) {
        transcodeExecutablePath = Path(args[1])
    } else {
        transcodeExecutablePath = Path("/Users/aldukeman/scripts/HandBrakeCLI")
    }

    val rootPath = Path(rootPathString)
    val originalPath = rootPath.resolve("original")
    val processedPath = rootPath.resolve("processed")

    val app = App(originalPath, processedPath, transcodeExecutablePath)
    app.start()
}
