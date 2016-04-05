/*
 * Copyright 2016 Michael Rozumyanskiy
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

import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import org.objectweb.asm.Type

sealed class ProvisionPoint {
  abstract val containerType: Type
  abstract val dependency: Dependency

  abstract class AbstractMethod : ProvisionPoint() {
    override val containerType: Type
      get() = injectionPoint.containerType

    abstract val injectionPoint: InjectionPoint.Method

    val method: MethodMirror
      get() = injectionPoint.method
  }

  class Constructor(
      override val dependency: Dependency,
      override val injectionPoint: InjectionPoint.Method
  ) : AbstractMethod()

  class Method(
      override val dependency: Dependency,
      override val injectionPoint: InjectionPoint.Method
  ) : AbstractMethod()

  class Field(
      override val containerType: Type,
      override val dependency: Dependency,
      val field: FieldMirror
  ) : ProvisionPoint()
}