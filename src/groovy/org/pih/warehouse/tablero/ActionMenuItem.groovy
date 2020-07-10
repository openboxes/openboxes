package org.pih.warehouse.tablero

class ActionMenuItem implements Serializable {

    String label;
    String linkIcon;
    String linkAction;

    ActionMenuItem(String label, String linkIcon, String linkAction) {
        this.label = label;
        this.linkIcon = linkIcon;
        this.linkAction = linkAction;
    }

    Map toJson() {
        [
                "label"      : label,
                "linkIcon"   : linkIcon,
                "linkAction" : linkAction,
        ]
    }
}
