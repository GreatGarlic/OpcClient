package com.opc.client;

import org.jinterop.dcom.core.JIVariant;
import org.junit.Before;
import org.junit.Test;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.Async20Access;
import org.openscada.opc.lib.da.AutoReconnectController;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DcomTest {
    private static String host = "192.168.153.167";
    private static String domain = "";
    private static String progId = "Kepware.KEPServerEX.V6";
    private static String user = "Administrator";
    private static String password = "123456";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String itemName = "channel1.device1.控制字1";
    String item2 = "channelone.device1.tag3";
    String item3 = "channelone.device1.tag4";
    //    KingView.View.1
    //Kepware.KEPServerEX.V6
    String item4 = "反应罐温度.Value";
    String item5 = "liuyuan.Value";
    String item6 = "channelone.device1.Value";
    private Logger LOGGER = LoggerFactory.getLogger(DcomTest.class);
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
    public void syncReadOpcItem() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);

        try {
            server.connect();
            Group group = server.addGroup();
            Item item = group.addItem(itemName);

            while (true) {
                ItemState state = item.read(true);
                Thread.sleep(1000);
                LOGGER.debug("获取时间:{} 标签值:{}", df.format(state.getTimestamp().getTime()),
                        state.getValue().getObjectAsShort());
            }
        } catch (Exception e) {
            LOGGER.error("连接异常", e);
        }
    }

    @Test
    public void asyncReadOpcItem() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);
        AutoReconnectController controller = new AutoReconnectController(server);
        controller.connect();
        /**
         * 其中100单位为毫秒，为每次从OPC获取刷新的间隔时间
         */
        AccessBase access = new Async20Access(server, 1000, false);

        /**
         * 只有Item的值有变化的时候才会触发CallBack函数
         */
        access.addItem(item5, new DataCallback() {
            public void changed(Item item, ItemState itemstate) {
                try {
                    LOGGER.debug("获取时间:{} 标签值:{}", df.format(itemstate.getTimestamp().getTime()), itemstate.getValue().getObjectAsFloat());
                } catch (Exception e) {
                    LOGGER.error("数据获取失败", e);
                }
            }
        });
        /** 开始监听 */
        access.bind();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.error("系统异常", e);
        }
        /** 监听 结束 */
        access.unbind();

        controller.disconnect();
    }


    @Test
    public void syncWriteAndAsyncReadOpcItem() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);
        AutoReconnectController controller = new AutoReconnectController(server);
        controller.connect();

        /**
         * 其中100单位为毫秒，为每次从OPC获取刷新的间隔时间
         */
        AccessBase access = new Async20Access(server, 1000, false);

        /**
         * 只有Item的值有变化的时候才会触发CallBack函数
         */
        access.addItem(itemName, new DataCallback() {
            public void changed(Item item, ItemState itemstate) {
                try {
                    LOGGER.debug("获取时间:{} 标签值:{}", df.format(itemstate.getTimestamp().getTime()), itemstate.getValue().getObjectAsShort());
                } catch (Exception e) {
                    LOGGER.error("数据获取失败", e);
                }
            }
        });
        /** 开始监听 */
        access.bind();

        Group group = server.addGroup();
        Item item = group.addItem(itemName);

        while (true) {
            Thread.sleep(1000);
            JIVariant value = new JIVariant((short) new Random().nextInt(Short.MAX_VALUE + 1));
            item.write(value);
        }
    }

    @Test
    public void syncWriteOpcItem() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);
        try {
            server.connect();
            Group group = server.addGroup();
            Item item = group.addItem(itemName);
            JIVariant value = new JIVariant(255);
            item.write(value);
        } catch (Exception e) {
            LOGGER.error("连接异常", e);
        }
    }
}
