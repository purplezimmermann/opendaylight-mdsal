/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

// FIXME: 5.0.0: this class seems to be unused, consider removing it
public final class YangSchemaUtils {
    public static final String AUGMENT_IDENTIFIER = "augment-identifier";

    private YangSchemaUtils() {
        throw new UnsupportedOperationException("Helper class. Instantiation is prohibited");
    }

    public static QName getAugmentationQName(final AugmentationSchemaNode augmentation) {
        requireNonNull(augmentation, "Augmentation must not be null.");
        final QName identifier = getAugmentationIdentifier(augmentation);
        if (identifier != null) {
            return identifier;
        }
        URI namespace = null;
        Optional<Revision> revision = null;
        if (augmentation instanceof NamespaceRevisionAware) {
            namespace = ((NamespaceRevisionAware) augmentation).getNamespace();
            revision = ((NamespaceRevisionAware) augmentation).getRevision();
        }
        if (namespace == null || revision == null) {
            for (DataSchemaNode child : augmentation.getChildNodes()) {
                // Derive QName from child nodes
                if (!child.isAugmenting()) {
                    namespace = child.getQName().getNamespace();
                    revision = child.getQName().getRevision();
                    break;
                }
            }
        }
        checkState(namespace != null, "Augmentation namespace must not be null");
        checkState(revision != null, "Augmentation revision must not be null");
        // FIXME: Always return a qname with module namespace.
        return QName.create(namespace, revision, "foo_augment");
    }

    public static QName getAugmentationIdentifier(final AugmentationSchemaNode augmentation) {
        for (final UnknownSchemaNode extension : augmentation.getUnknownSchemaNodes()) {
            if (AUGMENT_IDENTIFIER.equals(extension.getNodeType().getLocalName())) {
                return extension.getQName();
            }
        }
        return null;
    }

    public static @Nullable TypeDefinition<?> findTypeDefinition(final SchemaContext context, final SchemaPath path) {
        final Iterator<QName> arguments = path.getPathFromRoot().iterator();
        Preconditions.checkArgument(arguments.hasNext(), "Type Definition path must contain at least one element.");

        QName currentArg = arguments.next();
        DataNodeContainer currentNode = context.findModule(currentArg.getModule()).orElse(null);
        if (currentNode == null) {
            return null;
        }
        // Last argument is type definition, so we need to cycle until we hit last argument.
        while (arguments.hasNext()) {
            // Nested private type - we need to find container/grouping to which type belongs.
            final DataSchemaNode child = currentNode.getDataChildByName(currentArg);
            if (child instanceof DataNodeContainer) {
                currentNode = (DataNodeContainer) child;
            } else if (child instanceof ChoiceSchemaNode) {
                final QName caseQName = arguments.next();
                Preconditions.checkArgument(arguments.hasNext(), "Path must not refer case only.");
                currentNode = ((ChoiceSchemaNode) child).getCaseNodeByName(caseQName);
            } else {
                // Search in grouping
                for (GroupingDefinition grouping : currentNode.getGroupings()) {
                    if (currentArg.equals(grouping.getQName())) {
                        currentNode = grouping;
                        break;
                    }
                }
            }
            currentArg = arguments.next();
        }

        for (TypeDefinition<?> typedef : currentNode.getTypeDefinitions()) {
            if (typedef.getQName().equals(currentArg)) {
                return typedef;
            }
        }
        return null;
    }
}
