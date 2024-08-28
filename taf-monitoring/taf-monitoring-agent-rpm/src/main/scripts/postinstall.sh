#postinstall scriptlet (using /bin/sh):

INST_ROOT="/opt/ericsson/com.ericsson.cifwk.taf.monitoring"
PROGRAM="taf-agent"
if [ "$1" -gt "1" ]
then
        # its an upgrade or reinstall keep settings as they are
        echo "Upgrade ${PROGRAM}"

    echo "  - Upgrade: Uninstalling old ${PROGRAM} RPM..."

    UNINSTALL_SCRIPT="${INST_ROOT}/bin/uninstall"
    if [ ! -x "${UNINSTALL_SCRIPT}" ] ; then
        echo "  - Upgrade: Installation script ${UNINSTALL_SCRIPT} not found"
        exit 1
    fi

    ${UNINSTALL_SCRIPT}

    echo "  - Upgrade: Installing new ${PROGRAM} RPM..."
else
    echo "Initial Install - ${PROGRAM}"
fi
    INSTALL_SCRIPT="${INST_ROOT}/bin/install"
    if [ ! -x "${INSTALL_SCRIPT}" ] ; then
        echo "  - Upgrade: Installation script ${INSTALL_SCRIPT} not found"
        exit 1
    fi

    ${INSTALL_SCRIPT}
 
