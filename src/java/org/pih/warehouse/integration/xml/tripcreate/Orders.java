package org.pih.warehouse.integration.xml.tripcreate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {
        "orderId","extOrderId","departmentCode","orderType","modeOfTransport",
        "serviceType","deliveryTerms","goodsValue","typeOfBusiness","termsOfTrade",
        "orderParties","orderStartLocation","orderEndLocation","orderCargoSummary",
        "orderCargoDetails","manageReferences","manageRemarks"
})
public class Orders {
    private String orderId;
    private String extOrderId;
    private String departmentCode;
    private String orderType;
    private String modeOfTransport;
    private String serviceType;
    private String deliveryTerms;
    private GoodsValue goodsValue;
    private String typeOfBusiness;
    private TermsOfTrade termsOfTrade;
    private OrderParties orderParties;
    private LocationInfo orderStartLocation;
    private LocationInfo orderEndLocation;
    private OrderCargoSummary orderCargoSummary;
    private CargoDetails orderCargoDetails;
    private ManageReferences manageReferences;
    private ManageRemarks manageRemarks;

    @XmlElement(name = "OrderID")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @XmlElement(name = "EXTOrderID")
    public String getExtOrderId() {
        return extOrderId;
    }

    public void setExtOrderId(String extOrderId) {
        this.extOrderId = extOrderId;
    }

    @XmlElement(name = "DepartmentCode")
    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    @XmlElement(name = "OrderType")
    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    @XmlElement(name = "ModeOfTransport")
    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }

    @XmlElement(name = "ServiceType")
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @XmlElement(name = "DeliveryTerms")
    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    @XmlElement(name = "GoodsValue")
    public GoodsValue getGoodsValue() {
        return goodsValue;
    }

    public void setGoodsValue(GoodsValue goodsValue) {
        this.goodsValue = goodsValue;
    }

    @XmlElement(name = "TypeOfBusiness")
    public String getTypeOfBusiness() {
        return typeOfBusiness;
    }

    public void setTypeOfBusiness(String typeOfBusiness) {
        this.typeOfBusiness = typeOfBusiness;
    }

    @XmlElement(name = "TermsOfTrade")
    public TermsOfTrade getTermsOfTrade() {
        return termsOfTrade;
    }

    public void setTermsOfTrade(TermsOfTrade termsOfTrade) {
        this.termsOfTrade = termsOfTrade;
    }

    @XmlElement(name = "OrderParties")
    public OrderParties getOrderParties() {
        return orderParties;
    }

    public void setOrderParties(OrderParties orderParties) {
        this.orderParties = orderParties;
    }

    @XmlElement(name = "OrderStartLocation")
    public LocationInfo getOrderStartLocation() {
        return orderStartLocation;
    }

    public void setOrderStartLocation(LocationInfo orderStartLocation) {
        this.orderStartLocation = orderStartLocation;
    }

    @XmlElement(name = "OrderEndLocation")
    public LocationInfo getOrderEndLocation() {
        return orderEndLocation;
    }

    public void setOrderEndLocation(LocationInfo orderEndLocation) {
        this.orderEndLocation = orderEndLocation;
    }

    @XmlElement(name = "OrderCargoSummary")
    public OrderCargoSummary getOrderCargoSummary() {
        return orderCargoSummary;
    }

    public void setOrderCargoSummary(OrderCargoSummary orderCargoSummary) {
        this.orderCargoSummary = orderCargoSummary;
    }

    @XmlElement(name = "OrderCargoDetails")
    public CargoDetails getOrderCargoDetails() {
        return orderCargoDetails;
    }

    public void setOrderCargoDetails(CargoDetails orderCargoDetails) {
        this.orderCargoDetails = orderCargoDetails;
    }

    @XmlElement(name = "ManageReferences")
    public ManageReferences getManageReferences() {
        return manageReferences;
    }

    public void setManageReferences(ManageReferences manageReferences) {
        this.manageReferences = manageReferences;
    }

    @XmlElement(name = "ManageRemarks")
    public ManageRemarks getManageRemarks() {
        return manageRemarks;
    }

    public void setManageRemarks(ManageRemarks manageRemarks) {
        this.manageRemarks = manageRemarks;
    }
}
