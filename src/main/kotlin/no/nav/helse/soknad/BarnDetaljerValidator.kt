package no.nav.helse.soknad

import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.extractFodselsdato
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class BarnDetaljerValidator : ConstraintValidator<ValidBarnDetaljer, BarnDetaljer> {

    override fun isValid(value: BarnDetaljer?, context: ConstraintValidatorContext?): Boolean {
        if (value!!.fodselsnummer == null && value.fodselsdato == null) {
            return withError(context,"'fodselsnummer' eller 'fodselsdato' må oppgis.")
        }

        if (value.fodselsnummer != null && value.fodselsdato == null) {
            try {
                extractFodselsdato(fnr = Fodselsnummer(value.fodselsnummer!!))
            } catch (cause: Throwable) {
                return withError(context,"'fodselsdato' må sendes ettersom den ikke kan avledes fra 'fodselsnummer'.")
            }
        }


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