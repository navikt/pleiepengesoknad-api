package no.nav.helse.vedlegg

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException

class Image2PDFConverter(val imageScaler: ImageScaler) {
    private val logger = LoggerFactory.getLogger("nav.Image2PDFConverter")
    private val A4 = PDRectangle.A4
    private val PNG = MediaType.image("png")
    private val JPEG = MediaType.image("jpeg")
    private val PDF = MediaType.application("pdf")

    private val supportedMediaTypes: List<MediaType> = listOf(PNG, JPEG, PDF)
    private val tika = Tika()



    fun convert(bytes: ByteArray): ByteArray {
        val mediaType = mediaType(bytes)
        if (PDF == mediaType) {
            return bytes
        }
        if (validImageTypes(mediaType)) {
            return embedImagesInPdf(bytes, mediaType.subtype)
        }
        throw UnsupportedAttachementTypeException(mediaType.toString())
    }

    private fun embedImagesInPdf(image: ByteArray, imgType: String): ByteArray {
        try {
            PDDocument().use { doc ->
                ByteArrayOutputStream().use { outputStream ->
                    addPDFPageFromImage(doc, image, imgType)
                    doc.save(outputStream)
                    return outputStream.toByteArray()
                }
            }
        } catch (ex: IOException) {
            throw IllegalStateException("Konvertering av vedlegg feilet", ex)
        }

    }

    private fun validImageTypes(mediaType: MediaType): Boolean {
        val validImageTypes = supportedMediaTypes.contains(mediaType)
        logger.info("{} konvertere bytes av type {} til PDF", if (validImageTypes) "Vil" else "Vil ikke", mediaType)
        return validImageTypes
    }

    private fun mediaType(bytes: ByteArray): MediaType {
        val type = tika.detect(bytes).split("/")
        return MediaType(type[0], type[1])
    }

    private fun addPDFPageFromImage(doc: PDDocument, origImg: ByteArray, imgFormat: String) {
        val page = PDPage(A4)
        doc.addPage(page)
        val scaledImg = imageScaler.downToA4(origImg, imgFormat)
        try {
            PDPageContentStream(doc, page).use { contentStream ->
                val ximage = PDImageXObject.createFromByteArray(doc, scaledImg, "img")
                contentStream.drawImage(ximage, A4.lowerLeftX.toInt().toFloat(), A4.lowerLeftY.toInt().toFloat())
            }
        } catch (ex: IOException) {
            throw IllegalStateException("Konvertering av vedlegg feilet", ex)
        }

    }
}