import React from 'react';
import PropTypes from 'prop-types';
import ReactLoading from 'react-loading';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import { getRandomColor } from '../../consts/dataFormat/colorMapping';
import { translateWithDefaultMessage } from '../../utils/Translate';

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
          {
          props.title.code ?
            <span>{_.truncate(props.translate(props.title.code, props.title.message), { length: 25, omission: '...' }) }</span> :
            <span>{_.truncate(props.title, { length: 25, omission: '...' }) }</span>
          }

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
    translate={props.translate}
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
  } else if (props.type === 'numbersCustomColors') {
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
      translate={props.translate}
    >
      {graph}
    </PreviewIndicator>
  );
};


const ArchivedIndicators = props => (
  <div>
    {props.numberData.map((value, index) =>
      (value.archived && value.enabled ? (
        <ArchivedNumber
          key={`item-${value.id}`}
          index={index}
          title={value.title}
          type={value.type}
          data={value.data}
          handleAdd={props.handleAdd}
          unarchiveHandler={props.unarchiveHandler}
          size={props.size}
          translate={props.translate}
        />
      ) : null))}
    {props.graphData.map((value, index) =>
      (value.archived && value.enabled ? (
        <ArchivedGraph
          key={`item-${value.id}`}
          index={index}
          title={value.title}
          type={value.type}
          handleAdd={props.handleAdd}
          unarchiveHandler={props.unarchiveHandler}
          size={props.size}
          translate={props.translate}
        />
      ) : null))}
  </div>
);


const UnarchiveIndicators = (props) => {
  const size = props.graphData.filter(data => data.archived && data.enabled).length
    + props.numberData.filter(data => data.archived && data.enabled).length;

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
            translate={props.translate}
          />
        </ul>
      </div>
    </div>
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default (connect(mapStateToProps)(UnarchiveIndicators));

UnarchiveIndicators.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  showPopout: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};

PreviewIndicator.propTypes = {
  title: PropTypes.oneOfType([
    PropTypes.string.isRequired,
    PropTypes.shape({
      code: PropTypes.string.isRequired,
      message: PropTypes.string.isRequired,
    }).isRequired,
  ]).isRequired,
  index: PropTypes.number.isRequired,
  handleAdd: PropTypes.func.isRequired,
  type: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  translate: PropTypes.func.isRequired,
};

ArchivedGraph.propTypes = {
  type: PropTypes.string.isRequired,
  title: PropTypes.oneOfType([
    PropTypes.string.isRequired,
    PropTypes.shape({
      code: PropTypes.string.isRequired,
      message: PropTypes.string.isRequired,
    }).isRequired,
  ]).isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
};

ArchivedNumber.propTypes = {
  title: PropTypes.oneOfType([
    PropTypes.string.isRequired,
    PropTypes.shape({
      code: PropTypes.string.isRequired,
      message: PropTypes.string.isRequired,
    }).isRequired,
  ]).isRequired,
  handleAdd: PropTypes.func.isRequired,
  index: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
};

ArchivedIndicators.propTypes = {
  graphData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  unarchiveHandler: PropTypes.func.isRequired,
  handleAdd: PropTypes.func.isRequired,
  size: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
};
