/*
 * Copyright 2020 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.lightsaber.sample

import io.michaelrocks.lightsaber.ImportedBy
import io.michaelrocks.lightsaber.Module
import io.michaelrocks.lightsaber.Provide
import io.michaelrocks.lightsaber.sample.library.Droid

@Module(isDefault = true)
@ImportedBy(LightsaberComponent::class)
internal class LightsaberModule {

  @Provide
  private fun provideDroid(factory: DroidFactory): Droid = factory.produceR2D2("Silver")
}
