package com.opc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opc.client.config.AppConfig;
import com.opc.client.model.FieldAndItem;
import org.jinterop.dcom.core.JIVariant;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.Async20Access;
import org.openscada.opc.lib.da.AutoReconnectController;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.list.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.util.BitArray;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

//
//@RunWith(SpringRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
//@SpringBootTest(classes = StartProgram.class)
public class DcomTest {
    private static String host = "192.168.141.176";
    private static String domain = "";
    private static String progId = "KingView.View.1";
    private static String user = "Administrator";
    private static String password = "123456";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String itemName = "channel1.device1.控制字1";
    String item2 = "channelone.device1.tag3";
    String item3 = "channelone.device1.tag4";
    //    KingView.View.1
    //Kepware.KEPServerEX.V6

    String item5 = "开度111.Value";
    String item6 = "channelone.device1.Value";
    //    @Autowired
    AppConfig appConfig;
    private Logger LOGGER = LoggerFactory.getLogger(DcomTest.class);
    private ServerList serverList;
    private ConnectionInformation ci;

//    @Before
//    public void getOpcServerList() throws Exception {
//        serverList = new ServerList(host, user, password, domain);
//        final Collection<ClassDetails> detailsList =
//                serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer20}, new Category[]{});
//        for (final ClassDetails details : detailsList) {
//            LOGGER.debug("ProgID:{}", details.getProgId());
//            LOGGER.debug("ClsId:{}", details.getClsId());
//            LOGGER.debug("Description:{}", details.getDescription());
//        }
//        ci = new ConnectionInformation();
//        ci.setHost(host);
//        ci.setClsid(serverList.getClsIdFromProgId(progId));
//        ci.setUser(user);
//        ci.setPassword(password);
//    }

    @Test
    public void syncReadOpcItem() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Server server = new Server(ci, exec);

        try {
            server.connect();

            Group group = server.addGroup();
            Item item = group.addItem(item5);
            while (true) {
                ItemState state = item.read(true);
                Thread.sleep(2000);
                LOGGER.debug("获取时间:{} 标签值:{}", df.format(state.getTimestamp().getTime()),
                        state.getValue().getObjectAsInt());
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
                    LOGGER.debug("获取时间:{} 标签值:{}", df.format(itemstate.getTimestamp().getTime()),
                            itemstate.getValue().getObjectAsInt());
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
        access.addItem(item5, new DataCallback() {
            public void changed(Item item, ItemState itemstate) {
                try {
                    LOGGER.debug("获取时间:{} 标签值:{}", df.format(itemstate.getTimestamp().getTime()),
                            itemstate.getValue().getObjectAsInt());
                } catch (Exception e) {
                    LOGGER.error("数据获取失败", e);
                }
            }
        });
        /** 开始监听 */
        access.bind();

        Group group = server.addGroup();
        Item item = group.addItem(item5);

        while (true) {
            Thread.sleep(2000);
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
            Item item = group.addItem(item5);
            JIVariant value = new JIVariant(255);
            item.write(value);
        } catch (Exception e) {
            LOGGER.error("连接异常", e);
        }
    }

    @Test
    public void test1() {

        String[] arrayStr = appConfig.getPlcNumberDictionary().values().toArray(new String[0]);


        String[] itemName = FieldAndItem.getAllItemsByPlcNumbers(arrayStr).keySet().toArray(new String[0]);

        for (String s : itemName) {
            System.out.println(s);

        }

    }

    @Test
    public void test2() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("哈哈", "嘿嘿");
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        objectNode1.put("哈哈", "刘源");
        arrayNode.add(objectNode);
        arrayNode.add(objectNode1);
        String jsonStr = objectMapper.writeValueAsString(arrayNode);
        System.out.println(jsonStr);
        ArrayNode arrayNode1 = (ArrayNode) objectMapper.readTree(jsonStr);
        for (JsonNode haha : arrayNode1) {
            System.out.println(haha.get("哈哈").asText());
        }
    }

    @Test
    public void test3() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(formatter.print(new DateTime(new Date())));


        System.out.println(new DateTime(new Date()).toString("yyyy-MM-dd HH:mm:ss"));

    }

    @Test
    public void test4() {
        HashMap<String, FieldAndItem> haha = new HashMap<>();
        haha.put("haha", FieldAndItem.setCacheValue);
        haha.put("heihei", FieldAndItem.lowerLimit);

        HashMap<String, FieldAndItem> heihei = new HashMap<>();
        heihei.put("liuyuan", FieldAndItem.controlWord);
        heihei.put("nicai", FieldAndItem.ammeter);
        haha.putAll(heihei);

        for (Map.Entry<String, FieldAndItem> entity : haha.entrySet()) {

            System.out.println(entity.getKey());
            System.out.println(entity.getValue().getItemName());

        }
    }

    @Test
    public void test5() {
        byte[] intArray = ByteBuffer.allocate(4).putInt(5).array();
        BitArray bitArray = new BitArray(intArray.length*8, intArray);
        for (int i = 0; i < bitArray.length(); i++) {
            System.out.println(bitArray.get(bitArray.length()-1-i));

        }


    }

    @Test
    public void test6(){

        byte[] intArray = ByteBuffer.allocate(4).putInt(1).array();
        BitArray bitArray = new BitArray(intArray.length * 8, intArray);
        bitArray.set(bitArray.length() - 1 - 1, true);
        System.out.println( ByteBuffer.wrap(bitArray.toByteArray()).getInt());
    }

}
