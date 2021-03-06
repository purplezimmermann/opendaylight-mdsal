/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.cont.cont.choice.ContAug;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.root.RootAug;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Root;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.ContChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice.ContBase;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp.GrpCont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.root.RootBase;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class InstanceIdentifierSerializeDeserializeTest extends AbstractBindingCodecTest {
    public static final String TOP_LEVEL_LIST_KEY_VALUE = "foo";

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES =
            BA_TOP_LEVEL_LIST.augmentation(TreeComplexUsesAugment.class);

    public static final QName TOP_QNAME = Top.QNAME;
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY = QName.create(TOP_QNAME, "name");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(TOP_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TOP_LEVEL_LIST_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_1_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY,
                TOP_LEVEL_LIST_KEY_VALUE));

    @Test
    public void testYangIIToBindingAwareII() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(BI_TOP_PATH);
        assertEquals(Top.class, instanceIdentifier.getTargetType());
    }

    @Test
    public void testYangIIToBindingAwareIIListWildcarded() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(
            BI_TOP_LEVEL_LIST_PATH);
        assertNull(instanceIdentifier);
    }

    @Test
    public void testYangIIToBindingAwareIIListWithKey() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(
            BI_TOP_LEVEL_LIST_1_PATH);
        final InstanceIdentifier.PathArgument last = Iterables.getLast(instanceIdentifier.getPathArguments());
        assertEquals(TopLevelList.class, instanceIdentifier.getTargetType());
        assertFalse(instanceIdentifier.isWildcarded());
        assertTrue(last instanceof InstanceIdentifier.IdentifiableItem);
        final Identifier<?> key = ((InstanceIdentifier.IdentifiableItem<?, ?>) last).getKey();
        assertEquals(TopLevelListKey.class, key.getClass());
        assertEquals(TOP_LEVEL_LIST_KEY_VALUE, ((TopLevelListKey)key).getName());
    }

    @Test
    public void testBindingAwareIIToYangIContainer() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIWildcard() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIListWithKey() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class, TOP_FOO_KEY));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifierWithPredicates);
        assertTrue(((NodeIdentifierWithPredicates) lastPathArgument).values().contains(TOP_LEVEL_LIST_KEY_VALUE));
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIAugmentation() {
        final PathArgument lastArg = codecContext.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES).getLastPathArgument();
        assertTrue(lastArg instanceof AugmentationIdentifier);
    }

    @Test
    public void testBindingAwareIIToYangIILeafOnlyAugmentation() {
        final PathArgument leafOnlyLastArg = codecContext.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY)
                .getLastPathArgument();
        assertTrue(leafOnlyLastArg instanceof AugmentationIdentifier);
        assertTrue(((AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
    }

    @Test
    public void testChoiceCaseGroupingFromBinding() {
        final YangInstanceIdentifier contBase = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)), contBase);

        final YangInstanceIdentifier contAug = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(ContAug.QNAME.getModule()))), contAug);

        // Legacy: downcast the child to Class, losing type safety but still working. Faced with ambiguity, it will
        //         select the lexically-lower class
        assertEquals(1, ContBase.class.getCanonicalName().compareTo(ContAug.class.getCanonicalName()));
        final YangInstanceIdentifier contAugLegacy = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child((Class) GrpCont.class).build());
        assertEquals(contAug, contAugLegacy);

        final YangInstanceIdentifier rootBase = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(RootBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME)), rootBase);

        final YangInstanceIdentifier rootAug = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(RootAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(RootAug.QNAME.getModule()))), rootAug);
    }

    @Test
    public void testChoiceCaseGroupingToBinding() {
        final InstanceIdentifier<?> contBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)));
        assertEquals(InstanceIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build(), contBase);

        final InstanceIdentifier<?> contAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME), NodeIdentifier.create(ContChoice.QNAME),
                NodeIdentifier.create(GrpCont.QNAME.bindTo(ContAug.QNAME.getModule()))));
        assertEquals(InstanceIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build(), contAug);

        final InstanceIdentifier<?> rootBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME), NodeIdentifier.create(GrpCont.QNAME)));
        assertEquals(InstanceIdentifier.builder(RootBase.class, GrpCont.class).build(), rootBase);

        final InstanceIdentifier<?> rootAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
                NodeIdentifier.create(GrpCont.QNAME.bindTo(RootAug.QNAME.getModule()))));
        assertEquals(InstanceIdentifier.builder(RootAug.class, GrpCont.class).build(), rootAug);
    }
}
