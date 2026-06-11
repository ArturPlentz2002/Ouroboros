import { AppRegistry } from 'react-native';
import App from '../App';

// Entry web: monta o mesmo App compartilhado no <div id="root">.
AppRegistry.registerComponent('Ouroboros', () => App);
AppRegistry.runApplication('Ouroboros', {
  rootTag: document.getElementById('root'),
});
