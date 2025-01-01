package com.oj.security;

import java.util.Set;

public class RestrictedClassLoader extends ClassLoader {
    private final Set<String> whitelistedClasses;

    public RestrictedClassLoader(Set<String> whitelistedClasses, ClassLoader parent) {
        super(parent);
        this.whitelistedClasses = whitelistedClasses;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!whitelistedClasses.contains(name)) {
            throw new ClassNotFoundException("Class " + name + " is not whitelisted");
        }
        return super.loadClass(name, resolve);
    }
} 