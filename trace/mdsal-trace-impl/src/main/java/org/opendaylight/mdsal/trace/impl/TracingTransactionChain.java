/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

class TracingTransactionChain extends AbstractCloseTracked<TracingTransactionChain> implements DOMTransactionChain {

    private final DOMTransactionChain delegate;
    private final TracingBroker tracingBroker;
    private final CloseTrackedRegistry<TracingReadOnlyTransaction> readOnlyTransactionsRegistry;
    private final CloseTrackedRegistry<TracingWriteTransaction> writeTransactionsRegistry;
    private final CloseTrackedRegistry<TracingReadWriteTransaction> readWriteTransactionsRegistry;

    TracingTransactionChain(DOMTransactionChain delegate, TracingBroker tracingBroker,
            CloseTrackedRegistry<TracingTransactionChain> transactionChainsRegistry) {
        super(transactionChainsRegistry);
        this.delegate = requireNonNull(delegate);
        this.tracingBroker = requireNonNull(tracingBroker);

        final boolean isDebug = transactionChainsRegistry.isDebugContextEnabled();
        String anchor = "TransactionChain@" + Integer.toHexString(hashCode());
        this.readOnlyTransactionsRegistry  = new CloseTrackedRegistry<>(anchor, "newReadOnlyTransaction()", isDebug);
        this.writeTransactionsRegistry     = new CloseTrackedRegistry<>(anchor, "newWriteOnlyTransaction()", isDebug);
        this.readWriteTransactionsRegistry = new CloseTrackedRegistry<>(anchor, "newReadWriteTransaction()", isDebug);
    }

    @Override
    @SuppressWarnings("resource")
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        final DOMDataTreeReadTransaction tx = delegate.newReadOnlyTransaction();
        return new TracingReadOnlyTransaction(tx, readOnlyTransactionsRegistry);
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        return new TracingReadWriteTransaction(delegate.newReadWriteTransaction(), tracingBroker,
                readWriteTransactionsRegistry);
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        final DOMDataTreeWriteTransaction tx = delegate.newWriteOnlyTransaction();
        return new TracingWriteTransaction(tx, tracingBroker, writeTransactionsRegistry);
    }

    @Override
    public void close() {
        delegate.close();
        super.removeFromTrackedRegistry();
    }

    public CloseTrackedRegistry<TracingReadOnlyTransaction> getReadOnlyTransactionsRegistry() {
        return readOnlyTransactionsRegistry;
    }

    public CloseTrackedRegistry<TracingReadWriteTransaction> getReadWriteTransactionsRegistry() {
        return readWriteTransactionsRegistry;
    }

    public CloseTrackedRegistry<TracingWriteTransaction> getWriteTransactionsRegistry() {
        return writeTransactionsRegistry;
    }

    // https://jira.opendaylight.org/browse/CONTROLLER-1792

    @Override
    public final boolean equals(Object object) {
        return object == this || delegate.equals(object);
    }

    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getName() + "; delegate=" + delegate;
    }
}
