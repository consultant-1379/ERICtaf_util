package com.ericsson.cifwk.taf.handlers.netsim;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Command combination builder
 */
final class NetSimCommandEmitter {

    private NetSimCommandEmitter() {
    }

    public static String buildCommandLine(NetSimCommand... commands) {
        return buildCommandLine(Lists.newArrayList(commands));
    }

    public static String buildCommandLine(List<NetSimCommand> commands) {
        CommandLineBuilder builder = new CommandLineBuilderImpl();
        builder.start();

        for (NetSimCommand command : commands) {
            String commandString = commandString(command);
            System.out.println("From NetSimCommandEmitter ....... " + command);
            builder.startCommand(commandString);
            addSingleCommand(builder, command);
            builder.endCommand();
        }
        builder.end();
        return builder.build();
    }

    private static void addSingleCommand(CommandLineBuilder builder, NetSimCommand bean) {
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(bean);

        Map<AttributeKey, Object> attributeMap = new TreeMap<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if ("class".equals(descriptor.getName())) {
                continue;
            }
            addParameter(bean, attributeMap, descriptor);
        }

        builder.beforeParameters(attributeMap);

        Set<Map.Entry<AttributeKey, Object>> entrySet = attributeMap.entrySet();

        for (Iterator<Map.Entry<AttributeKey, Object>> iterator = entrySet.iterator(); iterator.hasNext();) {
            Map.Entry<AttributeKey, Object> entry = iterator.next();
            builder.addParameter(entry.getKey(), entry.getValue(), iterator.hasNext());
        }
    }

    private static String commandString(NetSimCommand bean) {
        Class<? extends NetSimCommand> commandClass = bean.getClass();
        return commandClass.getAnnotation(Cmd.class).value();
    }

    private static void addParameter(NetSimCommand bean, Map<AttributeKey, Object> commandMap, PropertyDescriptor descriptor) {
        String name = descriptor.getName();
        Method readMethod = descriptor.getReadMethod();
        Cmd annotation = readMethod.getAnnotation(Cmd.class);
        AttributeKey key = new AttributeKey(annotation.value(), annotation.index(), descriptor.getPropertyType(), annotation.quoted(), annotation.requiresAssignment());

        Object value = getValue(bean, name);
        if (value == null) {
            return;
        }
        commandMap.put(key, value);
    }

    private static Object getValue(NetSimCommand bean, String propertyName) {
        try {
            return PropertyUtils.getProperty(bean, propertyName);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static class AttributeKey implements Comparable<AttributeKey> {

        private final int index;

        private Class<?> type;

        private final String name;

        private boolean quoted = true;

        private boolean requiresAssignment = true;

        private AttributeKey(String name, int index, Class<?> type, boolean quoted, boolean requiresAssignment) {
            this.name = name;
            this.index = index;
            this.type = type;
            this.quoted = quoted;
            this.requiresAssignment = requiresAssignment;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public boolean isQuoted() {
            return quoted;
        }

        public boolean requiresAssignment() {
            return requiresAssignment;
        }

        @Override
        public int compareTo(AttributeKey that) {
            return Integer.compare(this.index, that.index);
        }
    }

    public static String buildShellCommand(List<NetSimCommand> commands) {
        CommandLineBuilder builder = new CommandLineBuilderImpl();

        for (NetSimCommand command : commands) {
            String commandString = commandString(command);
            builder.startCommand(commandString);
            addSingleCommand(builder, command);
            builder.endCommand();
        }

        return builder.build();
    }

    public static String getCmdExecutionStartPattern(Class<? extends NetSimCommand> clazz, String... params) {
        Cmd annotation = clazz.getAnnotation(Cmd.class);
        String paramList = Joiner.on(" ").join(params);
        return String.format("(?s).*%s.*%s%s.*", NetSimConstants.COMMAND_EXEC_START, annotation.value(), StringUtils.isBlank(paramList) ? "" : " " + paramList);
    }

}
