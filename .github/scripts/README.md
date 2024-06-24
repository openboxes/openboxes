# GitHub Actions Scripts

These scripts are called by [our workflows](../workflows) via the [`github-script`](https://github.com/actions/github-script) action. When creating a new workflow that uses `github-script`, make sure to checkout the repo first so the script is accessible.

```yaml
steps:
  - name: Checkout Code
    uses: actions/checkout@v4

  - name: Validate Pull Request Title
    uses: actions/github-script@v7
    with:
      script: |
        const action = require('${{ github.workspace }}/.github/scripts/<SCRIPT NAME HERE>.js')
        await action({github, context, core})
```

The script should export an asynchronous default function:

```js
module.exports = async ({ github, context, core }) => {
  // CODE HERE
};
```

Look at the existing scripts for examples and check the [`octokit.js` documentation](https://octokit.github.io/rest.js/v20) for GitHub API details.
