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

package io.michaelrocks.lightsaber.processor.analysis;

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class InjectionTargetAnalyzer extends ProcessorClassVisitor {
    private InjectionTargetDescriptor.Builder injectionTargetBuilder;

    public InjectionTargetAnalyzer(final ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        injectionTargetBuilder = new InjectionTargetDescriptor.Builder(Type.getObjectType(name));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final ScopeDescriptor scope = getProcessorContext().findScopeByAnnotationType(Type.getType(desc));
        if (scope != null) {
            injectionTargetBuilder.setScope(scope);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        if (MethodDescriptor.isDefaultConstructor(name, desc)) {
            injectionTargetBuilder.setHasDefaultConstructor(true);
        }

        return new InjectionMethodAnalyzer(
                getProcessorContext(), injectionTargetBuilder, access, name, desc, signature);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        return new InjectionFieldAnalyzer(
                getProcessorContext(), injectionTargetBuilder, access, name, desc, signature);
    }

    @Override
    public void visitEnd() {
        final InjectionTargetDescriptor injectionTarget = injectionTargetBuilder.build();
        if (!injectionTarget.getInjectableFields().isEmpty() || !injectionTarget.getInjectableMethods().isEmpty()) {
            getProcessorContext().addInjectableTarget(injectionTarget);
        }
        if (injectionTarget.getInjectableConstructors().size() > 1) {
            final String separator = System.lineSeparator() + "  ";
            final String constructors =
                    StringUtils.join(injectionTarget.getInjectableConstructors(), separator);
            reportError("Class has multiple injectable constructors:" + separator + constructors);
        } else if (!injectionTarget.getInjectableConstructors().isEmpty()) {
            getProcessorContext().addProvidableTarget(injectionTarget);
        }
    }
}
