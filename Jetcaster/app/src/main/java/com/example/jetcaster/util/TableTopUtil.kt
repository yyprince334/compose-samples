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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import kotlinx.coroutines.flow.collect

data class TableTopInfo(
    val isInTableTopMode: Boolean = false,
    val bounds: Rect? = null
)

@Composable
fun TableTopLayout(
    tableTopInfo: TableTopInfo,
    topContent: @Composable (Modifier) -> Unit,
    bottomContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!tableTopInfo.isInTableTopMode)
        throw IllegalStateException("TableTopLayout should be used when tabletop mode is active ")

    val bounds: Rect = tableTopInfo.bounds
        ?: throw IllegalStateException("Bounds should never be null in tabletop mode")

    val hingePosition = with(LocalDensity.current) {
        bounds.top.toDp()
    }
    val hingeHeight = with(LocalDensity.current) {
        (bounds.bottom - bounds.top).toDp()
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        topContent(Modifier.height(hingePosition))
        Spacer(modifier = Modifier.height(hingeHeight))
        bottomContent()
    }
}

@Composable
fun ComponentActivity.getTableTopInfo(): State<TableTopInfo> {
    val windowManager = windowInfoRepository()
    return produceState(initialValue = TableTopInfo(), key1 = windowManager) {
        this@getTableTopInfo.repeatOnLifecycle(Lifecycle.State.STARTED) {
            windowManager.windowLayoutInfo.collect { layoutInfo ->
                val foldingFeature: FoldingFeature? =
                    layoutInfo.displayFeatures.find { it is FoldingFeature } as? FoldingFeature
                value = TableTopInfo(isTableTopMode(foldingFeature), foldingFeature?.bounds)
            }
        }
    }
}

private fun isTableTopMode(foldFeature: FoldingFeature?) =
    foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
            foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
