import React from 'react';
import PropTypes from 'prop-types';
import ReactLoading from 'react-loading';
import { getRandomColor } from '../../consts/dataFormat/colorMapping';

/* global _ */

const Numbers = () => {
  const colors = ['green', 'yellow', 'red'];
  const classColor = `circle ${colors[_.random(0, colors.length - 1)]}`;

  return (
    <div className="value">
      <div className={classColor} /> {_.random(3, 95)}
    </div>
  );
};


const PreviewIndicator = props => (
  <li className="unarchived-item">
    <div className="archived-indicator">
      <div className="row">
        <div className="col col-3 graph-preview">{props.children}</div>
        <div className="col col-6">
          <span>{_.truncate(props.title, { length: 25, omission: '...' })}</span>
        </div>
        <div className="col col-3">
          <span
            role="button"
            tabIndex={0}
            className="unarchive-button"
            onClick={() => props.handleAdd(props.index, props.type)}
            onKeyDown={() => props.handleAdd(props.index, props.type)}
          >
            Unarchive
          </span>
        </div>
      </div>
    </div>
  </li>
);


const ArchivedNumber = props => (
  <PreviewIndicator
    title={props.title}
    index={props.index}
    handleAdd={props.handleAdd}
    type="number"
  >
    <span>{_.random(3, 95)}</span>
  </PreviewIndicator>
);


const ArchivedGraph = (props) => {
  let graph;

  if (props.type === 'line') {
    graph = <i className="fa fa-line-chart" style={{ color: getRandomColor() }} />;
  } else if (props.type === 'bar') {
    graph = <i className="fa fa-bar-chart" style={{ color: getRandomColor() }} />;
  } else if (props.type === 'doughnut') {
    graph = <i className="fa fa-pie-chart" style={{ color: getRandomColor() }} />;
  } else if (props.type === 'horizontalBar') {
    graph = (
      <i
        className="fa fa-bar-chart horizontal-bar"
        style={{ color: getRandomColor() }}
      />
    );
  } else if (props.type === 'numbers') {
    graph = <Numbers />;
  } else if (props.type === 'loading') {
    graph = (
      <ReactLoading
        type="bubbles"
        color={getRandomColor()}
        height="40px"
        width="40px"
      />
    );
  } else if (props.type === 'error') {
    graph = <i className="fa fa-repeat" />;
  } else if (props.type === 'table' || props.type === 'numberTable') {
    graph = <i className="fa fa-table" style={{ color: getRandomColor() }} />;
  }

  return (
    <PreviewIndicator
      title={props.title}
      index={props.index}
      handleAdd={props.handleAdd}
      type="graph"
    >
      {graph}
    </PreviewIndicator>
  );
};


const ArchivedIndicators = props => (
  <div>
    {props.numberData.map((value, index) =>
      (value.archived ? (
        <ArchivedNumber
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
    {props.graphData.map((value, index) =>
      (value.archived ? (
        <ArchivedGraph
          key={`item-${value.id}`}
          index={index}
          title={value.title}
          type={value.type}
          handleAdd={props.handleAdd}
          unarchiveHandler={props.unarchiveHandler}
          size={props.size}
        />
      ) : null))}
  </div>
);


const UnarchiveIndicators = (props) => {
  const size = props.graphData.filter(data => data.archived).length
    + props.numberData.filter(data => data.archived).length;

  return (
    <div
      className={
        props.showPopout ? 'unarchived-items popover-active' : 'unarchived-items'
      }
    >
      <div className="unarchive" role="button" tabIndex={0} onClick={props.unarchiveHandler} onKeyDown={props.unarchiveHandler}>
        <span>
          Archived Indicators ({size}) <i className="fa fa-archive" />
        </span>
      </div>
      <div className="unarchive-popover">
        <span role="button" tabIndex={0} className="close-button" onClick={props.unarchiveHandler} onKeyDown={props.unarchiveHandler} >
          &times;
        </span>
        <ul className="unarchived-list">
          <ArchivedIndicators
            graphData={props.graphData}
            numberData={props.numberData}
            handleAdd={props.handleAdd}
            unarchiveHandler={props.unarchiveHandler}
            size={size}
          />
        </ul>
      </div>
    </div>
  );
};

export default UnarchiveIndicators;

UnarchiveIndicators.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  showPopout: PropTypes.bool.isRequired,
};

PreviewIndicator.propTypes = {
  title: PropTypes.string.isRequired,
  index: PropTypes.number.isRequired,
  handleAdd: PropTypes.func.isRequired,
  type: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
};

ArchivedGraph.propTypes = {
  type: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
};

ArchivedNumber.propTypes = {
  title: PropTypes.string.isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
};

ArchivedIndicators.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  size: PropTypes.number.isRequired,
};
