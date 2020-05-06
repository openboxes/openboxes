const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const DEST = path.resolve(__dirname, 'grails-app/assets');
const BUILD_ASSETS = path.resolve(__dirname, 'build/assets');
const ASSETS = path.resolve(ROOT, 'assets');
const JS_DEST = path.resolve(__dirname, 'grails-app/assets/javascripts');
const CSS_DEST = path.resolve(__dirname, 'grails-app/assets/stylesheets');
const IMAGES_DEST = path.resolve(__dirname, 'grails-app/assets/images');
const GRAILS_VIEWS = path.resolve(__dirname, 'grails-app/views');
const COMMON_VIEW = path.resolve(GRAILS_VIEWS, 'common');
const RECEIVING_VIEW = path.resolve(GRAILS_VIEWS, 'partialReceiving');

const webpack = require('webpack');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const FileManagerPlugin = require('filemanager-webpack-plugin');

module.exports = env => {
  return {
    entry: {
      app: `${SRC}/index.jsx`,
    },
    output: {
      path: DEST,
      filename: 'javascripts/bundle.[hash].js',
      chunkFilename: 'bundle.[hash].[name].js',
      publicPath: `${env && env.REACT_APP_API_PATH ? env.REACT_APP_API_PATH : '/openboxes/'}assets/`,
    },
    stats: {
      colors: true,
    },
    plugins: [
      new FileManagerPlugin({
        onStart: {
          delete: [
            `${JS_DEST}/bundle.**`,
            `${CSS_DEST}/bundle.**`,
            `${BUILD_ASSETS}/bundle.**`
          ]
        },
        onEnd: {
          copy: [
            { source: `${DEST}/bundle.*.js`, destination: JS_DEST },
            { source: `${DEST}/bundle.*.css`, destination: CSS_DEST },
            { source: `${DEST}/*.eot`, destination: IMAGES_DEST },
            { source: `${DEST}/*.svg`, destination: IMAGES_DEST },
            { source: `${DEST}/*.woff2`, destination: IMAGES_DEST },
            { source: `${DEST}/*.ttf`, destination: IMAGES_DEST },
            { source: `${DEST}/*.woff`, destination: IMAGES_DEST },
            { source: `${JS_DEST}/bundle.*.js`, destination: BUILD_ASSETS },
            { source: `${CSS_DEST}/bundle.*.css`, destination: BUILD_ASSETS }
          ],
          delete: [
            `${DEST}/bundle.**`,
            `${DEST}/*.eot`,
            `${DEST}/*.svg`,
            `${DEST}/*.woff2`,
            `${DEST}/*.ttf`,
            `${DEST}/*.woff`
          ]
        }
      }),
      new MiniCssExtractPlugin({
        filename: 'stylesheets/bundle.[hash].css',
        chunkFilename: 'bundle.[hash].[name].css',
      }),
      new OptimizeCSSAssetsPlugin({}),
      new HtmlWebpackPlugin({
        filename: `${COMMON_VIEW}/_react.gsp`,
        template: `${ASSETS}/grails-template.html`,
        inject: false,
        templateParameters: compilation => ({
            jsSource: `\${resource(dir:'/assets', file:'bundle.${compilation.hash}.js')}`,
            cssSource: `\${resource(dir:'/assets', file:'bundle.${compilation.hash}.css')}`,
            receivingIfStatement: '',
          }),
      }),
      new HtmlWebpackPlugin({
        filename: `${RECEIVING_VIEW}/_create.gsp`,
        template: `${ASSETS}/grails-template.html`,
        inject: false,
        templateParameters: compilation => ({
            jsSource: `\${resource(dir:'/assets', file:'bundle.${compilation.hash}.js')}`,
            cssSource: `\${resource(dir:'/assets', file:'bundle.${compilation.hash}.css')}`,
            receivingIfStatement:
            // eslint-disable-next-line no-template-curly-in-string
            '<g:if test="${!params.id}">' +
            'You can access the Partial Receiving feature through the details page for an inbound shipment.' +
            '</g:if>',
          }),
      }),
      new webpack.DefinePlugin({
        'process.env.REACT_APP_API_PATH': JSON.stringify(env && env.REACT_APP_API_PATH ? env.REACT_APP_API_PATH : '/openboxes')
      }),
    ],
    module: {
      rules: [
        {
          enforce: 'pre',
          test: /\.jsx$/,
          exclude: /node_modules/,
          loader: 'eslint-loader',
        },
        {
          test: /\.jsx$/,
          loader: 'babel-loader?presets[]=es2015&presets[]=react&presets[]=stage-1',
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
        },
        {
          test: /\.(woff|woff2)$/,
          loader: 'url-loader?prefix=font/&limit=5000&name=./[hash].[ext]',
        },
        {
          test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
          loader: 'url-loader?limit=10000&mimetype=application/octet-stream&name=./[hash].[ext]',
        },
        {
          test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
          loader: 'url-loader?limit=10000&mimetype=image/svg+xml&name=./[hash].[ext]',
        },
      ],
    },
    resolve: {
      extensions: ['.js', '.jsx'],
    },
  };
};
