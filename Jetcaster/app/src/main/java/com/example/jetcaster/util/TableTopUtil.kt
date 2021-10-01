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

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import kotlinx.coroutines.flow.collect


@Composable
fun isInTableTopMode(): State<Boolean> {
    val activity = LocalContext.current.findActivity()
    val windowManager = activity.windowInfoRepository()
    return produceState(initialValue = false, key1 = activity, key2 = windowManager) {
        activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
            windowManager.windowLayoutInfo.collect { layoutInfo ->
                val foldingFeature: FoldingFeature? =
                    layoutInfo.displayFeatures.find { it is FoldingFeature } as? FoldingFeature
                value = isTableTopMode(foldingFeature)
            }
        }
    }
}

private fun isTableTopMode(foldFeature: FoldingFeature?) =
    foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
            foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL

/**
 * Find the closest Activity in a given Context.
 */
private tailrec fun Context.findActivity(): ComponentActivity =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity should be called in the context of an Activity"
        )
    }
