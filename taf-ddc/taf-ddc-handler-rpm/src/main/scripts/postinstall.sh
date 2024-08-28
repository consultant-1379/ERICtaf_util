#postinstall scriptlet (using /bin/sh):

INST_ROOT="/opt/ericsson/ERICddc/lib"
PROGRAM="taf-ddc"
echo "Initial Install - ${PROGRAM}"
INSTALL_SCRIPT="${INST_ROOT}/bin/install"
if [ ! -x "${INSTALL_SCRIPT}" ] ; then
    echo "  - Installation script ${INSTALL_SCRIPT} not found"
    exit 1
fi

sh ${INSTALL_SCRIPT} &


