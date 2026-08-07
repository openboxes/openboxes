# instances/

Scratch space for specific imports. Everything here (except this README and
`.gitkeep`) is git-ignored, so put each import's working files under its own
subfolder and they'll never be committed:

```
instances/
└── example/
    ├── source.csv   # the source export
    ├── mapping.json # the site-specific column mapping
    └── out/         # generated batches + skipped-records.csv
```

Run the tool against them with relative paths, e.g.:

```bash
groovy ../CsvToInventoryImport.groovy \
    --input instances/example/source.csv \
    --config instances/example/mapping.json \
    --output-dir instances/example/out --clean
```

Prefer keeping site data out of the repo entirely? The scripts take absolute
paths, so you can point them at a scratch dir anywhere on disk instead.
