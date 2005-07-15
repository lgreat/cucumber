1. Prerequisites: You will need the tomcat maven plugin:

cd ../GSData (you must go into the GSData project since it knows about the remote maven repos)
maven -DartifactId=maven-tomcat-plugin -DgroupId=codeczar-tomcat -Dversion=1.1 plugin:download

Also, make sure your tomcat has the admin user with password gsadmin.

2. To deploy without running unit tests:

maven -Dmaven.test.skip=true tomcat:install 
(or tomcat:reload or tomcat:remove)

Note: runinng tomcat:install also copies CVS files over to target/gs-web/ so that you can
edit JSP files in the exploded war without redeploying.

3. To deploy, you'll need to have the certificates installed on your system.

Currently it's in this folder:

/usr2/local/certificates/


