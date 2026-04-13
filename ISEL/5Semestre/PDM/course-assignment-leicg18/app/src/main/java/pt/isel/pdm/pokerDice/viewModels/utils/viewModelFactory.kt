package pt.isel.pdm.pokerDice.viewModels.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified VM : ViewModel> viewModelFactory(noinline creator: () -> VM) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            if (modelClass.isAssignableFrom(VM::class.java)) {
                creator() as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
    }