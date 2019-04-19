package nl._42.beanie.compatibility;

import nl._42.beanie.util.Classes;

import static java.lang.String.format;

/**
 * Responsible for dynamically resolving and instantiating
 * the methods implementation for the current Java.
 */
public final class MethodFactory {

  private static final String JAVA_VERSION_NAME = "java.version";
  private static final String JAVA_BASE_8 = "1.8.";

  private static Methods INSTANCE;

  private MethodFactory() {
  }

  /**
   * Retrieve the methods implementation. Once resolved the instance
   * will be cached and returned on each further invocation.
   * @return the methods
   */
  public static Methods get() {
    if (INSTANCE == null) {
      INSTANCE = instantiate();
    }
    return INSTANCE;
  }

  private static Methods instantiate() {
    String version = System.getProperty(JAVA_VERSION_NAME);
    String major = version.startsWith(JAVA_BASE_8) ? "8" : "9";
    String className = format("%s%s", Methods.class.getName(), major);
    return Classes.instantiate(className);
  }

}
