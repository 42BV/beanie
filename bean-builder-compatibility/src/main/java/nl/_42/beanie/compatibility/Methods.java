package nl._42.beanie.compatibility;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public interface Methods {

    default MethodHandle lookup(Method method) {
        try {
            return getMethodHandle(method);
        } catch (Throwable cause) {
            throw new IllegalStateException("Could not retrieve method handle", cause);
        }
    }

    MethodHandle getMethodHandle(Method method) throws Throwable;

}
