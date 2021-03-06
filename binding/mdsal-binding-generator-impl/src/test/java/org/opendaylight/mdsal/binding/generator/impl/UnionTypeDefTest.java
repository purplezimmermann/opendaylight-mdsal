/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class UnionTypeDefTest {
    @Test
    public void unionTypeResolvingTest() {
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            UnionTypeDefTest.class, "/union-test-models/abstract-topology.yang", "/ietf/ietf-inet-types.yang"));

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        // TODO: implement test
    }

    @Test
    public void unionTypedefLeafrefTest() {
        final List<Type> generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/leafref_typedef_union/bug8449.yang"));
        assertNotNull(generateTypes);
        for (final Type type : generateTypes) {
            if (type.getName().equals("Cont")) {
                final GeneratedType gt = (GeneratedType) type;
                assertNotNull(gt);
                final GeneratedType refType = gt.getEnclosedTypes().iterator().next();
                for (final GeneratedProperty generatedProperty : refType.getProperties()) {
                    switch (generatedProperty.getName()) {
                        case "stringRefValue":
                            assertEquals(Types.STRING, generatedProperty.getReturnType());
                            break;
                        default:
                            // ignore
                    }
                }
            }
        }
    }
}
