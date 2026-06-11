const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const root = path.resolve(__dirname, '..');

/** Casca web do app RN: alias react-native -> react-native-web e transpila so o nosso codigo. */
module.exports = (_env, argv) => {
  const isProd = argv && argv.mode === 'production';
  return {
    mode: isProd ? 'production' : 'development',
    entry: path.resolve(__dirname, 'index.web.tsx'),
    output: {
      path: path.resolve(__dirname, 'dist'),
      filename: 'bundle.js',
      clean: true,
    },
    resolve: {
      alias: { 'react-native$': 'react-native-web' },
      extensions: ['.web.tsx', '.web.ts', '.tsx', '.ts', '.web.js', '.js'],
    },
    module: {
      rules: [
        {
          test: /\.(tsx?|js)$/,
          // react-native-web ja vem compilado; transpilamos apenas o codigo do app.
          include: [path.resolve(root, 'src'), path.resolve(root, 'App.tsx'), __dirname],
          use: { loader: 'babel-loader' },
        },
      ],
    },
    plugins: [
      new HtmlWebpackPlugin({ template: path.resolve(__dirname, 'index.html') }),
      new webpack.DefinePlugin({
        __DEV__: JSON.stringify(!isProd),
        // `process` nao existe no browser; a URL da API entra como constante de build.
        __OUROBOROS_API_URL__: JSON.stringify(process.env.OUROBOROS_API_URL ?? null),
      }),
    ],
    devServer: {
      static: path.resolve(__dirname, 'dist'),
      host: '0.0.0.0',
      allowedHosts: 'all', // acesso via IP da rede local (celular/outra maquina)
      port: 8081,
      historyApiFallback: true,
    },
  };
};
