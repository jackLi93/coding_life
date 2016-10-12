package com.jack.codelife

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2016/9/30.
 */
public class StatusCollectService {
    private static  final GLog log = new GLog(StatusCollectService.class);
    public static  long CollectInterval = 5*60*1000;
    private static final String dbHost = "127.0.0.1";
    private static final int dbPort = 3306;
    private static final String dbUser = "root";
    private static final String dbPass = "xylx1.t!@#";

    private  StatusCollectService(){

    }

    private  static  StatusCollectService statusCollectService = new StatusCollectService();

    public static  StatusCollectService getStatusCollectService(){
        return  statusCollectService;
    }


 //   public  static TreeSet<SessionStatus> treeSet = new TreeSet<>(); //---->一个tree只能存一个key的数据

    public static ConcurrentHashMap<String ,TreeSet<SessionStatus>>  statusCache = new ConcurrentHashMap<>();

    //插入与取数据流程：1.--->插入一个status时，调用insert方法，内部操作需要进行判断，如果已经有该statuskey对应的tree，则从cache取出
    //否则，new一个tree中，加入cache中
    /**
     * insert a record of sessionStatus
     * @param status
     */
    public synchronized void insertStatus(SessionStatus status){
        String userKey  = status.getKey();
        if(statusCache.containsKey(userKey)){

            TreeSet<SessionStatus> treeSet = statusCache.get(userKey);
            treeSet.add(status);
            log.info("get treeset of key:"+userKey+"treeset size is :"+treeSet.size() );
        }else {
            TreeSet<SessionStatus> treeSet = new TreeSet<>();
            treeSet.add(status);
            statusCache.put(userKey,treeSet);
            log.info("new a treeset and add status...");
        }

        //treeSet.add(status);
    }

    /**
     * get  max delay for a special key
     * @param userkey
     * @return
     */
    public  long getMaxDelay(String userkey){
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        if(treeSet!=null) {
            log.debug(userkey + "----max----" + treeSet.last().getQueryDelay());
            return treeSet.last().getQueryDelay();
        }
        return -1;
    }

    /**
     * get min delay for a special key
     * @param userkey
     * @return
     */
    public   long getMinDelay(String userkey){
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        if(treeSet!=null){
            log.debug(userkey + "----min----" + treeSet.first().getQueryDelay());
            return  treeSet.first().getQueryDelay();
        }

        return -1;

    }

    /**
     * get 999 delay--->0.999
     * @param userkey
     * @return
     */
    public  long getDelay999(String userkey){
        int counter =0;
        long delay999=0l;
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        Iterator<SessionStatus> iterator = treeSet.iterator();
        while (iterator.hasNext()){
            counter++;
            if(counter>=treeSet.size()*0.999){
                SessionStatus status = iterator.next();
                delay999=status.getQueryDelay();
                return delay999;
            }
        }
        return 0;
    }

    /**
     * get 99 delay --->0.99
     * @param userkey
     * @return
     */
    public   long getDelay99(String userkey){
        int counter =0;
        long delay99=0l;
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        Iterator<SessionStatus> iterator = treeSet.iterator();
        while (iterator.hasNext()){
            counter++;
            if(counter>=treeSet.size()*0.99){
                SessionStatus status = iterator.next();
                delay99=status.getQueryDelay();
                return delay99;
            }
        }
        return 0;
    }

    public   long getAvrDelay(String userkey){

        long sum =0;
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        Iterator<SessionStatus> iterator = treeSet.iterator();
        while (iterator.hasNext()){
            SessionStatus status = iterator.next();
            sum=sum+status.getQueryDelay();
        }
        long avrDelay = sum/treeSet.size();
        return avrDelay;
    }

