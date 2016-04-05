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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.lightsaber.processor.io.FileSink
import io.michaelrocks.lightsaber.processor.io.FileSource
import io.michaelrocks.lightsaber.processor.io.IoFactory
import java.io.File
import java.util.*

class ProcessorContext(
    val inputFile: File,
    val outputFile: File,
    libraries: List<File>
) {
  var classFilePath: String? = null
  private val errorsByPath = LinkedHashMap<String, MutableList<Exception>>()

  val fileSourceFactory: FileSource.Factory = IoFactory
  val fileSinkFactory: FileSink.Factory = IoFactory
  val grip: Grip = GripFactory.create(listOf(inputFile) + libraries)

  fun hasErrors(): Boolean {
    return !errorsByPath.isEmpty()
  }

  val errors: Map<String, List<Exception>>
    get() = Collections.unmodifiableMap(errorsByPath)

  fun reportError(errorMessage: String) {
    reportError(ProcessingException(errorMessage, classFilePath))
  }

  fun reportError(error: Exception) {
    var errors: MutableList<Exception>? = errorsByPath.get(classFilePath.orEmpty())
    if (errors == null) {
      errors = ArrayList<Exception>()
      errorsByPath.put(classFilePath.orEmpty(), errors)
    }
    errors.add(error)
  }
}
