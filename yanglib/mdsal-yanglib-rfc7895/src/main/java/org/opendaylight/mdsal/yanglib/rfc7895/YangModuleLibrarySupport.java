/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@Beta
@NonNullByDefault
@Singleton
public final class YangModuleLibrarySupport implements YangLibSupport {
    private static final Revision REVISION = ModulesState.QNAME.getRevision().orElseThrow();

    private final BindingDataObjectCodecTreeNode<ModulesState> codec;
    private final EffectiveModelContext context;

    @Inject
    public YangModuleLibrarySupport(final YangParserFactory parserFactory, final BindingRuntimeGenerator generator,
            final BindingCodecTreeFactory codecFactory) throws YangParserException, IOException {
        final YangModuleInfo yangLibModule = $YangModuleInfoImpl.getInstance();

        context = parserFactory.createParser()
                .addLibSources(Collections2.transform(yangLibModule.getImportedModules(),
                    YangModuleLibrarySupport::createSource))
                .addSource(createSource(yangLibModule))
                .buildEffectiveModel();
        final BindingCodecTree codecTree = codecFactory.create(DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(context), SimpleStrategy.INSTANCE));

        this.codec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class)));
    }

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointIdentifier mountId,
            final SchemaContextResolver resolver) {
        return new MountPointContextFactoryImpl(mountId, resolver, context, codec);
    }

    @Override
    public Revision implementedRevision() {
        return REVISION;
    }

    private static YangTextSchemaSource createSource(final YangModuleInfo info) {
        final QName name = info.getName();
        return YangTextSchemaSource.delegateForByteSource(
            RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision()), info.getYangTextByteSource());
    }
}
