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

* [Option 1: Install from Zulu Linux](https://askubuntu.com/a/840945) **(recommended)**
This seems like an ok option if you're comfortable manually install Debian packages - it's pretty straightforward.

* [Option 2: Automatic Installation](https://askubuntu.com/a/803616) **(no longer recommended)** 
    I would recommend this solution. It's a little more complex but seems to behave as you'd expect when installing 
from the APT repository.

* [Option 3: Manual Installation](https://askubuntu.com/a/803616) **(not recommended)** You can certainly use this solution, but I had some trouble getting it to work so I would not recommend it. See 

* [Option 4: Oracle Java](https://askubuntu.com/a/761527) **(not not recommended)**[^1]
This would probably be a recommended option, but Oracle ended public support for JDK 7 along time ago.

* [Option 5: Install from an unsupported package maintainer](https://askubuntu.com/a/761527) **(not recommended)**
This is probably the easiest solution, but also the least secure. Your friends will call you names.

* [Option 6: Use docker](https://askubuntu.com/a/1059859) **(recommended)** I would recommend using docker, but unfortunately do not have any instructions to share at this time.

!!! danger
    Do not use Option 5 in production. Do not use this solution unless you are in a rush and either plan to do it 
    properly when you have time or you plan to throw away this server after evaluating OpenBoxes. 

[^1]: You read that correctly. I wrote "not not recommended", which is a double negative meaning 
"we're not recommending it, but we're also not not recommending it."

## Option 1: Zulu Linux
We're going to go with Option 1 since it appears the be the most straightforward approach at this point in time.

1. Go to <https://www.azul.com/>
1. Click on Downloads > Zulu Community
1. Locate the Zulu Community Linux Repositories section
1. Click the Apt-get link
1. Follow the instructions (the instructions below might not match what you see so please follow the instructions yourself)

For example, here's what I had to do to install Zulu JDK 7 on Ubuntu 18.04
```
$ sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9
$ sudo apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main'
$ sudo apt-get update
$ sudo apt-get install zulu-7
```

## Configure Java version 
```
$ sudo update-alternatives --config java
```
If there's more than one JRE on the system then you'll be prompted to choose one.
```
There are 5 choices for the alternative java (providing /usr/bin/java).

  Selection    Path                                            Priority   Status
------------------------------------------------------------
  0            /usr/lib/jvm/java-11-openjdk-amd64/bin/java      1111      auto mode
  1            /usr/lib/jvm/java-11-openjdk-amd64/bin/java      1111      manual mode
  2            /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java   1071      manual mode
  3            /usr/lib/jvm/java-7-oracle/jre/bin/java          1073      manual mode
* 4            /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java   1081      manual mode
  5            /usr/lib/jvm/java-9-openjdk-amd64/bin/java       1091      manual mode

Press <enter> to keep the current choice[*], or type selection number: 
```
If there's only one you'll see a message like this:
```
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/zulu-7-amd64/jre/bin/java
Nothing to configure.
```

## Check Java version
```
$ java -version
```

You should see output similar to the following
```
openjdk version "1.7.0_242"
OpenJDK Runtime Environment (Zulu 7.34.0.5-CA-linux64) (build 1.7.0_242-b7)
OpenJDK 64-Bit Server VM (Zulu 7.34.0.5-CA-linux64) (build 24.242-b7, mixed mode)
```
