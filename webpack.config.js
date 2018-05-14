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
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
};
