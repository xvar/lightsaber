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

package io.michaelrocks.lightsaber.processor.model

import io.michaelrocks.grip.mirrors.Type

data class Component(
  val type: Type.Object,
  val moduleProviders: Collection<ModuleProvider>,
  val parent: Type.Object?,
  val subcomponents: Collection<Type.Object>
) {

  val modules: Collection<Module> = moduleProviders.map { it.module }

  fun getModulesWithDescendants(): Sequence<Module> = sequence {
    yieldModulesWithDescendants(modules)
  }

  private suspend fun SequenceScope<Module>.yieldModulesWithDescendants(modules: Iterable<Module>) {
    modules.forEach { yieldModulesWithDescendants(it) }
  }

  private suspend fun SequenceScope<Module>.yieldModulesWithDescendants(module: Module) {
    yield(module)
    yieldModulesWithDescendants(module.modules)
  }
}
