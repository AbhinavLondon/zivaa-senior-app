package com.zivaa.app.data.remote

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.IOException

class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (progress: Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        return try {
            delegate.contentLength()
        } catch (e: IOException) {
            -1
        }
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten = 0L
        private val delayPerChunk = 50L // 50ms per chunk to make progress visible

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val totalBytes = contentLength()
            if (totalBytes > 0) {
                val progress = ((bytesWritten * 100) / totalBytes).toInt()
                onProgress(progress)
            }
            // Artificial delay to make progress visible for local/fast networks
            try {
                Thread.sleep(delayPerChunk)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }
}
