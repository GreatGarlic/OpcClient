package com.opc.client.util;


import com.opc.client.config.AppConfig;
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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class OpcClient {
    @Autowired
    AppConfig appConfig;
    private ConnectionInformation ci;
    private Server server;
    private Logger LOGGER = LoggerFactory.getLogger(OpcClient.class);
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ScheduledExecutorService exec;
    private volatile boolean isConnect = false;
    private Group group;
    private Map<String, Item> itemMap;

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
            ci = new ConnectionInformation();
            ci.setHost(appConfig.getHost());
            ci.setClsid(serverList.getClsIdFromProgId(appConfig.getProgId()));
            ci.setUser(appConfig.getUser());
            ci.setPassword(appConfig.getPassword());
            exec = Executors.newSingleThreadScheduledExecutor();
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
            getAllItemValue();

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
//            itemMap = group.addItems(appConfig.getItemNames());
        itemMap = group.addItems("闸2设定.Value");
        isConnect = true;
    }


    public String getAllItemValue() {
        String str = "";
        try {
            ItemState state = itemMap.get("闸2设定.Value").read(true);

            str = "标签值" + state.getValue().getObjectAsInt();
            LOGGER.debug("获取时间:{} 标签值:{}", df.format(state.getTimestamp().getTime()),
                    state.getValue().getObjectAsInt());
        } catch (Exception e) {
            LOGGER.error("获取变量数据出错", e);
            isConnect = false;
        }
        return str;
    }

    @PreDestroy
    public void destroy() {
        if (server != null) {
            server.disconnect();
        }
    }
}
