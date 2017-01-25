Welcome to the UI5 Boiler Plate (ui5bp)!
=================================


----------
# Summary

This is a small boilerplate environment that allows you to quickly start building UI5 apps. Besides its very versatile application structure, it is able to work both within an Eclipse environment and standalone. For the standalone case, a Makefile is provided which allows for a very fast development roundtrip, both to a local Tomcat as well as to an HCP instance, using differential deployment that the Eclipse integration of HCP does not support.

The remainder of this document describes how to make this work. We assume that you are using a unixoid environment (tested with Windows, Linux as well as Mac OS X).

----------
# Installation of your Environment


We first describe the installation of an environment that will kickstart your work. If you already have parts of it, you can skip the corresponding sections. We start by the most common situation, where you are working on a Windows system.

## Windows

We assume that you do not have administrative rights on your (64 bit) Windows system. We provide with a fully "portable" installation, i.e. you do not need to run any kind of installer to use this.

### Preparation

Utilize a drive where you have write permissions in the root directory of the drive. This could even be a thumb drive. Make sure it has about 1 GB of free space. For the reminder of this document, we assume that your drive is `e:` Create a folder `hcp` at the root of this drive.

### Download

We will provide you with download links for the various bits of the software. Some of those you may already have; in that section you just need to point the Makefile (`make.properties`, see below) to the right locations. There is an extensive description in the Windows version of the makefile, `make.properties.template.windows` on how to do this.

#### Git

The bare minimum for you to download is the `git.zip` - it contains a preconfigured `git` environment that you will then use to download the boilerplate application. Expand this zip so that you get a directory `e:\hcp\git`

#### Shortcuts

There is a convenience download that you can utilize, `shortcuts.zip` - it gives you some start scripts for the different pieces. Download it and expand it as `e:\hcp\shortcuts` Note that there are some Windows shortcuts inside, along with some `.cmd` scripts. Because of how Windows shortcuts work, they have the drive letter - `e:` - included and will hence point to the wrong place if you run on a different drive. You can right-click on any of those shortcuts and fix the reference.

#### Java

We have had good experiences with Java 7. Java 8 gave some issues when deploying to HCP, so we include, for your convenience, a pre-installed Java 7 JDK: Unfortunately, on Windows, the standard way to get Java is to run an installer, but actually, once you have run that installer, you will end up with a JDK directory that you can copy to any other system and use it, without running an installer. Download the file `java.zip` and expand it so that you get a folder `e:\hcp\java` and inside that e.g. a file `jdk1.7.0_80\bin\javac.exe`

If you already have a JDK available, you can spare that download and adapt your `make.properties` - see below - to point to it.

#### HANA Cloud SDK

You should always utilize the latest version of the HANA Cloud SDK as available from SAP. For your convenience, we include `hanacloudsdk.zip` - likely not the latest version - which you can expand to become `e:\hcp\hanacloudsdk` - which then, e.g., will contain a subdirectory `tools`

If you already have the HANA Cloud SDK available, you can spare that download and point to that location inside the `make.properties` file - see below.

#### Tomcat

For a local deployment, we can use a Tomcat environment. For your convenience, you can download `apache-tomcat-8.0.zip` and expand it so that you get `e:\hcp\apache-tomcat-8.0.24` and within that an empty `webapps` folder.

If you already have a Tomcat installation somewhere - we have had good results with Tomcat 8.x - you can spare this download and adapt your `make.properties` - see below - to point to the location where you have your `webapps` folder. 

#### Editors

