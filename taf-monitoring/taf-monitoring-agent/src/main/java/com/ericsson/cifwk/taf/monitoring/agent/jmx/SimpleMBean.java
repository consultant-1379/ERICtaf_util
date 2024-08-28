package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleMBean {
    private final MBeanServerConnection server;
    private final ObjectName objectName;
    private MBeanInfo beanInfo;
    private final boolean ignoreErrors;
    private final Map<String, String[]> operations = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SimpleMBean.class);

    public SimpleMBean(MBeanServerConnection server, String objectName) throws JMException, IOException {
        this(server, objectName, false);
    }

    public SimpleMBean(MBeanServerConnection server, String objectName, boolean ignoreErrors) throws JMException, IOException {
        this(server, new ObjectName(objectName), ignoreErrors);
    }

    public SimpleMBean(MBeanServerConnection server, ObjectName name) throws JMException, IOException {
        this(server, name, false);
    }


    public SimpleMBean(MBeanServerConnection server, ObjectName objectName, boolean ignoreErrors) throws JMException, IOException {
        this.server = server;
        this.objectName = objectName;
        this.ignoreErrors = ignoreErrors;
        this.beanInfo = server.getMBeanInfo(objectName);

        MBeanOperationInfo[] operationInfos = beanInfo.getOperations();
        for (MBeanOperationInfo info : operationInfos) {
            String[] signature = createSignature(info);
            // Construct a simplistic key to support overloaded operations on the MBean.
            String operationKey = createOperationKey(info.getName(), signature.length);
            operations.put(operationKey, signature);
        }
    }

    public MBeanServerConnection server() {
        return server;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public MBeanInfo info() {
        return beanInfo;
    }

    public Object getProperty(String property) {
        try {
            return server.getAttribute(objectName, property);
        } catch (MBeanException e) {
            throwExceptionWithTarget("Could not access property: " + property + ". Reason: ", e);
        } catch (Exception e) {
            throwException("Could not access property: " + property + ". Reason: ", e);
        }
        return null;
    }

    public void setProperty(String property, Object value) {
        try {
            server.setAttribute(objectName, new Attribute(property, value));
        } catch (MBeanException e) {
            throwExceptionWithTarget("Could not set property: " + property + ". Reason: ", e);
        } catch (Exception e) {
            throwException("Could not set property: " + property + ". Reason: ", e);
        }
    }

    public Map toMap() {
        Map attributes = new HashMap();
        for (MBeanAttributeInfo attribute : beanInfo.getAttributes()) {
            attributes.putAll(getAttributeValues(attribute.getName(), getProperty(attribute.getName())));
        }
        return attributes;
    }

    private Map<String, Number> getDataFromList(String attribute, Object property) {
        Map<String, Number> monitor = new HashMap<>();
        if (property instanceof List) {
            monitor.put(attribute, ((List) property).size());
        } else if (property.getClass().getName().startsWith("[")) {
            monitor.put(attribute, ((Object[]) property).length);
        } else {
            log.info("Attribute {} with value {} can not be returned", attribute, property);
        }
        return monitor;
    }

    private Map<String, Number> getAttributeValues(String attribute, Object property) {
        Map<String, Number> monitor = new HashMap<>();
        if (property instanceof Number) {
            monitor.put(attribute, (Number) getProperty(attribute));
        } else if (property instanceof CompositeDataSupport) {
            monitor.putAll(getDataFromCompositeData(attribute));
        } else {
            monitor.putAll(getDataFromList(attribute, property));
        }
        return monitor;
    }

    private Map<String, Number> getDataFromCompositeData(String attribute) {
        Map<String, Number> monitor = new HashMap<>();
        CompositeDataSupport compositeDataProperty = (CompositeDataSupport) getProperty(attribute);
        for (String key : compositeDataProperty.getCompositeType().keySet()) {
            Object attributeValue = compositeDataProperty.get(key);
            if (attributeValue instanceof Number) {
                monitor.put(attribute + "." + key, (Number) attributeValue);
            } else if (attributeValue instanceof CompositeDataSupport) {
                monitor.putAll(getDataFromCompositeData(attribute + "." + key));
            } else {
                monitor.putAll(getDataFromList(attribute + "." + key, attributeValue));
            }
        }
        return monitor;
    }

    public void invokeMethod(String method, Object... arguments) {
        // Locate the specific method based on the name and number of parameters
        String operationKey = createOperationKey(method, arguments.length);
        String[] signature = operations.get(operationKey);

        if (signature != null) {
            try {
                server.invoke(objectName, method, arguments, signature);
            } catch (MBeanException e) {
                throwExceptionWithTarget("Could not invoke method: " + method + ". Reason: ", e);
            } catch (Exception e) {
                throwException("Could not invoke method: " + method + ". Reason: ", e);
            }
        } else {
            log.info("Cannot find signature for method {}. Check mBean info {}", method, info());
        }
    }

    protected String[] createSignature(MBeanOperationInfo info) {
        MBeanParameterInfo[] params = info.getSignature();
        String[] answer = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            answer[i] = params[i].getType();
        }
        return answer;
    }

    /**
     * Construct a simple key based on the method name and the number of parameters
     *
     * @param operation - the mbean operation name
     * @param params    - the number of parameters the operation supports
     * @return simple unique identifier for a method
     */
    protected String createOperationKey(String operation, int params) {
        return operation + "_" + params;
    }

    /**
     * List of the names of each of the attributes on the MBean
     *
     * @return list of attribute names
     */
    public Collection<String> listAttributeNames() {
        List<String> list = new ArrayList<>();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();

            for (MBeanAttributeInfo attr : attrs) {
                list.add(attr.getName());
            }
        } catch (Exception e) {
            throwException("Could not list attribute names. Reason: ", e);
        }
        return list;
    }

    /**
     * The values of each of the attributes on the MBean
     *
     * @return list of values of each attribute
     */
    public List<String> listAttributeValues() {
        List<String> list = new ArrayList<>();
        Collection<String> names = listAttributeNames();
        for (String name : names) {
            try {
                Object val = this.getProperty(name);
                if (val != null) {
                    list.add(name + " : " + val.toString());
                }
            } catch (Exception e) {
                throwException("Could not list attribute values. Reason: ", e);
            }
        }
        return list;
    }

    /**
     * List of string representations of all of the attributes on the MBean.
     *
     * @return list of descriptions of each attribute on the mbean
     */
    public Collection<String> listAttributeDescriptions() {
        List<String> list = new ArrayList<>();
        try {
            MBeanAttributeInfo[] attrs = beanInfo.getAttributes();
            for (MBeanAttributeInfo attr : attrs) {
                list.add(describeAttribute(attr));
            }
        } catch (Exception e) {
            throwException("Could not list attribute descriptions. Reason: ", e);
        }
        return list;
    }

    /**
     * Description of the specified attribute name.
     *
     * @param attr - the attribute
     * @return String the description
     */
    protected String describeAttribute(MBeanAttributeInfo attr) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (attr.isReadable()) {
            buf.append("r");
        }
        if (attr.isWritable()) {
            buf.append("w");
        }
        buf.append(") ")
                .append(attr.getType())
                .append(" ")
                .append(attr.getName());
        return buf.toString();
    }

    /**
     * Description of the specified attribute name.
     *
     * @param attributeName - stringified name of the attribute
     * @return the description
     */
    public String describeAttribute(String attributeName) {
        String ret = "Attribute not found";
        try {
            MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
            for (MBeanAttributeInfo attribute : attributes) {
                if (attribute.getName().equals(attributeName)) {
                    return describeAttribute(attribute);
                }
            }
        } catch (Exception e) {
            throwException("Could not describe attribute '" + attributeName + "'. Reason: ", e);
        }
        return ret;
    }

    /**
     * Names of all the operations available on the MBean.
     *
     * @return all the operations on the MBean
     */
    public Collection<String> listOperationNames() {
        List<String> list = new ArrayList<>();
        try {
            MBeanOperationInfo[] beanInfoOperations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : beanInfoOperations) {
                list.add(operation.getName());
            }
        } catch (Exception e) {
            throwException("Could not list operation names. Reason: ", e);
        }
        return list;
    }

    /**
     * Description of all of the operations available on the MBean.
     *
     * @return full description of each operation on the MBean
     */
    public Collection<String> listOperationDescriptions() {
        List<String> list = new ArrayList<>();
        try {
            MBeanOperationInfo[] beanInfoOperations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : beanInfoOperations) {
                list.add(describeOperation(operation));
            }
        } catch (Exception e) {
            throwException("Could not list operation descriptions. Reason: ", e);
        }
        return list;
    }

    /**
     * Get the description of the specified operation.  This returns a Collection since
     * operations can be overloaded and one operationName can have multiple forms.
     *
     * @param operationName the name of the operation to describe
     * @return Collection of operation description
     */
    public List<String> describeOperation(String operationName) {
        List<String> list = new ArrayList<>();
        try {
            MBeanOperationInfo[] beanInfoOperations = beanInfo.getOperations();
            for (MBeanOperationInfo operation : beanInfoOperations) {
                if (operation.getName().equals(operationName)) {
                    list.add(describeOperation(operation));
                }
            }
        } catch (Exception e) {
            throwException("Could not describe operations matching name '" + operationName + "'. Reason: ", e);
        }
        return list;
    }

    /**
     * Description of the operation.
     *
     * @param operation the operation to describe
     * @return pretty-printed description
     */
    protected String describeOperation(MBeanOperationInfo operation) {
        StringBuilder buf = new StringBuilder();
        buf.append(operation.getReturnType())
                .append(" ")
                .append(operation.getName())
                .append("(");

        MBeanParameterInfo[] params = operation.getSignature();
        for (int j = 0; j < params.length; j++) {
            MBeanParameterInfo param = params[j];
            if (j != 0) {
                buf.append(", ");
            }
            buf.append(param.getType())
                    .append(" ")
                    .append(param.getName());
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * Return an end user readable representation of the underlying MBean
     *
     * @return the user readable description
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("MBean Name:")
                .append("\n  ")
                .append(objectName.getCanonicalName())
                .append("\n  ");
        if (!listAttributeDescriptions().isEmpty()) {
            buf.append("\nAttributes:");
            for (String attrDesc : listAttributeDescriptions()) {
                buf.append("\n  ").append(attrDesc);
            }
        }
        if (!listOperationDescriptions().isEmpty()) {
            buf.append("\nOperations:");
            for (String attrDesc : listOperationDescriptions()) {
                buf.append("\n  ").append(attrDesc);
            }
        }
        return buf.toString();
    }

    private void throwException(String m, Exception e) {
        if (!ignoreErrors) {
            throw new RuntimeException(m + e, e);
        }
    }

    private void throwExceptionWithTarget(String m, MBeanException e) {
        if (!ignoreErrors) {
            throw new RuntimeException(m + e, e.getTargetException());
        }
    }
}
