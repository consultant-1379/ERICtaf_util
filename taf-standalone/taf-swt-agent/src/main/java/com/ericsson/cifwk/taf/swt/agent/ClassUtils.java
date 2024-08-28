package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ClassUtils {

    /** Suffix for array class names: "[]" */
    public static final String ARRAY_SUFFIX = "[]";

    /** Prefix for internal array class names: "[L" */
    private static final String INTERNAL_ARRAY_PREFIX = "[L";

    /** The package separator character '.' */
    private static final char PACKAGE_SEPARATOR = '.';

    /** The inner class separator character '$' */
    private static final char INNER_CLASS_SEPARATOR = '$';

    /** The CGLIB class separator character "$$" */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(16);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        Set<Object> primitiveTypeNames = new HashSet<Object>(16);
        primitiveTypeNames.addAll(primitiveWrapperTypeMap.values());
        primitiveTypeNames.addAll(Arrays.asList(new Class[] {
                boolean[].class, byte[].class, char[].class, double[].class,
                float[].class, int[].class, long[].class, short[].class }));
        for (Object primitiveTypeName : primitiveTypeNames) {
            Class<?> primitiveClass = (Class<?>) primitiveTypeName;
            primitiveTypeNameMap.put(primitiveClass.getName(), primitiveClass);
        }
    }

    public static Class forName(String name) throws ClassNotFoundException, LinkageError {
        return forName(name, getDefaultClassLoader());
    }

    public static Class forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
        // Assert.notNull(name, "Name must not be null");

        Class clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        int internalArrayMarker = name.indexOf(INTERNAL_ARRAY_PREFIX);
        if (internalArrayMarker != -1 && name.endsWith(";")) {
            String elementClassName = null;
            if (internalArrayMarker == 0) {
                elementClassName = name.substring(INTERNAL_ARRAY_PREFIX.length(), name.length() - 1);
            }
            else if (name.startsWith("[")) {
                elementClassName = name.substring(1);
            }
            Class elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getDefaultClassLoader();
        }
        return classLoaderToUse.loadClass(name);
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Exception ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

}
