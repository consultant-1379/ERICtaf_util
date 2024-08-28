<#function camelize name>
    <#return name?replace('_', ' ')?replace('-', ' ')?capitalize?replace(' ', '')?uncap_first/>
</#function>
<#macro methods arg class_name value_type index>
    <#if arg.requiresAssignment?? && arg.quoted??>
    @Cmd(value="${arg.name}", index=${index},  quoted=${arg.quoted}, requiresAssignment=${arg.requiresAssignment})
    <#elseif arg.quoted?? >
    @Cmd(value="${arg.name}", index=${index}, quoted=${arg.quoted})
    <#elseif arg.requiresAssignment??>
	@Cmd(value="${arg.name}", index=${index}, requiresAssignment=${arg.requiresAssignment})
    <#elseif value_type == "String" >
    @Cmd(value="${arg.name}", index=${index}, quoted=true)
    <#else>
    @Cmd(value="${arg.name}", index=${index}, quoted=false)
    </#if>

    public ${value_type} get${camelize(arg.name)?cap_first}() {
        return this.${camelize(arg.name)};
    }

    public ${class_name} set${camelize(arg.name)?cap_first}(${value_type} value) {
        this.${camelize(arg.name)} = value;
        return this;
    }
</#macro>
<#macro createNetsimCommandClassMethods methodName className command >
    public static ${methodName}(${argList(className, command.args)}) {
        ${className} __object__ = new ${className}();
            <#list command.args as arg>
                <#if !arg.optional?? >
        __object__.set${camelize(arg.name)?cap_first}(${camelize(arg.name)});
                </#if >
            </#list>
        return __object__;
    }
</#macro>
<#function argJavaType arg>
    <#if arg.type??>
        <#switch arg.type>
            <#case "flag">
                <#return 'boolean' />
                <#break>
            <#case "vararg">
                <#return 'String[]' />
                <#break>
            <#case "boolean">
                <#return 'Boolean' />
                <#break>
            <#case "long">
                <#return 'Long' />
                <#break>
            <#case "int">
                <#return 'Integer' />
                <#break>
            <#case "float">
                <#return 'BigDecimal' />
                <#break>
            <#case "proplist">
                <#return 'List<Map>' />
                <#break>
            <#case "string">
                <#return 'String' />
                <#break>
            <#case "enum">
                <#return 'enum' />
                <#break>
        </#switch>
    </#if>
    <#return 'String' />
</#function>
<#function argList className args >
    <#assign argsList = '' />
    <#list args as arg>
        <#if arg.type?? && arg.type == 'enum'>
            <#assign argsList = argsList + "${className}.${camelize(arg.name)?cap_first} ${camelize(arg.name)}, ">
        <#elseif arg.type?? && arg.type == 'vararg' && args?size == 1>
            <#assign argsList = argsList + "String... ${camelize(arg.name)}, ">
        <#else>
            <#if !arg.optional?? >
            <#assign argsList = argsList + "${argJavaType(arg)} ${camelize(arg.name)}, ">
            </#if>
        </#if>
    </#list>
    <#if argsList?ends_with(", ") >
        <#assign lastIndex = argsList?last_index_of(", ")/>
        <#assign argsList = argsList?substring(0, lastIndex) />
    </#if>
    <#return argsList />
</#function>
<#function countNonOptionalParams list>
    <#assign nonOptionaParamsCount = 0>
	<#list list as nextInList>
	    <#if !nextInList.optional??><#assign nonOptionaParamsCount = nonOptionaParamsCount + 1></#if>
	</#list>
	<#return nonOptionaParamsCount>
</#function>
<@pp.dropOutputFile/>
<#assign packageName="com.ericsson.cifwk.taf.handlers.netsim.commands"/>
<@pp.changeOutputFile name="${packageName?replace('.', '/')}/dummy"/>
<@pp.dropOutputFile/>
<#list commands as command>
<#assign className="${command.cmd?replace('.','')?capitalize?replace(' ','')?replace('_','')}Command"/>
<@pp.changeOutputFile name="${className}.java"/>
package ${packageName};

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.handlers.netsim.*;

@Cmd(value="${command.cmd}")
public final class ${className} implements NetSimCommand {

    <#if command.args??>
        <#list command.args as arg>
            <#assign argType="${argJavaType(arg)}"/>
            <#if argType=='enum'>
    public static enum ${camelize(arg.name)?cap_first} {
                        <#list arg.values as value>
        ${value?upper_case}("${value}"),
                        </#list>
        ;

        private final String value;

        ${camelize(arg.name)?cap_first}(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
    private ${camelize(arg.name)?cap_first} ${camelize(arg.name)};
            <#elseif argType=='boolean'>
    private ${argType} ${camelize(arg.name)} = false;
            <#else>
    private ${argType} ${camelize(arg.name)};
            </#if>
        </#list>
    </#if>

    ${className}() {
    }

    <#if command.args??>
        <#list command.args as arg>
            <#assign argType="${argJavaType(arg)}"/>
            <#if argType=='enum'>
                <@methods arg=arg class_name=className value_type="${camelize(arg.name)?cap_first}" index=arg_index/>
            <#else>
                <@methods arg=arg class_name=className value_type="${argType}" index=arg_index/>
            </#if>
        </#list>
    </#if>
}
</#list>
<@pp.changeOutputFile name="NetSimCommands.java"/>
package ${packageName};

public final class NetSimCommands {

<#list commands as command>
    <#assign className="${command.cmd?replace('.','')?capitalize?replace(' ','')?replace('_','')}Command"/>
    <#assign methodName="${className} ${command.cmd?replace('.','')?capitalize?replace(' ','')?uncap_first}"/>
    public static ${methodName}() {
        return new ${className}();
    }

    <#if command.args??>
    <#if command.args?size&gt;0>
        <#if countNonOptionalParams(command.args)&lt;5 && countNonOptionalParams(command.args)&gt;0>
           <@createNetsimCommandClassMethods methodName=methodName className=className command=command />
        </#if>
    </#if>
    </#if>

</#list>
}
