package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.common.KafkaEnvironment
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getLoginServiceV1WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getTokendingsWellKnownUrl
import no.nav.helse.wiremock.getK9BrukerdialogCacheUrl
import no.nav.helse.wiremock.getK9MellomlagringUrl
import no.nav.helse.wiremock.getK9OppslagUrl
import no.nav.helse.wiremock.getSifInnsynApiUrl
import no.nav.security.mock.oauth2.MockOAuth2Server

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        kafkaEnvironment: KafkaEnvironment? = null,
        port : Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        sifInnaynApiUrl: String? = wireMockServer?.getSifInnsynApiUrl(),
        k9MellomlagringUrl : String? = wireMockServer?.getK9MellomlagringUrl(),
        k9BrukerdialogCacheUrl : String? = wireMockServer?.getK9BrukerdialogCacheUrl(),
        corsAdresses : String = "http://localhost:8080",
        mockOAuth2Server: MockOAuth2Server
    ) : Map<String, String> {
        val map = mutableMapOf(
            Pair("ktor.deployment.port", "$port"),
            Pair("nav.authorization.cookie_name", "selvbetjening-idtoken"),
            Pair("nav.gateways.k9_oppslag_url", "$k9OppslagUrl"),
            Pair("nav.gateways.sif_innsyn_api_url", "$sifInnaynApiUrl"),
            Pair("nav.gateways.k9_mellomlagring_url", "$k9MellomlagringUrl"),
            Pair("nav.gateways.k9_brukerdialog_cache_url", "$k9BrukerdialogCacheUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("NAIS_CLUSTER_NAME", "local")
        )

        // Clients
        if (wireMockServer != null) {
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = "pleiepengesoknad-api"
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.0.certificate_hex_thumbprint"] = ClientCredentials.ClientA.certificateHexThumbprint
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()

            map["nav.auth.clients.1.alias"] = "tokenx"
            map["nav.auth.clients.1.client_id"] = "pleiepengesoknad-api"
            map["nav.auth.clients.1.private_key_jwk"] = ClientCredentials.ClientC.privateKeyJwk
            map["nav.auth.clients.1.discovery_endpoint"] = wireMockServer.getTokendingsWellKnownUrl()

            map["nav.auth.scopes.k9-mellomlagring-scope"] = "k9-mellomlagring/.default"
            map["nav.auth.scopes.k9-brukerdialog-cache-tokenx-audience"] = "dev-gcp:dusseldorf:k9-brukerdialog-cache"

            map["nav.auth.issuers.0.alias"] = "login-service-v1"
            map["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.alias"] = "login-service-v2"
            map["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.audience"] = LoginService.V1_0.getAudience()
        }

        // Issuers
        map["no.nav.security.jwt.issuers.0.issuer_name"] = "tokendings"
        map["no.nav.security.jwt.issuers.0.discoveryurl"] = "${mockOAuth2Server.wellKnownUrl("tokendings")}"
        map["no.nav.security.jwt.issuers.0.accepted_audience"] = "dev-gcp:dusseldorf:pleiepengesoknad-api"

        map["no.nav.security.jwt.issuers.1.issuer_name"] = "login-service-v2"
        map["no.nav.security.jwt.issuers.1.discoveryurl"] = "${mockOAuth2Server.wellKnownUrl("login-service-v2")}"
        map["no.nav.security.jwt.issuers.1.accepted_audience"] = "dev-gcp:dusseldorf:pleiepengesoknad-api"
        map["no.nav.security.jwt.issuers.1.cookie_name"] = "selvbetjening-idtoken"

        map["nav.mellomlagring.søknad_tid_timer"] = "1"
        map["nav.mellomlagring.endringsmelding_tid_timer"] = "1"

        // Kafka
        kafkaEnvironment?.let {
            map["nav.kafka.bootstrap_servers"] = it.brokersURL
        }

        return map.toMap()
    }
}
