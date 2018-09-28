package com.opc.client.controller;


import com.opc.client.util.OpcClient;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "PlcManager", description = "Plc管理")
@RestController
@RequestMapping(path = "/api")
public class OpcClientControl {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcClientControl.class);

    @Autowired
    OpcClient opcClient;
//
//    @ApiOperation(value = "获取plc所有参数", notes = "获取plc所有参数")
//    @RequestMapping(path = "/items/value/{plcNumber}", method = RequestMethod.GET)
//    public String getAllItemValue(@PathVariable String plcNumber) {
//
//
//    }


}
