package com.opc.client.util;


import com.opc.client.config.AppConfig;
import com.opc.client.model.FieldAndItem;
import com.opc.client.model.OpcDataType;
import com.opc.client.model.OpcEntity;
import org.jinterop.dcom.core.JIVariant;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class OpcClient {
    @Autowired
    AppConfig appConfig;
    private Server server;
    private Logger LOGGER = LoggerFactory.getLogger(OpcClient.class);
    private volatile boolean isConnect = false;
    private Group group;
    private Map<String, Item> itemMap;
    private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try {
            ServerList serverList = new ServerList(appConfig.getHost(), appConfig.getUser(), appConfig.getPassword(),
                    appConfig.getDomain());
            final Collection<ClassDetails> detailsList =
                    serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer20}, new Category[]{});
            for (final ClassDetails details : detailsList) {
                LOGGER.debug("ProgID:{}", details.getProgId());
                LOGGER.debug("ClsId:{}", details.getClsId());
                LOGGER.debug("Description:{}", details.getDescription());
            }
            ConnectionInformation ci = new ConnectionInformation();
            ci.setHost(appConfig.getHost());
            ci.setClsid(serverList.getClsIdFromProgId(appConfig.getProgId()));
            ci.setUser(appConfig.getUser());
            ci.setPassword(appConfig.getPassword());
            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
            server = new Server(ci, exec);
        } catch (Exception e) {
            LOGGER.error("OpcServer客户端初始化错误", e);
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void reconnectHandlerTaskExecutor() {
        try {
            if (!isConnect) {
                connect();
            }
            Map<String, List<OpcEntity>> allPlcItemValues = getAllPlcItemValues();
            printAllPlcItemLog(allPlcItemValues);
        } catch (Exception e) {
            LOGGER.error("OpcServer连接错误,尝试重新连接", e);
            isConnect = false;
            if (server != null) {
                server.disconnect();
            }
        }
    }


    private void connect() throws Exception {
        if (server != null) {
            server.disconnect();
        }
        if (group != null) {
            group.clear();
            group.remove();
        }
        server.connect();
        group = server.addGroup();
        Map<String, FieldAndItem> allPlcItemsName = FieldAndItem.getAllItemsByPlcNumbers(appConfig.getPlcNumbers());
        String[] itemNames = allPlcItemsName.keySet().toArray(new String[0]);
        itemMap = group.addItems(itemNames);
        isConnect = true;
    }


    public Map<String, List<OpcEntity>> getAllPlcItemValues() {
        Map<String, List<OpcEntity>> allPlcItemValues = new HashMap<>();
        String[] plcNumbers = appConfig.getPlcNumbers();
        for (String plcNumber : plcNumbers) {
            List<OpcEntity> plcItemValues = getPlcItemValuesByPlcNumber(plcNumber);
            allPlcItemValues.put(plcNumber, plcItemValues);
        }
        return allPlcItemValues;
    }


    public List<OpcEntity> getPlcItemValuesByPlcNumber(String plcNumber) {
        List<OpcEntity> plcItemValues = new ArrayList<>();
        try {
            Set<Map.Entry<String, FieldAndItem>> plcItemNames =
                    FieldAndItem.getAllItemsByPlcNumber(plcNumber).entrySet();
            for (Map.Entry<String, FieldAndItem> item : plcItemNames) {
                ItemState state = itemMap.get(item.getKey()).read(true);
                String timestamp = formatter.print(new DateTime(state.getTimestamp().getTime()));
                Object value = state.getValue().getObject();
                plcItemValues.add(new OpcEntity(timestamp, item.getValue(), value));
            }
        } catch (Exception e) {
            LOGGER.error("获取变量数据出错", e);
            isConnect = false;
        }
        return plcItemValues;
    }

    private void printAllPlcItemLog(Map<String, List<OpcEntity>> allPlcItemValues) {

        for (Map.Entry<String, List<OpcEntity>> plcItemValue : allPlcItemValues.entrySet()) {
            String plcNumber = plcItemValue.getKey();
            List<OpcEntity> plcItem = plcItemValue.getValue();
            for (OpcEntity entity : plcItem) {
                OpcDataType opcDataType = entity.getFieldAndItem().getOpcDataType();
                switch (opcDataType) {
                    case Short:
                    case Int:
                        int valueInt = (Integer) entity.getValue();
                        LOGGER.debug("PLC编号:{} 获取时间:{} 变量名:{} 变量值:{}", plcNumber, entity.getTimestamp(),
                                entity.getFieldAndItem().getItemName(), valueInt);
                        break;
                    case Float:
                        float valueFloat = (Float) entity.getValue();
                        LOGGER.debug("PLC编号:{} 获取时间:{} 变量名:{} 变量值:{}", plcNumber, entity.getTimestamp(),
                                entity.getFieldAndItem().getItemName(), valueFloat);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void setItemValue(FieldAndItem fieldAndItem, String plcNumber, String value) {
        try {
            String itemName = fieldAndItem.getItemNameByPlcNumber(plcNumber);
            Item item = itemMap.get(itemName);
            JIVariant itemValue;
            switch (fieldAndItem.getOpcDataType()) {
                case Short:
                    itemValue = new JIVariant(Short.valueOf(value));
                    break;
                case Int:
                    itemValue = new JIVariant(Integer.valueOf(value));
                    break;
                default:
                    itemValue = new JIVariant("");
                    break;
            }
            item.write(itemValue);
        } catch (Exception e) {
            LOGGER.error("OpcServe写入Item错误,尝试重新连接", e);
            isConnect = false;
            if (server != null) {
                server.disconnect();
            }
        }
    }

    public Object getItemValue(FieldAndItem fieldAndItem, String plcNumber) {
        try {
            String itemName = fieldAndItem.getItemNameByPlcNumber(plcNumber);
            ItemState state = itemMap.get(itemName).read(true);
            return state.getValue().getObject();
        } catch (Exception e) {
            LOGGER.error("OpcServe读取Item错误,尝试重新连接", e);
            isConnect = false;
            if (server != null) {
                server.disconnect();
            }
        }
        return null;
    }

    @PreDestroy
    public void destroy() {
        if (server != null) {
            server.disconnect();
        }
    }
}
