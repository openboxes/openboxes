# Contributor Guide
Welcome to OpenBoxes! We're incredibly grateful that you've expressed interest in contributing.

For the most up-to-date information on how to contribute to the project, please see our [contributor guide](https://openboxes.gitbook.io/contributor-guide/).

[![Contributor Guide](https://img.shields.io/badge/contributor%20guide-grey?style=for-the-badge&logo=gitbook&logoColor=white)](https://openboxes.gitbook.io/contributor-guide/)

As an open source project, we rely on the generous contributions of the community to help us achieve our goals of making a supply chain tool that anyone can use, regardless of their socio-economic status. Every little bit helps, so whether you're an open source veteran or just getting started, we welcome you with open arms! Thank you for supporting open source!

Not a software engineer? Don't worry! There are plenty of ways to contribute to OpenBoxes that don't involve writing code. 

## Software Development
If you are not a core contributor (ie if you don't have permission to push directly to the repository), any code changes will need to be made in a [fork of this repository](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo). Once you've pushed your code change to a branch in your fork, you can make a pull request from that branch back into the openboxes repository.

We do our active development out of the 'develop' branch, so make sure to branch from 'develop' when making your changes.

### Feature Development and Bug Fixing
The bulk of the feature development in the core openboxes repository is driven internally by Partners In Health (PIH). This is why you'll often see pull requests referencing tickets from a PIH-only Jira board.

We're striving to be more transparent and community-oriented with our development, and so any [GitHub issues](https://github.com/openboxes/openboxes/issues) that we have in the repository are welcome to be worked on by the community. Please make sure to communicate what tasks you're picking up (so that someone else doesn't work on it at the same time), and don't be afraid to ask for clarification!

### Writing Automation Tests
A primary goal of the project is increasing our automation test coverage. If you're unsure where to start, feel free to reach out to us on [Slack](http://slack-signup.openboxes.com), and we can help you pick a feature to test.

Our current backend test coverage is: [![codecov](https://codecov.io/gh/openboxes/openboxes/branch/develop/graph/badge.svg?token=Ki6DtbxXok)](https://codecov.io/gh/openboxes/openboxes)

To run frontend unit tests:
```
npm test
```

To run backend unit tests:
```
grails test-app -unit
```

For instructions on how to run backend API tests, see the [integration test README file](/src/integration-test/README.md).

We also have an end-to-end (E2E) test suite written in Playwright. See the [e2e repository](https://github.com/openboxes/openboxes-e2e) for instructions on how to contribute.

## Writing Documentation
We have multiple sources of documentation:
- [OpenBoxes Confluence](https://openboxes.atlassian.net/wiki/spaces/OBW/overview): Contributor guide (technical and non-technical) & non-technical user guide
- [OpenBoxes MkDocs](https://docs.openboxes.com/en/latest/): Technical guide for administrative users about how to install, deploy, and configure the app
- [OpenBoxes GitBook](https://openboxes.gitbook.io/contributor-guide/): Guide for anyone, techinical or non-technical, wishing to contribute to the project

If you'd like to make changes on the Confluence, please create an account in Confluence then request access.

If you'd like to make changes to the MkDocs, you can do so by creating a pull request on the files in the `/docs` folder. The changes will be deployed automatically when the pull request is merged. See the [docs README](/docs/README.md) for more information.

If you'd like to make changes to the GitBook, you can do so by creating a pull request in the [openboxes-docs](https://github.com/openboxes/openboxes-docs) repository.

## Suggesting New Features
If you find yourself wishing for a feature that doesn't exist in OpenBoxes, you are probably not alone. Many of the features that OpenBoxes has today have been added because our users saw the need.

We encourage you to start a new [discussion topic](https://github.com/openboxes/openboxes/discussions/categories/ideas) that outlines the feature you would like to see, why you need it, and how it should work.

## Translating the App
There are instances of OpenBoxes running all over the world, but there are many languages that we're missing essential localization for. If you speak another language, we'd greatly appreciate your help in translating the app!

We use [Crowdin](https://crowdin.com/) for handling all of our translations, so if you're interested in contributing new translations, please [join our Crowdin project](https://crowdin.com/project/openboxes).

## Reporting Bugs
Even if you don't have the capacity to provide a fix yourself, reporting bugs is a helpful way to help us improve stability of the app.

If you've found a bug in OpenBoxes, please [open an issue](https://github.com/openboxes/openboxes/issues/new) so that someone can investigate.

If you need developer support, take a look at our [support guide](/SUPPORT.md)

## Contributing Financially
Financial contributions help to sustain our community by covering expenses like hosting fees and allowing us to bring on new developers or fund new projects. Every little bit helps, so thank you so much for your support! [See how you can contribute](https://opencollective.com/openboxes/contribute).


## Have Other Ideas?

Are you looking to contribute in a way that is not listed above, we're eager to hear about it! Please create a [discussion topic](https://github.com/openboxes/openboxes/discussions/categories/ideas) here on GitHub outlining your ideas, or chat with us directly on [Slack](http://slack-signup.openboxes.com). Thank you so much for your interest!
