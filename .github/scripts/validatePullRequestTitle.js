// Validates that a pull request title is formatted as expected.
// https://github.com/Esri/calcite-design-system/blob/main/.github/scripts/validatePullRequestTitle.js
module.exports = async ({ context, core }) => {

    // Title must match this regex. Currently, will match strings like:
    //     [ABC-123] xyz
    //     [ABC-123, ABC-456] xyz
    //     [N/A] xyz
    //     [#0000] xyz
    const REGEX = new RegExp("\\[[a-zA-Z0-9\\-,\\s\\/#]*\\]+.*");

    // The minimum length of the title
    const MIN_LENGTH = 5;

    // The maximum length of the title (-1 is no max)
    const MAX_LENGTH = -1;

    const { title } = context.payload.pull_request;
    if (!REGEX.test(title)) {
        core.setFailed(`Pull Request title "${title}" failed to match regex - ${REGEX}`);
        return;
    }

    if (title.length < MIN_LENGTH) {
        core.setFailed(`Pull Request title "${title}" is smaller than the minimum length - ${MIN_LENGTH}`);
        return;
    }

    if (MAX_LENGTH > 0 && title.length > MAX_LENGTH) {
        core.setFailed(`Pull Request title "${title}" is greater than the maximum length - ${MAX_LENGTH}`);
        return;
    }
};