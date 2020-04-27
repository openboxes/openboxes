/* eslint-disable no-undef */
import React from 'react';
import PropTypes from 'prop-types';

const NumbersTableCard = props => (
  <div className="delayedShipment">
    <div className="delayedShipmentNumber">
      <div className="shipmentNumberCard">
        <div> <img src={props.data.numberIndicator.first.link} alt="air" />{props.data.numberIndicator.first.value} </div>
        <div>{props.data.numberIndicator.first.subtitle}</div>
      </div>
      <div className="shipmentNumberCard">
        <div> <img src={props.data.numberIndicator.second.link} alt="sea" /> {props.data.numberIndicator.second.value} </div>
        <div>{props.data.numberIndicator.second.subtitle}</div>
      </div>
      <div className="shipmentNumberCard">
        <div> <img src={props.data.numberIndicator.third.link} alt="land" /> {props.data.numberIndicator.third.value} </div>
        <div>{props.data.numberIndicator.third.subtitle}</div>
      </div>
    </div>
    <div className="tableCard">
      <table>
        <thead>
          <tr>
            <th>{props.data.numberIndicator.labelShipment}</th>
            <th className="end">{props.data.numberIndicator.labelName}</th>
          </tr>
        </thead>
        <tbody>
          {props.data.tableData.map(item => (
            <tr
              onClick={() => window.open(item.link, '_blank')}
              key={`item-${item.number}`}
              className="tableLink"
            >
              <td>{item.number}</td>
              <td className="mid">{_.truncate(item.name, { length: 80 })}</td>
            </tr>
        ))}
        </tbody>
      </table>
    </div>
  </div>
);

NumbersTableCard.propTypes = {
  data: PropTypes.shape({
    numberIndicator: PropTypes.shape({
      labelShipment: PropTypes.string,
      labelName: PropTypes.string,
      first: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
      second: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
      third: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
    }).isRequired,
    tableData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  }).isRequired,
};

export default NumbersTableCard;
