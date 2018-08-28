const webpack = require('webpack');
const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const DEST = path.resolve(__dirname, 'web-app/js');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  devtool: 'cheap-module-source-map',
  entry: {
    app: `${SRC}/index.jsx`,
  },
  output: {
    path: DEST,
    filename: 'bundle.js',
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
    new ExtractTextPlugin('../css/bundle.css', {
      allChunks: true,
    }),
    new webpack.optimize.UglifyJsPlugin({
      comments: false,
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
