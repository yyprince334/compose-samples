/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetcaster.util

import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Information about a foldable device
 */
data class FoldableInfo(
    val isInTableTopPosture: Boolean = false,
    val isInBookPosture: Boolean = false,
    val hingePosition: Rect? = null
)

/**
 * Flow of [FoldableInfo] that emits every time there's a change in the windowLayoutInfo
 */
@Composable
fun ComponentActivity.rememberFoldableInfo(): Flow<FoldableInfo> {
    val windowManager = windowInfoRepository()
    val coroutineScope = rememberCoroutineScope()
    return remember(this, coroutineScope, windowManager) {
        windowManager.windowLayoutInfo
            .flowWithLifecycle(this.lifecycle)
            .map { layoutInfo ->
                val foldingFeature: FoldingFeature? =
                    layoutInfo.displayFeatures.find { it is FoldingFeature } as? FoldingFeature
                FoldableInfo(
                    isInTableTopPosture = isTableTopPosture(foldingFeature),
                    isInBookPosture = isBookPosture(foldingFeature),
                    hingePosition = foldingFeature?.bounds
                )
            }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FoldableInfo()
            )
    }
}

private fun isTableTopPosture(foldFeature: FoldingFeature?) =
    foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
        foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL

private fun isBookPosture(foldFeature: FoldingFeature?) =
    foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
        foldFeature.orientation == FoldingFeature.Orientation.VERTICAL
