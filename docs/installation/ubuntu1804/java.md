# Installing Java 7
You must install a Java 7 JRE/JDK. Unfortunately, the APT Repository on Ubuntu 16.04 does not include a version 
of the Java 7 JRE or JDK so we'll need to do some work to get this working. I would personally recommend 
**Option 2** below.


!!! note
    For the time being, you **MUST** use Java 7! The version of Grails that we're using does not support Java 8+ or 
    beyond. We are working on upgrading to the latest version of Grails, but we're still several months away from 
    completing that migration. 

!!! important
    In case it wasn't clear from the box above this means you should NOT attempt to install the `default-jre`, 
    `openjdk-8-jre`, or `openjdk-9-jre` packages from the APT repository. 
    
    Grails 1.3.9 does not support Java 8+ so Tomcat will fail to deploy OpenBoxes. 
    When it fails, you will send an email to 
    [support@openboxes.com](mailto:support@openboxes.com) asking why the installation failed. When we receive your email, 
    we will point you back to the little blue box that you ignored. Don't be that person.

## Choose your choice
See the following [StackExchange question](https://askubuntu.com/questions/761127/how-do-i-install-openjdk-7-on-ubuntu-16-04-or-higher) for more details 

* [Option 1: Manual Installation](https://askubuntu.com/a/803616) **(not recommended)** You can certainly use this solution, but I had some trouble getting it to work so I would not recommend it. See 

* [Option 2: Automatic Installation](https://askubuntu.com/a/803616) **(recommended)** 
    I would recommend this solution. It's a little more complex but seems to behave as you'd expect when installing 
from the APT repository.

* [Option 3: Oracle Java](https://askubuntu.com/a/761527) **(not not recommended)**[^1]
This would probably be a recommended option, but Oracle ended public support for JDK 7 along time ago.

* [Option 4: Install from Zulu Linux](https://askubuntu.com/a/840945) **(not not recommended)**[^1]
This seems like an ok option if you're comfortable manually install Debian packages - it's pretty straightforward.

* [Option 5: Install from an unsupported package maintainer](https://askubuntu.com/a/761527) **(not recommended)**
This is probably the easiest solution, but also the least secure. Your friends will call you names.

* [Option 6: Use docker](https://askubuntu.com/a/1059859) **(recommended)** I would recommend using docker, but unfortunately do not have any instructions to share at this time.

!!! danger
    Do not use Option 5 in production. Do not use this solution unless you are in a rush and either plan to do it 
    properly when you have time or you plan to throw away this server after evaluating OpenBoxes. 

[^1]: You read that correctly. I wrote "not not recommended", which is a double negative meaning 
"we're not recommending it, but we're also not not recommending it."

## Configure Java version 
```
$ sudo update-alternatives --config java
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
Nothing to configure.
```

## Check Java version
Make sure you see something like `java version 1.7.0_xyz`.
```
$ java -version
java version "1.7.0_161"
OpenJDK Runtime Environment (IcedTea 2.6.12) (7u161-2.6.12-1)
OpenJDK 64-Bit Server VM (build 24.161-b01, mixed mode)
```

