/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.binding.runtime.spi.ModuleInfoBackedContext;
import org.opendaylight.binding.runtime.spi.ModuleInfoRegistry;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class OsgiModuleInfoRegistry implements ModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiModuleInfoRegistry.class);

    private final SchemaContextProvider schemaContextProvider;
    private final ModuleInfoBackedContext moduleInfoRegistry;

    @GuardedBy("this")
    private ServiceRegistration<?> registration;
    @GuardedBy("this")
    private long generation;

    OsgiModuleInfoRegistry(final ModuleInfoBackedContext moduleInfoRegistry,
        final SchemaContextProvider schemaContextProvider) {

        this.moduleInfoRegistry = requireNonNull(moduleInfoRegistry);
        this.schemaContextProvider = requireNonNull(schemaContextProvider);
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        return new ObjectRegistrationWrapper(registerInfo(yangModuleInfo));
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    synchronized void updateService() {
        final SchemaContext context;
        try {
            context = schemaContextProvider.getSchemaContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.error("Error updating the schema context", e);
            return;
        }
        LOG.trace("Assembled context {}", context);

        //        // FIXME: MDSAL-392: UGH, this should be a snapshot
        //        final BindingRuntimeContext next = DefaultBindingRuntimeContext.create(
        //            new DefaultBindingRuntimeGenerator().generateTypeMapping(context), moduleInfoRegistry);

        // FIXME: publish new the new context, remove the old one
    }

    ObjectRegistration<YangModuleInfo> registerInfo(final YangModuleInfo yangModuleInfo) {
        return moduleInfoRegistry.registerModuleInfo(yangModuleInfo);
    }

    private class ObjectRegistrationWrapper implements ObjectRegistration<YangModuleInfo> {
        private final ObjectRegistration<YangModuleInfo> inner;

        ObjectRegistrationWrapper(final ObjectRegistration<YangModuleInfo> inner) {
            this.inner = requireNonNull(inner);
        }

        @Override
        public YangModuleInfo getInstance() {
            return inner.getInstance();
        }

        @Override
        @SuppressWarnings("checkstyle:illegalCatch")
        public void close() {
            try {
                inner.close();
            } finally {
                // send modify event when a bundle disappears
                updateService();
            }
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }
}
