var path = require('path');

module.exports = {
    entry: {
        index: '/home/jmiranda/git/openboxes/src/js/app/index.js'
    },
    output: {
        path: '/home/jmiranda/git/openboxes/web-app/js',
        publicPath: '/js/',
        filename: 'bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                include: path.join(__dirname, 'src/js'),
                loaders: ['babel-loader?presets[]=react,presets[]=es2015'],
                // loader: 'babel',
                // query: {
                //     presets: ['es2015', 'react']
                // }
            }
        ]
    }
};