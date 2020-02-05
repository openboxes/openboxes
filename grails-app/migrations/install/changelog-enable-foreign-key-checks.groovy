databaseChangeLog = {
    changeSet(author: "jmiranda", id: "enable-foreign-key-checks", runAlways: true) {
        sql("SET FOREIGN_KEY_CHECKS=1;")
    }
}
