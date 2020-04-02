import React from 'react';
import PropTypes from 'prop-types';

const Numbers = props => (
  <div className="gyrIndicator">
    <a className="numberIndicator" href={props.data.green.link}>
      <div className="value">
        <div className="circle green" /> {props.data.green.value}
      </div>
      <div className="subtitle">{props.data.green.subtitle}</div>
    </a>
    <a className="numberIndicator" href={props.data.yellow.link}>
      <div className="value">
        <div className="circle yellow" /> {props.data.yellow.value}
      </div>
      <div className="subtitle">{props.data.yellow.subtitle}</div>
    </a>
    <a className="numberIndicator" href={props.data.red.link}>
      <div className="value">
        <div className="circle red" /> {props.data.red.value}
      </div>
      <div className="subtitle">{props.data.red.subtitle}</div>
    </a>
  </div>
);

Numbers.propTypes = {
  data: PropTypes.shape({
    red: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    yellow: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    green: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
  }),
};

Numbers.defaultProps = {
  data: null,
};

export default Numbers;
