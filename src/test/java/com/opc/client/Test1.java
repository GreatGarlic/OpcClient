package com.opc.client;

import org.junit.Test;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Test1 {

    private static String host = "localhost";
    private static String domain = "";
    private static String progId = "KingView.View.1";
    private static String user = "Administrator";
    private static String password = "123456";
    private Logger LOGGER = LoggerFactory.getLogger(Test1.class);

    @Test
    public void demo1() throws Exception {
        ServerList serverList = new ServerList(host, user, password, domain);
        final Collection<ClassDetails> detailsList = serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer20}, new Category[]{});
        for (final ClassDetails details : detailsList) {
            LOGGER.debug("ProgID:{}",details.getProgId());
            LOGGER.debug("ClsId:{}",details.getClsId());
            LOGGER.debug("Description:{}",details.getDescription());
        }

    }
}
