package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.util.regex.Pattern;

import com.ericsson.cifwk.meta.API;

/**
 * Created by ejohlyn on 1/27/16.
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
*/
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class CLICommandHelperConstants {

    protected static final Pattern splitStdOutPattern = Pattern.compile("([ ]*\r)");

    public static final String STRICT_HOST_KEY_CHECKING_YES = "-o StrictHostKeyChecking=yes";
    public static final String STRICT_HOST_KEY_CHECKING_NO = "-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no";
    public static final String FAILED_TO_HOP_TO = "Failed to hop to %s in two attempts";
    public static final String NO_DEFAULT_USER_MSG = "There is no default user for host";
    public static final String DESTINATION_HOST_UNREACHABLE_ERR_MSG = "Destination Host Unreachable";
    public static final String FAILED_TO_GET_IP_FROM_ETC_HOST_FILE_ERR_MSG = "No IP was supplied for the destination host and failed to retrieve IP from /etc/hosts";
    public static final String NO_ROUTE_TO_HOST_ERR_MSG = "No route to host";
    public static final String CONNECTION_REFUSED_ERR_MSG = "Connection refused";
    public static final String PERMISSION_DENIED_ERR_MSG = "Permission denied";
    public static final String PASSWORD_NOT_ACCEPTED_ERR_MSG = "Password was not accepted";
    public static final String INCORRECT_PASSWORD = "incorrect password";
    public static final String DOES_NOT_EXIST = "does not exist";
    public static final String FAILED_TO_HOP_TO_USER = "Failed to hop to user ";
    public static final int USER_HOP_REPEAT_COUNT = 5;
    public static final String INVALID_PATH_TO_FILE_MSG = "Provided path to file is blank or null." +
            " Please provide a correct path to file.";
    public static final String FILE_NOT_FOUND_MSG = "Path to private key file is not valid. Please verify that the path provided is correct: %s";

    public static final String GENERIC_HASH_PROMPT = "^[^#]+#\\s*$";
    public static final String GENERIC_ANGLE_PROMPT = "^[^>]+>\\s*$";
    public static final String GENERIC_DOLLAR_PROMPT = "\\$\\s*";
    public static final String USER_NAME_PATTERN = "|(?s).*%s@(\\w+.*|\\])";
    public static final String USER_PASSWORD_PROMPT = String.format("(?s).*[Pp]assword:|%s|%s|.*%s",GENERIC_HASH_PROMPT,GENERIC_ANGLE_PROMPT, GENERIC_DOLLAR_PROMPT);

    public static final Pattern INITIAL_LOGIN_PATTERN = Pattern.compile(String.format("Are you sure|[Pp]assword|%s|%s|%s|[$]", GENERIC_HASH_PROMPT, GENERIC_ANGLE_PROMPT, NO_ROUTE_TO_HOST_ERR_MSG));
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(".*[Pp]assword.*");
    public static final Pattern GENERIC_PROMPT_PATTERN = Pattern.compile("[#>$]");
    public static final Pattern SEND_PASS_PATTERN = Pattern.compile("(?s).*[Pp]assword|(?s).*[#>$]\\s*");

    public static final String GETENT_HOSTS = "getent hosts %s | /usr/bin/awk '{print $1}'";
}
