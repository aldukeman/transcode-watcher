package com.antondukeman.transcoding.transcoder

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.io.path.createTempDirectory

class ForkedHandbrakeTranscoder(
        val transcodeExecutablePath: Path,
        val copyBeforeHandbrake: Boolean
) : Transcoder {
    private class WorkItem(val input: Path, val output: Path) {}

    private val queue: BlockingQueue<WorkItem> = LinkedBlockingQueue<WorkItem>()
    private var started = AtomicBoolean(false)

    override fun process(input: Path, output: Path) {
        queue.put(WorkItem(input, output))
    }

    fun startTranscodeVideoThread() {
        if (started.getAndSet(true)) {
            return
        }

        thread() {
            while (true) {
                val nextElement = queue.take()
                val input = nextElement.input
                val output = nextElement.output

                transcodeVideo(input, output)
            }
        }
    }

    private fun transcodeVideo(input: Path, output: Path) {
        System.out.println("Doing things with " + input.toString())
        try {
            val process = startTranscodeProcess(input, output)
            System.out.println("Done with " + output.toString() + " exited: " + process.waitFor())
        } catch (x: IOException) {
            System.out.println("Failed to execute transcode: " + x)
            Thread.sleep(10 * 1000L) // a very short simulation of the transcoding activity
        }
    }

    private fun startTranscodeProcess(input: Path, output: Path): Process {
        val cmd = generateCommand(input, output)
        System.out.println(cmd.joinToString(" "))

        return ProcessBuilder(cmd.asList())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectErrorStream(true)
                .start()
    }

    private fun generateCommand(input: Path, output: Path): Array<String> {
        var transcodeArr = arrayOf(transcodeExecutablePath.toString())
        transcodeArr += arrayOf("--input", input.toString())
        transcodeArr += arrayOf("--output", output.toString())
        transcodeArr += arrayOf("--markers") // add chapter markers
        transcodeArr += arrayOf("--encoder", "vt_h265_10bit")
        transcodeArr += arrayOf("--audio-lang-list", "eng")
        transcodeArr += arrayOf("--first-audio")
        transcodeArr += arrayOf("--aencoder", "copy") // choose the best audio and pass it through
        transcodeArr += arrayOf("--auto-anamorphic")
        transcodeArr += arrayOf("--subtitle-lang-list", "eng")
        transcodeArr += arrayOf("--subtitle", "scan,1")
        transcodeArr += arrayOf("--subtitle-forced=scan") // only include the scan subtitle if forced flag is set

        return transcodeArr
    }
}
