This process is triggered when there are showstopper bugs that need to be deployed to production
before the next release. Generally, a Jira ticket will be created that describes the showstopper 
bug.

1. Create a new hotfix branch

         git checkout master
         git pull --rebase
         git checkout -b release/0.8.16-hotfix1
         git push -u origin release/0.8.16-hotfix1

1. Configure the openboxes-release linked respository to point to the new branch so that commits/merges
   to this branch will be compiled and deployed to the OBNAVSTAGE server automatically. 
1. Review the hotfix ticket OBPIH-1234
1. Create a new branch for your hotfix
  
         git checkout release/0.8.16-hotfix1
         git pull --rebase
         git checkout -b OBPIH-1234

1. Make changes to the code
1. Test the changes locally
1. Commit the changes
   
         git commit -m "OBPIH-1234 Describe the hotfix"

1. Push the changes upstream

         git push -u origin OBPIH-1234  

1. Create a pull request targetting the release/0.8.16-hotfix1 branch
1. Code review
1. Merge code to release/0.8.16-hotfix1
1. Bamboo automatically builds and deploys the new version.
1. Test the changes on OBNAVSTAGE
1. Deploy to production
