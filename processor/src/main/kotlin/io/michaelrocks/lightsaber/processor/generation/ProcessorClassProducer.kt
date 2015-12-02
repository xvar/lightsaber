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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.io.ClassFileVisitor
import io.michaelrocks.lightsaber.processor.logging.getLogger
import java.io.IOException

class ProcessorClassProducer(
    private val classFileVisitor: ClassFileVisitor,
    private val processorContext: ProcessorContext
) : ClassProducer {
  private val logger = getLogger()

  override fun produceClass(internalName: String, classData: ByteArray) {
    logger.debug("Producing class {}", internalName)
    val classFileName = internalName + ".class"
    try {
      classFileVisitor.visitClassFile(classFileName, classData)
    } catch (exception: IOException) {
      val message = "Failed to produce class with %d bytes".format(classData.size)
      processorContext.reportError(ProcessingException(message, exception, classFileName))
    }
  }
}