import fs from 'node:fs';
import path from 'node:path';

import template from 'lodash/template.js';

const grailsResource = (dir, file) => `\${resource(dir: '${dir}', file: '${file}')}`;

const findEntryChunk = (bundle, entry) => {
  const entryChunk = Object.values(bundle)
    .find((chunk) => chunk.type === 'chunk' && chunk.isEntry && chunk.facadeModuleId === entry);
  if (!entryChunk) {
    throw new Error(`[grails-gsp] Entry chunk for ${entry} not found in bundle`);
  }
  return entryChunk;
};

const findEntryCss = (entryChunk) => {
  const cssFiles = [...entryChunk.viteMetadata.importedCss];
  if (cssFiles.length !== 1) {
    throw new Error(`[grails-gsp] Expected exactly one CSS file for entry, got: ${cssFiles.join(', ')}`);
  }
  return cssFiles[0];
};

const findCssEntryAsset = (bundle, cssEntry) => {
  const asset = Object.values(bundle)
    .find((file) => file.type === 'asset' && file.originalFileNames?.includes(cssEntry));
  if (!asset) {
    throw new Error(`[grails-gsp] CSS asset for ${cssEntry} not found in bundle`);
  }
  return asset;
};

export default function grailsGspPlugin({
  entry, vendorCss, templatePath, pages,
}) {
  const resolved = { outDir: '', root: '' };

  return {
    name: 'grails-gsp',
    apply: 'build',
    configResolved(config) {
      resolved.outDir = config.build.outDir;
      resolved.root = config.root;
    },
    writeBundle(_, bundle) {
      const entryChunk = findEntryChunk(bundle, entry);
      const vendorCssAsset = findCssEntryAsset(bundle, path.relative(resolved.root, vendorCss));
      const resourceDir = path.basename(resolved.outDir);
      const render = template(fs.readFileSync(templatePath, 'utf-8'));
      const assetParams = {
        jsSource: grailsResource(resourceDir, entryChunk.fileName),
        cssSource: grailsResource(resourceDir, findEntryCss(entryChunk)),
        vendorCssSource: grailsResource(resourceDir, vendorCssAsset.fileName),
        scriptType: 'module',
      };

      pages.forEach(({ output, params }) => {
        fs.mkdirSync(path.dirname(output), { recursive: true });
        fs.writeFileSync(output, render({ ...assetParams, ...params }));
      });
    },
  };
}
