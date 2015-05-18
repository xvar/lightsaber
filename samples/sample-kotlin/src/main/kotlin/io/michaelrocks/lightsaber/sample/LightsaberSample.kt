/*
 * Copyright 2015 Michael Rozumyanskiy
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

import io.michaelrocks.lightsaber.Lightsaber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.platform.platformStatic

public class LightsaberSample {
    companion object {
        platformStatic public fun main(args: Array<String>) {
            LightsaberSample().run()
        }
    }

    Inject
    private var wookiee: Wookiee? = null
    Inject
    private var wookieeProvider: Provider<Wookiee>? = null

    Inject
    private var droid: Droid? = null
    Inject
    private var droidProvider: Provider<Droid>? = null

    private fun run() {
        System.out.println("Before injection")
        System.out.println("Wookiee: $wookiee")
        System.out.println("Droid: $droid")
        val injector = Lightsaber.createInjector(LightsaberModule())
        injector.injectMembers(this)
        System.out.println("After injection")
        System.out.println("Wookiee: $wookiee from ${wookiee!!.planet}")
        val anotherWookiee = wookieeProvider!!.get()
        System.out.println("Another wookiee: $anotherWookiee from ${anotherWookiee.planet}")
        System.out.println("Droid: $droid")
        val anotherDroid = droidProvider!!.get()
        System.out.println("Another droid: $anotherDroid")
        wookiee!!.roar()
        droid!!.repair()
    }
}