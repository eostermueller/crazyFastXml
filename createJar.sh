#export JMETER_HOME=
export JAR_FILE_NAME=$JMETER_HOME/lib/ext/crazyFastXml.jar

jar cvfM $JAR_FILE_NAME -C target/classes .
jar uvfM $JAR_FILE_NAME -C target/test-classes .

echo "Create jar file:"
ls -lart $JAR_FILE_NAME
