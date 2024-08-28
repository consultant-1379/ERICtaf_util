#!/bin/bash
cd archetype
mvn clean archetype:create-from-project -Darchetype.filteredExtensions=java -DpackageName=com.ericsson.taffit
cd target/generated-sources/archetype/
mvn deploy