[Sublime Text](http://www.sublimetext.com/3) is an awesome editor  that you can use. It is not free software, so while we provide for a pre-installed version, make sure that if you want to use it, you purchase a license for it from their web site. You can expand `sublimetext.zip` so that you get a directory `e:\hcp\sublimetext` which contains a default installation of Sublime Text 3.

The UI5BP boiler plate contains a file `ui5bp.sublime-project` which hard-wires the keyboard shortcut `Ctrl+B` to run the build process using the Makefile.

Since the UI5BP boiler plate is also an Eclipse project, if you prefer Eclipse, you can just import the project into Eclipse.

Finally, the `git.zip` environment contains a recent version of `vim` which you can extend to your liking. See, for example, [here](http://www.lucianofiandesio.com/vim-configuration-for-happy-java-coding) and [here](http://spf13.com/post/perfect-vimrc-vim-config-file).


## Linux

If you want to run on Linux, you only have to make sure that you have `make`, `rsync` and `git` available on top of the normal Linux tools such as `find` etc. To get you started even faster, we can also provide you with a download link to a pre-configured virtual machine that contains the complete development environment pre-installed.


## Mac OS X
 
If you want to run on Mac OS X, you can of course do so - as with Linux, make sure you have `make`, `rsync` and `git` installed and you are good to go.



----------
# Configuration of the Git Environment

The following process works mostly the same way on the different operating systems; we have added some convenience to the Windows environment to configure your `git` installation, and on the `Linux` virtual machine, we have already configured `git` - that's why we describe the process starting with the most common environment, Windows.

## Windows

On Windows, double-click the `e:\hcp\shortcuts\bash.cmd` script, or the `Bash` shortcut (you perhaps would have had to fix its reference, see above). At the first run, you will be asked to enter your Name (Firstname and Lastname) as well as your email address:

```
Need to Configure Git.
Enter your name : Matthias Nott
Enter your email: matthias.nott@sap.com
Is this information correct? (y/n): y
Thank you.

Administrator@xp64 MINGW64 /e/hcp/projects
$
```

You'll also notice that at this point, a directory `e:\hcp\projects` will have been created, and your shell has been changed into that directory. This is the directory into which you will clone your boiler plate app.

## Linux, Mac OS X

The configuration that was actually applied by the above-mentioned process under Windows, besides the creation of the `projects` folder, was this (note that you never enter the `$ ` part at the beginning of the commands shown in the examples below; this is just the prompt of your command shell):

```
$ git.config --global user.name "Matthias Nott"
$ git.config --global user.email "matthias.nott@sap.com"
$ git config --global push.default simple
$ git config --global http.sslverify false
```

The `http.sslverify` configuration is needed for the SAP internal Github. Now, if you have not yet done these configurations on your Linux or Windows environments, you can do so in a command shell window.

If you are working through a proxy, you may consider doing something like this:

```
$  git config --global http.proxy proxy:8080
```
This configures git to use the proxy host `proxy` that's listening on port `8080`.

Then, create some location where you want to download your boiler plate into.



----------
# Download of the UI5BP Boiler Plate

We now assume that on any operating system mentioned above, you have a shell window open and it is in a location where you want to have your boiler plate project as a subdirectory. So for the Windows example, you are now with your shell window in `e:\hcp\projects` If you want to now clone e.g. the `ui5pmils` branch of the boiler plate, you do this:

```
$ git clone -b ui5pmils https://github.wdf.sap.corp/I052341/ui5bp.git
Cloning into 'ui5bp'...
remote: Counting objects: 662, done.
remote: Compressing objects: 100% (336/336), done.
remote: Total 662 (delta 337), reused 587 (delta 262)
Receiving objects: 100% (662/662), 2.25 MiB | 444.00 KiB/s, done.
Resolving deltas: 100% (337/337), done.
Checking connectivity... done.
```

If you are using the preconfigured Linux vm, when you open the command shell from the desktop, you are automatically placed into the ui5bp directory and simply do a `git pull github-sap ui5pmils`

If you pull from another location that requires authentication, we have in some cases noticed that you may not be able to enter your password. In this case, you might have to put it on the command line like so:

```
$  git clone -b ui5pmils http://user:pass@git.yourhost.org/git/ui5bp.git
```

----------
# Configuration of the UI5BP Boiler Plate


To configure the boiler plate, change your command shell into the directory that you have cloned it into. So in the above, Windows, example, do:

```
$ cd ui5bp
```

## Windows

The only thing that you need to do to configure the boiler plate on Windows, if you sticked with the default installation (going into some `\hcp` directory in the root of some local drives using all the downloads mentioned above), is to do this:

```
$ cp make.properties.template.windows make.properties
```

## Linux

Similarly, on Linux you do

```
$ cp make.properties.template.linux make.properties
```

## Mac OS X

On Mac OS X, you do

```
$ cp make.properties.template.osx make.properties
```

## Common Configuration

Once you have created a copy of your platform specific `make.properties.template` you edit the file `make.properties` - all changes that you do there are not going to be overwritten by `git` and are not going to be pushed back into the `git` repository should you do a `git push` somewhere at some point in time. The reason for this is that you want to have a place where you can also store things like passwords, without sharing them.

Note that the flip side of this strategy is that should you later pull an update to your application from the boiler plate repository, you may want to check whether there are changes to the configuration variables (new ones, changed ones), i.e., compare your `make.properties` with the potentially updated, platform specific template.

Note also that whatever you write into your `make.properties` will overwrite existing configurations in your `Makefile` - in other words, you should always modify `make.properties` and not modify `Makefile` 

**`Makefile` contains an exhaustive list of all configurations used. If you want to change them, copy them into your `make.properties` and change them there.**

## Download of the lib folder

The `lib` folder is not part of the boiler plate `git` project. Download the file `ui5bp-lib.zip` from the same location that you had downloaded the other zip files, and expand it into the project directory, so that e.g. you get a folder `ui5bp/lib` alongside the `ui5bp/src` folder.

**If you forget to download the `lib` folder, you'll get a large number of compilation errors when you do a `make compile` (see below).** 


----------
# Usage of the UI5BP Boiler Plate

## Getting the Help Screen

Just call `make` to see the help screen:

```
$ make

=============================================
Welcome to this massively informative help...
=============================================

---------------------------------------------
Local Deployment Targets                     
---------------------------------------------

You have the following targets:              

make              Show this help screen      

make clean        Clean the project          

make compile      Compile the project        

make deploy       Deploy the project         

make undeploy     Undeploy the project       

make test         Show configuration         

---------------------------------------------
HCP Deployment Targets                       
---------------------------------------------

make hcpdeploy    Deploy the project to HCP  

make hcpundeploy  Deploy the project to HCP  

make hcpstop      Stop the HCP webapp        

make hcpstart     Start the HCP webapp       

make hcprestart   Restart the HCP webapp     

make hcpstatus    Get the HCP webapp status  

make hcpruntimes  List available HCP runtimes

=============================================

```


If you forgot to create a `make.properties` file, the make process below will fail and hence remind you about this:

```
$ make
Makefile:146: make.properties: No such file or directory
make: *** No rule to make target `make.properties'.  Stop.
```

## Verifying the Configuration

Just do this (not showing the complete output here) to see the configurations that `Make` will use:

```
$ make test
=============================================
Makefile Configuration
=============================================
...
CLASSPATH=../$WEBROOT/WEB-INF/classes:../lib/gson-2.3.1.jar:../lib/log4j.jar:../lib/mysql-connector-java-5.1.7-bin.jar:../lib/ngdbc.jar:../lib/servlet-api.jar:.
TOMCAT=/pgm/java/tomcat/apache-tomcat-8.0.23/
HCP_SDK=/pgm/java/hanacloudsdk/tools/neo.sh
WEBAPP=ui5bp
WEBROOT=WebRoot
SOURCE=
TMPDIR=tmp
=============================================
HCP_HOST=hanatrial.ondemand.com
HCP_ACCOUNT=i052341trial
HCP_USER=i052341
HCP_PASS=
HCP_RUNTIME_VERSION=2
HCP_JAVA_VERSION=8
=============================================
```

## Compile

To compile your code, you just do:

```
$ make compile
```

## Deploy locally

To deploy to your local tomcat (and implicitly compile what has changed), you do

```
$ make deploy
```

On the Windows environment, you may have downloaded (see above) Tomcat which you can start using the shortcut from the `e:\hcp\shortcuts` directory.

Here is an example run on the Linux template vm, where you can see that the process uses rsync to cut down the transfer time:

```
hcp@hcp:~/Documents/projects/ui5bp$ make deploy
make compile
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)
sending incremental file list

sent 225 bytes  received 19 bytes  488.00 bytes/sec
total size is 0  speedup is 0.00
make deploy
/home/hcp/Documents/projects/ui5bp/tmp/ui5bp
sending incremental file list
deleting WEB-INF/lib/ngdbc.jar

...

com.sap.uxap.uxap-uilib_1.28.1.jar

sent 59.45M bytes  received 548 bytes  16.98M bytes/sec
total size is 63.87M  speedup is 1.07
sending incremental file list
WEB-INF/web.xml
WEB-INF/classes/
WEB-INF/lib/

sent 5.48K bytes  received 59 bytes  11.08K bytes/sec
total size is 68.86M  speedup is 12,424.85
```

## Deploy to HCP

Similarly, to do a differential deployment (and prior compilation of changes) to HCP, you do

```
$ make hcpdeploy
```

Here is a sample run; you will be prompted for an HCP password only if you did not specify it in your configuration file:

```
hcp@hcp:~/Documents/projects/ui5bp$ make hcpdeploy
make compile
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)
sending incremental file list

