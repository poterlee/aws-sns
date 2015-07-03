FROM tomcat:8-jre7

MAINTAINER "Forrest Inc."

ADD tomcat-users.xml /usr/local/tomcat/conf/
ADD server.xml /usr/local/tomcat/conf/
ADD mint.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]