import React, { Component } from 'react';
import PropTypes from 'prop-types';
import apiClient from './../../utils/apiClient';

class Filter extends Component {
  constructor(props) {
    super(props);
    this.state = {
      addingFilter: false,
      filterCategorySelected: false,
      listFilterSelected: [],
      listCategoryData: [],
      titlePopup: 'Add filter',
      categorySelected: '',
    };
  }


  getCategoryRows = (endpoint) => {
    apiClient.get(endpoint)
      .then((response) => {
        let newListCategoryData = response.data.data || [];

        // Remove from list of filter availables if filter already selected
        newListCategoryData = newListCategoryData.filter(categoryData =>
          !this.state.listFilterSelected
            .some(filterSelected => filterSelected[1].id === categoryData.id));
        this.setState({ listCategoryData: newListCategoryData });
      })
      .catch(() => this.setState({ listCategoryData: [] }));
  }

  toggleAddingFilter = () => {
    // Popup add filter shows up or close
    this.setState({ addingFilter: !this.state.addingFilter });
  }

  toggleCategorySelected = (nameCategory, categoryData) => {
    if (categoryData) this.getCategoryRows(categoryData.endpoint);
    this.setState({ titlePopup: nameCategory || 'Add Filter' });
    this.setState({ categorySelected: nameCategory || '' });
    // Popup filterSelection show up or close
    this.setState({ filterCategorySelected: !this.state.filterCategorySelected });
  }

  addFilterToTheList = (nameCategory, valueCategory) => {
    // Management of the list of the filter in the DOM
    this.state.listFilterSelected.push([nameCategory, valueCategory]);

    // Management of the filterList in the session storage
    const listFilterToSend = JSON.parse(sessionStorage.getItem(nameCategory)) || [];
    listFilterToSend.push(valueCategory.id);
    sessionStorage.setItem(nameCategory, JSON.stringify(listFilterToSend));
    if (!sessionStorage.getItem('currentCategory')) {
      sessionStorage.setItem('currentCategory', nameCategory);
    }

    this.toggleAddingFilter();
    this.toggleCategorySelected();

    // Refresh data
    this.props.fetchData(this.props.activeConfig, false);
  }

  removeFilterFromList = (key) => {
    const actualList = this.state.listFilterSelected;
    const elementToDelete = actualList[key];

    // Management of the filterList in the session storage
    const actualFilterList = JSON.parse(sessionStorage.getItem(elementToDelete[0])) || [];
    const newFilterList = actualFilterList.filter(item => item !== elementToDelete[1].id);
    sessionStorage.setItem(elementToDelete[0], JSON.stringify(newFilterList));

    // Management of the list of the filter in the DOM
    const newList = actualList.slice(0, key).concat(actualList.slice(key + 1, actualList.length));
    this.setState({ listFilterSelected: newList });

    // Removing current category if no filter selected
    if (JSON.parse(sessionStorage.getItem(elementToDelete[0])).length === 0) {
      sessionStorage.removeItem('currentCategory');
      sessionStorage.removeItem(elementToDelete[0]);
    }

    // Refresh data
    this.props.fetchData(this.props.activeConfig, false);
  }

  render() {
    if (!this.props.configs) {
      return null;
    }
    let filterAvailable = false;
    let pageFilters = [];
    const allPages = Object.entries(this.props.configs).map(([key, value]) => [key, value]);

    allPages.forEach((page) => {
      const filters = Object.entries(page[1].filters)
        .map(([keyFilter, valueFilter]) => [keyFilter, valueFilter]);

      if (filters.length > 0 && page[0] === this.props.activeConfig) {
        filterAvailable = true;
        pageFilters = filters;
      }
    });

    return (
      filterAvailable ?
        <div className="category-filter">
          {
              Object.entries(this.state.listFilterSelected).map((value, key) => (
                <div
                  key={value}
                  className="category-item"
                >
                  <div className="category-title"> {value[1][1].name}</div>
                  <div
                    className="delete-button"
                    role="button"
                    tabIndex={0}
                    onClick={() => this.removeFilterFromList(key)}
                    onKeyPress={() => this.removeFilterFromList(key)}
                  > x
                  </div>
                </div>
              ))
              }

          <div>
            <div
              className="category-item add-category-btn"
              role="button"
              tabIndex={0}
              onClick={this.toggleAddingFilter}
              onKeyPress={this.toggleAddingFilter}
              hidden={this.state.addingFilter}
            >
              + Add filter
            </div>
            <div
              className="add-category-popup"
              hidden={!this.state.addingFilter}
            >
              <div>
                <span
                  role="button"
                  tabIndex={0}
                  onClick={() => this.toggleCategorySelected()}
                  onKeyPress={() => this.toggleCategorySelected()}
                  hidden={!this.state.filterCategorySelected}
                > {'<'}
                </span> {this.state.titlePopup}
                <span
                  role="button"
                  tabIndex={0}
                  onClick={() => {
                    this.toggleAddingFilter();
                    this.setState({ filterCategorySelected: false });
                    }}
                  onKeyPress={() => {
                    this.toggleAddingFilter();
                    this.setState({ filterCategorySelected: false });
                  }}
                > X
                </span>
              </div>
              <ul className={`filter-menu ${this.state.filterCategorySelected ? 'scrollable' : ''}`}>
                {
                  // If filter not selected
                  !this.state.filterCategorySelected ?
                   Object.entries(this.props.configs).map(([key, value]) => (
                     // We select the filters availables for this page
                     key === this.props.activeConfig ?
                       Object.entries(value.filters).map(([nameCategory, categoryData]) => (
                         <li
                           key={categoryData.endpoint}
                           className="category-name-to-select"
                         > {nameCategory}
                           <span
                             role="button"
                             tabIndex={0}
                             onClick={() =>
                              this.toggleCategorySelected(nameCategory, categoryData)}
                             onKeyPress={() =>
                               this.toggleCategorySelected(nameCategory, categoryData)}
                           > {'>'}
                           </span>
                         </li>
                       )) : null
                      )) :
                      // Once the filter is selected
                      Object.entries(pageFilters).map(category => (
                        // Find in all filters available the one selected
                        category[1][0] === this.state.categorySelected ?
                        // category[1][0] --> name of the category
                        Object.entries(this.state.listCategoryData).map(categoryData => (
                          <li
                            key={categoryData[1].id}
                          >
                            <span
                              className="category-value-to-select"
                              role="button"
                              tabIndex={0}
                              onClick={
                                () => this.addFilterToTheList(
                                  this.state.categorySelected,
                                   categoryData[1],
                                  )}
                              onKeyPress={
                                () => this.addFilterToTheList(
                                  this.state.categorySelected,
                                  categoryData[1],
                                  )}
                            >
                              {categoryData[1].name}
                            </span>
                          </li>
                        )) : null
                      ))
                    }
              </ul>
            </div>
          </div>
        </div> : null
    );
  }
}

export default Filter;

Filter.propTypes = {
  configs: PropTypes.shape({}).isRequired,
  activeConfig: PropTypes.string.isRequired,
  fetchData: PropTypes.func.isRequired,
};
