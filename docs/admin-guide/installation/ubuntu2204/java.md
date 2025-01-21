
OpenBoxes 0.9.x is now built using Grails 3 which requires Java 8. Therefore, we must install a 
Java 8 Runtime Engine (JRE) or Java 8 Virtual Machine (JVM). The JVM is used for development and 
since we're not going to be doing any development on this server, it's safe to install the JRE. 
We also usually choose to install the headless version because we have no need for the graphical 
user interface (GUI) components included in the default packages.

## Install Java 8 package
```shell
sudo apt install openjdk-8-jre-headless
```

## Check Java version
```
java -version
```

You should see output similar to the following
```
java -version
openjdk version "1.8.0_432"
OpenJDK Runtime Environment (build 1.8.0_432-8u432-ga~us1-0ubuntu2~22.04-ga)
OpenJDK 64-Bit Server VM (build 25.432-bga, mixed mode)
```

[//]: # (## Configure Java version [optional])

[//]: # (If you notice a version that is not Java 8, it's possible that the default Java package was already )

[//]: # (installed. You have two options )

[//]: # ()
[//]: # (### Option 1: Uninstall the installed Java package)

[//]: # ()
[//]: # (```shell)

[//]: # (apt list --installed "*jdk*")

[//]: # (```)

[//]: # (You should see a listing similar to the one below.)

[//]: # (```)

[//]: # (Listing... Done)

[//]: # (openjdk-11-jre-headless/jammy-updates,jammy-security,now 11.0.25+9-1ubuntu1~22.04 amd64 [installed,automatic])

[//]: # (openjdk-11-jre/jammy-updates,jammy-security,now 11.0.25+9-1ubuntu1~22.04 amd64 [installed,automatic])

[//]: # (openjdk-8-jre-headless/jammy-updates,jammy-security,now 8u432-ga~us1-0ubuntu2~22.04 amd64 [installed])

[//]: # (```)

[//]: # (To remove the offending package&#40;s&#41;, execute the `apt purge` command including the packages you want )

[//]: # (to purge from your system.)

[//]: # (```shell)

[//]: # (sudo apt purge openjdk-11-jre openjdk-11-jre-headless)

[//]: # (```)

[//]: # ()
[//]: # (### Option 2: Configure the appropriate version)

[//]: # (```)

[//]: # (sudo update-alternatives --config java)

[//]: # (```)

[//]: # (If there's more than one JRE on the system then you'll be prompted to choose one.)

[//]: # (```)

[//]: # (There are 5 choices for the alternative java &#40;providing /usr/bin/java&#41;.)

[//]: # ()
[//]: # (  Selection    Path                                            Priority   Status)

[//]: # (------------------------------------------------------------)

[//]: # (  0            /usr/lib/jvm/java-11-openjdk-amd64/bin/java      1111      auto mode)

[//]: # (  1            /usr/lib/jvm/java-11-openjdk-amd64/bin/java      1111      manual mode)

[//]: # (  2            /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java   1071      manual mode)

[//]: # (  3            /usr/lib/jvm/java-7-oracle/jre/bin/java          1073      manual mode)

[//]: # (* 4            /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java   1081      manual mode)

[//]: # (  5            /usr/lib/jvm/java-9-openjdk-amd64/bin/java       1091      manual mode)

[//]: # ()
[//]: # (Press <enter> to keep the current choice[*], or type selection number: )

[//]: # (```)

[//]: # (If there's only one you'll see a message like this:)

[//]: # (```)

[//]: # (There is only one alternative in link group java &#40;providing /usr/bin/java&#41;: /usr/lib/jvm/zulu-8-amd64/jre/bin/java)

[//]: # (Nothing to configure.)

[//]: # (```)

