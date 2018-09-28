package com.opc.client.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

@Api(tags = "PlcManager", description = "Plc管理")
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
        //转换成OpcServer配置的plc序号
        String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
        List<OpcEntity> plcItemValues = opcClient.getPlcItemValuesByPlcNumber(opcPlcNumber);

        return "";
    }

    @ApiOperation(value = "设置指定plc的参数", notes = "设置指定plc的参数")
    @RequestMapping(path = "/items/value/", method = RequestMethod.PUT)
    public String setItemValue(@RequestParam String plcNumber, @RequestParam FieldAndItem itemName, @RequestParam String itemValue) {
        //转换成OpcServer配置的plc序号
        String opcPlcNumber = appConfig.getPlcNumberDictionary().get(plcNumber);
        opcClient.setItemValue(itemName,plcNumber,itemValue);


        List<OpcEntity> plcItemValues = opcClient.getPlcItemValuesByPlcNumber(opcPlcNumber);

        return "";
    }


}
