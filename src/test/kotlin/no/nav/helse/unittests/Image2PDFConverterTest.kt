package no.nav.helse.unittests

import no.nav.helse.vedlegg.Image2PDFConverter
import no.nav.helse.vedlegg.ImageScaler
import no.nav.helse.vedlegg.UnsupportedAttachementTypeException
import org.apache.tika.Tika
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Image2PDFConverterTest() {
    private val PDF_SIGNATURE = byteArrayOf(0x25, 0x50, 0x44, 0x46)

    private var converter = Image2PDFConverter(ImageScaler())

    @Test
    fun jpgConvertsToPdf() {
        "vedlegg/jks.jpg".fromResources {
            assertTrue(isPdf(converter.convert(it)))
        }
    }

    @Test
    fun pngConvertsToPdf() {
        "vedlegg/nav-logo.png".fromResources {
            assertTrue(isPdf(converter.convert(it)))
        }
    }

    @Test
    fun pdfRemainsUnchanged() {
        "vedlegg/test123.pdf".fromResources {
            val converted = converter.convert(it)
            assertEquals("application/pdf", Tika().detect(converted))
            assertTrue(isPdf(converted))
        }
    }

    @Test(expected = UnsupportedAttachementTypeException::class)
    fun whateverElseIsNotAllowed() {
        "vedlegg/loading.gif".fromResources {
            converter.convert(it)
        }
    }

    @Test
    fun pdfManyPages() {
        "vedlegg/spring-framework-reference.pdf".fromResources {
            val converted = converter.convert(it)
            assertEquals(it, converted)
        }
    }

    private fun isPdf(fileContents: ByteArray): Boolean {
        return Arrays.equals(Arrays.copyOfRange(fileContents, 0, PDF_SIGNATURE.size), PDF_SIGNATURE)
    }

    private fun String.fromResources(work: (ByteArray) -> Unit) {
        val content= Thread.currentThread().contextClassLoader.getResource(this).readBytes()
        work(content)
    }
}