    /**
     * 将延时的相关计算以数组方式返回，减少多余的遍历
     * @param userkey
     * @return
     */
    public  long[] getDelays(String userkey){
        long [] delays = new long[3];
        long sum = 0L;
        long delay99 = 0L;
        int indexForDelay99 = 0;
        long delay999 = 0L;
        int indexForDelay999 = 0;
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        Iterator<SessionStatus> sessionStatusIterator = treeSet.iterator();
        while (sessionStatusIterator.hasNext()) {
            SessionStatus sess = sessionStatusIterator.next();
            long current = sess.getQueryDelay();
            sum += current;
            indexForDelay99++;
            indexForDelay999++;
            if (delay99 == 0L && indexForDelay99 >= treeSet.size() * 0.99) {
                delay99 = current;
            }
            if (delay999 == 0L && indexForDelay999 >= treeSet.size() * 0.999) {
                delay999 = current;
            }
        }
        delays[0]=delay99;
        delays[1]=delay999;
        delays[2]=sum/treeSet.size();
        return  delays;
    }

    /**
     * 返回请求次数
     * @param userkey
     * @return
     */
    public   int getRequestNum(String userkey){
        return  getTreeSet(userkey).size();
    }

    /**
     * 返回成功的请求次数
     * @param userkey
     * @return
     */
    public   int getSuccessNum(String userkey){
        TreeSet<SessionStatus> treeSet = getTreeSet(userkey);
        Iterator<SessionStatus> iterator = treeSet.iterator();
        int successNum=0;
        while (iterator.hasNext()){
            SessionStatus status=iterator.next();
            if(status.getStatus()==1){
                successNum++;
            }
        }
        return successNum;
    }
    private  TreeSet<SessionStatus> getTreeSet(String userKey){
        if(statusCache.containsKey(userKey)){
            return  statusCache.get(userKey);
        }
        log.error("invalid key to get treeset...");
        return  null;
    }

