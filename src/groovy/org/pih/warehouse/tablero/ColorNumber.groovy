
package org.pih.warehouse.tablero

class ColorNumber implements Serializable {
    
    def value;
    String subtitle;
    String link;
    String color;

    ColorNumber(def value, String subtitle, String link = null, String color = null ) {
        this.value = value;
        this.subtitle = subtitle;
        this.link = link;
        this.color = color;
    }

    def getColor(int period) {
      if(period == 3) {
          switch(this.value) {
              case {it >= 25}:  this.color = 'success'; break;
              case {it >= 18 && it < 25}: this.color = 'warning'; break;
              case {it < 18}: this.color = 'error'; break;
              default : this.color =  null;
          }
      }
       if(period == 6) {
          switch(this.value) {
              case {it >= 50}:  this.color = 'success'; break;
              case {it >= 36 && it < 50}: this.color = 'warning'; break;
              case {it < 36}: this.color = 'error'; break;
              default : this.color =  null;
          }
      }
       if(period == 9) {
          switch(this.value) {
              case {it >= 75}:  this.color = 'success'; break;
              case {it >= 54 && it < 75}: this.color = 'warning'; break;
              case {it < 54}: this.color = 'error'; break;
              default : this.color =  null;
          }
      }
       if(period == 12) {
          switch(this.value) {
              case {it >= 95}:  this.color = 'success'; break;
              case {it >= 75 && it < 95}: this.color = 'warning'; break;
              case {it < 75}: this.color = 'error'; break;
              default : this.color =  null;
          }
      }
       if(period == 0) {
          switch(this.value) {
              case {it >= 95}:  this.color = 'success'; break;
              case {it >= 75 && it < 95}: this.color = 'warning'; break;
              case {it < 75}: this.color = 'error'; break;
              default : this.color =  null;
          }
      }
    }

    Map toJson() {
        [
                "value"     : value,
                "subtitle"  : subtitle,
                "link"      : link,
                "color"     : color
        ]
    }
}
