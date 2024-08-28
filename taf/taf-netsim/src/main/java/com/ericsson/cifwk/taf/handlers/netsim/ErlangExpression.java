package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
final class ErlangExpression {

    private ErlangExpression() {
    }

    public static String build(List<?> list) {
        StringBuilder builder = new StringBuilder();
        recurse(list, builder);
        return builder.toString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private static void recurse(Object object, StringBuilder builder) {
        if (object instanceof List) {
            builder.append("[");
            for (Iterator iterator = ((List) object).iterator(); iterator.hasNext(); ) {
                Object item = iterator.next();
                recurse(item, builder);
                if (iterator.hasNext()) builder.append(",");
            }
            builder.append("]");
        } else if (object instanceof Map) {
            Map map = ((Map) object);
            Set<Map.Entry> set = map.entrySet();
            builder.append("{");
            for (Map.Entry entry : set) {
                recurse(entry.getKey(), builder);
                builder.append(",");
                recurse(entry.getValue(), builder);
            }
            builder.append("}");
        } else if ((object instanceof Number) || (object instanceof Boolean)) {
            builder.append(object);
        } else {
            builder
                    .append("\\\"")
                    .append(object)
                    .append("\\\"");
        }
    }

}