    /**
     * 内部服务，实现定时同步cache到mysql
     * 只能单线程运行
     */
    public synchronized  void sync2DBService(){
        long currentTimeMillis = System.currentTimeMillis();
        int  counter=0;
        MysqlUtility mysqlUtility = new MysqlUtility(dbHost, dbPort, dbUser, dbPass);
        if(mysqlUtility.getConnection() && mysqlUtility.isConnOk()) {
            log.debug("conn is ok");
            String nodeName = GetLocalIp.getNodeName();
          // log.info(nodeName);
            Iterator<String> iterator = statusCache.keySet().iterator();
            while (iterator.hasNext()){
                counter++;
                String userksey = iterator.next();
                String [] keys = userksey.split("\t");
                if(keys.length!=3){
                    continue;
                }
                String username = keys[0];
                String tableName = keys[1];
                String clientIp = keys[2];
                long minDelay = getMinDelay(userksey);
                long maxDelay  = getMaxDelay(userksey);
/*                long delay99 = getDelay99(userksey);
                long delay999 = getDelay999(userksey);
                long delayAvr = getAvrDelay(userksey);*/
                long [] delays = getDelays(userksey);
                long delay99=delays[0];
                long delay999 =delays[1];
                long avrdelay=delays[2];
                long inserTime = currentTimeMillis;
                int requestNum = getRequestNum(userksey);
                int successNum = getSuccessNum(userksey);
                //('" + keys[0] + "', '" + keys[1] + "', '" + threadUUID + "', '" + currentTimeMillis +"')"
                String insertSql = "INSERT INTO migu_adp_dmp.status_collect_info (`userName`, `tableName`,`clientIp`,`nodeName`,`insert_time`, `maxDelay`, `minDelay`, `delay99`, `delay999`, `avrDelay`, `requestNum`, `successNum`) VALUES ('"+username +"','"+tableName +"','"+clientIp +"','"+nodeName+"','"+currentTimeMillis +"','"+maxDelay+"','"+minDelay+"','"+delay999+"','"+delay99+"','"+avrdelay+"','"+requestNum+"','"+successNum+"')";
                //注意sql语句的语法，引号勿忘。
                try {
                    System.out.println(insertSql);
                   mysqlUtility.executeUpdate(insertSql);
                       // mysqlUtility.executeUpdate("INSERT INTO migu_adp_dmp.status_collect_info (`userkey`, `insert_time`, `maxDelay`, `minDelay`, `delay99`, `delay999`, `avrDelay`, `requestNum`, `successNum`) VALUES ('jack6', '8888', '8888888', '666', '777777', '887777', '666666', '9999', '8888')");
                    log.debug("insert a record into db ...");
                    statusCache.remove(userksey);//取出
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
           // statusCache.clear();//不用clear 采用remove比较好，每次向db中写入数据后即remover掉即可。
            log.info("本次同步操作共插入"+counter+"条数据到数据库，共花费："+(System.currentTimeMillis()-currentTimeMillis)+"ms");
        }
    }
    public static class syncTaskTimer{
        public syncTaskTimer(long period){
            Calendar cal = Calendar.getInstance();
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            long delay = (60*60*1000 - (minute*60+second)*1000) % period;
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(
                    new TimerTask(){
                        @Override
                        public void run(){
                            //syncFromDB();
                            getStatusCollectService().sync2DBService();
                        }
                    },
                    delay,
                    period
            );
        }
    }
    public static void main(String []args){
        System.out.println("good job.....");

        final StatusCollectService statusCollectService =  StatusCollectService.getStatusCollectService();
        new syncTaskTimer(1*5*1000L);
        SessionStatus statusTest1 = new SessionStatus("user1", "table1", "127.0.0.1", 6L, 1);
        SessionStatus statusTest2 = new SessionStatus("user2", "table1", "127.0.0.1", 8L, 1);
        SessionStatus statusTest3 = new SessionStatus("user3", "table1", "127.0.0.1", 9L, 1);
        SessionStatus statusTest4 = new SessionStatus("user2", "table1", "127.0.0.1", 120L, 1);
        SessionStatus statusTest5 = new SessionStatus("user1", "table1", "127.0.0.1", 660L, 1);
        SessionStatus statusTest6 = new SessionStatus("user3", "table1", "127.0.0.1", 6667L, 1);
        SessionStatus statusTest7 = new SessionStatus("user1", "table1", "127.0.0.1", 6688L, 1);
        SessionStatus statusTest8 = new SessionStatus("user2", "table1", "127.0.0.1", 6699L, 1);
        statusCollectService.insertStatus(statusTest1);
        statusCollectService.insertStatus(statusTest2);
        statusCollectService.insertStatus(statusTest3);
        statusCollectService.insertStatus(statusTest4);
        statusCollectService.insertStatus(statusTest5);
        statusCollectService.insertStatus(statusTest6);
        statusCollectService.insertStatus(statusTest7);
        statusCollectService.insertStatus(statusTest8);
        statusCollectService.insertStatus(new SessionStatus("user6", "table1", "127.0.0.1", 6699L, 1));
        statusCollectService.insertStatus(new SessionStatus("user8", "table1", "127.0.0.1", 6699L, 1));
        long  currenttime = System.currentTimeMillis();
/*        for(int i=0;i<1000;i++){
            String usrtest= "usertest"+i;
            for(int j=0;j<1000;j++)
            statusCollectService.insertStatus(new SessionStatus(usrtest, "table1", "127.0.0.1", 6699L+10*j, 1));
        }

        System.out.println("--------------插入100万条数据耗时："+(System.currentTimeMillis()-currenttime)+"ms");*/
        String key1 = statusTest1.getKey();
        String key2 = statusTest2.getKey();
        String key3 = statusTest3.getKey();
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    statusCollectService.insertStatus(new SessionStatus("user6", "table1", "127.0.0.1", 6699L, 1));
                    statusCollectService.insertStatus(new SessionStatus("user8", "table1", "127.0.0.1", 6699L, 1));
                }
            }
        }).start();
        Thread.yield();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        statusCollectService.getMaxDelay(key1);
        statusCollectService.getMaxDelay(key2);
        statusCollectService.getMaxDelay(key3);
        statusCollectService.getMinDelay(key1);
        statusCollectService.getMinDelay(key2);
        statusCollectService.getMinDelay(key3);

        System.out.println("---------------功能测试--------------over");

        //statusCollectService.sync2DBService();
        //SessionStatus.syncTaskTimer syncTaskTimer = new StatusCollectService().syncTaskTimer(1*60*1000L);
      //  new syncTaskTimer(1*5*1000L);

       // System.out.println(10000>1000*0.9999);
/*        final Random random = new Random();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true)
                statusCollectService.insertStatus(new SessionStatus("kktest"+random.nextInt(), "table1", "127.0.0.1", random.nextLong(), 1));
            }
        }).start();*/

    }

}
