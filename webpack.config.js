const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');

// this value is tightly coupled with src/js/index.jsx::__webpack_public_path__
const WEBPACK_OUTPUT = path.resolve(ROOT, 'main/webapp/webpack');
const TEMPLATES = path.resolve(ROOT, 'assets');
const GRAILS_VIEWS = path.resolve(__dirname, 'grails-app/views');
const COMMON_VIEW = path.resolve(GRAILS_VIEWS, 'common');
const RECEIVING_VIEW = path.resolve(GRAILS_VIEWS, 'partialReceiving');

const ESLintPlugin = require('eslint-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const FileManagerPlugin = require('filemanager-webpack-plugin');

module.exports = {
    entry: {
      app: `${SRC}/index.jsx`,
    },
    optimization: {
      splitChunks: {
        cacheGroups: {
          vendor: {
            name: 'vendors',
            test: /[\\/]node_modules[\\/]/,
          },
        },
      },
    },
    output: {
      path: WEBPACK_OUTPUT,
      /*
       * Don't set publicPath here, because it's hard to know the
       * application context at bundle time. Instead, we rely on
       * __webpack_public_path__, specified in src/js/index.jsx, q.v.
       */
      filename: 'bundle.[hash].js',
      chunkFilename: 'bundle.[hash].[name].js',
    },
    stats: {
      colors: false,
    },
    plugins: [
      new ESLintPlugin({}),
      new FileManagerPlugin({
        events: {
          onStart: {
            delete: [
              WEBPACK_OUTPUT,
            ],
          },
        },
      }),
      new MiniCssExtractPlugin({
        filename: 'bundle.[hash].css',
        chunkFilename: 'bundle.[hash].[name].css',
      }),
      new OptimizeCSSAssetsPlugin({}),
      /*
       * We use the HtmlWebpackPlugin to render templates to .gsp pages. In
       * the templateParameters field, ${x} is a JavaScript variable expansion,
       * while \${y} is exported literally as ${y} for Grails to later parse
       * as a GString. Of particular note, the calls to resource() below depend
       * on Grails' Resources plugin and can bypass the asset-pipeline entirely
       * (OGBM-404); see the following document's advice beginning with
       * "if you do not want to use the asset-pipeline plugin ..."
       *
       * https://gsp.grails.org/latest/guide/resources.html
       */
      new HtmlWebpackPlugin({
        filename: `${COMMON_VIEW}/_react.gsp`,
        template: `${TEMPLATES}/grails-template.html`,
        inject: false,
        templateParameters: (compilation) => ({
          // eslint-disable-next-line no-template-curly-in-string,no-useless-escape
          contextPath: '\${util.ConfigHelper.contextPath}',
          jsSource: `\${resource(dir: '${path.basename(WEBPACK_OUTPUT)}', file: 'bundle.${compilation.hash}.js')}`,
          cssSource: `\${resource(dir: '${path.basename(WEBPACK_OUTPUT)}', file: 'bundle.${compilation.hash}.css')}`,
          receivingIfStatement: '',
        }),
      }),
      new HtmlWebpackPlugin({
        filename: `${RECEIVING_VIEW}/_create.gsp`,
        template: `${TEMPLATES}/grails-template.html`,
        inject: false,
        templateParameters: (compilation) => ({
          // eslint-disable-next-line no-template-curly-in-string,no-useless-escape
          contextPath: '\${util.ConfigHelper.contextPath}',
          jsSource: `\${resource(dir: '${path.basename(WEBPACK_OUTPUT)}', file: 'bundle.${compilation.hash}.js')}`,
          cssSource: `\${resource(dir: '${path.basename(WEBPACK_OUTPUT)}', file: 'bundle.${compilation.hash}.css')}`,
          receivingIfStatement:
          // eslint-disable-next-line no-template-curly-in-string
          '<g:if test="${!params.id}">' +
          'You can access the Partial Receiving feature through the details page for an inbound shipment.' +
          '</g:if>',
      }),
    }),
  ],
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        use: ['cache-loader', 'babel-loader?presets[]=@babel/react&presets[]=@babel/env'],
        include: SRC,
        exclude: /node_modules/,
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader'],
      },
      {
        test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file-loader?name=./[hash].[ext]',
        options: {
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.(woff|woff2)$/,
        loader: 'url-loader?prefix=font/&limit=5000&name=./[hash].[ext]',
        options: {
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=10000&mimetype=application/octet-stream&name=./[hash].[ext]',
        options: {
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=10000&mimetype=image/svg+xml&name=./[hash].[ext]',
        options: {
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.(png|jpg|gif)$/i,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
            },
          },
        ],
      },
    ],
  },
  resolve: {
    alias: {
      root: ROOT,
      src: SRC,
      components: path.resolve(SRC, 'components'),
      hooks: path.resolve(SRC, 'hooks'),
      reducers: path.resolve(SRC, 'reducers'),
      actions: path.resolve(SRC, 'actions'),
      consts: path.resolve(SRC, 'consts'),
      tests: path.resolve(SRC, 'tests'),
      utils: path.resolve(SRC, 'utils'),
      templates: path.resolve(SRC, 'templates'),
      store: path.resolve(SRC, 'store'),
      css: path.resolve(ROOT, 'css'),
      api: path.resolve(SRC, 'api'),
    },
    extensions: ['.js', '.jsx'],
  },
};
