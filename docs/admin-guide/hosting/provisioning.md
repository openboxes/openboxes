The following is a guide for provisioning a Virtual Machine using a generic cloud provider.

## Choose a Hosting Provider
Select one of the [hosting providers](../installation/hosting.md) based on your budget, region, and preference.

## Create Account and Log In
Create an account and log in to the provider’s console.

## Select a Virtual Machine Instance Type
Choose an instance type or plan that meets the hardware requirements specified above.

## Configure the VM
- **Operating System:** Choose a Linux distribution (e.g., Ubuntu 22.04 LTS).
- **Region:** Select a data center region close to your users.
- **Storage:** Allocate at least 50 GB of SSD storage.

## Networking
Configure security groups or firewalls to allow inbound connections on necessary ports (e.g., 80, 443, 3306, 8080).

## Launch the Virtual Machine
Follow the provider's instructions to launch the VM. Take note of the VM's public IP address and SSH access details. 

## Access the Virtual Machine
Use SSH to connect to your VM.  

```bash
ssh username@your_vm_ip_address
```

!!!tip

     You can usually add your public key to the hosting provider configuration for passwordless 
     access to the server. If you did not configure a public key then you will likely be prompted for a password.

## Post-Deployment Configuration
Update system packages and install necessary dependencies as outlined in the installation guide.
    ```shell
    sudo apt update
    sudo apt upgrade
    sudo reboot    
    ```

## Conclusion

Once your virtual machine is provisioned, you are ready to install dependencies, configure basic settings, and deploy 
the application. This guide provides a starting point for setting up the infrastructure needed to host the application effectively.

For further assistance and installation details, please refer to the [OpenBoxes Installation Guide](../installation/index.md).

   

If you need more detailed instructions for a specific provider or have any other questions, feel free to ask!
