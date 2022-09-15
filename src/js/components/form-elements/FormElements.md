* `<Button>`
#### Available props:
| Name          | Type                      | Values                               | Required | Description                                     |   
|---------------|---------------------------|--------------------------------------|----------|-------------------------------------------------|
| label         | translation code (string) |                                      | yes      | Translation code                                | 
| defaultLabel  | string                    |                                      | yes      | Default label for button                        | 
| disabled      | boolean                   | false by default                     | no       | Boolean to disable button                       | 
| type          | string                    | e.g. "submit", "button"              | no       | Type of button, by default "button"             |
| variant       | string                    | (for 15.09) "primary", "transparent" | no       | Variant/styling of button, by default "primary" |
| onClickAction | function                  | undefined by default                 | no       | onClick function passed from props              | 