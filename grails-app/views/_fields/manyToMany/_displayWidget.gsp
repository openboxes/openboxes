<ul>
    <g:each in="${value}" var="item">
        <li><scaffold:associationLink value="${item}" controller="${persistentProperty.associatedEntity.decapitalizedName}" /></li>
    </g:each>
</ul>
