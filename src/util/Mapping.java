package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping {
    String className;
    String methodName;
    HashMap<String, Class<?>> argument;

    /**
     * @param className
     * @param methodName
     * @param argument
     */
    public Mapping(String className, String methodName, HashMap<String, Class<?>> argument) {
        this.className = className;
        this.methodName = methodName;
        this.argument = argument;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the argument
     */
    public HashMap<String, Class<?>> getArgument() {
        return argument;
    }

    /**
     * @param argument the argument to set
     */
    public void setArgument(HashMap<String, Class<?>> argument) {
        this.argument = argument;
    }

    public Class<?>[] method_param() {
        Class<?>[] clazz = new Class[argument.size()];

        int i = 0;
        for (Map.Entry<String, Class<?>> entry : argument.entrySet()) {
            clazz[i] = entry.getValue();
            i++;
        }
        return clazz;

    }

    public List<String> liste_param() {
        List<String> retour = new ArrayList<>();
        for (Map.Entry<String, Class<?>> entry : argument.entrySet()) {
            retour.add(entry.getKey());
        }
        return retour;
    }

}
