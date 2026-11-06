package com.medical.cardio.support

import java.util.UUID
import java.util.concurrent.TimeUnit

class RedisTestContainer : AutoCloseable {

    private val containerName = "cardio-redis-test-${UUID.randomUUID()}"
    val port: Int

    init {
        try {
            docker(
                "run", "-d", "--rm",
                "--name", containerName,
                "-p", "127.0.0.1::6379",
                "redis:7-alpine",
                timeoutSeconds = 120,
            )

            port = docker("port", containerName, "6379/tcp")
                .substringAfterLast(':')
                .toInt()

            awaitReady()
        } catch (exception: Exception) {
            try {
                close()
            } catch (cleanupException: Exception) {
                exception.addSuppressed(cleanupException)
            }
            throw exception
        }
    }

    private fun awaitReady() {
        val started = System.nanoTime()
        val timeout = TimeUnit.SECONDS.toNanos(30)

        while (System.nanoTime() - started < timeout) {
            val reply = try {
                docker("exec", containerName, "redis-cli", "PING")
            } catch (_: IllegalStateException) {
                null
            }

            if (reply == "PONG") return

            Thread.sleep(100)
        }

        error("Test Redis did not become ready")
    }

    override fun close() {
        docker("rm", "-f", containerName)
    }

    private fun docker(
        vararg arguments: String,
        timeoutSeconds: Long = 10,
    ): String {
        val process = ProcessBuilder(listOf("docker") + arguments)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            error("Docker command timed out")
        }

        val output = process.inputStream.bufferedReader()
            .use { it.readText() }
            .trim()

        check(process.exitValue() == 0) {
            "Docker command failed: ${arguments.joinToString(" ")}"
        }

        return output
    }
}