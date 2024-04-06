package com.n0te15m3.quick

sealed class LoadingState {
    data object Loading : LoadingState()

    data class Success(
        val data: ByteArray,
        val source: DataSource,
    ) : LoadingState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class Failure(
        val reason: String
    ) : LoadingState()
}