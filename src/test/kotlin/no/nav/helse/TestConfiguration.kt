package no.nav.helse

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.common.KafkaEnvironment
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getLoginServiceV1WellKnownUrl
import no.nav.helse.wiremock.getK9MellomlagringUrl
import no.nav.helse.wiremock.getK9OppslagUrl
import no.nav.helse.wiremock.getPleiepengesoknadMottakUrl

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        kafkaEnvironment: KafkaEnvironment? = null,
        port : Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        pleiepengesoknadMottakUrl : String? = wireMockServer?.getPleiepengesoknadMottakUrl(),
        k9MellomlagringUrl : String? = wireMockServer?.getK9MellomlagringUrl(),
        corsAdresses : String = "http://localhost:8080",
        redisServer: RedisServer
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port", "$port"),
            Pair("nav.authorization.cookie_name", "localhost-idtoken"),
            Pair("nav.gateways.k9_oppslag_url", "$k9OppslagUrl"),
            Pair("nav.gateways.pleiepengesoknad_mottak_base_url", "$pleiepengesoknadMottakUrl"),
            Pair("nav.gateways.k9_mellomlagring_url", "$k9MellomlagringUrl"),
            Pair("nav.cors.addresses", corsAdresses),
        )

        // Clients
        if (wireMockServer != null) {
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = "pleiepengesoknad-api"
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.0.certificate_hex_thumbprint"] = ClientCredentials.ClientA.certificateHexThumbprint
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.pleiepengesoknad-mottak-client-id"] = "pleiepengesoknad-mottak/.default"
            map["nav.auth.scopes.k9-mellomlagring-scope"] = "k9-mellomlagring/.default"

            map["nav.auth.issuers.0.alias"] = "login-service-v1"
            map["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.alias"] = "login-service-v2"
            map["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.audience"] = LoginService.V1_0.getAudience()
        }

        map["nav.redis.host"] = "localhost"
        map["nav.redis.port"] = "${redisServer.bindPort}"
        map["nav.storage.passphrase"] = "verySecret"

        map["nav.mellomlagring.tid_timer"] = "1"

        // Kafka
        kafkaEnvironment?.let {
            map["nav.kafka.bootstrap_servers"] = it.brokersURL
        }

        return map.toMap()
    }
}
