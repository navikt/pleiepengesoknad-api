package no.nav.helse.soknad

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class BarnDetaljerValidator : ConstraintValidator<ValidBarnDetaljer, BarnDetaljer> {

    override fun isValid(value: BarnDetaljer?, context: ConstraintValidatorContext?): Boolean {
        return true
    }

    private fun withError(
        context: ConstraintValidatorContext?,
        error: String) : Boolean {
        context!!.disableDefaultConstraintViolation()
        context
            .buildConstraintViolationWithTemplate(error)
            .addConstraintViolation()
        return false
    }

}