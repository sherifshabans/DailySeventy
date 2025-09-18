package com.elsharif.dailyseventy.presentation.components


import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

sealed interface Movement {
    enum class Vertical : Movement { Top, Bottom }
    enum class Horizontal : Movement { Left, Right }
}

object AutoScrollCarouselDefaults {
    const val DefaultSpeedPxPerMillis: Float = 0.05f
    const val FirstVisibleItemOffset: Int = 0
    const val InteractionDelayMillis: Long = 500
    const val FirstVisibleItemIndex: Int = 0
    val ItemSpacing = 10.dp
}

private const val VeryLargeItemCount = Int.MAX_VALUE

private fun calculateStartIndex(itemsSize: Int, startIndex: Int = 0): Int {
    require(startIndex >= 0) { "Index must start from 0" }
    require(startIndex < itemsSize) { "Given index cannot exceed the items size" }
    val midPoint = VeryLargeItemCount / 2
    return midPoint - (midPoint % itemsSize) + startIndex
}

@Composable
fun <T> AutoScrollCarouselList(
    items: List<T>,
    movement: Movement,
    modifier: Modifier = Modifier,
    isScrolling: Boolean = true,
    initialFirstVisibleItemScrollOffset: Int = AutoScrollCarouselDefaults.FirstVisibleItemOffset,
    scrollSpeedPxPerMillis: Float = AutoScrollCarouselDefaults.DefaultSpeedPxPerMillis,
    interactionDelayMillis: Long = AutoScrollCarouselDefaults.InteractionDelayMillis,
    firstVisibleItemIndex: Int = AutoScrollCarouselDefaults.FirstVisibleItemIndex,
    itemSpacing: Dp = AutoScrollCarouselDefaults.ItemSpacing,
    itemContent: @Composable LazyItemScope.(index: Int, value: T) -> Unit,
) {
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = calculateStartIndex(items.size, firstVisibleItemIndex),
        initialFirstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
    )

    LaunchedEffect(
        key1 = Pair(isScrolling, lazyListState.isScrollInProgress),
        key2 = movement,
        key3 = Pair(interactionDelayMillis, scrollSpeedPxPerMillis)
    ) {
        if (!isScrolling) return@LaunchedEffect
        var lastTimeNanos: Long

        snapshotFlow { lazyListState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .first() // suspend until not scrolling

        delay(interactionDelayMillis)

        lastTimeNanos = withFrameNanos { it }

        while (!lazyListState.isScrollInProgress) {
            val currentTimeNanos = withFrameNanos { it }
            val deltaMillis = (currentTimeNanos - lastTimeNanos) / 1_000_000f

            val directionFactor = when (movement) {
                Movement.Horizontal.Left,
                Movement.Vertical.Top -> -1

                Movement.Horizontal.Right,
                Movement.Vertical.Bottom -> 1
            }

            lazyListState.scrollBy(directionFactor * scrollSpeedPxPerMillis * deltaMillis)
            lastTimeNanos = currentTimeNanos
        }
    }

    when (movement) {
        is Movement.Horizontal -> {
            LazyRow(
                modifier = modifier,
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                content = {
                    items(VeryLargeItemCount) { virtualIndex ->
                        val actualIndex = (virtualIndex % items.size + items.size) % items.size
                        val item = items[actualIndex]
                        itemContent(actualIndex, item)
                    }
                }
            )
        }

        is Movement.Vertical -> {
            LazyColumn(
                modifier = modifier,
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                content = {
                    items(VeryLargeItemCount) { virtualIndex ->
                        val actualIndex = (virtualIndex % items.size + items.size) % items.size
                        val item = items[actualIndex]
                        itemContent(actualIndex, item)
                    }
                }
            )
        }
    }
}
