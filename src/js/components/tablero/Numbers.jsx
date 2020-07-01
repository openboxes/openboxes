import React from 'react';
import PropTypes from 'prop-types';

const Numbers = props => (
  <div className="gyr-indicator">
    <a className="number-indicator" href={props.data.first.link}>
      <div className="value">
        <div className="circle" style={{ backgroundColor: props.options.colors.first }} /> {props.data.first.value}
      </div>
      <div className="subtitle">{props.data.first.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.second.link}>
      <div className="value">
        <div className="circle" style={{ backgroundColor: props.options.colors.second }} /> {props.data.second.value}
      </div>
      <div className="subtitle">{props.data.second.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.third.link}>
      <div className="value">
        <div className="circle" style={{ backgroundColor: props.options.colors.third }} /> {props.data.third.value}
      </div>
      <div className="subtitle">{props.data.third.subtitle}</div>
    </a>
  </div>
);

Numbers.propTypes = {
  data: PropTypes.shape({
    third: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    second: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    first: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
  }),
  options: PropTypes.shape({
    colors: PropTypes.shape({
      first: PropTypes.string,
      second: PropTypes.string,
      third: PropTypes.string,
    }),
  }).isRequired,
};

Numbers.defaultProps = {
  data: null,
};

export default Numbers;
