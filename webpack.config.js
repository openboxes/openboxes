const webpack = require('webpack');
const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const ASSETS = path.resolve(ROOT, 'assets');
const JS_DEST = path.resolve(__dirname, 'web-app/js');
const CSS_DEST = path.resolve(__dirname, 'web-app/css');
const GRAILS_VIEWS = path.resolve(__dirname, 'grails-app/views');
const STOCK_MOVEMENT_VIEW = path.resolve(GRAILS_VIEWS, 'stockMovement');
const PUT_AWAY_VIEW = path.resolve(GRAILS_VIEWS, 'putAway');
const RECEIVING_VIEW = path.resolve(GRAILS_VIEWS, 'partialReceiving');

const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  devtool: 'cheap-module-source-map',
  entry: {
    app: `${SRC}/index.jsx`,
  },
  output: {
    path: JS_DEST,
    filename: 'bundle.[hash].js',
    publicPath: '/js/',
  },
  stats: {
    colors: true,
  },
  plugins: [
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery',
    }),
    new ExtractTextPlugin({
      filename: '../css/bundle.[hash].css',
      allChunks: true,
    }),
    new CleanWebpackPlugin([`${JS_DEST}/bundle.*`, `${CSS_DEST}/bundle.*`]),
    new HtmlWebpackPlugin({
      filename: `${STOCK_MOVEMENT_VIEW}/_create.gsp`,
      template: `${ASSETS}/grails-template.html`,
      inject: false,
      templateParameters: compilation => ({
        jsSource: `\${createLinkTo(dir:'/js', file:'bundle.${compilation.hash}.js')}`,
        cssSource: `\${createLinkTo(dir:'css/', file:'bundle.${compilation.hash}.css')}`,
        receivingIfStatement: '',
      }),
    }),
    new HtmlWebpackPlugin({
      filename: `${PUT_AWAY_VIEW}/_create.gsp`,
      template: `${ASSETS}/grails-template.html`,
      inject: false,
      templateParameters: compilation => ({
        jsSource: `\${createLinkTo(dir:'/js', file:'bundle.${compilation.hash}.js')}`,
        cssSource: `\${createLinkTo(dir:'css/', file:'bundle.${compilation.hash}.css')}`,
        receivingIfStatement: '',
      }),
    }),
    new HtmlWebpackPlugin({
      filename: `${RECEIVING_VIEW}/_create.gsp`,
      template: `${ASSETS}/grails-template.html`,
      inject: false,
      templateParameters: compilation => ({
        jsSource: `\${createLinkTo(dir:'/js', file:'bundle.${compilation.hash}.js')}`,
        cssSource: `\${createLinkTo(dir:'css/', file:'bundle.${compilation.hash}.css')}`,
        receivingIfStatement:
          // eslint-disable-next-line no-template-curly-in-string
          '<g:if test="${!params.id}">' +
          'You can access the Partial Receiving feature through the details page for an inbound shipment.' +
          '</g:if>',
      }),
    }),
  ],
  module: {
    loaders: [
      {
        enforce: 'pre',
        test: /\.jsx$/,
        exclude: /node_modules/,
        loaders: ['eslint-loader'],
      },
      {
        test: /\.jsx$/,
        loaders: ['babel-loader?presets[]=es2015&presets[]=react&presets[]=stage-1'],
        include: SRC,
        exclude: /node_modules/,
      },
      {
        test: /\.scss$/,
        use: ExtractTextPlugin.extract('css-loader!sass-loader'),
      },
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({ fallback: 'style-loader', use: ['css-loader'] }),
      },
      { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, use: 'file-loader?name=./fonts/[hash].[ext]' },
      { test: /\.(woff|woff2)$/, use: 'url-loader?prefix=font/&limit=5000&name=./fonts/[hash].[ext]' },
      { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, use: 'url-loader?limit=10000&mimetype=application/octet-stream&name=./fonts/[hash].[ext]' },
      { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, use: 'url-loader?limit=10000&mimetype=image/svg+xml&name=./fonts/[hash].[ext]' },
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
};
