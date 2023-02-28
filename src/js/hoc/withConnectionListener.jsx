import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';
import { compose } from 'redux';

import { setOffline, setOnline } from 'actions';
import { translateWithDefaultMessage } from 'utils/Translate';

const withConnectionListener = (WrappedComponent) => {
  class ConnectionListenerHOC extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        prevOnlineStatus: navigator.onLine,
        timeoutId: null,
      };

      this.setOnline = this.setOnline.bind(this);
      this.setOffline = this.setOffline.bind(this);
      this.checkOnlineStatus = this.checkOnlineStatus.bind(this);
      this.onChangedOnlineStatus = this.onChangedOnlineStatus.bind(this);
    }

    componentDidMount() {
      window.addEventListener('offline', this.onChangedOnlineStatus);
      window.addEventListener('online', this.onChangedOnlineStatus);
    }

    componentWillUnmount() {
      window.removeEventListener('offline', this.onChangedOnlineStatus);
      window.removeEventListener('online', this.onChangedOnlineStatus);
      if (this.state.timeoutId) {
        clearTimeout(this.state.timeoutId);
      }
    }

    onChangedOnlineStatus() {
      if (!this.state.timeoutId) {
        this.setState({
          timeoutId: setTimeout(this.checkOnlineStatus, 5000),
        });
      }
    }

    setOnline() {
      this.props.setConnectionStatusOnline();
      Alert.success(
        this.props.translate('react.notification.connectivity.online.message'),
        { timeout: 10000 },
      );
    }

    setOffline() {
      this.props.setConnectionStatusOffline();
      Alert.warning(
        this.props.translate('react.notification.connectivity.offline.message'),
        { timeout: 10000 },
      );
    }

    checkOnlineStatus() {
      if (this.state.prevOnlineStatus !== navigator.onLine) {
        if (navigator.onLine) {
          this.setOnline();
        } else {
          this.setOffline();
        }
      }

      clearTimeout(this.state.timeoutId);
      this.setState({
        timeoutId: null,
        prevOnlineStatus: navigator.onLine,
      });
    }


    render() {
      return <WrappedComponent {...this.props} />;
    }
  }

  ConnectionListenerHOC.propTypes = {
    setConnectionStatusOnline: PropTypes.func.isRequired,
    setConnectionStatusOffline: PropTypes.func.isRequired,
    translate: PropTypes.func.isRequired,
  };

  return ConnectionListenerHOC;
};


const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default compose(
  connect(mapStateToProps, {
    setConnectionStatusOffline: setOffline,
    setConnectionStatusOnline: setOnline,
  }),
  withConnectionListener,
);
