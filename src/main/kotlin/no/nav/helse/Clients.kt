package no.nav.helse

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.Logging
import no.nav.helse.dusseldorf.ktor.client.MonitoredHttpClient
import no.nav.helse.dusseldorf.ktor.client.setProxyRoutePlanner
import no.nav.helse.dusseldorf.ktor.client.sl4jLogger
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import org.apache.http.HttpRequestInterceptor

class Clients {
    companion object {
        fun sparkelClient(
            apiGatewayHttpRequestInterceptor : HttpRequestInterceptor
        ) : MonitoredHttpClient = MonitoredHttpClient(
            source = "pleiepengesoknad-api",
            destination = "sparkel",
            overridePaths = mapOf(
                Regex("/api/arbeidsgivere/.+") to "/api/arbeidsgivere",
                Regex("/api/person/.+") to "/api/person"
            ),
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
                install (Logging) {
                    sl4jLogger("sparkel")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                        addInterceptorLast(apiGatewayHttpRequestInterceptor)
                    }
                }
            }
        )

        fun stsClient(
            apiGatewayHttpRequestInterceptor: HttpRequestInterceptor
        ) : MonitoredHttpClient = MonitoredHttpClient(
            source = "pleiepengesoknad-api",
            destination = "nais-sts",
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
                install (Logging) {
                    sl4jLogger("nais-sts")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                        addInterceptorLast(apiGatewayHttpRequestInterceptor)
                    }
                }
            }
        )

        fun aktoerRegisterClient(
            apiGatewayHttpRequestInterceptor: HttpRequestInterceptor
        ) : MonitoredHttpClient = MonitoredHttpClient(
            source = "pleiepengesoknad-api",
            destination = "aktoer-register",
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
                install (Logging) {
                    sl4jLogger("aktoer-register")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                        addInterceptorLast(apiGatewayHttpRequestInterceptor)
                    }
                }
            }
        )

        fun pleiepengesoknadProsesseringClient(
            apiGatewayHttpRequestInterceptor: HttpRequestInterceptor
        ) : MonitoredHttpClient = MonitoredHttpClient(
            source = "pleiepengesoknad-api",
            destination = "pleiepengesoknad-prosessering",
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        dusseldorfConfigured()
                    }
                }
                install (Logging) {
                    sl4jLogger("pleiepengesoknad-prosessering")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                        addInterceptorLast(apiGatewayHttpRequestInterceptor)
                    }
                }
            }
        )

        fun pleiepengerDokumentClient() : MonitoredHttpClient = MonitoredHttpClient(
            source = "pleiepengesoknad-api",
            destination = "pleiepenger-dokument",
            overridePaths = mapOf(
                Regex("/v1/dokument/.+") to "/v1/dokument"
            ),
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        dusseldorfConfigured()
                    }
                }
                install (Logging) {
                    sl4jLogger("pleiepenger-dokument")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                    }
                }
            }
        )
    }
}