package nl._42.beanie.compatibility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Methods8 implements Methods {

    @Override
    public MethodHandle getMethodHandle(final Method method) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();

        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);

        MethodHandles.Lookup lookup = constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE);
        return lookup.unreflectSpecial(method, declaringClass);
    }

}
