package no.nav.helse


class SoknadUtils {
    companion object {
        fun forLangtNavn() =
            "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

        fun bodyMedFodselsnummerPaaBarn(
            fodselsnummer: String,
            fraOgMed: String? = "2018-10-10",
            tilOgMed: String? = "2019-10-10",
            vedleggUrl1: String,
            vedleggUrl2: String,
            utenGrad: Boolean = false
        ): String {
            return """
                {
                    "barn": {
                        "fodselsnummer": "$fodselsnummer",
                        "navn": "Barn Barnesen"
                    },
                    "relasjon_til_barnet": "mor",
                    "fra_og_med": "$fraOgMed",
                    "til_og_med": "$tilOgMed",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ"
                                ${if (utenGrad)
                """
                                    , "skal_jobbe_prosent": 50
                                """.trimIndent()
            else ""}
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
                        "utenlandsopphold_i_perioden": {
                            "skal_oppholde_seg_i_utlandet_i_perioden": true,
                            "opphold": [
                                {
                                    "fra_og_med": "2019-10-10",
                                    "til_og_med": "2019-11-10",
                                    "landkode": "SE",
                                    "landnavn": "Sverige"
                                },
                                {
                                    "landnavn": "USA",
                                    "landkode": "US",
                                    "fra_og_med": "2020-01-08",
                                    "til_og_med": "2020-01-09",
                                    "er_utenfor_eos": true,
                                    "er_barnet_innlagt": true,
                                    "arsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                                }
                            ]
                        },
                    ${if (utenGrad) """
                        "dager_per_uke_borte_fra_jobb": 4.0,
                    """.trimIndent() else """
                        "grad": 100,
                    """.trimIndent()}
                    "har_medsoker": true,
                    "har_bekreftet_opplysninger": true,
                    "har_forstatt_rettigheter_og_plikter": true
                }
                """.trimIndent()
        }

        fun bodyMedAktoerIdPaaBarn(
            aktoerId: String,
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            return """
                {
                    "barn": {
                        "aktoer_id": "$aktoerId"
                    },
                    "fra_og_med": "2018-10-10",
                    "til_og_med": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ"
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
                    "utenlandsopphold_i_perioden": {
                        "skal_oppholde_seg_i_utlandet_i_perioden": false,
                        "opphold": []
                    },
                    "grad": 100,
                    "har_medsoker": true,
                    "har_bekreftet_opplysninger": true,
                    "har_forstatt_rettigheter_og_plikter": true
                }
                """.trimIndent()
        }


        fun bodyUtenIdPaaBarn(
            vedleggUrl1: String,
            vedleggUrl2: String
        ): String {
            return """
                {
                    "barn": {},
                    "relasjon_til_barnet": "mor",
                    "fra_og_med": "2018-10-10",
                    "til_og_med": "2019-10-10",
                    "arbeidsgivere": {
                        "organisasjoner": [
                            {
                                "organisasjonsnummer": "917755736",
                                "navn": "Bjeffefirmaet ÆÆÅ"
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
                    "utenlandsopphold_i_perioden": {
                        "skal_oppholde_seg_i_utlandet_i_perioden": false,
                        "opphold": []
                    },
                    "grad": 100,
                    "har_medsoker": true,
                    "har_bekreftet_opplysninger": true,
                    "har_forstatt_rettigheter_og_plikter": true
                }
                """.trimIndent()
        }
    }
}
