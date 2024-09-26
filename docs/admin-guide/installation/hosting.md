
Here are some popular cloud hosting providers where you can provision a virtual machine for OpenBoxes:


<div class="grid cards" markdown>

- :material-aws:{ .lg .middle } __Amazon Web Services (AWS)__
    
    ---
    [AWS EC2](https://aws.amazon.com/ec2/) offers flexible computing resources and a wide range of instance types.
    
    **Recommended Instance:** t3.medium or larger

    [Launching an EC2 Instance](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EC2_GetStarted.html)

- :material-microsoft-azure:{ .lg .middle } __Microsoft Azure__

    ---
    [Azure Virtual Machines](https://azure.microsoft.com/en-us/services/virtual-machines/) provide a variety of Linux and Windows VMs.

    **Recommended Instance:** B2ms or larger
    [Create a Linux VM in the Azure Portal](https://learn.microsoft.com/en-us/azure/virtual-machines/linux/quick-create-portal)

- :material-google-cloud: __Google Cloud Platform (GCP)__

    ---
    [Compute Engine](https://cloud.google.com/compute) offers customizable virtual machines and supports automatic scaling.
    
    **Recommended Instance:** e2-medium or larger

    [Creating a VM Instance](https://cloud.google.com/compute/docs/instances/create-start-instance)

- :material-digital-ocean: __DigitalOcean__

    ---
    [Droplets](https://www.digitalocean.com/products/droplets/) are scalable virtual machines with a simple pricing model.

    **Recommended Plan:** Basic Droplet with 4 GB RAM, 2 vCPUs
    
    [Creating a Droplet](https://docs.digitalocean.com/products/droplets/how-to/create/)

- :fontawesome-brands-linode: __Linode__

    ---
    [Linode](https://www.linode.com/) offers simple, affordable, and powerful cloud computing.

    **Recommended Plan:** Linode 8GB

    [Deploy a Linode](https://www.linode.com/docs/guides/getting-started/)

- :material-tree: __RimuHosting__

    ---
    [RimuHosting](https://rimuhosting.com/) provides flexible and customizable VPS and dedicated servers.

    **Recommended Plan:** VPS with 8 GB RAM, 2 vCPUs, SSD storage

    [Provisioning a VPS on RimuHosting](https://rimuhosting.com/order/startorder.jsp)


</div>






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
