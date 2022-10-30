package com.antondukeman.transcoding.pathprocessor

import com.antondukeman.transcoding.transcoder.Transcoder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

class TranscodingPathProcessor(
        val fileExtension: String,
        val sourceRoot: Path,
        val outputRoot: Path,
        val transcoder: Transcoder
) : PathProcessor {
    override fun process(path: Path) {
        if (path.toString().endsWith(fileExtension, true)) {
            val sourceFile = sourceRoot.resolve(path)
            val outputFile = outputRoot.resolve(path)

            try {
                Files.createDirectories(outputFile.parent)
            } catch (x: FileAlreadyExistsException) {
                // expected
            }

            transcoder.process(sourceFile, outputFile)
        }
    }
}
