# Release Process
Created Thursday 21 March 2019

## Resources
* https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
* https://gitversion.readthedocs.io/en/latest/git-branching-strategies/gitflow-examples/
* https://gitversion.readthedocs.io/en/latest/git-branching-strategies/gitflow/
* https://danielkummer.github.io/git-flow-cheatsheet/
* https://medium.com/hard-work/gitflow-release-hotfix-bddee96fc5c3
* https://www.fredonism.com/a-practical-take-on-gitflow-and-semantic-versioning

## Overview
* Create JIRA version (usually done while planning the release before development begins)
* Create release branch
* Deploy to staging server (OBNAVSTAGE)
* QA Process
* Finalize Release

### Create JIRA version
This step usually occurs before development has begun on a new release as the version number 
`fixVersion` is usually attached to a ticket to let us know under what release that ticket should be 
included. However, if you haven't created a new JIRA version by the time you're ready to release 
here's one way to do it. 

![Create JIRA Release](../img/create-jira-release.png "Create JIRA Release")

To create a version in JIRA:

1. Go to the OpenBoxes PIH project
1. Click on Releases
1. Click on Create Version
1. Enter version number, start date, end date, and comment
1. Click Save

You can also create a release from the Kanban board (when releasing closed tickets) or in the 
Agile (Scrum) board (see Epics and Versions sidebar).
 
### Create release branch
Once you have completed all of the tickets for the new version (which could take several sprints) 
and you have merged all pull requests (PRs) for these tickets, you're ready to create a release 
branch. This branch acts as a container for all changes that should be released for this version
as well as any last minute bug fixes that need to be made before the release is finalized.

1. First of all, you'll need to checkout the develop branch and make sure you have all of the latest
changes.

        git checkout develop
        git pull --rebase

1. Create new release branch off develop

        git checkout -b release/0.8.9
        git push --set-upstream origin release/0.8.9

1. Change version number in application.properties

        app.version=0.8.9

1. Commit version change

        git commit -am "bumped app version to 0.8.9"

### Change release branch on Bamboo

1. Go to Linked Repositories
1. Select openboxes-release
1. Change the Branch to release/0.8.9

![Change Release Branch](../img/bamboo-change-release-branch.png "Change Release Branch")

Bamboo should automatically trigger a build for OBNAVSTAGE, but if that doesn't happen within a 
few minutes just go to the build plan page and trigger it manually.
<http://bamboo.pih-emr.org:8085/browse/OPENBOXES-SDONS>

### Testing Release
Once the latest release branch has been deployed to OBNAVSTAGE we can start the QA pass. During 
this process we might add a few Bug tickets, but there should be no new features. Developers 
should either create branches off of release/0.8.9 or commit directly to the release branch.

### Finalize Release
Finalizing the release involves making the following changes to JIRA and Github.

Once the QA pass has been completed and there are no more bugs, we can start to finalize the 
release. 

1. Close any tickets that have been completed
1. Move remaining tickets to the next sprint
1. Remove or change the fixVersion of any open tickets (0.8.9 -> 0.8.10) 
1. Go to Agile board > Active Sprints 
1. Close the current sprint using the current date as the End Date.
1. Go to Agile Board > Complete Sprint
1. Go to Kanban Board > Release 
1. Go to Versions page 
1. Merge 0.8.9-kanban1 into 0.8.9
1. Git > Merge release/0.8.9 into master

        git checkout master
        git merge release/0.8.9
        git push
        
1. Git > Tag release/0.8.9 (http://docs.openboxes.com/en/latest/developer-guide/tagging/)

        git tag -a v0.8.9 -m 'Release 0.8.9' <commit-sha>
        git push --tags

1. Git > Merge master into develop

        git checkout develop
        git merge master
        # fix conflicts
        git push
        
1. Git > Bump version

        # bump version number in application.properties
        git add application.properties
        git commit -m "bumped app version to 0.8.10-SNAPSHOT"
        git push

1. Git > Delete release/0.8.9 branch

        git branch -d release/0.8.9

1. Github > Create new release with release notes 
1. Bamboo > Download latest WAR from Daily Stable Build (master)
1. GitHub > Upload WAR to release 
1. Publish Release Notes to openboxes.com
1. Bamboo > Change openboxes-release back to master 
