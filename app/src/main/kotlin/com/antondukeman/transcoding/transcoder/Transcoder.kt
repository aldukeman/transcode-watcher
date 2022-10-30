package com.antondukeman.transcoding.transcoder

import kotlin.io.path.*
import java.nio.file.Path

interface Transcoder {
    fun process(input: Path, output: Path)
}

class SystemOutTranscoder: Transcoder {
    override fun process(input: Path, output: Path) {
        System.out.println("Input: " + input + ", Output: " + output)
    }
}