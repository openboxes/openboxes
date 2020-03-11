import React from 'react';
import ReactLoading from 'react-loading';
import { loadColors } from '../../consts/dataFormat/dataLoading';
import { getColor } from '../../consts/dataFormat/chartColors';

const Numbers = () => {
  const colors = ['green', 'yellow', 'red'];
  const classColor = `circle ${colors[_.random(0, colors.length - 1)]}`;

  return (
    <div className="value">
      <div className={classColor} /> {_.random(3, 95)}
    </div>
  );
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
          Archived Indicators ({size}) <i className="fa fa-archive" />
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
