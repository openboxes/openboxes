import _ from 'lodash';
import React, { Component } from 'react';
import { Tooltip } from 'react-tippy';
import update from 'immutability-helper';
import PropTypes from 'prop-types';

import ModalWrapper from '../form-elements/ModalWrapper';
import Input from '../../utils/Input';
import Select from '../../utils/Select';

class SplitLineModal extends Component {
  constructor(props) {
    super(props);

    this.state = { splitItems: [] };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.isValid = this.isValid.bind(this);
  }

  onOpen() {
    let splitItems = [];

    if (this.props.splitItems && this.props.splitItems.length > 0) {
      splitItems = [...this.props.splitItems];
    } else {
      splitItems.push({
        quantity: this.props.putawayItem.quantity,
        putawayFacility: {
          id: this.props.putawayItem.putawayFacility
            ? this.props.putawayItem.putawayFacility.id : null,
        },
        putawayLocation: {
          id: this.props.putawayItem.putawayLocation
            ? this.props.putawayItem.putawayLocation.id : null,
        },
      });
    }

    this.setState({ splitItems });
  }

  onSave() {
    this.props.saveSplitItems(_.filter(this.state.splitItems, item => item.quantity && item.quantity !== '0'));
  }

  isValid() {
    const qtySum = _.reduce(this.state.splitItems, (sum, val) =>
      (sum + (val.quantity ? _.toInteger(val.quantity) : 0)), 0);

    return qtySum === _.toInteger(this.props.putawayItem.quantity);
  }

  render() {
    return (
      <ModalWrapper
        btnOpenText="Split line"
        btnOpenClassName="btn btn-outline-success"
        onOpen={this.onOpen}
        onSave={this.onSave}
        btnSaveDisabled={!this.isValid()}
        title={() => (
          <div>
            <h3 className="font-weight-bold">{`${this.props.putawayItem.product.productCode} ${this.props.putawayItem.product.name}`}</h3>
            <div className="font-weight-bold">Expiry: {this.props.putawayItem.inventoryItem.expirationDate}</div>
            <div className="font-weight-bold">Total QTY: {this.props.putawayItem.quantity}</div>
          </div>)}
      >
        <div className="text-center">
          <table className="table table-striped text-center border">
            <thead>
              <tr>
                <th>Put Away Bin</th>
                <th>Quantity</th>
              </tr>
            </thead>
            <tbody>
              { _.map(this.state.splitItems, (item, index) => (
                <tr
                  // eslint-disable-next-line react/no-array-index-key
                  key={index}
                >
                  <td className="align-middle">
                    <Select
                      options={this.props.bins}
                      value={item.putawayLocation ? item.putawayLocation.id : null}
                      onChange={value => this.setState({
                        splitItems: update(this.state.splitItems, {
                          [index]: {
                            putawayLocation: { id: { $set: value } },
                          },
                        }),
                      })}
                    />
                  </td>
                  <td className="align-middle">
                    <Tooltip
                      // eslint-disable-next-line max-len
                      html={(<div>Sum of all split items quantities should equal original put-away item quantity</div>)}
                      disabled={this.isValid()}
                      theme="transparent"
                      arrow="true"
                      delay="150"
                      duration="250"
                      hideDelay="50"
                    >
                      <div className={!this.isValid() ? 'has-error' : ''}>
                        <Input
                          type="number"
                          className="form-control"
                          value={!item.quantity ? 0 : item.quantity}
                          onChange={value => this.setState({
                            splitItems: update(this.state.splitItems, {
                              [index]: { quantity: { $set: value } },
                            }),
                          })}
                        />
                      </div>
                    </Tooltip>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <button
            className="btn btn-outline-success"
            onClick={() => this.setState({
              splitItems: update(this.state.splitItems, {
                $push: [{
                  quantity: 0,
                  putawayFacility: {
                    id: this.props.putawayItem.putawayFacility
                      ? this.props.putawayItem.putawayFacility.id : null,
                  },
                  putawayLocation: { id: null },
                }],
              }),
            })}
          >Add Line
          </button>
        </div>
      </ModalWrapper>
    );
  }
}

export default SplitLineModal;

SplitLineModal.propTypes = {
  saveSplitItems: PropTypes.func.isRequired,
  putawayItem: PropTypes.shape({
    product: PropTypes.shape({
      productCode: PropTypes.string,
      name: PropTypes.string,
    }),
    inventoryItem: PropTypes.shape({
      expirationDate: PropTypes.string,
    }),
    quantity: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
    ]),
    putawayFacility: PropTypes.shape({
      id: PropTypes.string,
    }),
    putawayLocation: PropTypes.shape({
      id: PropTypes.string,
    }),
  }),
  splitItems: PropTypes.arrayOf(PropTypes.shape({})),
  bins: PropTypes.arrayOf(PropTypes.shape({})),
};

SplitLineModal.defaultProps = {
  putawayItem: {},
  splitItems: [],
  bins: [],
};
