package no.nav.helse.soknad

import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.extractFodselsdato
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class BarnDetaljerValidator : ConstraintValidator<ValidBarnDetaljer, BarnDetaljer> {

    override fun isValid(value: BarnDetaljer?, context: ConstraintValidatorContext?): Boolean {
        var valid = true

        if (value!!.fodselsnummer == null && value.fodselsdato == null) {
            valid = withError(context,"'fodselsnummer' eller 'fodselsdato' må oppgis.")
        } else if (value.fodselsnummer != null && value.fodselsdato == null) {
            try {
                extractFodselsdato(fnr = Fodselsnummer(value.fodselsnummer))
            } catch (cause: Throwable) {
                valid =  withError(context,"'fodselsdato' må sendes ettersom den ikke kan avledes fra 'fodselsnummer'.")
            }
        } else if (value.fodselsnummer != null && value.fodselsdato != null) {
            if (!value.fodselsdato!!.isEqual(extractFodselsdato(Fodselsnummer(value.fodselsnummer)))) {
                valid =  withError(context,"'fodselsdato' og 'fodselsnummer' samsvarer ikke.")
            }
        }

        return valid
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