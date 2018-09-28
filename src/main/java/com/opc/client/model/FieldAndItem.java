package com.opc.client.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Opc变量名称.
 *
 * @author 刘源
 */
public enum FieldAndItem {
    voltmeter("电压表"),
    ammeter("电流表"),
    controlWord("控制字"),
    setCacheValue("设定缓存值"),
    warningTimeCache("预警时间缓存"),
    opening("开度"),
    load("荷重"),
    load90("荷重九十"),
    load110("荷重一百一"),
    motorFailure("电机故障"),
    upperLimit("上限"),
    lowerLimit("下限"),
    setting("设定"),
    warning("预警");

    private String itemName;

    FieldAndItem(String itemName) {
        this.itemName = itemName;
    }

    public static FieldAndItem[] getAllItems() {
        return values();
    }

    /**
     * 获取指定编号的PLC对应的OpcServer的变量名
     *
     * @param plcNumber
     * @return
     */
    public static Map<String,FieldAndItem> getAllItemsByPlcNumber(String plcNumber) {
        Map<String,FieldAndItem>allItemsName=new HashMap<>();
        for (FieldAndItem item : values()) {
            allItemsName.put(item.getItemNameByPlcNumber(plcNumber),item);
        }
        return allItemsName;
    }

    /**
     * 获取所有PLC对应的OpcServer的变量名
     *
     * @param plcNumbers
     * @return
     */
    public static Map<String,FieldAndItem> getAllItemsByPlcNumbers(String[] plcNumbers) {
        Map<String,FieldAndItem> allPlcItemsName = new HashMap<>();
        for (String plcNumber : plcNumbers) {
            allPlcItemsName.putAll(getAllItemsByPlcNumber(plcNumber));
        }
        return allPlcItemsName;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemNameByPlcNumber(String plcNumber) {
        return this.itemName + plcNumber + ".Value";
    }

}
