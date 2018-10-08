package com.opc.client.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opc.client.config.AppConfig;
import com.opc.client.model.FieldAndItem;
import com.opc.client.model.OpcEntity;
import com.opc.client.util.OpcClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.security.util.BitArray;

import java.nio.ByteBuffer;
import java.util.List;

@Api(tags = "PlcManagement", description = "Plc管理")
@RestController
@RequestMapping(path = "/api")
public class OpcClientControl {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcClientControl.class);

    @Autowired
    OpcClient opcClient;
    @Autowired
    AppConfig appConfig;

    private ObjectMapper objectMapper = new ObjectMapper();


    @ApiOperation(value = "获取指定plc所有参数", notes = "获取指定plc所有参数")
    @RequestMapping(path = "/items/value/{plcNumber}", method = RequestMethod.GET)
    public String getAllItemValue(@PathVariable String plcNumber) {
        try {
            //转换成OpcServer配置的plc序号
            String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
            List<OpcEntity> plcItemValues = opcClient.getPlcItemValuesByPlcNumber(opcPlcNumber);
            ObjectNode rootNode = objectMapper.createObjectNode();
            for (OpcEntity entity : plcItemValues) {
                if (entity.getFieldAndItem() == FieldAndItem.motorFailure) {

                    int failureCode = (Integer) entity.getValue();
                    byte[] intArray = ByteBuffer.allocate(4).putInt(failureCode).array();
                    BitArray bitArray = new BitArray(intArray.length * 8, intArray);
                    ObjectNode itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 0));
                    rootNode.set("远程自动", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 1));
                    rootNode.set("主回路升", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 2));
                    rootNode.set("主回路降", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 3));
                    rootNode.set("故障保护", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 4));
                    rootNode.set("机械上限", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 5));
                    rootNode.set("机械下限", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 8));
                    rootNode.set("仪表上限", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 9));
                    rootNode.set("仪表下限", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 10));
                    rootNode.set("仪表上升", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 11));
                    rootNode.set("仪表下降", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 12));
                    rootNode.set("荷重90%", itemNode);

                    itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    itemNode.put("value", bitArray.get(bitArray.length() - 1 - 13));
                    rootNode.set("荷重110%", itemNode);


                } else {
                    ObjectNode itemNode = objectMapper.createObjectNode();
                    itemNode.put("timestamp", entity.getTimestamp());
                    switch (entity.getFieldAndItem().getOpcDataType()) {
                        case Short:
                        case Int:
                            itemNode.put("value", (Integer) entity.getValue());
                            break;
                        case Float:
                            itemNode.put("value", (Float) entity.getValue());
                            break;
                        default:
                            break;
                    }
                    rootNode.set(entity.getFieldAndItem().getItemName(), itemNode);
                }
            }
            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @ApiOperation(value = "设置指定plc的参数", notes = "设置指定plc的参数")
    @RequestMapping(path = "/items/value/", method = RequestMethod.PUT)
    public String setItemValue(@RequestParam String plcNumber, @RequestParam FieldAndItem itemName,
                               @RequestParam String itemValue) {

        try {
            //转换成OpcServer配置的plc序号
            String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
            opcClient.setItemValue(itemName, opcPlcNumber, itemValue);
            int value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
            byte[] intArray = ByteBuffer.allocate(4).putInt(value).array();
            BitArray bitArray = new BitArray(intArray.length * 8, intArray);
            if (itemName == FieldAndItem.setCacheValue) {
                bitArray.set(bitArray.length() - 1 - 4, true);
                String valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
                opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
                Thread.sleep(500);
                value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
                intArray = ByteBuffer.allocate(4).putInt(value).array();
                bitArray = new BitArray(intArray.length * 8, intArray);
                bitArray.set(bitArray.length() - 1 - 4, false);
                valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
                opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);

            } else if (itemName == FieldAndItem.warningTimeCache) {
                bitArray.set(bitArray.length() - 1 - 6, true);
                String valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
                opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
                Thread.sleep(500);
                value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
                intArray = ByteBuffer.allocate(4).putInt(value).array();
                bitArray = new BitArray(intArray.length * 8, intArray);
                bitArray.set(bitArray.length() - 1 - 6, false);
                valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
                opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
            }
        } catch (Exception e) {
            LOGGER.error("设置出错", e);
            return "error";
        }
        return getAllItemValue(plcNumber);
    }

    @ApiOperation(value = "启动闸门", notes = "启动闸门")
    @RequestMapping(path = "/gate/start", method = RequestMethod.PUT)
    public String startGate(@RequestParam String plcNumber) {

        try {
            //转换成OpcServer配置的plc序号
            String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
            int value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
            byte[] intArray = ByteBuffer.allocate(4).putInt(value).array();
            BitArray bitArray = new BitArray(intArray.length * 8, intArray);
            bitArray.set(bitArray.length() - 1 - 0, true);
            String valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
            opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
            Thread.sleep(500);
            value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
            intArray = ByteBuffer.allocate(4).putInt(value).array();
            bitArray = new BitArray(intArray.length * 8, intArray);
            bitArray.set(bitArray.length() - 1 - 0, false);
            valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
            opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
        } catch (Exception e) {
            LOGGER.error("闸门开启出错", e);
        }
        return getAllItemValue(plcNumber);
    }
    @ApiOperation(value = "停止闸门", notes = "停止闸门")
    @RequestMapping(path = "/gate/stop", method = RequestMethod.PUT)
    public String stopGate(@RequestParam String plcNumber) {

        try {
            //转换成OpcServer配置的plc序号
            String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
            int value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
            byte[] intArray = ByteBuffer.allocate(4).putInt(value).array();
            BitArray bitArray = new BitArray(intArray.length * 8, intArray);
            bitArray.set(bitArray.length() - 1 - 1, true);
            String valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
            opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
            Thread.sleep(500);
            value = (int) opcClient.getItemValue(FieldAndItem.controlWord, opcPlcNumber);
            intArray = ByteBuffer.allocate(4).putInt(value).array();
            bitArray = new BitArray(intArray.length * 8, intArray);
            bitArray.set(bitArray.length() - 1 - 1, false);
            valueStr = String.valueOf(ByteBuffer.wrap(bitArray.toByteArray()).getInt());
            opcClient.setItemValue(FieldAndItem.controlWord, opcPlcNumber, valueStr);
        } catch (Exception e) {
            LOGGER.error("闸门停止出错", e);
        }
        return getAllItemValue(plcNumber);
    }


}
