package no.nav.helse.soknad

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class SoknadValidator : ConstraintValidator<ValidSoknad, Soknad> {

    override fun isValid(value: Soknad?, context: ConstraintValidatorContext?): Boolean {
        var valid = true

        if (value!!.tilOgMed.isBefore(value.fraOgMed)) {
            valid = withError(context, "Startdato kan ikke være før sluttdato")
        }

        return valid
    }

    private fun withError(
        context: ConstraintValidatorContext?,
        error: String) : Boolean {
        context!!.disableDefaultConstraintViolation()
        context
            .buildConstraintViolationWithTemplate(error)
            .addPropertyNode("soknad")
            .addConstraintViolation()
        return false
    }

}