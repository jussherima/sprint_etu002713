javac -cp "lib/*:" -d bin src/**/*.java

jar -cvf sprint_etu002713.jar -C bin . -C lib .
cp sprint_etu002713.jar ../usage_frameworks/lib/
cp sprint_etu002713.jar ../usage_frameworks/WEB/WEB-INF/lib/