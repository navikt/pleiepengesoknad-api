package no.nav.helse


class Soknad {
    companion object {
        fun forLangtNavn() = "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"
        fun body(
            fodselsnummer: String,
            fraOgMed: String? = "2018-10-10",
            tilOgMed: String? = "2019-10-10",
            vedleggUrl1: String,
            vedleggUrl2: String) : String {
            return """
                {
                    "barn": {
                        "navn": "Santa ÆØÅ Winter",
                        "fodselsnummer": "$fodselsnummer"
                    },
                    "relasjon_til_barnet": "mor",
                    "fra_og_med": "$fraOgMed",
                    "til_og_med": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet"
                            }
                        ]
                    },
                    "vedlegg": [
                        "$vedleggUrl1",
                        "$vedleggUrl2"
                    ],
                    "medlemskap" : {
                        "har_bodd_i_utlandet_siste_12_mnd" : false,
                        "skal_bo_i_utlandet_neste_12_mnd" : true
                    },
                    "grad": 100,
                    "har_medsoker": true,
	                "er_selvstendig_naeringsdrivende_eller_frilanser": false,
	                "forventes_at_barnet_kan_vaere_i_etablert_tilsynsordning": false
                }
                """.trimIndent()
        }
    }
}
