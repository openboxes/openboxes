## FilterForm documentation
* `<FilterForm>`
#### Available props:
| Name                   | Type              | Values                                            | Required | Description                                                                                                                             |   
|------------------------|-------------------|---------------------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------|
| filterFields           | object of objects | check example below                               | yes      | The same type as we use for<br/>normal forms (check below)                                                                              | 
| updateFilterParams     | function          |                                                   | yes      | Action to trigger after submiting filter form or assigning default values (e.g. fetching data with values from filter's form as params) | 
| searchFieldPlaceholder | string            | by default: 'Search'                              | no       | All filter forms are supposed to have search field, so provide placeholder for that field as props                                      |
| formProps              | object            | check example below                               | no       | Form props for some fields' needs, e.g. **getDynamicAttr**                                                                              | 
| searchFieldId          | string            | id of searchField input (by default 'searchTerm') | no       | Id of search field - check what backend expects for searchTerm checking (could be 'searchTerm', 'name' etc.)                            | 
| defaultValues          | object            |                                                   | no       | Default values for filter form                                                                                                          | 
| allowEmptySubmit       | boolean           | true/false (false by default)                     | no       | Prop to determine if Search button should be enabled/disabled when empty values only                                                    | 
| hidden                 | boolean           | true/false (true by default)                      | no       | Hide filters by default                                                                                                                 | 
#### Example:
````md
const filterFields = {
    address: {
        type: TextField,
        label: 'address.description.label',
        defaultMessage: 'Address',
    },
    destination: {
        type: SelectField,
        label: 'react.inboundReturns.destination.label',
        defaultMessage: 'Destination',
        attributes: {
            required: true,
            async: true,
            showValueTooltip: true,
            openOnClick: false,
            autoload: false,
            cache: false,
            options: [],
            filterOptions: options => options,
        },
        getDynamicAttr: props => ({
            loadOptions: props.debouncedDestinationLocationsFetch,
        }),
    },
    date: {
        type: DateField,
        label: 'react.invoice.invoiceDate.label',
        defaultMessage: 'Date',
        attributes: {
            dateFormat: 'MM/DD/YYYY',
        },
    },
}

const debouncedOriginLocationsFetch =
    debounceLocationsFetch(
        debounceTime,
        minSearchLength,
        [], // activityCodes
        false, // fetchAll
        false, // withOrgCode
        true, // withTypeDescription
        true, // isReturnOrder
    );   

<FilterForm
    onSubmit={values => console.log(values)}
    filterFields={filterFields}
    searchFieldPlaceholder="Search...."
    formProps={
        {
            debouncedOriginLocationsFetch: debouncedOriginLocationsFetch, 
        }
    }
/>
````


<br>
<br>

* `<FilterVisibilityToggler>`
#### Available props:
| Name             | Type     | Values                              | Required | Description                                                                                                           |   
|------------------|----------|-------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------|
| filtersHidden    | boolean  | true/false                          | yes      | State determining if filters are shown to display correct div with either Hide or Show filters label and correct icon | 
| amountFilled     | number   | amountFilled from `<FilterForm>`    | yes      | Calculated amount of filled fields provided further to `<BadgeCount>`                                                 | 
| setFiltersHidden | function | setFiltersHidden from `<FilterForm>` | yes      | Set state function to hide/show filters                                                                               |

<br>
<br>

* `<BadgeCount>`
#### Available props:
| Name  | Type   | Values | Required | Description                            |   
|-------|--------|--------|----------|----------------------------------------|
| count | number |        | yes      | Simple number to display inside circle | 

