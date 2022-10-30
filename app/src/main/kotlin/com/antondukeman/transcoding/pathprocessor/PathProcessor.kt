package com.antondukeman.transcoding.pathprocessor

import java.nio.file.Path
import kotlin.io.path.*

interface PathProcessor {
    fun process(path: Path)
}

class SystemOutPathProcessor : PathProcessor {
    override fun process(path: Path) {
        System.out.println(path)
    }
}
