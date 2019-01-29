package no.nav.helse

import com.github.tomakehurst.wiremock.http.Cookie
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.io.streams.asInput
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun TestApplicationEngine.handleRequestUploadImage(cookie: Cookie,
                                                   vedlegg: ByteArray = "vedlegg/iPhone_6.jpg".fromResources(),
                                                   fileName: String = "iPhone_6.jpg",
                                                   contentType: String = "image/jpeg") : String {
    val boundary = "***vedlegg***"

    handleRequest(HttpMethod.Post, "/vedlegg") {
        addHeader("Cookie", cookie.toString())
        addHeader(
            HttpHeaders.ContentType,
            ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
        )
        setBody(
            boundary, listOf(
                PartData.FileItem(
                    { vedlegg.inputStream().asInput() }, {},
                    headersOf(
                        Pair(
                            HttpHeaders.ContentType,
                            listOf(contentType)
                        ),
                        Pair(
                            HttpHeaders.ContentDisposition,
                            listOf(
                                ContentDisposition.File
                                    .withParameter(ContentDisposition.Parameters.Name, "vedlegg")
                                    .withParameter(ContentDisposition.Parameters.FileName, fileName)
                                    .toString()
                            )
                        )
                    )
                )
            )
        )
    }.apply {
        assertEquals(HttpStatusCode.Created, response.status())
        val locationHeader= response.headers[HttpHeaders.Location]
        assertNotNull(locationHeader)
        return locationHeader
    }
}

fun String.fromResources() : ByteArray {
    return Thread.currentThread().contextClassLoader.getResource(this).readBytes()
}