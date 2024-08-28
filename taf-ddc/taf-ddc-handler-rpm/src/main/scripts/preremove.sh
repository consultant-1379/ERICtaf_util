#preremove scriptlet (using /bin/sh):

INST_ROOT="/opt/ericsson/ERICddc/lib"
echo "Cleaning files from ${INST_ROOT}"

${INST_ROOT}/bin/uninstall
