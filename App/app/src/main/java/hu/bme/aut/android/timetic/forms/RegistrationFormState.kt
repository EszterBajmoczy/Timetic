package hu.bme.aut.android.timetic.forms

/**
 * Data validation state of the login form.
 */
data class RegistrationFormState(val usernameError: Int? = null,
                                 val emailError: Int? = null,
                                 val passwordError: Int? = null,
                                 val isDataValid: Boolean = false)
