package com.opc.client;

import org.junit.Before;
import org.junit.Test;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Test1 {
    private Logger LOGGER = LoggerFactory.getLogger(Test1.class);



    private static String host = "localhost";
    private static String domain = "";
    private static String progId = "KingView.View.1";
    private static String user = "Administrator";
    private static String password = "123456";

    private ServerList serverList;
    private ConnectionInformation ci;


    @Before
    public void getOpcServerList() throws Exception {
        serverList = new ServerList(host, user, password, domain);
        final Collection<ClassDetails> detailsList =
                serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer20}, new Category[]{});
        for (final ClassDetails details : detailsList) {
            LOGGER.debug("ProgID:{}", details.getProgId());
            LOGGER.debug("ClsId:{}", details.getClsId());
            LOGGER.debug("Description:{}", details.getDescription());
        }
        ci = new ConnectionInformation();
        ci.setHost(host);
        ci.setClsid(serverList.getClsIdFromProgId(progId));
        ci.setUser(user);
        ci.setPassword(password);
    }


    @Test
    public void getOpcServerClient() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);
        try {
            server.connect();
        } catch (Exception e) {
            LOGGER.error("连接异常",e);
        }

    }

}
