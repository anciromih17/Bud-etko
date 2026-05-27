package si.um.feri.budzetko.viewmodel

data class ProfileEditState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
