package org.knime.knip.mm;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A set of helpers to work with classes via reflection.
 * 
 * @author Johannes Schindelin
 */
class Reflection {

	/**
	 * Instantiates a class loaded in the given class loader.
	 * 
	 * @param loader the class loader with which to load the class
	 * @param className the name of the class to be instantiated
	 * @param parameters the parameters to pass to the constructor
	 * @return the new instance
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T construct(final ClassLoader loader,
		final String className, final Object... parameters)
	{
		try {
			final Class<?> clazz = loader.loadClass(className);
			for (final Constructor<?> constructor : clazz.getConstructors()) {
				if (doParametersMatch(constructor.getParameterTypes(), parameters)) {
					return (T) constructor.newInstance(parameters);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException(new NoSuchMethodException("No matching method found"));
	}

	/**
	 * Invokes a static method of a given class.
	 * <p>
	 * This method tries to find a static method matching the given name and the
	 * parameter list. Just like {@link #newInstance(String, Object...)}, this
	 * works via reflection to avoid a compile-time dependency on ImageJ2.
	 * </p>
	 * 
	 * @param loader the class loader with which to load the class
	 * @param className the name of the class whose static method is to be called
	 * @param methodName the name of the static method to be called
	 * @param parameters the parameters to pass to the static method
	 * @return the return value of the static method, if any
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T
		invokeStatic(final ClassLoader loader, final String className,
			final String methodName, final Object... parameters)
	{
		try {
			final Class<?> clazz = loader.loadClass(className);
			for (final Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName) &&
					doParametersMatch(method.getParameterTypes(), parameters))
				{
					return (T) method.invoke(null, parameters);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException(new NoSuchMethodException("No matching method found"));
	}

	/**
	 * Invokes a method of a given object.
	 * <p>
	 * This method tries to find a method matching the given name and the
	 * parameter list. Just like {@link #newInstance(String, Object...)}, this
	 * works via reflection to avoid a compile-time dependency on ImageJ2.
	 * </p>
	 * 
	 * @param loader the class loader with which to load the class
	 * @param object the object whose method is to be called
	 * @param methodName the name of the static method to be called
	 * @param parameters the parameters to pass to the static method
	 * @return the return value of the method, if any
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T
		invoke(final Object object,
			final String methodName, final Object... parameters)
	{
		final Class<?> clazz = object.getClass();
		try {
			for (final Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName) &&
					doParametersMatch(method.getParameterTypes(), parameters))
				{
					return (T) method.invoke(object, parameters);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException(new NoSuchMethodException(
				"No matching method found (looked for '" + methodName + "' in "
						+ clazz.getName()));
	}

	/**
	 * Check whether a list of parameters matches a list of parameter types. This
	 * is used to find matching constructors and (possibly static) methods.
	 * 
	 * @param types the parameter types
	 * @param parameters the parameters
	 * @return whether the parameters match the types
	 */
	private static boolean
		doParametersMatch(Class<?>[] types, Object[] parameters)
	{
		if (types.length != parameters.length) return false;
		for (int i = 0; i < types.length; i++)
			if (parameters[i] != null) {
				Class<?> clazz = parameters[i].getClass();
				if (types[i].isPrimitive()) {
					if (types[i] != Long.TYPE && types[i] != Integer.TYPE &&
						types[i] != Boolean.TYPE) throw new RuntimeException(
						"unsupported primitive type " + clazz);
					if (types[i] == Long.TYPE && clazz != Long.class) return false;
					else if (types[i] == Integer.TYPE && clazz != Integer.class) return false;
					else if (types[i] == Boolean.TYPE && clazz != Boolean.class) return false;
				}
				else if (!types[i].isAssignableFrom(clazz)) return false;
			}
		return true;
	}

}
