import React from 'react';
import PropTypes from 'prop-types';
import ReactLoading from 'react-loading';
import { loadColors, getColor } from '../../consts/dataFormat/dataLoading';

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

const ArchivedNumber = props => (
  <li className="unarchivedItem">
    <div className="archived-indicator">
      <div className="row">
        <div className="col col-3 graph-preview">{_.random(3, 95)}</div>
        <div className="col col-6">
          <span>{_.truncate(props.title, { length: 25, omission: '...' })}</span>
        </div>
        <div className="col col-3">
          <span
            role="button"
            tabIndex={0}
            className="unarchive-button"
            onClick={() => props.handleAdd(props.index, 'number')}
            onKeyDown={() => props.handleAdd(props.index, 'number')}
          >
            Unarchive
          </span>
        </div>
      </div>
    </div>
  </li>
);

const ArchivedIndicators = (props) => {
  let graph;
  const property = props;

  if (props.type === 'line') {
    property.data.datasets = loadColors(props.data, 'line');
    graph = <i className="fa fa-line-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'bar') {
    property.data.datasets = loadColors(props.data, 'bar');
    graph = <i className="fa fa-bar-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'doughnut') {
    property.data.datasets = loadColors(props.data, 'doughnut');
    graph = <i className="fa fa-pie-chart" style={{ color: getColor() }} />;
  } else if (props.type === 'horizontalBar') {
    property.data.datasets = loadColors(props.data, 'horizontalBar');
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
  } else if (props.type === 'table') {
    graph = <i className="fa fa-table" style={{ color: getColor() }} />;
  }

  return (
    <li className="unarchivedItem">
      <div className="archived-indicator">
        <div className="row">
          <div className="col col-3 graph-preview">{graph}</div>
          <div className="col col-6">
            <span>{_.truncate(props.title, { length: 25, omission: '...' })}</span>
          </div>
          <div className="col col-3">
            <span
              role="button"
              tabIndex={0}
              className="unarchive-button"
              onClick={() => props.handleAdd(props.index, 'graph')}
              onKeyDown={() => props.handleAdd(props.index, 'graph')}
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
  const size = props.graphData.filter(data => data.archived).length
    + props.numberData.filter(data => data.archived).length;

  return (
    <div
      className={
        props.showPopout ? 'unarchivedItems popover-active' : 'unarchivedItems'
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
        <ul className="unarchivedList">
          <PopOut
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

export default UnarchiveIndicator;

UnarchiveIndicator.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  showPopout: PropTypes.bool.isRequired,
};

ArchivedIndicators.propTypes = {
  type: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
  data: PropTypes.shape().isRequired,
};


ArchivedNumber.propTypes = {
  title: PropTypes.string.isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
};

PopOut.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  size: PropTypes.number.isRequired,
};
