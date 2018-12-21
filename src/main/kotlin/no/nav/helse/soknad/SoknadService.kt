package no.nav.helse.soknad

import no.nav.helse.barn.BarnService
import no.nav.helse.barn.KomplettBarn
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.soker.SokerService
import no.nav.helse.vedlegg.Image2PDFConverter

class SoknadService(val soknadKafkaProducer: SoknadKafkaProducer,
                    val barnService: BarnService,
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
            barn = leggTilFodselsnummer(soknad.barn, fnr),
            vedlegg =  prosseserVedlegg(soknad.vedlegg),
            ansettelsesforhold = soknad.ansettelsesforhold
        )

        soknadKafkaProducer.produce(komplettSoknad)
    }

    private suspend fun leggTilFodselsnummer(
        barnDetlajer : List<BarnDetaljer>,
        sokerFnr: Fodselsnummer) : List<BarnDetaljer> {

        val kompletteBarn = barnService.getBarn(sokerFnr)

        barnDetlajer.forEach {
            if (it.fodselsnummer == null) {
                for (komplettBarn in kompletteBarn) {
                    if (erSammeBarn(komplettBarn, it)) {
                        it.medFodselsnummer(komplettBarn.fodselsnummer)
                    }
                }
            }
        }
        return barnDetlajer
    }

    private fun erSammeBarn(komplettBarn: KomplettBarn,
                            barnDetaljer: BarnDetaljer) : Boolean {
        return  komplettBarn.fodselsdato.isEqual(barnDetaljer.fodselsdato) &&
                komplettBarn.fornavn.trim().equals(barnDetaljer.fornavn.trim(), ignoreCase = true) &&
                komplettBarn.etternavn.trim().equals(barnDetaljer.etternavn.trim(), ignoreCase = true)
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