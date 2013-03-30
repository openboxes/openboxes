<div id="localization-dialog" class="dialog" style="display: none;" title="Edit Translation">
    <div id="localization-form">
        <g:form controller="localization" action="save">
            <style>
            #localization-form label { display: block;
                float: left;
                width: 100px;}
            #localization-form label.block { display: block; }
            #localization-form div { margin: 10px; }
            </style>
            <div style="float: left;">
                <div data-bind="if: id">
                    <label>ID</label>
                    <span data-bind="text: id"></span>
                    <input type="hidden" data-bind="value: id"/>
                </div>
                <div class="prop">
                    <label>Locale</label>
                    <input type="hidden" data-bind="value: locale"/>
                    <span data-bind="text: locale"></span>
                </div>
                <div class="prop">
                    <label>Code</label>
                    <input type="hidden" data-bind="value: code"/>
                    <span data-bind="text: code"></span>
                </div>
                <div class="prop">
                    <label>Original Text</label>
                    <span data-bind="text: text"></span>
                </div>
                <div class="prop">
                    <label>Translation</label>
                    <textarea cols="60" rows="6" data-bind="value: translation"></textarea>
                </div>
            </div>
            <!--
            <div style="float: left">
                <div>
                    <label>Translation</label>
                    <textarea cols="60" rows="3" data-bind="value: text"></textarea>
                </div>
                <div>
                    <label>Translation</label>
                    <div data-bind="text: translation"></div>
                </div>
                <div>
                    <select id="src" name="src">
                        <option value="en">English</option>
                        <option value="fr">French</option>
                        <option value="sp">Spanish</option>
                    </select>
                    to
                    <select id="dest" name="dest">
                        <option value="en">English</option>
                        <option value="fr" selected>French</option>
                        <option value="sp">Spanish</option>
                    </select>
                </div>
                <div>
                    <button id="help-localization-btn" class="button">Help</button>

                </div>
            </div>
            -->
            <div class="clear"></div>
            <div class="buttons">
                <button id="save-localization-btn" class="button">Save</button>
                <button id="delete-localization-btn" class="button">Delete</button>
                <button id="close-localization-dialog-btn" class="button">Cancel</button>


            </div>
        </g:form>
    </div>
</div>
