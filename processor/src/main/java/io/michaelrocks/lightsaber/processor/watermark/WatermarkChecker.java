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

package io.michaelrocks.lightsaber.processor.watermark;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WatermarkChecker extends ClassVisitor {
    private boolean isLightsaberClass;

    public WatermarkChecker() {
        super(Opcodes.ASM5);
    }

    public static boolean isLightsaberClass(final Path file) throws IOException {
        final ClassReader classReader = new ClassReader(Files.readAllBytes(file));
        final WatermarkChecker checker = new WatermarkChecker();
        classReader.accept(checker, new Attribute[] { new LightsaberAttribute() },
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return checker.isLightsaberClass();
    }

    public boolean isLightsaberClass() {
        return isLightsaberClass;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        isLightsaberClass = false;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        super.visitAttribute(attr);
        if (attr instanceof LightsaberAttribute) {
            isLightsaberClass = true;
        }
    }
}