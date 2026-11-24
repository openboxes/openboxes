package org.pih.warehouse.ajaxtags

import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class AjaxTagLibSpec extends Specification implements TagLibUnitTest<AjaxTagLib> {

    void 'formRemote should render a form that submits itself with jQuery.ajax and updates the target element'() {
        when:
        String output = applyTemplate('<g:formRemote id="addProductGroupToProduct" name="addProductGroupToProduct" ' +
                'update="productGroups" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()" ' +
                'url="[controller: \'product\', action: \'addProductGroupToProduct\']">' +
                '<input name="id" type="hidden" value="1"/>' +
                '</g:formRemote>')

        then:
        output == '<form onsubmit="jQuery.ajax({type:\'POST\',data:jQuery(this).serialize(), ' +
                'url:\'/product/addProductGroupToProduct\',' +
                'success:function(data,textStatus){jQuery(\'#productGroups\').html(data);onSuccess(data,textStatus);},' +
                'error:function(XMLHttpRequest,textStatus,errorThrown){},' +
                'complete:function(XMLHttpRequest,textStatus){onComplete()}});return false" ' +
                'method="post" action="/product/addProductGroupToProduct" id="addProductGroupToProduct">' +
                '<input name="id" type="hidden" value="1"/>' +
                '</form>'
    }

    void 'remoteLink should render a link that posts its params with jQuery.ajax and updates the target element'() {
        when:
        String output = applyTemplate('<g:remoteLink controller="product" action="deleteSynonym" update="synonyms" ' +
                'class="button" id="2" params="[productId: \'1\']">Delete</g:remoteLink>')

        then:
        output == '<a href="/product/deleteSynonym/2?productId=1" ' +
                'onclick="jQuery.ajax({type:\'POST\',data:{\'productId\': \'1\'}, url:\'/product/deleteSynonym/2\',' +
                'success:function(data,textStatus){jQuery(\'#synonyms\').html(data);},' +
                'error:function(XMLHttpRequest,textStatus,errorThrown){}});return false;" ' +
                'class="button" id="2">Delete</a>'
    }
}
