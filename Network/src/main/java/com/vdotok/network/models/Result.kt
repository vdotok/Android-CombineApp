package com.vdotok.network.models


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 12:50 PM in 2021
 *
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val error: ParsedError) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[apiError=$error]"
        }
    }

    fun getDataOrNull() = if (this is Success) data else null
}


data class ParsedError (
    val message: String,
    val responseCode: Int
)
