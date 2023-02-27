import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';
import { compose } from 'redux';

import { setOffline, setOnline } from 'actions';
import { translateWithDefaultMessage } from 'utils/Translate';

const withConnectivityListener = (WrappedComponent) => {
  class ConnectivityListenerHOC extends React.Component {
    constructor(props) {
      super(props);

      this.setOnline = this.setOnline.bind(this);
      this.setOffline = this.setOffline.bind(this);
    }

    componentDidMount() {
      window.addEventListener('offline', this.setOffline);
      window.addEventListener('online', this.setOnline);
    }

    componentWillUnmount() {
      window.removeEventListener('offline', this.setOffline);
      window.removeEventListener('online', this.setOnline);
    }

    setOnline() {
      Alert.success(
        this.props.translate('react.notification.connectivity.online.message'),
        { timeout: 10000 },
      );
      this.props.setOnline();
    }

    setOffline() {
      Alert.warning(
        this.props.translate('react.notification.connectivity.offline.message'),
        { timeout: 10000 },
      );
      this.props.setOffline();
    }


    render() {
      return <>
        <button onClick={() => this.setOnline()}>hello</button>
        <WrappedComponent {...this.props} />
      </>;
    }
  }

  ConnectivityListenerHOC.propTypes = {
    setOnline: PropTypes.func.isRequired,
    setOffline: PropTypes.func.isRequired,
    translate: PropTypes.func.isRequired,
  };

  return ConnectivityListenerHOC;
};


const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default compose(
  connect(mapStateToProps, { setOffline, setOnline }),
  withConnectivityListener,
);
