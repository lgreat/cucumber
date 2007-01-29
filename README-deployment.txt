tlddoc
======
Run:

% maven plugin:download -Dmaven.repo.remote=http://maven-taglib.sourceforge.net/maven -DgroupId=maven-taglib -DartifactId=maven-taglib-plugin -Dversion=1.4.2



maven-tomcat plugin
===================


The maven-tomcat plugin in GSWeb allows command line
redeploying to your local tomcat by doing:

   * maven tomcat:install (if the webapp is not already installed)
   * maven tomcat:reload (if you want to update an already running version with
the latest changes)
   * maven tomcat:uninstall (to remove it from tomcat)



1. Prerequisites: You will need the tomcat maven plugin:

cd ../GSData (you must go into the GSData project since it knows about the remote maven repos)
maven -DartifactId=maven-tomcat-plugin -DgroupId=codeczar-tomcat -Dversion=1.1 plugin:download

Also, make sure your tomcat has the admin user with password gsadmin by adding the following
lines to conf/tomcat-users.xml:

  <role rolename="manager"/>
  <role rolename="admin"/>
  <user username="admin" password="gsadmin" roles="admin,manager"/>

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

/usr2/certificates/

5. To make it so you can change a class in IDEA and see the changes, you
must make the application reloadable in Tomcat.

Setup:
a) Do step 2.
b) Then in IDEA right click on gs-web -> module settings -> paths and change Output path
to that path of your source tree such as C:\java\greatschools\GSWeb\src\webapp\WEB-INF\classes .
c) go to gs-web -> module settings -> Java EE Build Settings and un-check "Create web module exploded directory"
d) Edit $TOMCAT_HOME\conf\server.xml and add the following modified for your source
path one line above the </Host> tag:
<Context path="/gs-web" docBase="c:/java/greatschools/GSWeb/src/webapp" reloadable="true">
</Context>

Usage:
Hit ctrl-shift-F9 to rebuild a class, and hit refresh in your browser and see the changes:

Same as steps a, b, and c above
Using IDEA to Compile into build directory
==========================================
To compile gs-web Java classes into the maven build directory, do the following:
   * in IDEA, go to Settings->Modules->GS-Web->Paths(tab)
   * set the output path to <your path to GSWeb>\GSWeb\src\webapp\WEB-INF\classes

If you have configured tomcat to be deployed out of your src\webapp dir,
then files compiled with IDEA will automatically trigger a reload in tomcat,
and your changes will be available without rebuilding the whole project or restarting tomcat.
Also, you need to set reloadable=true in the context descriptor for gsweb.
This should be in <tomcat_home>\conf\Catalina\localhost\ROOT.xml or <tomcat_home>\conf\Catalina\localhost\gs-web.xml


