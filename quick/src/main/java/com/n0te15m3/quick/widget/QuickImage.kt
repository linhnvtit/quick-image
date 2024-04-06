package com.n0te15m3.quick.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.tl.speedimage.FetchingState
import com.n0te15m3.quick.Quick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun QuickImage(
    modifier: Modifier = Modifier,
    url: String,
    loadingPlaceholder: @Composable (() -> Unit)? = null,
    failurePlaceholder: @Composable (() -> Unit)? = null,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    when (val source = filteredData(url = url)) {
        is FetchingState.Fetching -> {
            if (loadingPlaceholder != null) {
                loadingPlaceholder.invoke()
            } else {
                Box(modifier = modifier)
            }
        }

        is FetchingState.Failure -> {
            if (failurePlaceholder != null) {
                failurePlaceholder.invoke()
            } else {
                DefaultFailurePlaceholder(modifier = modifier)
            }
        }

        is FetchingState.Success -> {
            Image(
                modifier = modifier,
                bitmap = source.data,
                contentDescription = contentDescription,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = filterQuality
            )
        }
    }
}

@Composable
fun QuickImage(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        imageVector = imageVector, contentDescription = contentDescription, modifier = modifier, alignment = alignment, contentScale = contentScale, alpha = alpha, colorFilter = colorFilter
    )
}

@Composable
fun QuickImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter, contentDescription = contentDescription, modifier = modifier, alignment = alignment, contentScale = contentScale, alpha = alpha, colorFilter = colorFilter
    )
}

@Composable
fun filteredData(url: String): FetchingState {
    val context = LocalContext.current

    var data: FetchingState by remember { mutableStateOf(FetchingState.Fetching) }

    LaunchedEffect(url) {
        launch(Dispatchers.Default) {
            Quick.get(context).getFilteredSource(url).collectLatest {
                if (it::class != data::class) {
                    data = it
                }
            }
        }

        launch(Dispatchers.Default) {
            Quick.get(context).fetchImage(url)
        }
    }

    return data
}

