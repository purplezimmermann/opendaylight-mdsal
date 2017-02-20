/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

/**
 * This class represents ancestor for other <code>Type</code>s
 */
@Beta
public abstract class AbstractBaseType implements Type {

    /**
     * Name of the package to which this <code>Type</code> belongs.
     */
    protected final String packageName;

    /**
     * Name of this <code>Type</code>.
     */
    protected final String name;

    /**
     * Constructs the instance of this class with the concrete package name type
     * name.
     *
     * @param pkName
     *            string with the package name to which this <code>Type</code>
     *            belongs
     * @param name
     *            string with the name for this <code>Type</code>
     */
    protected AbstractBaseType(final String pkName, final String name) {
        this.packageName = Preconditions.checkNotNull(pkName, "Package Name for Generated Type cannot be null!");
        this.name = Preconditions.checkNotNull(name, "Name of Generated Type cannot be null!");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.packageName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Type)) {
            return false;
        }
        final Type other = (Type) obj;
        return Objects.equals(this.name, other.getName()) && Objects.equals(this.packageName, other.getPackageName());
    }


    @Override
    public String toString() {
        if (this.packageName.isEmpty()) {
            return "Type (" + this.name + ")";
        }
        return "Type (" + this.packageName + "." + this.name + ")";
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getFullyQualifiedName() {
        if (this.packageName.isEmpty()) {
            return this.name;
        } else {
            return this.packageName + "." + this.name;
        }
    }
}