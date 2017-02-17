# Tagging a release

## Find the current build number in the footer of the page for the desired version of the application:
```
Build Number: v0.5.0-329-gc1b3544 
```
In this case, the tag v0.5.0 is out-of-date, so we want to update this to v0.5.1. We first need to find the commit related to this build number (e.g. `c1b3544`).
You can view the commit log to view the history or you might need to browse through old commits on github to double check that it's correct.

```
$ git log --pretty=oneline | grep c1b3544
```

## Create tag release
Once you've determined that this is the correct commit and you're ready to create a new tag, run the following command:
```
$ git tag -a v0.5.1 -m 'Release 0.5.1' c1b3544
```

## Push tags to git remote repo
Then you can push this (and all other tags) to your repository to share:
```
$ git push --tags
```

## Additional reading
See the following article for more informaton
http://git-scm.com/book/en/Git-Basics-Tagging