Once you have completed all of the tickets for the new version (which could take several sprints) 
and you have merged all pull requests (PRs) for these tickets, you're ready to create a release 
branch. This branch acts as a container for all changes that should be released for this version
as well as any last minute bug fixes that need to be made before the release is finalized.

### Create Release Branch

1. First of all, you'll need to checkout the develop branch and make sure you have all of the latest
changes.

        git checkout develop
        git pull --rebase

1. Create new release branch off develop

        git checkout -b release/x.y.z
        git push --set-upstream origin release/x.y.z

1. Change version number in application.properties

        app.version=x.y.z

1. Commit version change

        git commit -am "bumped app version to x.y.z"

### Configure Continuous Integration

1. Go to Linked Repositories
1. Select openboxes-release
1. Change the Branch to release/x.y.z

![Change Release Branch](../../img/bamboo-change-release-branch.png "Change Release Branch")

Bamboo should automatically trigger a build for OBNAVSTAGE, but if that doesn't happen within a 
few minutes just go to the build plan page and trigger it manually.
<http://bamboo.pih-emr.org:8085/browse/OPENBOXES-SDONS>

