/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

/**
 * {@link AbstractTypeProvider} which generates enough type information for runtime support. For a codegen-compatible
 * provider use {@link CodegenTypeProvider}.
 */
@Beta
public final class RuntimeTypeProvider extends AbstractTypeProvider {
    public RuntimeTypeProvider(final SchemaContext schemaContext, final Map<SchemaNode, JavaTypeName> renames) {
        super(schemaContext, renames);
    }

    @VisibleForTesting
    RuntimeTypeProvider(final SchemaContext schemaContext) {
        this(schemaContext, ImmutableMap.of());
    }

    @Override
    public void addEnumDescription(final EnumBuilder enumBuilder, final EnumTypeDefinition enumTypeDef) {
        // No-op
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genTOBuilder, final TypeDefinition<?> typeDef) {
        // No-op
    }

    @Override
    public Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
        return ImmutableMap.of();
    }

    @Override
    public GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
        return new RuntimeGeneratedTOBuilder(identifier);
    }

    @Override
    public GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
        return new RuntimeGeneratedTypeBuilder(identifier);
    }

    @Override
    public AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new RuntimeEnumerationBuilder(identifier);
    }
}
