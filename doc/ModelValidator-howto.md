# Using ModelValidator

## Installation in eXistdb

For betterFORM Fore a complete package migration was done to avoid conflicts when installed alongside betterFORM 5.
All packages starting with 'de.betterform' have been renamed to 'de.betterform.fore'.

* execute ```ant create-single-jar``` in 'web' directory
* move the resulting jar from 'web/target/' to EXISTDB_HOME/extensions/betterform/main/lib
* move dependent libs (see next section) to EXISTDB_HOME/extensions/betterform/main/lib
* copy web.sample.xml from 'xar/ModelValidatorDemo' to  web.xml at EXISTDB_HOME/webapp/WEB-INF
* copy betterform-config.xml from 'web/src/main/webapp/WEB-INF/betterform-config.xml to 'EXISTDB_HOME/webapp/WEB-INF/betterform-config.xml'
***Attention***: when rebuilding eXistdb from sources you have to change web.xml.tmpl. This will overwrite changes to web.xml.
* restart eXistdb if applicable


## Dependencies

* core/src/main/lib/jackson-*
* woodstox-core-lgpl-4.2.0.jar
* stax2-api-3.1.1.jar


*** jackson-core-2.1.2 in EXISTDB_HOME/lib/core must be upgraded to jackson 2.4.3 to be compatible

## Using ModelValidator

ModelValidator in eXistdb requires the use of xhtml-conform HTML documents at the moment. ModelValidator itself can
handle HTML5 but there are still some restrictions using that with out-of-the-box eXistdb.



