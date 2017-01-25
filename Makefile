#################################################
# 
# Makefile for Java
#
# (c) 2015 Matthias Nott
#
#################################################


#################################################
# 
# Configuration
# 
# All configurations may depend on the local
# development environment. For this reason,
# we only list the configuration variables
# here and give some values; the actual
# configuration file may overwrite them.
#
#################################################


#
# Makefile that is not commited to git and that
# will overwrite configuration variables done
# in this file. This is a good place to actually
# configure your build environment, store passwords,
# etc.
# 
MAKE_CONFIG=make.properties

#
# Our javac lives in $$JAVA_HOME/bin
# 
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home

#
# Classpath. This is what we need to compile the web application's
# class files. It is not necessarily identical with what we need
# to deploy into WEB-INF/lib: It is what we need to compile.
# 
CLASSPATH=../$$WEBROOT/WEB-INF/classes:../lib/other/gson-2.3.1.jar:../lib/other/log4j.jar:../lib/other/slf4j-log4j12-1.7.13.jar:../lib/other/slf4j-api-1.7.13.jar:../lib/other/mysql-connector-java-5.1.7-bin.jar:../lib/other/ngdbc.jar:../lib/other/servlet-api.jar:../lib/other/httpclient-4.5.1.jar:../lib/other/httpcore-4.4.3.jar:../lib/other/commons-logging-1.2.jar:../lib/other/commons-pool2-2.4.2.jar:../lib/other/commons-dbcp2-2.1.1.jar:../lib/other/javax.persistence-2.1.0.jar:../lib/jpa/eclipselink.jar:../lib/sap/com.sap.ui5.resource_1.28.7.jar:../lib/other/commons-codec-1.10.jar:../lib/other/cxf-core-3.1.4.jar:../lib/other/cxf-rt-frontend-jaxrs-3.1.4.jar:../lib/other/cxf-rt-transports-http-3.1.4.jar:../lib/other/org.apache.olingo-olingo-odata2-api-2.0.5.jar:../lib/other/org.apache.olingo-olingo-odata2-api-annotation-2.0.5.jar:../lib/other/org.apache.olingo-olingo-odata2-core-2.0.5.jar:../lib/other/org.apache.olingo-olingo-odata2-jpa-processor-api-2.0.5.jar:../lib/other/org.apache.olingo-olingo-odata2-jpa-processor-core.jar:.

#
# Tomcat target deployment directory. This directory
# contains the webapps directory, into which we are
# going to deploy our web application.
#
TOMCAT=/pgm/java/tomcat/apache-tomcat-8.0.23/

#
# Location of the neo.sh script
#
HCP_SDK=/pgm/java/hanacloudsdk/tools/neo.sh

#
# Name of the webapp we are going to deploy into.
# This is a directory we are going to create under
# $$TOMCAT/webapps and on HCP.
#
WEBAPP=hcpcu

#
# Webroot folder inside your Eclipse directory.
#
WEBROOT=WebRoot

#
# Source Folders relative to the project root of
# the project. More than one need to be space
# separated
#
SOURCES=src

#
# Temporary Directory
#
TMPDIR=tmp

