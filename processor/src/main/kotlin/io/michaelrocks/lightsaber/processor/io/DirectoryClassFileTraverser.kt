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

package io.michaelrocks.lightsaber.processor.io

import io.michaelrocks.lightsaber.processor.ProcessingException
import java.io.File
import java.io.IOException

class DirectoryClassFileTraverser @Throws(IOException::class) constructor(
    private val classesDirectory: File
) : ClassFileTraverser<File>() {

  @Throws(IOException::class)
  override fun iterateFiles(): Iterable<File> {
    if (!classesDirectory.exists() || !classesDirectory.isDirectory) {
      throw ProcessingException("Invalid classes directory", classesDirectory.absolutePath)
    }

    return classesDirectory.walkTopDown().asIterable()
  }

  override fun isDirectory(file: File): Boolean = file.isDirectory

  override fun getFilePath(file: File): String = file.relativeTo(classesDirectory)

  @Throws(IOException::class)
  override fun readAsByteArray(file: File): ByteArray = file.readBytes()

  override fun close() {
  }
}