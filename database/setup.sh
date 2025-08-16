#!/bin/bash

# TODO: 
#   check if npm is installed before using 

# setting up vars 
package_name='@dbml/cli'
dbml_file='database.dbml'
schema_file='schema.sql'
db_type='postgres'

# install dbml2sql module from npm if it isn't  
if [[ "$(npm list -g $package_name)" =~ "empty" ]]; then
    echo "Installing $package_name ..."
    npm install -g $package_name
else
    echo "$package_name is already installed"
fi

# for every dbml file under the database directory
for filename in ./**/*.dbml; do
    dbml2sql "--$db_type" "./$dbml_file" -o $schema_file
done