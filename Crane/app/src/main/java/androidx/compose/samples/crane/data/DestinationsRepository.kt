/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.samples.crane.data

import androidx.compose.samples.crane.home.MAX_PEOPLE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.random.Random

interface DestinationsRepository {
    val destinations: List<ExploreModel>
    val hotels: List<ExploreModel>
    val restaurants: List<ExploreModel>

    fun getDestination(cityName: String): ExploreModel?
    suspend fun calculateDestinations(people: Int): List<ExploreModel>
    suspend fun filterDestinations(destination: String): List<ExploreModel>
}

open class DestinationsRepositoryImpl(
    private val destinationsLocalDataSource: DestinationsLocalDataSource,
    private val defaultDispatcher: CoroutineDispatcher
): DestinationsRepository {
    override val destinations: List<ExploreModel> = destinationsLocalDataSource.craneDestinations
    override val hotels: List<ExploreModel> = destinationsLocalDataSource.craneHotels
    override val restaurants: List<ExploreModel> = destinationsLocalDataSource.craneRestaurants

    override fun getDestination(cityName: String): ExploreModel? {
        return destinationsLocalDataSource.craneDestinations.firstOrNull {
            it.city.name == cityName
        }
    }

    override suspend fun calculateDestinations(people: Int): List<ExploreModel> =
        withContext(defaultDispatcher) {
            if (people > MAX_PEOPLE) {
                emptyList()
            } else {
                destinations.shuffled(Random(people * (1..100).shuffled().first()))
            }
        }

    override suspend fun filterDestinations(destination: String): List<ExploreModel> =
        withContext(defaultDispatcher) {
            destinations.filter { it.city.nameToDisplay.contains(destination) }
        }
}
