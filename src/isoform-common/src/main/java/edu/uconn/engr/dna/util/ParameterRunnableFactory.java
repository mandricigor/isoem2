package edu.uconn.engr.dna.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class ParameterRunnableFactory<T, R> {
    public abstract ParameterRunnable<T, R> createParameterRunnable();

    public static <T, R> ParameterRunnableFactory<T, R> instance(Class<? extends ParameterRunnable<T, R>> runnableClass,
                                                                 final Object... parameters) {
        final Constructor<ParameterRunnableFactory<T, R>> c = findConstructor(runnableClass, parameters);
        if (c == null)
            throw new IllegalArgumentException("Unknown constructor for class " + runnableClass);
        return new ParameterRunnableFactory<T, R>() {
            @Override
            public ParameterRunnable<T, R> createParameterRunnable() {
                try {
                    return (ParameterRunnable<T, R>) c.newInstance(parameters);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private static Constructor findConstructor(Class<?> objClass, Object[] parameters) {
        Constructor[] constructors = objClass.getConstructors();
        outer:
        for (int i = 0; i < constructors.length; ++i) {
            Constructor c = constructors[i];
            Class<?>[] cparameters = c.getParameterTypes();
            if (cparameters.length == parameters.length) {
                for (int j = 0; j < cparameters.length; ++j)
                    if (!cparameters[j].isAssignableFrom(parameters[j].getClass()))
                        continue outer;
                return c;
            }
        }
        return null;
    }
}
