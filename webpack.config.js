const path = require('path');

const ROOT = path.resolve(__dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const DEST = path.resolve(__dirname, 'web-app');
const ASSETS = path.resolve(ROOT, 'assets');
const JS_DEST = path.resolve(__dirname, 'web-app/js');
const CSS_DEST = path.resolve(__dirname, 'web-app/css');
const GRAILS_VIEWS = path.resolve(__dirname, 'grails-app/views');
const COMMON_VIEW = path.resolve(GRAILS_VIEWS, 'common');
const RECEIVING_VIEW = path.resolve(GRAILS_VIEWS, 'partialReceiving');

const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: {
    app: `${SRC}/index.jsx`,
  },
  output: {
    path: DEST,
    filename: 'js/bundle.[hash].js',
    chunkFilename: 'js/bundle.[hash].[name].js',
    publicPath: '/openboxes/',
  },
  stats: {
    colors: true,
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: 'css/bundle.[hash].css',
      chunkFilename: 'css/bundle.[hash].[name].css',
    }),
    new OptimizeCSSAssetsPlugin({}),
    new CleanWebpackPlugin([`${JS_DEST}/bundle.**`, `${CSS_DEST}/bundle.**`]),
    new HtmlWebpackPlugin({
      filename: `${COMMON_VIEW}/_react.gsp`,
      template: `${ASSETS}/grails-template.html`,
      inject: false,
      templateParameters: compilation => ({
        jsSource: `\${createLinkTo(dir:'/js', file:'bundle.${compilation.hash}.js')}`,
        cssSource: `\${createLinkTo(dir:'css/', file:'bundle.${compilation.hash}.css')}`,
        helpScoutBeacon: '<script type="text/javascript">' +
            'window.Beacon("suggest", [' +
              '<!-- suggest the "Logging In" help article, via its hash -->' +
              '"6229284aab585b230a89ed8c",' +
              '{' +
                'text: \'Logging in\',' +
                'url: \'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1266516059/Log+In\',' +
              '},' +
              '{' +
                'text: \'Basic navigation\',' +
                'url: \'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1296138352/Basic+Navigation\',' +
              '}' +
            '])' +
          '</script>' +
          '<script type="text/javascript">\n' +
          '        !function (e, t, n) {\n' +
          '            function a() {\n' +
          '                var e = t.getElementsByTagName("script")[0],\n' +
          '                    n = t.createElement("script");\n' +
          '                n.type = "text/javascript", n.async = !0, n.src = "https://beacon-v2.helpscout.net", e.parentNode.insertBefore(n, e)\n' +
          '            }\n' +
          '            if (e.Beacon = n = function (t, n, a) {\n' +
          '                e.Beacon.readyQueue.push({\n' +
          '                    method: t,\n' +
          '                    options: n,\n' +
          '                    data: a\n' +
          '                })\n' +
          '            }, n.readyQueue = [], "complete" === t.readyState) return a();\n' +
          '            e.attachEvent ? e.attachEvent("onload", a) : e.addEventListener("load", a, !1)\n' +
          '        }(window, document, window.Beacon || function () {\n' +
          '        });\n' +
          '    </script>\n' +
          '    <script type="text/javascript">\n' +
          '        window.Beacon("init", "5eafcb3f-9e7e-4943-8ebb-669f60a696cd")\n' +
          '    </script>',
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
        helpScoutBeacon: '<script type="text/javascript">' +
              'window.Beacon("suggest", [' +
              '<!-- suggest the "Logging In" help article, via its hash -->' +
              '"6229284aab585b230a89ed8c",' +
              '{' +
                'text: \'Logging in\',' +
                'url: \'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1266516059/Log+In\',' +
              '},' +
              '{' +
                'text: \'Basic navigation\',' +
                'url: \'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1296138352/Basic+Navigation\',' +
              '}' +
            '])' +
          '</script>' +
          '<script type="text/javascript">\n' +
          '        !function (e, t, n) {\n' +
          '            function a() {\n' +
          '                var e = t.getElementsByTagName("script")[0],\n' +
          '                    n = t.createElement("script");\n' +
          '                n.type = "text/javascript", n.async = !0, n.src = "https://beacon-v2.helpscout.net", e.parentNode.insertBefore(n, e)\n' +
          '            }\n' +
          '            if (e.Beacon = n = function (t, n, a) {\n' +
          '                e.Beacon.readyQueue.push({\n' +
          '                    method: t,\n' +
          '                    options: n,\n' +
          '                    data: a\n' +
          '                })\n' +
          '            }, n.readyQueue = [], "complete" === t.readyState) return a();\n' +
          '            e.attachEvent ? e.attachEvent("onload", a) : e.addEventListener("load", a, !1)\n' +
          '        }(window, document, window.Beacon || function () {\n' +
          '        });\n' +
          '    </script>\n' +
          '    <script type="text/javascript">\n' +
          '        window.Beacon("init", "5eafcb3f-9e7e-4943-8ebb-669f60a696cd")\n' +
          '    </script>',
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
        loader: 'file-loader?name=./fonts/[hash].[ext]',
      },
      {
        test: /\.(woff|woff2)$/,
        loader: 'url-loader?prefix=font/&limit=5000&name=./fonts/[hash].[ext]',
      },
      {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=10000&mimetype=application/octet-stream&name=./fonts/[hash].[ext]',
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=10000&mimetype=image/svg+xml&name=./fonts/[hash].[ext]',
      },
    ],
  },
  resolve: {
    alias: {
      root: ROOT,
      src: SRC,
      components: path.resolve(SRC, 'components'),
      reducers: path.resolve(SRC, 'reducers'),
      actions: path.resolve(SRC, 'actions'),
      consts: path.resolve(SRC, 'consts'),
      tests: path.resolve(SRC, 'tests'),
      utils: path.resolve(SRC, 'utils'),
      templates: path.resolve(SRC, 'templates'),
      store: path.resolve(SRC, 'store'),
      css: path.resolve(ROOT, 'css'),
    },
    extensions: ['.js', '.jsx'],
  },
};
