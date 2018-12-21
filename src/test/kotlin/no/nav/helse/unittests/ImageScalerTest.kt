package no.nav.helse.unittests

import no.nav.helse.vedlegg.ImageScaler
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageScalerTest {
    private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val scaler = ImageScaler()

    @Test
    fun imgSmallerThanA4RemainsUnchanged() {
        "vedlegg/jks.jpg".fromResources {
            val scaled = scaler.downToA4(it, "jpg")
            assertEquals(it.size, scaled.size)
        }
    }

    @Test
    fun imgBiggerThanA4IsScaledDown() {
        "vedlegg/rdd.png".fromResources {
            val scaled = scaler.downToA4(it, "jpg")
            val origImg = fromBytes(it)
            val scaledImg = fromBytes(scaled)
            assertTrue(scaledImg.width < origImg.width)
            assertTrue(scaledImg.height < origImg.height)
        }
    }

    @Test
    fun scaledImgHasRetainedFormat() {
        "vedlegg/rdd.png".fromResources {
            val scaled = scaler.downToA4(it, "jpg")
            assertTrue(isJpeg(scaled))
        }
    }

    @Test
    fun rotateLandscapeToPortrait() {
        "vedlegg/landscape.jpg".fromResources {
            val scaled = scaler.downToA4(it, "jpg")
            val origImg = fromBytes(it)
            val scaledImg = fromBytes(scaled)
            assertTrue(origImg.width > origImg.height)
            assertTrue(scaledImg.height > scaledImg.width)

        }
    }

    private fun isJpeg(fileContents: ByteArray): Boolean {
        return Arrays.equals(Arrays.copyOfRange(fileContents, 0, JPEG_SIGNATURE.size), JPEG_SIGNATURE)
    }

    private fun String.fromResources(work: (ByteArray) -> Unit) {
        val content= Thread.currentThread().contextClassLoader.getResource(this).readBytes()
        work(content)
    }


    private fun fromBytes(bytes: ByteArray): BufferedImage {
        try {
            ByteArrayInputStream(bytes).use { `in` -> return ImageIO.read(`in`) }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

}