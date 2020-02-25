/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable jsx-a11y/click-events-have-key-events */
/* eslint-disable no-param-reassign */
/* eslint-disable react/prop-types */
import React from 'react';
import ReactLoading from 'react-loading';
import { loadColors } from '../../../assets/dataFormat/dataLoading';

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}
const Numbers = () => {
  const colors = ['green', 'yellow', 'red'];
  const classColor = `circle ${colors[getRandomInt(0, colors.length)]}`;

  return (
    <div className="value">
      <div className={classColor} /> {getRandomInt(3, 95)}
    </div>
  );
};

const getColor = () => {
  const colors = [
    '#6fb98f',
    '#004445',
    '#2e5685',
    '#fcc169',
    '#cf455c',
    '#e89da2',
    '#e0b623',
    '#444444',
  ];
  return colors[getRandomInt(0, colors.length)];
};

const ArchivedIndicators = (props) => {
  let graph;

  if (props.type === 'line') {
    props.data.datasets = loadColors(props.data, 'line');
    graph = <i className="fa fa-line-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'bar') {
    props.data.datasets = loadColors(props.data, 'bar');
    graph = <i className="fa fa-bar-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'doughnut') {
    props.data.datasets = loadColors(props.data, 'doughnut');
    graph = <i className="fa fa-pie-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'horizontalBar') {
    props.data.datasets = loadColors(props.data, 'horizontalBar');
    graph = (
      <i
        className="fa fa-bar-chart horizontal-bar"
        style={{ color: getColor() }}
      />
    );
  } else if (props.type === 'numbers') {
    graph = <Numbers />;
  } else if (props.type === 'loading') {
    graph = (
      <ReactLoading
        type="bubbles"
        color={getColor()}
        height="40px"
        width="40px"
      />
    );
  } else if (props.type === 'error') {
    graph = <i className="fa fa-repeat" />;
  }

  return (
    <li className="unarchivedItem">
      <div className="archived-indicator">
        <div className="row">
          <div className="col col-3 graph-preview">{graph}</div>
          <div className="col col-6">
            <span>{props.title}</span>
          </div>
          <div className="col col-3">
            <span
              className="unarchive-button"
              onClick={() => props.handleAdd(props.index)}
            >
              Unarchive
            </span>
          </div>
        </div>
      </div>
    </li>
  );
};

const PopOut = props => (
  <div>
    {props.data.map((value, index) =>
      (value.archived ? (
        <ArchivedIndicators
          key={`item-${value.id}`}
          index={index}
          title={value.title}
          type={value.type}
          data={value.data}
          handleAdd={props.handleAdd}
          unarchiveHandler={props.unarchiveHandler}
          size={props.size}
        />
      ) : null))}
  </div>
);

const UnarchiveIndicator = (props) => {
  const size = props.data.filter(data => data.archived).length;

  return (
    <div
      className={
        props.showPopout ? 'unarchivedItems popover-active' : 'unarchivedItems'
      }
    >
      <div className="unarchive" onClick={props.unarchiveHandler}>
        <span>
          Unarchive indicator ({size}) <i className="fa fa-archive" />
        </span>
      </div>
      <div className="unarchive-popover">
        <span className="close-button" onClick={props.unarchiveHandler}>
          &times;
        </span>
        <ul className="unarchivedList">
          <PopOut
            data={props.data}
            handleAdd={props.handleAdd}
            unarchiveHandler={props.unarchiveHandler}
            size={size}
          />
        </ul>
      </div>
    </div>
  );
};

export default UnarchiveIndicator;
