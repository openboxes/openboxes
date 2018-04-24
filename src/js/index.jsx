import ReactDOM from 'react-dom';
import React, { Component } from 'react';

import '../css/main.scss';

class App extends Component {
    render() {
        return (
            <div>React Component</div>
        );
    }
}

ReactDOM.render(
    <App />, document.getElementById('root')
);
