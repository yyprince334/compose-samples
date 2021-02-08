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

package androidx.compose.samples.crane.home

import androidx.compose.samples.crane.data.DestinationsLocalDataSource
import androidx.compose.samples.crane.data.DestinationsRepository
import androidx.compose.samples.crane.data.DestinationsRepositoryImpl
import androidx.compose.samples.crane.data.ExploreModel
import androidx.compose.samples.crane.di.DefaultDispatcher
import androidx.compose.samples.crane.di.DestinationsRepositoryModule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@UninstallModules(DestinationsRepositoryModule::class)
@HiltAndroidTest
class HomeTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MainScreen({ }, { })
        }
    }

    @Test
    fun home_navigatesToAllScreens() {
        composeTestRule.mainClock.advanceTimeBy(2001) // Bypass splash animation
        composeTestRule.onNodeWithText("Explore Flights by Destination").assertIsDisplayed()
        composeTestRule.onNodeWithText("SLEEP").performClick()
        composeTestRule.onNodeWithText("Explore Properties by Destination").assertIsDisplayed()
        composeTestRule.onNodeWithText("EAT").performClick()
        composeTestRule.onNodeWithText("Explore Restaurants by Destination").assertIsDisplayed()
        composeTestRule.onNodeWithText("FLY").performClick()
        composeTestRule.onNodeWithText("Explore Flights by Destination").assertIsDisplayed()
    }

    @Test
    fun home_peopleClickRefreshesDestinations() {
        composeTestRule.mainClock.advanceTimeBy(2001) // Bypass splash animation
        composeTestRule.onNodeWithText("Explore Flights by Destination").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 Adult, Economy").performClick()
        // When selecting two people, only the Madrid destination will be on the screen
        composeTestRule.onNodeWithText("Madrid", substring = true).assertIsDisplayed()
    }

    /**
     * Creates a DestinationsRepository to be used _just_ in the test class. When asked for
     * fly destinations, it'll return only "Khumbu Valley". When asked about destinations for
     * two people, "Madrid" will be returned.
     */
    @Module
    @InstallIn(SingletonComponent::class)
    class DestinationsRepositoryTestModule {
        @Provides
        fun provideDestinationsRepository(
            destinationsLocalDataSource: DestinationsLocalDataSource,
            @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
        ): DestinationsRepository {
            return object : DestinationsRepositoryImpl(
                destinationsLocalDataSource,
                defaultDispatcher
            ) {
                override val destinations: List<ExploreModel> =
                    destinationsLocalDataSource.craneDestinations.subList(0, 1)

                override suspend fun calculateDestinations(people: Int): List<ExploreModel> =
                    withContext(defaultDispatcher) {
                        when {
                            people > MAX_PEOPLE -> emptyList<ExploreModel>()
                            people == 2 -> findDestination("Madrid")
                            else -> findDestination("Rome")
                        }
                    }

                private fun findDestination(cityName: String): List<ExploreModel> =
                    destinationsLocalDataSource.craneDestinations.filter {
                        it.city.nameToDisplay.contains(cityName)
                    }
            }
        }
    }
}
