## notification documentation
* `notification(...)(...)`
#### Usage:
notification is implemented as curried function - as argument it expects `type` of the notification which is from the:
`NotificationType` enum.

It returns a function that expects `message` (required), `details`, `detailsArray`, `icon` as arguments to then call the proper `Alert` function from `react-s-alert` package.

                                                                                                        
#### Examples:
````md
 notification(NotificationType.INFO)({
    message: 'Lost connection',
    details: 'You are now offline. The changes you made will not be saved',
    icon: <RiWifiOffLine />,
});
````
The above generates the Alert of info type (gray border) with message (title [bold]) of "Lost connection" and the details of the message with the start icon of Wifi icon.

````
 notification(NotificationType.SUCCESS)({
    message: 'Lorem ipsum',
    details: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
});
````

The above generates the Alert of type success. We have not provided the `icon` prop, so the default will be: `<RiCheckboxCircleLine />`

We can also provide an array of errors to be displayed one below another:
```
````md
 notification(NotificationType.INFO)({
    message: 'Lost connection',
    detailsArray: ['1: Location wrong', '2: Reason code wrong'],
    icon: <RiWifiOffLine />,
});
```
