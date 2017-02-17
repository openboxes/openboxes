
# Syncing your fork

## Resources

* [Configuring a Remote for a Fork](https://help.github.com/articles/configuring-a-remote-for-a-fork/)
* [Syncing a Fork](https://help.github.com/articles/syncing-a-fork/)

## List your remote repositories

        $ git remote -v
        origin  https://github.com/YOUR_USERNAME/openboxes.git (fetch)
        origin  https://github.com/YOUR_USERNAME/openboxes.git (push)
        
## Add the upstream remote 

        $ git remote add upstream https://github.com/openboxes/openboxes.git
               
## Verify upstream remote

        $ git remote -v
        origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (fetch)
        origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (push)
        upstream  https://github.com/ORIGINAL_OWNER/ORIGINAL_REPOSITORY.git (fetch)
        upstream  https://github.com/ORIGINAL_OWNER/ORIGINAL_REPOSITORY.git (push)
        
## Pull changes into your fork as upstream/master

        $ git fetch upstream
        remote: Counting objects: 4165, done.
        remote: Compressing objects: 100% (22/22), done.
        remote: Total 4165 (delta 1920), reused 1928 (delta 1918), pack-reused 2225
        Receiving objects: 100% (4165/4165), 1.83 MiB | 329.00 KiB/s, done.
        Resolving deltas: 100% (2728/2728), completed with 583 local objects.
        From https://github.com/openboxes/openboxes
         * [new branch]      develop    -> upstream/develop
         * [new branch]      feature/94-upgrade-to-grails-2.5.x -> upstream/feature/94-upgrade-to-grails-2.5.x
         * [new branch]      hotfix/100-improve-performance-qoh-calculation -> upstream/hotfix/100-improve-performance-qoh-calculation
         * [new branch]      hotfix/161-support-additional-languages -> upstream/hotfix/161-support-additional-languages
         * [new branch]      hotfix/163-add-health-endpoint-for-monitoring -> upstream/hotfix/163-add-health-endpoint-for-monitoring
         * [new branch]      hotfix/165-replace-inventory-with-adjustment -> upstream/hotfix/165-replace-inventory-with-adjustment
         * [new branch]      hotfix/22-override-grails-config-locations -> upstream/hotfix/22-override-grails-config-locations
         * [new branch]      master     -> upstream/master
         * [new branch]      user-guide-docs -> upstream/user-guide-docs

## Checkout the branch you want to sync 
This will likely either be `develop` or `master`. 
        
        $ git checkout master
        
## Merge upstream/master into you local master
        
        $ git merge upstream/master

## Push changes up to your repository
        
        $ git push
        
## Profit
![Profit](../img/profit.jpg)
