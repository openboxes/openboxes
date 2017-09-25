/**
 * Created by Mark Volkmann, @link https://github.com/mvolkmann/react-examples/blob/643bacaa48cffdbc54ae8e05cd9569e18077c7a9/gift/src/autobind.js
 * Creates a new version of each method on obj
 * that begins with a given prefix (typically "on")
 * followed by an uppercase letter.
 */
function autobind(obj, prefix) {
    /* eslint prefer-reflect:0 */
    const re = new RegExp(prefix + '[A-Z]');
    const props = Object.getOwnPropertyNames(Object.getPrototypeOf(obj));
    for (const prop of props) {
        if (re.test(prop)) {
            const value = obj[prop];
            if (typeof value === 'function' && prop !== 'constructor') {
                console.log('autobinding ', prop, 'method');
                obj[prop] = value.bind(obj);
            }
        }
    }
}

export default autobind;