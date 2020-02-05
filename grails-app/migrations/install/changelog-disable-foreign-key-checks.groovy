databaseChangeLog = {
    changeSet(author: "jmiranda", id: "disable-foreign-key-checks", runAlways: true) {
        sql("SET FOREIGN_KEY_CHECKS=0;")
    }
}
