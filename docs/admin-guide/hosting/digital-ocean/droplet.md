# Digital Ocean 

[//]: # (## Referral)

[//]: # ([![DigitalOcean Referral Badge]&#40;https://web-platforms.sfo2.cdn.digitaloceanspaces.com/WWW/Badge%201.svg&#41;]&#40;https://www.digitalocean.com/?refcode=da4712a483b4&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge&#41;)

## Instructions

### Create a Droplet Manually
The easiest way to create a virtual machine is to create a Digital Ocean droplet.



!!! todo

      include discussion on the [digital ocean referral program](https://www.digitalocean.com/referral-program)
      
   

### Step 1: Create a DigitalOcean Account
1. If you don't already have one, [sign up](https://cloud.digitalocean.com/login?refcode=da4712a483b4&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge) for a DigitalOcean account.

### Step 2: Create a New Droplet
1. Log in to your DigitalOcean account.
2. Click on the **Create** button in the top right corner and select **Droplets**.

### Step 3: Choose an Image
1. Select **Ubuntu** as the operating system.
2. Choose **Ubuntu 22.04 (LTS)**.

### Step 4: Choose a Plan
1. Select the droplet plan according to your needs. For OpenBoxes, the basic plan with 2GB of RAM and 1 CPU should be sufficient for small to medium-sized installations.
2. For production use, consider higher plans based on your expected load.

### Step 5: Choose a Datacenter Region
1. Select a datacenter region closest to your location or where most of your users are located.

### Step 6: Select Additional Options
1. Enable **IPv6** if required.
2. Enable **Monitoring** for better insights into your droplet's performance.

### Step 7: Add SSH Keys
1. Add your SSH key for secure access to the droplet. If you don't have an SSH key, you can generate one using the following command on your local machine:
    ```bash
    ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
    ```
2. Follow the prompts to save the key and then add the public key to your DigitalOcean account.

### Step 8: Finalize and Create
1. Choose a hostname for your droplet, such as `openboxes-server`.
2. Click **Create Droplet**.

### Step 9: Access Your Droplet
1. Once the droplet is created, note the IP address.
2. SSH into your new droplet using the following command:
    ```bash
    ssh root@your_droplet_ip
    ```
