const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');

// this value is tightly coupled with src/js/index.jsx::__webpack_public_path__
const WEBPACK_OUTPUT = path.resolve(ROOT, 'main/webapp/webpack');
const TEMPLATES = path.resolve(ROOT, 'assets');
const GRAILS_VIEWS = path.resolve(__dirname, 'grails-app/views');
const COMMON_VIEW = path.resolve(GRAILS_VIEWS, 'common');
const RECEIVING_VIEW = path.resolve(GRAILS_VIEWS, 'partialReceiving');

const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const FileManagerPlugin = require('filemanager-webpack-plugin');
const webpack = require('webpack');

module.exports = {
    cache: true,
    entry: {
      app: `${SRC}/index.jsx`,
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
    /* We generate source maps so Sentry can map errors to lines of code, even when the code is minified */
    devtool: 'source-map',
    plugins: [
      new ESLintPlugin({
        extensions: ['js', 'jsx'],
      }),
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
        filename: `${COMMON_VIEW}/react.gsp`,
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
      // We need to explicitly define our environment variables here so that they can be referenced
      // in the frontend.
      new webpack.DefinePlugin({
        'process.env.REACT_APP_WEB_SENTRY_DSN': JSON.stringify(process.env.REACT_APP_WEB_SENTRY_DSN),
        'process.env.REACT_APP_SENTRY_ENVIRONMENT': JSON.stringify(process.env.REACT_APP_SENTRY_ENVIRONMENT),
        'process.env.REACT_APP_WEB_SENTRY_TRACES_SAMPLE_RATE': JSON.stringify(process.env.REACT_APP_WEB_SENTRY_TRACES_SAMPLE_RATE),
        'process.env.REACT_APP_WEB_SENTRY_REPLAYS_SAMPLE_RATE': JSON.stringify(process.env.REACT_APP_WEB_SENTRY_REPLAYS_SAMPLE_RATE),
        'process.env.REACT_APP_WEB_SENTRY_REPLAYS_ERROR_SAMPLE_RATE': JSON.stringify(process.env.REACT_APP_WEB_SENTRY_REPLAYS_ERROR_SAMPLE_RATE),
      }),
      new HtmlWebpackPlugin({
        filename: `${RECEIVING_VIEW}/create.gsp`,
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
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
              '@babel/preset-env',
              '@babel/react',
            ]
          },
        },
        include: SRC,
        exclude: /node_modules/,
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader'],
      },
      {
        test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file-loader',
        options: {
          name: './[hash].[ext]',
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.(woff|woff2)$/,
        loader: 'url-loader',
        options: {
          limit: 5000,
          name: './[hash].[ext]',
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
          prefix: 'font/'
        },
      },
      {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'application/octet-stream',
          name: './[hash].[ext]',
          postTransformPublicPath: (p) => `__webpack_public_path__ + ${p}`,
        },
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'image/svg+xml',
          name: './[hash].[ext]',
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
  optimization: {
    minimizer: [
      `...`,
      new CssMinimizerPlugin(),
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
      wrappers: path.resolve(SRC, 'wrappers'),
    },
    extensions: ['.js', '.jsx'],
  },
};
