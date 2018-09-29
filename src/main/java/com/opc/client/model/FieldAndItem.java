package com.opc.client.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Opc变量名称.
 *
 * @author 刘源
 */
public enum FieldAndItem {
    voltmeter("电压表",OpcDataType.Float),
    ammeter("电流表",OpcDataType.Float),
    controlWord("控制字",OpcDataType.Int),
    setCacheValue("设定缓存值",OpcDataType.Int),
    warningTimeCache("预警时间缓存",OpcDataType.Int),
    opening("开度",OpcDataType.Int),
    load("荷重",OpcDataType.Int),
    load90("荷重九十",OpcDataType.Int),
    load110("荷重一百一",OpcDataType.Int),
    motorFailure("电机故障",OpcDataType.Int),
    upperLimit("上限",OpcDataType.Int),
    lowerLimit("下限",OpcDataType.Int),
    setting("设定",OpcDataType.Int),
    warning("预警",OpcDataType.Int);

    private String itemName;
    private OpcDataType opcDataType;

    FieldAndItem(String itemName,OpcDataType opcDataType) {
        this.itemName = itemName;
        this.opcDataType=opcDataType;
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

    public OpcDataType getOpcDataType() {
        return opcDataType;
    }
}
