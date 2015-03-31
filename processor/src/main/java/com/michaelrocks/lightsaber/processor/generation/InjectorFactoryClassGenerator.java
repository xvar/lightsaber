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

package com.michaelrocks.lightsaber.processor.generation;

import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.patterns.Lightsaber$$GlobalModule;
import com.michaelrocks.lightsaber.processor.patterns.Lightsaber$$InjectorFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;

import static org.objectweb.asm.Opcodes.ASM5;

public class InjectorFactoryClassGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public InjectorFactoryClassGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateInjectorFactory() {
        final String path = Lightsaber$$InjectorFactory.class.getSimpleName() + ".class";
        try (final InputStream stream = Lightsaber$$InjectorFactory.class.getResourceAsStream(path)) {
            final ClassReader classReader = new ClassReader(stream);
            final ClassWriter classWriter =
                    new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classReader.accept(
                    new InjectorFactoryClassVisitor(classWriter), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            final byte[] classData = classWriter.toByteArray();
            classProducer.produceClass(processorContext.getInjectorFactoryType().getInternalName(), classData);
        } catch (final IOException exception) {
            processorContext.reportError(exception);
        }
    }

    private class InjectorFactoryClassVisitor extends ClassVisitor {
        public InjectorFactoryClassVisitor(final ClassVisitor classVisitor) {
            super(ASM5, classVisitor);
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature,
                final String superName, final String[] interfaces) {
            final String newName = processorContext.getInjectorFactoryType().getInternalName();
            super.visit(version, access, newName, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                final String[] exceptions) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitor(ASM5, methodVisitor) {
                private final String patternType = Type.getType(Lightsaber$$GlobalModule.class).getInternalName();
                private final String replacementType =
                        processorContext.getGlobalModule().getModuleType().getInternalName();

                @Override
                public void visitTypeInsn(final int opcode, final String type) {
                    if (patternType.equals(type)) {
                        System.out.println("New " + replacementType);
                        super.visitTypeInsn(opcode, replacementType);
                    } else {
                        super.visitTypeInsn(opcode, type);
                    }
                }

                @Override
                public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
                        final boolean itf) {
                    if (patternType.equals(owner)) {
                        System.out.println("Call " + replacementType);
                        super.visitMethodInsn(opcode, replacementType, name, desc, itf);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }
                }
            };
        }
    }
}
