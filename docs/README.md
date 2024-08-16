# Getting Started 

## Prerequisites
* python3 
* pip

## Installation Instructions

1. Verify **python 3** and **python-pip** is installed. 
    ```sh
    python3 --version
    pip --version
    ```

1. Clone the repository
    ```shell
    git clone git@github.com:openboxes/openboxes.git
    cd openboxes
    ```

1. Install required dependencies
    ```shell
    pip install -r docs/requirements.txt
    ```

1. Run the mkdocs server
    ```shell
    mkdocs serve
    ```
1. Open docs in browser

[<img src="./assets/img/mkdocs.png">](http://localhost:8000)

## Contributing Instructions

1. Create a new branch for your documentation changes
```shell
git checkout -b docs/1234-fix-typo
```

> [!TIP]
>   Branching naming should follow convention above. If possible, use the GitHub issue number 
>   and summary in your branch name.

2. Make documentation changes in an editor of your choice
3. Commit changes to branch with meaningful commit message 
```
#1234: fixed typo in docs
```
4. Push changes to GitHub
5. Create a pull request
6. Assign `jmiranda` as a reviewer
