/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.0
*/
(function(){var C=YAHOO.Bubbling,A=YAHOO.util.Event,D=YAHOO.lang,B=YAHOO.util.Dom;YAHOO.plugin.Lighter=function(){var K={},J={},H="yui-cms-selector",F="yui-cms-item",G="selected",E={persistent:false,onReset:null,onSelect:null};function I(O,M){var N=function(P){B.removeClass(P,G)};var L=B.getElementsByClassName(F,"*",O);if(L.length>0){B.batch(L,N,K,true)}if(D.isFunction(M.onReset)){M.onReset.apply(M,[O])}}C.on("rollover",function(N,M){var Q,P,O,L={},R;P=C.getOwnerByClassName(M[1].target,F);if(P&&(Q=C.getOwnerByClassName(P,H))&&!B.hasClass(P,G)){for(R in J){if(J.hasOwnProperty(R)&&B.hasClass(Q,R)){L=J[R]}}if(!B.hasClass(Q,G)){B.addClass(Q,G);O=function(S){if(!C.virtualTarget(S,Q)){I(Q,L)}};if(!L.persistent){A.removeListener(Q,"mouseout",O);A.addListener(Q,"mouseout",O,K,true)}}I(Q,L);B.addClass(P,G);if(D.isFunction(L.onSelect)){L.onSelect.apply(L,[P,Q])}}});K.add=function(M,L){if(D.isString(M)&&(M!=="")){K.remove(M);J[M]=L||E}};K.remove=function(L){if(D.isString(L)&&(L!=="")&&(J[L])){J[L]=null}J[L]=null};return K}()})();YAHOO.register("lighter",YAHOO.plugin.Lighter,{version:"2.0",build:"210"});