sent 225 bytes  received 19 bytes  488.00 bytes/sec
total size is 0  speedup is 0.00
make hcpdeploy
/home/hcp/Documents/projects/ui5bp/tmp
/home/hcp/Documents/projects/ui5bp/tmp/ui5bp
sending incremental file list
deleting WEB-INF/lib/ngdbc.jar

...

com.sap.uxap.uxap-uilib_1.28.1.jar

sent 59.45M bytes  received 548 bytes  16.98M bytes/sec
total size is 63.87M  speedup is 1.07
updating: WEB-INF/web.xml (deflated 78%)
updating: WEB-INF/lib/ (stored 0%)
updating: WEB-INF/classes/ (stored 0%)

SAP HANA Cloud Platform Console Client



Requesting deployment for:
   application           : ui5bp
   account               : i052341trial
   source                : ../ui5bp.war
   runtime version       : 2
   JVM version           : 8
   host                  : https://hanatrial.ondemand.com
   delta deployment      : yes
   elasticity data       : [1 .. 1]
   SDK version           : 1.79.8.1
   user                  : i052341

Password for your user: 

[Wed Aug 19 12:17:21 CEST 2015] Delta deployment started...
[Wed Aug 19 12:17:23 CEST 2015] Calculating differences....
No differences: the provided source is the same as the deployed content.

[Wed Aug 19 12:17:27 CEST 2015] Processing started........
[Wed Aug 19 12:17:40 CEST 2015] Processing completed in 13.4 s
Warning: The application runtime version (2) is not compatible with version (1.79) of the SDK. The application may not work correctly if it uses features not available in version 2

[Wed Aug 19 12:17:40 CEST 2015] Delta deployment finished successfully
Warning: No compute unit size was specified for the application so size was set automatically to 'lite'.


[Wed Aug 19 12:17:40 CEST 2015] Total time: 23.7 s
```

As you can see, the deployment time was about 24 seconds, which is by orders of magnitude faster than what you will achieve with a full deployment from Eclipse.

Note also, that on Windows, due to how MinGW works, the HCP processes will run in a new window which you can close after the fact.

Depending on your environment, you'll also potentially have to restart your HCP app, which you can likewise do from the makefile:

```
$ make hcprestart
```

The Makefile has many other options, as listed by a simple call of `make` (see above).


