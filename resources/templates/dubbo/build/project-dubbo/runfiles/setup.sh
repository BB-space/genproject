#!/bin/sh
#if($enablepinpoint.equals("true"))
export AGENT_VERSION==$AGENT_VERSION
export AGENT_ID=$AGENT_ID
export APPLICATION_NAME=$APPLICATION_NAME
export AGENT_PATH=$AGENT_PATH
nohup java -Xms1024m -Xmx1024m -Xmn512m -XX:PermSize=128M -XX:MaxPermSize=256M -javaagent:\\\$AGENT_PATH/pinpoint-bootstrap-1.6.0.jar -Dpinpoint.agentId=\\\$AGENT_ID -Dpinpoint.applicationName=\\\$APPLICATION_NAME -jar bboss-rt-${bboss_version}.jar > dubbo.log &
#else
nohup java -Xms1024m -Xmx1024m -Xmn512m -XX:PermSize=128M -XX:MaxPermSize=256M -jar bboss-rt-${bboss_version}.jar > dubbo.log & 
#end