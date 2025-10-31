
Once the necessary resources are provisioned on your server, we will connect to the server
and update the system to make sure the latest software and security updates are applied.

## Connect to Server

```shell
ssh <username>@<server-ip>
```
After connecting to the server, you should see a message like this. 
```title="Ubuntu 22.04 Welcome Message"
Welcome to Ubuntu 22.04.5 LTS (GNU/Linux 5.15.0-124-generic x86_64)

 * Documentation:  https://help.ubuntu.com
 * Management:     https://landscape.canonical.com
 * Support:        https://ubuntu.com/pro

 System information as of Fri Jan 17 17:48:47 UTC 2025

  System load:  0.07               Processes:             109
  Usage of /:   10.6% of 77.35GB   Users logged in:       0
  Memory usage: 82%                IPv4 address for eth0: 104.248.57.113
  Swap usage:   0%                 IPv4 address for eth0: 10.10.0.6

Expanded Security Maintenance for Applications is not enabled.

15 updates can be applied immediately.
To see these additional updates run: apt list --upgradable

4 additional security updates can be applied with ESM Apps.
Learn more about enabling ESM Apps service at https://ubuntu.com/esm


*** System restart required ***
Last login: Fri Dec 27 20:54:55 2024 from 162.192.16.32
```

## Update Package Repository
You may have already executed this command in the previous step, but this command is used to update 
the system package list with the latest available software. You should execute this any time you're 
installing packages from the APT repository.

```shell
sudo apt update
```

```shell
Hit:1 https://repos-droplet.digitalocean.com/apt/droplet-agent main InRelease
Get:2 http://security.ubuntu.com/ubuntu jammy-security InRelease [129 kB]
Hit:3 http://mirrors.digitalocean.com/ubuntu jammy InRelease
Hit:4 http://mirrors.digitalocean.com/ubuntu jammy-updates InRelease
Hit:5 http://mirrors.digitalocean.com/ubuntu jammy-backports InRelease
Fetched 129 kB in 4s (32.6 kB/s)                         
Reading package lists... Done
Building dependency tree... Done
Reading state information... Done
89 packages can be upgraded. Run 'apt list --upgradable' to see them.
```

## Upgrade Packages
If you see a message similar to the following at the bottom of the previous step it means that 
there are installed packages on the system that have available updates in the package repository.
```shell
89 packages can be upgraded. Run 'apt list --upgradable' to see them.

```
To upgrade these packages, execute the following command:
```shell
sudo apt upgrade
```


## Reboot Server

Finally, we recommend that you to reboot your server to ensure that any updates (primarily kernel
and security updates) are applied properly. It's also advisable to reboot if you see the following 
line in the welcome message when you SSH'd into the server.
```
``*** System restart required ***
```
To reboot your server, execute the following command:
```shell
sudo reboot
```

!!! note 
    It might take a few minutes before you are able to SSH back into the server. 

