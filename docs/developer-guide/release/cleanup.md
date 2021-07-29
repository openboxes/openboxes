
1. On Bamboo, change openboxes-release back to master 
   
    ![Change Linked Repo](../../img/bamboo-change-linked-repo-to-master.png)

1. Merge master into develop

        git checkout develop
        git merge master
        # fix conflicts
        git push
        
1. Bump version (if major/minor release)

        # bump version number in application.properties
        git add application.properties
        git commit -m "bumped app version to x.y.z+1-SNAPSHOT"
        git push

1. Delete release/x.y.z branch

        git branch -d release/x.y.z

1. ... everybody do your share

      ![test](https://thumbs.gfycat.com/HilariousOldfashionedAnglerfish-max-1mb.gif)
