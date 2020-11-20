import React, { Component } from 'react';
import PropTypes from 'prop-types';
import apiClient from './../../utils/apiClient';

class Filter extends Component {
  constructor(props) {
    super(props);
    this.state = {
      addingFilter: false,
      filterCategorySelected: false,
      filterAvailable: false,
      listFilterSelected: [],
      listCategoryData: [],
      listCategoryDataFiltered: [],
      titlePopup: 'Add filter',
      searchTerm: '',
      categorySelected: '',
    };
  }

  componentDidMount() {
    this.loadStoredFilters(this.props.pageFilters);
  }

  componentDidUpdate(prevProps) {
    if (prevProps.pageFilters !== this.props.pageFilters
      || prevProps.activeConfig !== this.props.activeConfig) {
      this.loadStoredFilters(this.props.pageFilters);
    }
  }

  getCategoryRows = (endpoint) => {
    apiClient.get(endpoint)
      .then((response) => {
        let newListCategoryData = response.data.data || [];

        // Remove from list of filter availables if filter already selected
        newListCategoryData = newListCategoryData.filter(categoryData =>
          !this.state.listFilterSelected
            .some(filterSelected => filterSelected.id === categoryData.id));
        this.setState({
          listCategoryData: newListCategoryData,
          listCategoryDataFiltered: newListCategoryData,
        });
      })
      .catch(() => this.setState({
        listCategoryData: [],
        listCategoryDataFiltered: [],
      }));
  }

  searchOnChange = (event) => {
    const filteredList = this.state.listCategoryData
      .filter(categoryData => categoryData.name
        .toLowerCase()
        .includes(event.target.value.toLowerCase()));
    this.setState({
      listCategoryDataFiltered: filteredList,
      searchTerm: event.target.value,
    });
  }

  toggleAddingFilter = () => {
    // Popup add filter shows up or close
    this.setState({ addingFilter: !this.state.addingFilter });
  }

  toggleCategorySelected = (nameCategory, categoryData) => {
    if (categoryData) this.getCategoryRows(categoryData.endpoint);
    this.setState({ titlePopup: nameCategory || 'Add Filter' });
    this.setState({ categorySelected: nameCategory || '' });
    // Value in the searchBar removed
    this.setState({ searchTerm: '' });
    // Popup filterSelection show up or close
    this.setState({ filterCategorySelected: !this.state.filterCategorySelected });
  }

  loadStoredFilters = (pageFilters) => {
    if (this.props.pageFilters.length > 0) {
      const listFilterSelected = [];

      // Initialization of the page config
      let pageConfig = JSON.parse(sessionStorage.getItem('pageConfig'));
      if (!pageConfig) {
        pageConfig = {};
        sessionStorage.setItem('pageConfig', JSON.stringify(pageConfig));
      }

      if (!pageConfig[`${this.props.activeConfig}`]) {
        pageConfig[`${this.props.activeConfig}`] = {};
      }
      sessionStorage.setItem('pageConfig', JSON.stringify(pageConfig));

      pageFilters.forEach((category) => {
        const listFilterStored = JSON.parse(sessionStorage.getItem('pageConfig'))[`${this.props.activeConfig}`][`${category.name}`] || [];
        listFilterStored.forEach((filter) => {
          listFilterSelected.push(filter);
        });
      });
      this.setState({ listFilterSelected });
      this.setState({ filterAvailable: true });
    } else { this.setState({ filterAvailable: false }); }
  }

  addFilterToTheList = (nameCategory, valueCategory) => {
    // Management of the list of the filter in the DOM
    this.state.listFilterSelected.push({
      id: valueCategory.id,
      name: valueCategory.name,
      nameCategory,
    });

    // Management of the filterList in the session storage
    if (!sessionStorage.getItem('pageConfig')) {
      sessionStorage.setItem('pageConfig', JSON.stringify({}));
    }

    const listFilterToSend = JSON.parse(sessionStorage.getItem('pageConfig'));
    if (!listFilterToSend[`${this.props.activeConfig}`][`${nameCategory}`]) {
      listFilterToSend[`${this.props.activeConfig}`][`${nameCategory}`] = [];
    }

    listFilterToSend[`${this.props.activeConfig}`][`${nameCategory}`].push({
      id: valueCategory.id,
      name: valueCategory.name,
      nameCategory,
    });
    sessionStorage.setItem('pageConfig', JSON.stringify(listFilterToSend));

    this.toggleAddingFilter();
    this.toggleCategorySelected();

    // Refresh data
    this.props.fetchData(this.props.activeConfig);
  }

  removeFilterFromList = (key) => {
    const actualList = this.state.listFilterSelected;
    const elementToDelete = actualList[key];

    // Management of the filterList in the session storage
    const newFilterList = JSON.parse(sessionStorage.getItem('pageConfig'));
    newFilterList[`${this.props.activeConfig}`][`${elementToDelete.nameCategory}`] = newFilterList[`${this.props.activeConfig}`][`${elementToDelete.nameCategory}`]
      .filter(item => item.id !== elementToDelete.id);
    sessionStorage.setItem('pageConfig', JSON.stringify(newFilterList));

    // Management of the list of the filter in the DOM
    const newList = actualList.slice(0, key).concat(actualList.slice(key + 1, actualList.length));
    this.setState({ listFilterSelected: newList });

    // Removing current category if no filter selected
    const pageConfig = JSON.parse(sessionStorage.getItem('pageConfig'));
    if (pageConfig[`${this.props.activeConfig}`][`${elementToDelete.nameCategory}`].length === 0) {
      delete pageConfig[`${this.props.activeConfig}`][`${elementToDelete.nameCategory}`];
      sessionStorage.setItem('pageConfig', JSON.stringify(pageConfig));
    }

    // Refresh data
    this.props.fetchData(this.props.activeConfig);
  }

  render() {
    return (
      this.state.filterAvailable ?
        <div className="category-filter">
          {
              this.state.listFilterSelected.map((value, key) => (
                <div
                  key={`${value.id} - ${value.name}`}
                  className="category-item"
                >
                  <div className="category-title"> {value.name}</div>
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
              <input
                type="text"
                placeholder="search..."
                onChange={this.searchOnChange}
                hidden={!this.state.filterCategorySelected}
                value={this.state.searchTerm}
              />
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
                         >
                           <div
                             role="button"
                             tabIndex={0}
                             onClick={() =>
                            this.toggleCategorySelected(nameCategory, categoryData)}
                             onKeyPress={() =>
                            this.toggleCategorySelected(nameCategory, categoryData)}
                           >
                             {nameCategory}
                             <span > {'>'}
                             </span>
                           </div>
                         </li>
                       )) : null
                      )) :
                      // Once the filter is selected
                      this.props.pageFilters.map(category => (
                        // Find in all filters available the one selected
                        category.name === this.state.categorySelected ?
                        // category[1][0] --> name of the category
                        Object.entries(this.state.listCategoryDataFiltered
                          .sort((a, b) => a.name.localeCompare(b.name)))
                          .map(categoryData => (
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
  pageFilters: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
  }).isRequired).isRequired,
};
