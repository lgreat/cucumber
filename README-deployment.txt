1. Prerequisites: You will need the tomcat maven plugin:

cd ../GSData (you must go into the GSData project since it knows about the remote maven repos)
maven -DartifactId=maven-tomcat-plugin -DgroupId=codeczar-tomcat -Dversion=1.1 plugin:download

Also, make sure your tomcat has the admin user with password gsadmin.
TODO: say where to do this.

2. To deploy the WAR directly into the src/webapp directory to allow live editing of JSP pages,
create a build.properites file with the following line:
maven.war.webapp.dir=${maven.war.src}

Then do a "maven tomcat:remove" and a "maven tomcat:install" while Tomcat is running. Then you
should be able to edit JSP file and see the changes simply by hitting refresh in your browser.

3. To deploy without running unit tests:

maven -Dmaven.test.skip=true tomcat:install
(or tomcat:reload or tomcat:remove)

Note: runinng tomcat:install also copies CVS files over to target/gs-web/ so that you can
edit JSP files in the exploded war without redeploying.

4. To deploy, you'll need to have the certificates installed on your system.

Currently it's in this folder:

/usr2/local/certificates/

5. To make it so you can change a class in IDEA and see the changes, you
must make the application reloadable in Tomcat.

Setup:
a) Do step 2.
b) Then in IDEA right click on gs-web -> module settings -> paths and change Output path
to that path of your source tree such as C:\java\greatschools\GSWeb\src\webapp\WEB-INF\classes .
c) Edit $TOMCAT_HOME\conf\server.xml and add the following modified for your source
path one line above the </Host> tag:
<Context path="/gs-web" docBase="c:/java/greatschools/GSWeb/src/webapp" reloadable="true">
</Context>

Usage:
Hit ctrl-shift-F9 to rebuild a class, and hit refresh in your browser and see the changes:

