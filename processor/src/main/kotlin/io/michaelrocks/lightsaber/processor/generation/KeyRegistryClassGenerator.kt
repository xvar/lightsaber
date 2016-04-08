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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.internal.GenericArrayTypeImpl
import io.michaelrocks.lightsaber.internal.ParameterizedTypeImpl
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

private val KEY_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.TYPE_TYPE, Types.ANNOTATION_TYPE)

private val PARAMETERIZED_TYPE_IMPL_TYPE = getType<ParameterizedTypeImpl>()
private val GENERIC_ARRAY_TYPE_IMPL_TYPE = getType<GenericArrayTypeImpl>()

private val PARAMETERIZED_TYPE_IMPL_CONSTRUCTOR =
    MethodDescriptor.forConstructor(Types.TYPE_TYPE, Types.TYPE_TYPE, Types.TYPE_TYPE.toArrayType())
private val GENERIC_ARRAY_TYPE_IMPL_CONSTRUCTOR =
    MethodDescriptor.forConstructor(Types.TYPE_TYPE)

class KeyRegistryClassGenerator(
    private val classProducer: ClassProducer,
    private val classRegistry: ClassRegistry,
    private val annotationCreator: AnnotationCreator,
    private val generationContext: GenerationContext
) {
  private val keyRegistry = generationContext.keyRegistry

  fun generate() {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        keyRegistry.type.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        null)

    generateFields(classVisitor)
    generateStaticInitializer(classVisitor)
    generateConstructor(classVisitor)

    classVisitor.visitEnd()
    val classBytes = classWriter.toByteArray()
    classProducer.produceClass(keyRegistry.type.internalName, classBytes)
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    for (field in keyRegistry.fields.values) {
      val fieldVisitor = classVisitor.visitField(
          ACC_PUBLIC or ACC_STATIC or ACC_FINAL,
          field.name,
          field.descriptor,
          null,
          null)
      fieldVisitor.visitEnd()
    }
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forDefaultConstructor())
    generator.visitCode()
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor())
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateStaticInitializer(classVisitor: ClassVisitor) {
    val staticInitializer = MethodDescriptor.forStaticInitializer()
    val generator = GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer)
    generator.visitCode()

    for ((dependency, field) in keyRegistry.fields.entries) {
      generator.newKey(dependency)
      generator.putStatic(keyRegistry.type, field)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun GeneratorAdapter.newKey(dependency: Dependency) {
    newInstance(Types.KEY_TYPE)
    dup()

    val type = dependency.type.rawType.box()
    val packageInvader = generationContext.findPackageInvaderByTargetType(type)
    val field = packageInvader?.fields?.get(type)

    if (field != null) {
      getStatic(packageInvader!!.type, field)
    } else {
      push(type)
    }

    if (dependency.qualifier == null) {
      pushNull()
    } else {
      annotationCreator.newAnnotation(this, dependency.qualifier)
    }

    invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
  }

  private fun GeneratorAdapter.push(type: GenericType) {
    when (type) {
      is GenericType.RawType -> push(type.type.box())
      is GenericType.ParameterizedType -> newParameterizedType(type)
      is GenericType.GenericArrayType -> newGenericArrayType(type)
      else -> error("Unsupported generic type $type")
    }
  }

  private fun GeneratorAdapter.newParameterizedType(type: GenericType.ParameterizedType) {
    newInstance(PARAMETERIZED_TYPE_IMPL_TYPE)
    dup()
    pushNull()
    push(type.type)
    newArray(Types.TYPE_TYPE, type.typeArguments.size)
    type.typeArguments.forEach { push(it) }
    invokeConstructor(PARAMETERIZED_TYPE_IMPL_TYPE, PARAMETERIZED_TYPE_IMPL_CONSTRUCTOR)
  }

  private fun GeneratorAdapter.newGenericArrayType(type: GenericType.GenericArrayType) {
    newInstance(GENERIC_ARRAY_TYPE_IMPL_TYPE)
    dup()
    push(type.elementType)
    invokeConstructor(GENERIC_ARRAY_TYPE_IMPL_TYPE, GENERIC_ARRAY_TYPE_IMPL_CONSTRUCTOR)
  }
}
