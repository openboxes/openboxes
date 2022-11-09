* `<Button>`
#### Available props:
| Name          | Type                      | Values                               | Required | Description                                                             |   
|---------------|---------------------------|--------------------------------------|----------|-------------------------------------------------------------------------|
| label         | translation code (string) |                                      | yes      | Translation code                                                        | 
| defaultLabel  | string                    |                                      | yes      | Default label for button                                                | 
| disabled      | boolean                   | false by default                     | no       | Boolean to disable button                                               | 
| type          | string                    | e.g. "submit", "button"              | no       | Type of button, by default "button"                                     |
| variant       | string                    | primary, secondary, transparent      | no       | Variant/styling of button, by default "primary"                         |
| isDropdown    | bool                      | (for 15.09) "primary", "transparent" | no       | Boolean to set button with appropriate props for dropdown functionality |
| onClick       | function                  | undefined by default                 | no       | onClick function passed from props                                      | 
