package com.n0te15m3.quick

import androidx.compose.ui.graphics.ImageBitmap
import com.n0te15m3.quick.DataSource
import com.n0te15m3.quick.LoadingState
import com.n0te15m3.quick.utils.toImageBitmap

sealed class FetchingState {
    data object Fetching : FetchingState()

    data class Success(
        val data: ImageBitmap,
        val source: DataSource
    ) : FetchingState()

    data class Failure(
        val reason: String
    ) : FetchingState()

    companion object {
        fun from(loadingState: LoadingState): FetchingState {
            return when (loadingState) {
                is LoadingState.Loading -> Fetching
                is LoadingState.Failure -> Failure(loadingState.reason)
                is LoadingState.Success -> Success(
                    data = loadingState.data.toImageBitmap(),
                    source = loadingState.source
                )
            }
        }
    }
}