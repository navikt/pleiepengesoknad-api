package no.nav.helse.soknad

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.VedleggService

class SoknadService(val pleiepengesoknadProsesseringGateway: PleiepengesoknadProsesseringGateway,
                    val sokerService: SokerService,
                    val vedleggService: VedleggService) {

    suspend fun registrer(
        soknad: Soknad,
        fnr: Fodselsnummer,
        callId: CallId
    ) {

        val komplettSoknad = KomplettSoknad(
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = sokerService.getSoker(fnr = fnr, callId = callId),
            barn = soknad.barn,
            vedlegg = vedleggService.hentOgSlettVedlegg(
                vedleggUrls = soknad.vedlegg,
                fnr = fnr
            ),
            ansettelsesforhold = soknad.ansettelsesforhold
        )

        pleiepengesoknadProsesseringGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )
    }
}