
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
              case {it >= 25}:  this.color = '#689f38'; break;
              case {it >= 18 && it < 25}: this.color = '#fbc02d'; break;
              case {it < 18}: this.color = '#d32f2f'; break;
              default : this.color =  null;
          }
      }
       if(period == 6) {
          switch(this.value) {
              case {it >= 50}:  this.color = '#689f38'; break;
              case {it >= 36 && it < 50}: this.color = '#fbc02d'; break;
              case {it < 36}: this.color = '#d32f2f'; break;
              default : this.color =  null;
          }
      }
       if(period == 9) {
          switch(this.value) {
              case {it >= 75}:  this.color = '#689f38'; break;
              case {it >= 54 && it < 75}: this.color = '#fbc02d'; break;
              case {it < 54}: this.color = '#d32f2f'; break;
              default : this.color =  null;
          }
      }
       if(period == 12) {
          switch(this.value) {
              case {it >= 95}:  this.color = '#689f38'; break;
              case {it >= 75 && it < 95}: this.color = '#fbc02d'; break;
              case {it < 75}: this.color = '#d32f2f'; break;
              default : this.color =  null;
          }
      }
       if(period == 0) {
          switch(this.value) {
              case {it >= 95}:  this.color = '#689f38'; break;
              case {it >= 75 && it < 95}: this.color = '#fbc02d'; break;
              case {it < 75}: this.color = '#d32f2f'; break;
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
