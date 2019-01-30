package nl._42.beanie;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

final class Methods {

  static MethodHandle getMethodHandle(final Method method) throws Throwable {
    final Class<?> declaringClass = method.getDeclaringClass();

    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
    return lookup.unreflectSpecial(method, declaringClass);
  }

}
