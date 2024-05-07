# copier le bin dans le classes
cd bin/
cp -r * ../WEB/WEB-INF/classes/
cd ../


# transformez le projet de test en .war
cd WEB
jar -cvf ../framework.war *
cd ../
# copier le war dans tomcat
cp framework.war $CATALINA_HOME/webapps/

$CATALINA_HOME/bin/shutdown.bat
$CATALINA_HOME/bin/startup.bat