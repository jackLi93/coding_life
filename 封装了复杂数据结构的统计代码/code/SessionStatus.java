package cn.migu.adp.dmp.query_service;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by Smart on 2016/9/27.
 */
public class SessionStatus implements Comparable {

    private String userName;    //访问用户名
    private String tableName;   //访问表名
    private String clientIp;    //访问客户端IP
    private long queryDelay;    //查询耗时时间
    private int status;         //状态：1=succ

    public SessionStatus(String userName, String tableName, String clientIp, long queryDelay, int status) {
        this.userName = userName;
        this.tableName = tableName;
        this.clientIp = clientIp;
        this.queryDelay = queryDelay;
        this.status = status;
    }

    public  String getKey(){
        return userName + '\t' + tableName + '\t' + clientIp;
    }

    public static String getKey(String userName, String tableName, String clientIp, String nodename) {
        return userName + '\t' + tableName + '\t' + clientIp + '\t' + nodename;
    }

    public static String getKey(SessionStatus status, String nodename) {
        return status.userName + '\t' + status.tableName + '\t' + status.clientIp + '\t' + nodename;
    }

    public String getUserName() {
        return userName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getClientIp() {
        return clientIp;
    }

    public int getStatus() {
        return status;
    }

    public long getQueryDelay() {
        return this.queryDelay;
    }

    @Override
    public String toString() {
        return userName + " " + tableName + " " + clientIp + " " + queryDelay + " " + status;
    }

    @Override
    public int compareTo(Object o) {
        if (o != null && o instanceof SessionStatus) {
            SessionStatus status = (SessionStatus) o;
            if (this.queryDelay < status.getQueryDelay()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    public static void main(String[] args) {
        TreeSet<SessionStatus> treeSet = new TreeSet<>();

        System.out.println("开始生成对象");
        Random r = new Random();
        for (int i = 0; i < 1000000; i++) {
            treeSet.add(new SessionStatus("user1", "table1", "127.0.0.1", 5L, 1));
//            treeSet.add(new SessionStatus("user" + r.nextInt(100), "table1" + r.nextInt(100), "127.0.0.1", (long) Math.abs(r.nextInt(100)), 1));
        }
        treeSet.add(new SessionStatus("user1", "table1", "127.0.0.1", 2L, 1));
        treeSet.add(new SessionStatus("user1", "table1", "127.0.0.1", 10L, 1));

        System.out.println("结束生成对象，大小：" + treeSet.size());


        System.out.println("开始计算");
        long start = System.currentTimeMillis();
        long sum = 0L;
        long delay99 = 0L;
        int indexForDelay99 = 0;
        long delay999 = 0L;
        int indexForDelay999 = 0;
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
//            System.out.println(sess);
    }

    System.out.println("avg: " + sum / treeSet.size() + "ms");
    System.out.println("delay99: <" + delay99 + "ms");
    System.out.println("delay999: <" + delay999 + "ms");
    System.out.println("min: " + treeSet.first().getQueryDelay() + "ms");
    System.out.println("max: " + treeSet.last().getQueryDelay() + "ms");

    long end = System.currentTimeMillis();
    System.out.println("计算耗时：" + (end - start) + "ms");
    }
}
