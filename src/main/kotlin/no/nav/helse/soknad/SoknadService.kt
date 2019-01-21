package no.nav.helse.soknad

import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.extractFodselsdato
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.Image2PDFConverter

class SoknadService(val soknadKafkaProducer: SoknadKafkaProducer,
                    val sokerService: SokerService,
                    val image2PDFConverter: Image2PDFConverter) {

    suspend fun registrer(
        soknad: Soknad,
        fnr: Fodselsnummer
    ) {

        val komplettSoknad = KomplettSoknad(
            fraOgMed = soknad.fraOgMed,
            tilOgMed = soknad.tilOgMed,
            soker = sokerService.getSoker(fnr),
            barn = leggTilFodselsDato(soknad.barn),
            vedlegg = prosseserVedlegg(soknad.vedlegg),
            ansettelsesforhold = soknad.ansettelsesforhold
        )

        soknadKafkaProducer.produce(komplettSoknad)
    }

    private fun leggTilFodselsDato(barnDetaljer: BarnDetaljer) : BarnDetaljer {
        if (barnDetaljer.fodselsdato == null) {
            barnDetaljer.medFodselsDato(extractFodselsdato(Fodselsnummer(barnDetaljer.fodselsnummer!!)))
        }
        return barnDetaljer
    }

    private fun prosseserVedlegg(vedlegg: List<Vedlegg>) : List<PdfVedlegg> {
        val pdfVedlegg = mutableListOf<PdfVedlegg>()
        vedlegg.forEach {
            pdfVedlegg.add(
                PdfVedlegg(
                innhold = image2PDFConverter.convert(it.innhold)
            ))
        }
        return pdfVedlegg.toList()
    }
}