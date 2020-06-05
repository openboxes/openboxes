import React from 'react';
import PropTypes from 'prop-types';

const NumbersCustomColors = props => (
  <div className="big-gyr-indicator">
    <a className="number-indicator" href={props.data.first.link}>
      <div className="circle" style={{ backgroundColor: props.data.first.color }} />
      <div className="value" style={{ color: props.data.first.color }}>
        {props.data.first.value}
      </div>
      <div className="subtitle">{props.data.first.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.second.link}>
      <div className="circle" style={{ backgroundColor: props.data.second.color }} />
      <div className="value" style={{ color: props.data.second.color }}>
        {props.data.second.value}
      </div>
      <div className="subtitle">{props.data.second.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.third.link}>
      <div className="circle" style={{ backgroundColor: props.data.third.color }} />
      <div className="value" style={{ color: props.data.third.color }}>
        {props.data.third.value}
      </div>
      <div className="subtitle">{props.data.third.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.fourth.link}>
      <div className="circle" style={{ backgroundColor: props.data.fourth.color }} />
      <div className="value" style={{ color: props.data.fourth.color }}>
        {props.data.fourth.value}
      </div>
      <div className="subtitle">{props.data.fourth.subtitle}</div>
    </a>
    <a className="number-indicator" href={props.data.fifth.link}>
      <div className="circle" style={{ backgroundColor: props.data.fifth.color }} />
      <div className="value" style={{ color: props.data.fifth.color }}>
        {props.data.fifth.value}
      </div>
      <div className="subtitle">{props.data.fifth.subtitle}</div>
    </a>
  </div>
);

NumbersCustomColors.propTypes = {
  data: PropTypes.shape({
    fifth: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.string,
      link: PropTypes.string,
      color: PropTypes.string,
    }),
    fourth: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.string,
      link: PropTypes.string,
      color: PropTypes.string,
    }),
    third: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.string,
      link: PropTypes.string,
      color: PropTypes.string,
    }),
    second: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.string,
      link: PropTypes.string,
      color: PropTypes.string,
    }),
    first: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.string,
      link: PropTypes.string,
      color: PropTypes.string,
    }),
  }),
};

NumbersCustomColors.defaultProps = {
  data: null,
};

export default NumbersCustomColors;
