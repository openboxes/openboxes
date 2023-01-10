/* eslint-disable no-param-reassign */
/* eslint-disable no-underscore-dangle */
import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { Bar, Doughnut, HorizontalBar, Line } from 'react-chartjs-2';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { SortableElement } from 'react-sortable-hoc';
import { Tooltip } from 'react-tippy';

import DragHandle from 'components/dashboard/DragHandle';
import LoadingCard from 'components/dashboard/LoadingCard';
import Numbers from 'components/dashboard/Numbers';
import NumbersRAG from 'components/dashboard/NumbersRAG';
import NumbersTableCard from 'components/dashboard/NumbersTableCard';
import TableCard from 'components/dashboard/TableCard';
import { translateWithDefaultMessage } from 'utils/Translate';

// TODO: OBPIH-4384 Refactor FilterComponent to be more generic.
// It should be built from config instead of being hardcoded (and move it out to separate file)
class FilterComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      timeFrame: 6,
      locationSelected: '',
      yearType: '',
    };
  }

  handleChange = (element, cardId, loadIndicator) => {
    const dropdown = element.target;
    let params = '';

    const timeFrame = this.state.timeFrame || null;
    const location = this.state.locationSelected || null;
    if (dropdown.id === 'locationSelector') {
      params = timeFrame !== null ? `querySize=${timeFrame}&` : params;
      params = `${params}destinationLocation=${dropdown.value}`;
      const locationSelected = this.props.allLocations.find(value => value.id === dropdown.value);
      this.setState({ locationSelected });
    }
    if (element.target.id === 'timeFrameSelector') {
      params = `querySize=${dropdown.value}`;
      params = location !== null ? `${params}&destinationLocation=${location.id}` : params;
      this.setState({ timeFrame: dropdown.value });
    }
    if (element.target.id === 'yearTypeSelector' && this.props.yearTypeFilter) {
      params = `${this.props.yearTypeFilter.parameter || 'querySize'}=${dropdown.value}`;
      this.setState({ yearType: dropdown.value });
    }

    if (params !== '') {
      loadIndicator(this.props.widgetId, params);
    }

    dropdown.size = 1;
  };

  render() {
    return (
      <div className="data-filter">
        { this.props.locationFilter &&
          <select
            className="location-filter custom-select"
            size="1"
            onFocus={(e) => { e.target.size = 3; }}
            onBlur={(e) => { e.target.size = 1; }}
            onChange={e => this.handleChange(e, this.props.cardId, this.props.loadIndicator)}
            disabled={!this.props.locationFilter}
            value={this.state.locationSelected.id}
            id="locationSelector"
          >
            { this.props.allLocations.map((value) => {
              if (value.name.code && value.name.message) {
                return (
                  <option key={value.id} value={value.id}>
                    {this.props.translate(value.name.code, value.name.message)}
                  </option>
                      );
              }
              return <option key={value.id} value={value.id}> {value.name}</option>;
            })}

          </select>
        }
        { this.props.timeFilter &&
          <select
            className="time-filter custom-select"
            onChange={e => this.handleChange(e, this.props.cardId, this.props.loadIndicator)}
            disabled={!this.props.timeFilter}
            defaultValue={this.state.timeFrame}
            id="timeFrameSelector"
          >
            <option value="1">
              {this.props.translate(
                this.props.label[0],
                this.props.label[1],
              {
                number: '',
                timeUnit: this.props.translate('react.dashboard.timeFilter.month.label', 'Month'),
              },
            )}
            </option>
            <option value="3">
              {this.props.translate(
                this.props.label[0],
                this.props.label[1],
                {
                  number: 3,
                  timeUnit: this.props.translate('react.dashboard.timeFilter.month.label', 'Month'),
                },
              )}
            </option>
            <option value="6">
              {this.props.translate(
                this.props.label[0],
                this.props.label[1],
                {
                  number: 6,
                  timeUnit: this.props.translate('react.dashboard.timeFilter.month.label', 'Month'),
                },
              )}
            </option>
            <option value="12">
              {this.props.translate(
                this.props.label[0],
                this.props.label[1],
                {
                  number: '',
                  timeUnit: this.props.translate('react.dashboard.timeFilter.year.label', 'Year'),
                },
              )}
            </option>
            {
              this.props.timeLimit === 24 &&
              <option value="24">
                {this.props.translate(
                  this.props.label[0],
                  this.props.label[1],
                  {
                    number: 2,
                    timeUnit: this.props.translate('react.dashboard.timeFilter.years.label', 'Years'),
                  },
                )}
              </option>
            }
          </select>
        }
        {this.props.yearTypeFilter && (
          <select
            className="time-filter custom-select"
            onChange={e => this.handleChange(e, this.props.cardId, this.props.loadIndicator)}
            disabled={!this.props.yearTypeFilter}
            defaultValue={this.state.yearType || this.props.yearTypeFilter.defaultValue}
            id="yearTypeSelector"
          >
            {this.props.yearTypeFilter.options && this.props.yearTypeFilter.options.map(option => (
              <option value={option.value}>
                {this.props.translate(option.label, option.label)}
              </option>
            ))}
          </select>
        )}
      </div>
    );
  }
}

