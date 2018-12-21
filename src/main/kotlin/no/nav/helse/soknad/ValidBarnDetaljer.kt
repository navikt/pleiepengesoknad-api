package no.nav.helse.soknad

import javax.validation.Constraint
import kotlin.reflect.KClass

@Constraint(validatedBy = arrayOf(BarnDetaljerValidator::class))
annotation class ValidBarnDetaljer(
    val message: String = "Ikke gyldige opplysninger om barn.",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)