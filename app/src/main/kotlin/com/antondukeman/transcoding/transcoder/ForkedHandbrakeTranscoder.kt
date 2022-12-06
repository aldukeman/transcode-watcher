package com.antondukeman.transcoding.transcoder

import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.security.InvalidParameterException
import kotlin.concurrent.thread

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
        } catch (x: Exception) {
            System.out.println("Failed to execute transcode: " + x)
            Thread.sleep(10 * 1000L) // a very short simulation of the transcoding activity
        }
    }

    private fun startTranscodeProcess(input: Path, output: Path): Process {
        val cmd: Array<String>
        val inputPathStr = input.toString()
        if(inputPathStr.contains("dvd")) {
            cmd = generateDvdCommand(input, output)
        } else if(inputPathStr.contains("bluray_tv")) {
            cmd = generateBlurayTvCommand(input, output)
        } else if(inputPathStr.contains("bluray_movie")) {
            cmd = generateBlurayMovieCommand(input, output)
        } else if(inputPathStr.contains("uhd")) {
            cmd = generateUhdMovieCommand(input, output)
        } else {
            throw IllegalArgumentException("Unknown processing type " + inputPathStr)
        }
        System.out.println(cmd.joinToString(" "))

        return ProcessBuilder(cmd.asList())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectErrorStream(true)
                .start()
    }

    private fun generateDvdCommand(input: Path, output: Path): Array<String> {
        var transcodeArr = arrayOf(transcodeExecutablePath.toString())
        transcodeArr += arrayOf("--input", input.toString())
        transcodeArr += arrayOf("--output", output.toString())
        transcodeArr += arrayOf("--markers") // add chapter markers

        transcodeArr += arrayOf("--encoder", "vt_h265")
        transcodeArr += arrayOf("--vb", "2000") // video bitrate in kbps

        transcodeArr += arrayOf("--audio-lang-list", "eng")
        transcodeArr += arrayOf("--first-audio")
        transcodeArr += arrayOf("--aencoder", "copy") // choose the best audio and pass it through
        transcodeArr += arrayOf("--audio-copy-mask", "truehd,dtshd,dts")

        transcodeArr += arrayOf("--subtitle-lang-list", "eng")
        // only include the scan subtitle if forced flag is set
        transcodeArr += arrayOf("--subtitle-forced=scan")

        transcodeArr += arrayOf("--verbose")

        return transcodeArr
    }

    private fun generateBlurayTvCommand(input: Path, output: Path): Array<String> {
        var transcodeArr = arrayOf(transcodeExecutablePath.toString())
        transcodeArr += arrayOf("--input", input.toString())
        transcodeArr += arrayOf("--output", output.toString())
        transcodeArr += arrayOf("--markers") // add chapter markers

        transcodeArr += arrayOf("--encoder", "vt_h265")
        transcodeArr += arrayOf("--vb", "4000") // video bitrate in kbps

        transcodeArr += arrayOf("--audio-lang-list", "eng")
        transcodeArr += arrayOf("--first-audio")
        transcodeArr += arrayOf("--aencoder", "copy") // choose the best audio and pass it through
        transcodeArr += arrayOf("--audio-copy-mask", "truehd,dtshd,dts")

        transcodeArr += arrayOf("--subtitle-lang-list", "eng")
        // only include the scan subtitle if forced flag is set
        transcodeArr += arrayOf("--subtitle-forced=scan")

        transcodeArr += arrayOf("--verbose")

        return transcodeArr
    }

    private fun generateBlurayMovieCommand(input: Path, output: Path): Array<String> {
        var transcodeArr = arrayOf(transcodeExecutablePath.toString())
        transcodeArr += arrayOf("--input", input.toString())
        transcodeArr += arrayOf("--output", output.toString())
        transcodeArr += arrayOf("--markers") // add chapter markers

        transcodeArr += arrayOf("--encoder", "vt_h265")
        transcodeArr += arrayOf("--vb", "6000") // video bitrate in kbps
        // transcodeArr += arrayOf("--two-pass")

        transcodeArr += arrayOf("--audio-lang-list", "eng")
        transcodeArr += arrayOf("--first-audio")
        transcodeArr += arrayOf("--aencoder", "copy") // choose the best audio and pass it through
        transcodeArr += arrayOf("--audio-copy-mask", "truehd,dtshd,dts")

        transcodeArr += arrayOf("--subtitle-lang-list", "eng")
        // only include the scan subtitle if forced flag is set
        transcodeArr += arrayOf("--subtitle-forced=scan")

        transcodeArr += arrayOf("--verbose")

        return transcodeArr
    }

    private fun generateUhdMovieCommand(input: Path, output: Path): Array<String> {
        var transcodeArr = arrayOf(transcodeExecutablePath.toString())
        transcodeArr += arrayOf("--input", input.toString())
        transcodeArr += arrayOf("--output", output.toString())
        transcodeArr += arrayOf("--markers") // add chapter markers

        transcodeArr += arrayOf("--encoder", "vt_h265_10bit")
        transcodeArr += arrayOf("--vb", "12000") // video bitrate in kbps

        transcodeArr += arrayOf("--audio-lang-list", "eng")
        transcodeArr += arrayOf("--first-audio")
        transcodeArr += arrayOf("--aencoder", "copy") // choose the best audio and pass it through
        transcodeArr += arrayOf("--audio-copy-mask", "truehd,dtshd,dts")

        transcodeArr += arrayOf("--subtitle-lang-list", "eng")
        // only include the scan subtitle if forced flag is set
        transcodeArr += arrayOf("--subtitle-forced=scan")

        transcodeArr += arrayOf("--verbose")

        return transcodeArr
    }
}