#
# Content that is to be deployed to WEB-INF/classes,
# relative to the project root of the project. More
# than one location to be separated by spaces.
#
TO_CLASSES=config/* data/*

#
# Content that is to be copied to WEB-INF/lib,
# relative to the project root of the project. More
# than one location to be separated by spaces.
#
TO_LIB=lib/other/gson-2.3.1.jar lib/other/log4j.jar lib/other/mysql-connector-java-5.1.7-bin.jar lib/other/ngdbc.jar lib/other/commons-pool2-2.4.2.jar lib/other/commons-dbcp2-2.1.1.jar lib/other/javax.persistence-2.1.0.jar lib/jpa/eclipselink.jar lib/other/*jar lib/sap/*jar

#
# Content that is to be copied to WEB-INF/classes/META-INF,
# relative to the project root of the project. More
# than one location to be separated by spaces.
# 
TO_METAINF=src/META-INF/persistence.xml


#################################################
# 
# HCP Specific configuration
# 
#################################################

#
# HCP Hostname
#
HCP_HOST=hanatrial.ondemand.com

#
# HCP Account Name
#
HCP_ACCOUNT=i052341trial

#
# HCP Username
# 
HCP_USER=i052341

#
# HCP User Password
# 
# Prefix the Password with -p 
#
# HCP_PASS=-p topsecret

#
# HCP Runtime Version. See
# 
# make hcpruntimes
#
HCP_RUNTIME_VERSION=2

#
# HCP JAVA VERSION
#
HCP_JAVA_VERSION=8



#################################################
# 
# End of Configuration
#
#################################################

PATH=$(JAVA_HOME)/bin:$(shell printenv PATH)

include $(MAKE_CONFIG)

ifeq ($(MAKE),)
    MAKE := make
endif

define uc
$(shell echo $1 | tr a-z A-Z)
endef

.SILENT :

.EXPORT_ALL_VARIABLES :

.NOTPARALLEL :


#################################################
#
# Help function
#
#################################################
.PHONY: help
help :

	echo
	echo "============================================================"
	echo "Welcome to this massively informative help...               "
	echo "============================================================"
	echo 
	echo "------------------------------------------------------------"
	echo "Local Deployment Targets                                    "
	echo "------------------------------------------------------------"
	echo
	echo "You have the following targets:                             "
	echo 
	echo "make                Show this help screen                   "
	echo
	echo "make clean          Clean the project                       "
	echo
	echo "make compile        Compile the project                     "
	echo
	echo "make deploy         Deploy the project                      "
	echo
	echo "make undeploy       Undeploy the project                    "
	echo
	echo "make test           Show configuration                      "
	echo
	echo "------------------------------------------------------------"
	echo "HCP Deployment Targets                                      "
	echo "------------------------------------------------------------"
	echo
	echo "make hcpdeploy      Differential Deploy the project to HCP  "
	echo
	echo "make hcpfulldeploy  Full Deploy the project to HCP          "
	echo
	echo "make hcpundeploy    Deploy the project to HCP               "
	echo
	echo "make hcpstop        Stop the HCP webapp                     "
	echo
	echo "make hcpstart       Start the HCP webapp                    "
	echo
	echo "make hcprestart     Restart the HCP webapp                  "
	echo
	echo "make hcpstatus      Get the HCP webapp status               "
	echo
	echo "make hcpruntimes    List available HCP runtimes             "
	echo
	echo "============================================================"
	echo


#################################################
# 
# Test
#
#################################################

.PHONY: test
test :
	echo "=============================================";\
	echo "Makefile Configuration";\
	echo "=============================================";\
	echo PATH=$$PATH;\
	echo JAVA_HOME=$$JAVA_HOME;\
	echo CLASSPATH=$$CLASSPATH;\
	echo TOMCAT=$$TOMCAT;\
	echo HCP_SDK=$$HCP_SDK;\
	echo WEBAPP=$$WEBAPP;\
	echo WEBROOT=$$WEBROOT;\
	echo SOURCE=$$SOURCE;\
	echo TMPDIR=$$TMPDIR;\
	echo TO_CLASSES=$$TO_CLASSES;\
	echo TO_LIB=$$TO_LIB\;
	echo TO_METAINF=$$TO_METAINF\;
	echo "=============================================";\
	echo HCP_HOST=$$HCP_HOST;\
	echo HCP_ACCOUNT=$$HCP_ACCOUNT;\
	echo HCP_USER=$$HCP_USER;\
	echo HCP_PASS=$$HCP_PASS;\
	echo HCP_RUNTIME_VERSION=$$HCP_RUNTIME_VERSION;\
	echo HCP_JAVA_VERSION=$$HCP_JAVA_VERSION;
	echo "=============================================";


#################################################
# 
# Compile
#
#################################################

.PHONY: compile
compile :
	echo "----------------------------------" 
	echo "make compile"
	echo "----------------------------------" 
	for src in $$SOURCES; do \
		cd $$src; \
		java -version;\
		rsync -avh --delete --include='*/' --exclude='*' . ../$$WEBROOT/WEB-INF/classes/; \
		find . -name *class -exec sh -c 'mv $$(dirname $$1)/$$(basename $$1) ../$$WEBROOT/WEB-INF/classes/$$(dirname $$1)' _ "{}"  \; ;\
		find . -name *java  -exec sh -c 'f=$$(basename $$1);fn=$$(dirname $$1)/$${f%.*};if test ! -f ../$$WEBROOT/WEB-INF/classes/$${fn}.class -o $${fn}.java -nt ../$$WEBROOT/WEB-INF/classes/$${fn}.class; then echo $${fn}.java ; javac -d . -Xlint:deprecation -Xlint:unchecked -cp "$${CLASSPATH}" $${fn}.java && mv $${fn}.class ../$$WEBROOT/WEB-INF/classes/$${fn}.class; fi' _ "{}"  \; ;\
		find . -name *class -exec sh -c 'mv $$(dirname $$1)/$$(basename $$1) ../$$WEBROOT/WEB-INF/classes/$$(dirname $$1)' _ "{}"  \; ;\
	done;


