import React, { Component } from 'react';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import PropTypes from 'prop-types';

import store from '../store';
import apiClient from '../utils/apiClient';
import { translateWithDefaultMessage } from '../utils/Translate';

class LoginForm extends Component {
  constructor(props) {
    super(props);

    this.state = {
      username: '',
      password: '',
    };

    this.onLogin = this.onLogin.bind(this);
  }

  onLogin() {
    const url = '/openboxes/api/login';
    const payload = {
      username: this.state.username,
      password: this.state.password,
    };

    apiClient.post(url, payload)
      .then(() => {
        this.setUserLocation();
        this.props.onClose();
      })
      .catch(() => this.props.onClose());
  }

  setUserLocation() {
    const url = `/openboxes/api/chooseLocation/${this.props.currentLocationId}`;

    apiClient.put(url);
  }

  render() {
    return (
      <div className="login-modal-container">
        <div className="login-modal-header px-3 py-2">
          <span><i className="fa fa-unlock-alt pr-2" />{this.props.translate('react.default.login.label', 'Login')}</span>
        </div>
        <div className="px-3">
          <input
            id="username"
            name="username"
            type="text"
            className="form-control my-2"
            placeholder={this.props.translate('react.default.username.placeholder', 'email or username')}
            value={this.state.username}
            onChange={(event) => {
              this.setState({ username: event.target.value });
            }}
          />
          <input
            id="password"
            name="password"
            type="password"
            className="form-control my-2"
            placeholder={this.props.translate('react.default.password.placeholder', 'password')}
            value={this.state.password}
            onChange={(event) => {
              this.setState({ password: event.target.value });
            }}
          />
          <button
            className="btn btn-outline-primary btn-block my-3"
            disabled={!this.state.username || !this.state.password}
            onClick={this.onLogin}
          >{this.props.translate('react.default.button.login.label', 'Login')}
          </button>
        </div>
      </div>
    );
  }
}

LoginForm.propTypes = {
  onClose: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  currentLocationId: PropTypes.string.isRequired,
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currentLocationId: state.session.currentLocation.id,
});

const ConnectedLoginForm = connect(mapStateToProps)(LoginForm);

const LoginModal = props => (<ConnectedLoginForm {...props} store={store} />);

export default LoginModal;
