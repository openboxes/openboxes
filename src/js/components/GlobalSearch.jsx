import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Select from '../utils/Select';
import { debounceGlobalSearch } from '../utils/option-utils';

class GlobalSearch extends Component {
  constructor(props) {
    super(props);

    this.state = {
      inputValue: '',
    };

    this.onInputChange = this.onInputChange.bind(this);

    this.debouncedGlobalSearch =
      debounceGlobalSearch(this.props.debounceTime, this.props.minSearchLength);
  }

  onInputChange(inputValue) {
    this.setState({ inputValue });

    return inputValue;
  }

  render() {
    return (
      <div className="global-search">
        <Select
          async
          placeholder="Search..."
          loadOptions={this.debouncedGlobalSearch}
          cache={false}
          options={[]}
          showValueTooltip
          noResultsText={`${this.state.inputValue.length >= 3 ? 'No results found' : ''}`}
          menuContainerStyle={{ maxHeight: '500px' }}
          menuStyle={{ maxHeight: '500px' }}
          filterOptions={options => options}
          onChange={(value) => {
            window.location = value.url;
          }}
          onInputChange={this.onInputChange}
          onEnterPress={() => {
            window.location = `/openboxes/dashboard/globalSearch?searchTerms=${this.state.inputValue}`;
          }}
          optionRenderer={option => <strong style={{ color: option.color ? option.color : 'black' }}>{option.label}</strong>}
        />
      </div>);
  }
}

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default connect(mapStateToProps)(GlobalSearch);

GlobalSearch.propTypes = {
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