#################################################
# 
# Deploy
#
#################################################

.PHONY: deploy
deploy : compile
	echo "----------------------------------" 
	echo "make deploy"
	echo "----------------------------------" 
	if [ ! -d "$$TOMCAT/webapps/$$WEBAPP" ]; then \
		mkdir "$$TOMCAT/webapps/$$WEBAPP"; \
	fi; \
	if [ ! -d "$$TMPDIR" ]; then \
		mkdir "$$TMPDIR"; \
	fi; \
	cd "$$TMPDIR"; \
	if [ ! -d "$$WEBAPP" ]; then \
		mkdir "$$WEBAPP"; \
	fi; \
	cd "$$WEBAPP"; \
	pwd;\
	rsync -avh --delete "../../$$WEBROOT"/* .; \
	touch WEB-INF/web.xml; \
	for i in $$TO_CLASSES; do rsync -avh ../../$$i WEB-INF/classes/; done; \
	for i in $$TO_LIB; do rsync -avh ../../$$i WEB-INF/lib/; done; \
	for i in $$TO_METAINF; do rsync -avh ../../$$i WEB-INF/classes/META-INF/; done; \
	rsync -avh --delete . "$$TOMCAT/webapps/$$WEBAPP/" ;


#################################################
# 
# Undeploy
#
#################################################

.PHONY: undeploy
undeploy :
	echo "----------------------------------" 
	echo "make undeploy"
	echo "----------------------------------" 
	if [ -d "$$TOMCAT/webapps/$$WEBAPP" ]; then \
		rm -rf "$$TOMCAT/webapps/$$WEBAPP"; \
	fi;



#################################################
# 
# HCP Deploy
#
#################################################

.PHONY: hcpdeploy
hcpdeploy : compile
	echo "----------------------------------" 
	echo "make hcpdeploy"
	echo "----------------------------------" 
	if [ ! -d "$$TMPDIR" ]; then \
		mkdir "$$TMPDIR"; \
	fi; \
	cd "$$TMPDIR"; \
	pwd;\
	if [ ! -d "$$WEBAPP" ]; then \
		mkdir "$$WEBAPP"; \
	fi; \
	cd "$$WEBAPP"; \
	pwd;\
	rsync -avh --delete "../../$$WEBROOT"/* .; \
	touch WEB-INF/web.xml; \
	for i in $$TO_CLASSES; do rsync -avh ../../$$i WEB-INF/classes/; done; \
	for i in $$TO_LIB; do rsync -avh ../../$$i WEB-INF/lib/; done; \
	for i in $$TO_METAINF; do rsync -avh ../../$$i WEB-INF/classes/META-INF/; done; \
	zip -u -r ../$$WEBAPP.war *; \
	$$HCP_SDK deploy -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP --source ../$$WEBAPP.war --runtime-version $$HCP_RUNTIME_VERSION -j $$HCP_JAVA_VERSION --delta -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Full Deploy
#
#################################################

.PHONY: hcpfulldeploy
hcpfulldeploy : compile
	echo "----------------------------------" 
	echo "make hcpfulldeploy"
	echo "----------------------------------" 
	if [ ! -d "$$TMPDIR" ]; then \
		mkdir "$$TMPDIR"; \
	fi; \
	cd "$$TMPDIR"; \
	pwd;\
	if [ ! -d "$$WEBAPP" ]; then \
		mkdir "$$WEBAPP"; \
	fi; \
	cd "$$WEBAPP"; \
	pwd;\
	rsync -avh --delete "../../$$WEBROOT"/* .; \
	touch WEB-INF/web.xml; \
	for i in $$TO_CLASSES; do rsync -avh ../../$$i WEB-INF/classes/; done; \
	for i in $$TO_LIB; do rsync -avh ../../$$i WEB-INF/lib/; done; \
	for i in $$TO_METAINF; do rsync -avh ../../$$i WEB-INF/classes/META-INF/; done; \
	zip -u -r ../$$WEBAPP.war *; \
	$$HCP_SDK deploy -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP --source ../$$WEBAPP.war --runtime-version $$HCP_RUNTIME_VERSION -j $$HCP_JAVA_VERSION -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Undeploy
#
#################################################

.PHONY: hcpundeploy
hcpundeploy : hcpstop
	echo "----------------------------------" 
	echo "make hcpundeploy"
	echo "----------------------------------" 
	$$HCP_SDK undeploy -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Stop
#
#################################################

.PHONY: hcpstop
hcpstop :
	echo "----------------------------------" 
	echo "make hcpstop"
	echo "----------------------------------" 
	$$HCP_SDK stop -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Start
#
#################################################

.PHONY: hcpstart
hcpstart : 
	echo "----------------------------------"
	echo "make hcpstart"
	echo "----------------------------------"
	$$HCP_SDK start -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Restart
#
#################################################

.PHONY: hcprestart
hcprestart :
	echo "----------------------------------" 
	echo "make hcprestart"
	echo "----------------------------------" 
	$$HCP_SDK restart -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
# 
# HCP Status
#
#################################################

.PHONY: hcpstatus
hcpstatus :
	echo "----------------------------------" 
	echo "make hcpstatus"
	echo "----------------------------------" 
	$$HCP_SDK status -h $$HCP_HOST -u $$HCP_USER --application $$WEBAPP -a $$HCP_ACCOUNT $$HCP_PASS;


#################################################
#
# HCP List Runtime Versions 
#
#################################################

.PHONY: hcpruntimes
hcpruntimes :
	echo "----------------------------------" 
	echo "make hcpruntimeversions"
	echo "----------------------------------" 
	$$HCP_SDK list-runtime-versions -h $$HCP_HOST -u $$HCP_USER $$HCP_PASS; \
	$$HCP_SDK list-runtimes -h $$HCP_HOST -u $$HCP_USER $$HCP_PASS;


#################################################
# 
# Remove all generated files
#
#################################################

.PHONY: clean
clean :
	echo "----------------------------------" 
	echo "make clean"
	echo "----------------------------------" 
	if [ -d "$$TOMCAT/webapps/$$WEBAPP" ]; then \
		rm -rf "$$TOMCAT/webapps/$$WEBAPP"; \
	fi; \
	if [ -d $$WEBROOT/WEB-INF/classes ]; then \
		rm -rf $$WEBROOT/WEB-INF/classes; \
		mkdir $$WEBROOT/WEB-INF/classes; \
	fi; \
	if [ -d $$WEBROOT/WEB-INF/lib ]; then \
		rm -rf $$WEBROOT/WEB-INF/lib; \
		mkdir $$WEBROOT/WEB-INF/lib; \
	fi; \
	if [ -d "$$TMPDIR" ]; then \
		rm -rf "$$TMPDIR"; \
	fi; \
	mkdir "$$TMPDIR"; \
