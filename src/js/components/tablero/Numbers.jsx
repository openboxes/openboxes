import React from 'react';
import PropTypes from 'prop-types';

const Numbers = props => (
  <div className="gyrIndicator">
    <a className="numberIndicator" href={props.data.first.link}>
      <div className="value">
        <div className={props.data.gyrColors ? 'circle green' : 'circle gray'} /> {props.data.first.value}
      </div>
      <div className="subtitle">{props.data.first.subtitle}</div>
    </a>
    <a className="numberIndicator" href={props.data.second.link}>
      <div className="value">
        <div className={props.data.gyrColors ? 'circle yellow' : 'circle gray'} /> {props.data.second.value}
      </div>
      <div className="subtitle">{props.data.second.subtitle}</div>
    </a>
    <a className="numberIndicator" href={props.data.third.link}>
      <div className="value">
        <div className={props.data.gyrColors ? 'circle red' : 'circle gray'} /> {props.data.third.value}
      </div>
      <div className="subtitle">{props.data.third.subtitle}</div>
    </a>
  </div>
);

Numbers.propTypes = {
  data: PropTypes.shape({
    gyrColors: PropTypes.bool,
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
};

Numbers.defaultProps = {
  data: null,
};

export default Numbers;
