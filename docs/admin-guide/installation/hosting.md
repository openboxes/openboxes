# Hosting
Whether you plan to install OpenBoxes on-premise or in the cloud, the installation instructions should be relatively 
similar. 

## Cloud Hosting Providers
Here are a few recommended options for hosting.

### Recommended

The following 

| Hosting Provider / Instance Type                                                                 | Memory | vCPU | Disk | Monthly Cost | Comments                           |
|--------------------------------------------------------------------------------------------------| -- | --- | --- |--------------|------------------------------------|
| [Digital Ocean](https://m.do.co/c/da4712a483b4) Droplet                                          | 4GB | 2 | 80GB | $20          | Very easy to get started.          |
| [RimuHosting](https://rimuhosting.com/vps-servers?r=d91cf2bcee5396e721c700ded9a22481) Custom VPS |  4GB | 8 | 50GB | $15 | Highly recommended, great support. |


[//]: # (### Various Others)

[//]: # ()
[//]: # (| Hosting Provider                                                     | Instance Type | Memory | vCPU | Disk | Monthly Cost | Comments                      |     |)

[//]: # (|----------------------------------------------------------------------| ------ | --- | --- | --- |--------------|------------------------------------| --- |)

[//]: # (| [Amazon Web Services EC2]&#40;http://www.ec2instances.info/&#41; | t3.medium | 4GB | 2 | 8GB | $30          | |)

[//]: # (| [Azure VM]&#40;https://cloud.google.com/compute/pricing&#41;                 | B2S | 4GB | 2 | 8GB | $30          ||)

[//]: # (| [Google Compute Engine]&#40;https://cloud.google.com/compute/pricing&#41;    | n1-standard-1 | 3.75GB | 1 | 30GB | $25          | |)

[//]: # (| [Google Compute Engine]&#40;https://cloud.google.com/compute/pricing&#41;    | n1-standard-2 | 7.5GB | 2 | 30GB | $50          | |)

[//]: # (| [Linode]&#40;https://www.linode.com/pricing&#41;                             | Linode 4GB | 4GB | 2 | 80GB | $20          ||)

[//]: # (| [Linode]&#40;https://www.linode.com/pricing&#41;                             | Dedicated 4GB | 4GB | 2 | 25GB | $30          ||)


[//]: # (NOTE: AWS has a free-tier that includes a free year of 750 hours per month for t2.micro EC2 instances &#40;as well as other )

[//]: # (services&#41;. It's a great deal it if you're not going to be using OpenBoxes too heavily. Unfortunately, keeping a )

[//]: # (Java-based web application like OpenBoxes happy on a t2.micro &#40;1GB of RAM&#41; is not easy. You may need to reduce the heap )

[//]: # (size and permgen memory allocated to Tomcat to something minimal &#40;see Installing Tomcat page&#41;.)


!!! note
    Please let us know if we missed a good hosting option.

## On-Premise
Installing OpenBoxes on-premise requires a bit of work to install the appropriate Ubuntu version on a rack-mounted 
server, desktop, or laptop that you've designated as your server. Our installation docs will not describe how to install 
Ubuntu Desktop or Server, so you'll need to consult Ubuntu docs. Here are a few tutorials that might be helpful.

* [Ubuntu Desktop](https://tutorials.ubuntu.com/tutorial/tutorial-install-ubuntu-desktop)
* [Ubuntu Server](https://tutorials.ubuntu.com/tutorial/tutorial-install-ubuntu-server)

Once Ubuntu is installed, you can continue to the next step (Installing Ubuntu dependencies).

!!! note
    Contact us if you'd like to discuss what it would take to host OpenBoxes in the cloud and on-premise. This can 
    be useful if Internet and power are unreliable as well as if you have teams distributed across multiple facilities 
    or in multiple countries.
