package no.nav.helse.soknad

import javax.validation.Constraint
import kotlin.reflect.KClass

@Constraint(validatedBy = arrayOf(SoknadValidator::class))
annotation class ValidSoknad(
    val message: String = "Ikke gyldige opplysninger på søknad.",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)