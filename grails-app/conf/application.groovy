grails.gorm.default.constraints = {
    expirationDateConstraint(nullable:true, min: new Date("01/01/2000"))
}

grails.gorm.default.mapping = {
    '*'(cascadeValidate: 'none')
}
