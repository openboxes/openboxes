<select>

    <g:each in="${attrs.categories}" var="category">
        <option>
            ${category.name}
        </option>
    </g:each>
</select>
