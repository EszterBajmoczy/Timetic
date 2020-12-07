package hu.bme.aut.android.timetic.forms

class ResetFormState(val code: Int? = null,
                     val passwordError: Int? = null,
                     val passwordsNotMatchError: Int? = null,
                     val isDataValid: Boolean = false)