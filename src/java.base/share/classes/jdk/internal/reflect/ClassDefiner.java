/*
 * Copyright (c) 2001, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.reflect;

import java.security.AccessController;
import java.security.PrivilegedAction;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;

/** Utility class which assists in calling defineClass() by
 *  creating a new class loader which delegates to the one needed in
 *  order for proper resolution of the given bytecodes to occur.
 *
 *  This is only used to define SerializationConstructorAccessor.
 */

class ClassDefiner {
    static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

    /** <P> We define generated code into a new class loader which
      delegates to the defining loader of the target class. It is
      necessary for the VM to be able to resolve references to the
      target class from the generated bytecodes, which could not occur
      if the generated code was loaded into the bootstrap class
      loader. </P>

      <P> There are two primary reasons for creating a new loader
      instead of defining these bytecodes directly into the defining
      loader of the target class: first, it avoids any possible
      security risk of having these bytecodes in the same loader.
      Second, it allows the generated bytecodes to be unloaded earlier
      than would otherwise be possible, decreasing run-time
      footprint. </P>
    */
    static Class<?> defineClass(String name, byte[] bytes, int off, int len,
                                final ClassLoader parentClassLoader)
    {
        @SuppressWarnings("removal")
        ClassLoader newLoader = AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                        return new DelegatingClassLoader(parentClassLoader);
                    }
                });
        return JLA.defineClass(newLoader, name, bytes, null, "__ClassDefiner__");
    }
}


// NOTE: this class's name and presence are known to the virtual
// machine as of the fix for 4474172.
class DelegatingClassLoader extends ClassLoader {
    DelegatingClassLoader(ClassLoader parent) {
        super(parent);
    }
}
