package no.nav.helse.vedlegg

class UnsupportedAttachementTypeException(type: String) : RuntimeException("Formatet '$type' støttes ikke som vedlegg.")