const handleChartClick = (elements) => {
  const link = elements[0]._chart.data.datasets[0].links[elements[0]._index];

  if (link && link !== '') {
    window.location = link;
  }
};

const GraphCard = SortableElement(({
  cardId,
  widgetId,
  cardTitle,
  cardType,
  cardLink,
  cardInfo,
  data,
  options,
  loadIndicator,
  timeFilter = false,
  yearTypeFilter = null,
  timeLimit = 24,
  locationFilter = false,
  allLocations,
  size = null,
  translate,
  hideDraghandle,
}) => {
  let graph;
  // eslint-disable-next-line no-template-curly-in-string
  let label = ['react.dashboard.timeFilter.last.label', 'last ${0} ${1}'];

  const translateDataLabels = (listLabels) => {
    const listTranslated = listLabels.map(labelToTranslate =>
      translate(labelToTranslate.code, labelToTranslate.message));
    return listTranslated;
  };

  if (cardType === 'line') {
    if (data.labels) {
      // Checking if the list of labels sent is composed by code dans message
      if (data.labels[0].code && data.labels[0].message) {
        data.labels = translateDataLabels(data.labels);
      }
    }
    graph = (
      <Line
        data={data}
        options={options}
        onElementsClick={elements => handleChartClick(elements)}
      />
    );
    // eslint-disable-next-line no-template-curly-in-string
    label = ['react.dashboard.timeFilter.next.label', 'next ${0} ${1}'];
  } else if (cardType === 'bar') {
    graph = <Bar data={data} options={options} />;
  } else if (cardType === 'doughnut') {
    graph = <Doughnut data={data} options={options} />;
  } else if (cardType === 'horizontalBar') {
    graph = (<HorizontalBar
      data={data}
      options={options}
      onElementsClick={elements => handleChartClick(elements)}
    />);
  } else if (cardType === 'numbers') {
    graph = <Numbers data={data} options={options} />;
  } else if (cardType === 'numbersCustomColors') {
    graph = <NumbersRAG data={data} />;
  } else if (cardType === 'table') {
    graph = <TableCard data={data} />;
  } else if (cardType === 'numberTable') {
    graph = <NumbersTableCard data={data} options={options} />;
  } else if (cardType === 'loading') {
    graph = <LoadingCard />;
  } else if (cardType === 'error') {
    graph = <button onClick={() => loadIndicator(widgetId)} ><i className="fa fa-repeat" /></button>;
  }

  return (
    <div className={`graph-card ${size === 'big' ? 'big-size' : ''} ${cardType === 'error' ? 'error-card' : ''}`}>
      <div className="header-card">
        {cardLink ?
          <a target="_blank" rel="noopener noreferrer" href={cardLink.code} className="title-link">
            <span className="title-link">
              {translate(cardTitle, cardTitle)}
            </span>
          </a>
          :
          <span className="title-link">
            {translate(cardTitle, cardTitle)}
          </span>
        }
        {
          cardInfo ?
            <div className="graph-infos">
              <Tooltip
                html={
                  <p>
                    {translate(cardInfo, cardInfo)}
                  </p>
                }
                theme="transparent"
                arrow="true"
              >
                <i className="fa fa-info-circle" />
              </Tooltip>
            </div>
        : null}
        {!hideDraghandle && <DragHandle />}
      </div>
      <div className="content-card">

        <FilterComponent
          cardId={cardId}
          widgetId={widgetId}
          loadIndicator={loadIndicator}
          locationFilter={locationFilter}
          timeLimit={timeLimit}
          timeFilter={timeFilter}
          yearTypeFilter={yearTypeFilter}
          label={label}
          data={data.length === 0 ? null : data}
          allLocations={allLocations}
          translate={translate}
        />
        <div className="graph-container">
          {graph}
        </div>
      </div>
    </div>
  );
});

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default (connect(mapStateToProps)(GraphCard));

GraphCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardType: PropTypes.string.isRequired,
  cardInfo: PropTypes.string.isRequired,
  timeLimit: PropTypes.number,
  loadIndicator: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};

FilterComponent.defaultProps = {
  timeFilter: false,
  locationFilter: false,
  yearTypeFilter: null,
};

FilterComponent.propTypes = {
  locationFilter: PropTypes.bool,
  timeFilter: PropTypes.bool,
  yearTypeFilter: PropTypes.shape({
    parameter: PropTypes.string,
    defaultValue: PropTypes.string,
    options: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  timeLimit: PropTypes.number.isRequired,
  label: PropTypes.arrayOf(PropTypes.string.isRequired).isRequired,
  cardId: PropTypes.number.isRequired,
  widgetId: PropTypes.string.isRequired,
  loadIndicator: PropTypes.func.isRequired,
  allLocations: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
};
