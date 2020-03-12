import React from 'react';
import PropTypes from 'prop-types';

const Numbers = props => (
  <div className="gyrIndicator">
    <div className="numberIndicator">
      <div className="value">
        <div className="circle green" /> {props.data.green.value}
      </div>
      <div className="subtitle">{props.data.green.subtitle}</div>
    </div>
    <div className="numberIndicator">
      <div className="value">
        <div className="circle yellow" /> {props.data.yellow.value}
      </div>
      <div className="subtitle">{props.data.yellow.subtitle}</div>
    </div>
    <div className="numberIndicator">
      <div className="value">
        <div className="circle red" /> {props.data.red.value}
      </div>
      <div className="subtitle">{props.data.red.subtitle}</div>
    </div>
  </div>
);

Numbers.propTypes = {
  data: PropTypes.shape({
    red: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
    }),
    yellow: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
    }),
    green: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
    }),
  }),
};

Numbers.defaultProps = {
  data: null,
};

export default Numbers;
