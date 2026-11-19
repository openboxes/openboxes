const FIXES = [
  {
    file: 'node_modules/react-localize-redux/es/LocalizeProvider.js',
    search: ', INITIALIZE, InitializePayload }',
    replace: ', INITIALIZE }',
  },
  {
    file: 'node_modules/react-virtualized/dist/es/WindowScroller/utils/onScroll.js',
    search: "import { bpfrpt_proptype_WindowScroller } from '../WindowScroller.js';",
    replace: '',
  },
  {
    file: 'node_modules/uniqid/index.js',
    search: "if(typeof __webpack_require__ !== 'function' && typeof require !== 'undefined'){",
    replace: 'if(false){',
  },
];

const stripQuery = (id) => id.split('?')[0];

export default function fixDependencyImportsPlugin() {
  return {
    name: 'fix-dependency-imports',
    transform(code, id) {
      const fix = FIXES.find(({ file }) => stripQuery(id).endsWith(file));
      if (!fix) {
        return null;
      }
      if (!code.includes(fix.search)) {
        throw new Error(`[fix-dependency-imports] Expected code not found in ${fix.file}, the dependency was probably updated`);
      }
      return { code: code.replace(fix.search, fix.replace), map: null };
    },
  };
}
