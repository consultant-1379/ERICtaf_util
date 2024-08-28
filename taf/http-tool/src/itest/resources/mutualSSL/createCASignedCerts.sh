#!/bin/sh
openssl genrsa -out ${1}.key 2048
openssl req -new -key ${1}.key -out ${1}.csr
openssl x509 -req -days 3652 -in ${1}.csr -CA ${2}.crt -CAkey ${2}.key -set_serial 01 -out ${1}.crt
openssl pkcs12 -export -in ${1}.crt -inkey ${1}.key -out ${1}.p12 -name ${1}

