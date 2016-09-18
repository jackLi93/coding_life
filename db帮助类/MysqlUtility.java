package cn.migu.adp.dmp.query_service;

import cn.migu.adp.dmp.query_service.bean.QueryServiceInfo;

import java.sql.*;


/**
 * Created by Smart on 2016/9/8.
 */
public class MysqlUtility {

    private static String DB_URL_FORMAT = "jdbc:mysql://%s:%d/?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true&noAccessToProcedureBodies=true&Pooling=false&autoReconnect=true&maxReconnects=3&initialTimeout=6";
    private static final int QUERY_TIMEOUT = 600 * 1000;

    private String ip = "";
    private long port = 0L;
    private String userName = "";
    private String passwd = "";
    private Connection conn = null;

    public MysqlUtility(String ip, long port, String userName, String passwd) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.passwd = passwd;
    }

    public boolean getConnection() {
        String connUrl = String.format(DB_URL_FORMAT, this.ip, this.port);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connUrl, this.userName, this.passwd);
            conn.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isConnOk() {
        if (null == conn) {
            return false;
        }
        try {
            if (conn.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeConn() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException{
        Statement execSql = null;
        execSql = conn.createStatement();
        execSql.setQueryTimeout(QUERY_TIMEOUT);
        return execSql.executeQuery(sql);
    }

    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void getQueryServiceInfo(){

    }

    public void updateDmpStatusInfo(){

    }


    public static void main(String[] args) {

        long currentTimeMillis = System.currentTimeMillis();
        MysqlUtility mysqlUtility = new MysqlUtility("127.0.0.1", 3306, "root", "xylx1.t!@#");
        mysqlUtility.getConnection();
        if(mysqlUtility.isConnOk()){
            System.out.println("conn is ok");
            ResultSet rs = null;
            try {
                rs = mysqlUtility.executeQuery("select * from migu_adp_dmp.user_conf_table");
                while (rs.next()){
                    String userName = rs.getString(1);
                    String tableName = rs.getString(2);
                    String secretCode = rs.getString(3);
                    long startTime = rs.getLong(4);
                    long endTime = rs.getLong(5);
                    long maxQueryPerMinute = rs.getLong(6);
                    int isVisible = rs.getInt(7);

                    //update-1:delete isVisible=0, and delete expired items
                    if(isVisible == 0 || currentTimeMillis > endTime || currentTimeMillis < startTime) {
                        AccessControl.serviceInfoMap.remove(QueryServiceInfo.getKey(userName, tableName));
                    }

                    //update-2:update others
                    QueryServiceInfo qinfo = new QueryServiceInfo(userName, tableName, secretCode, startTime, endTime, maxQueryPerMinute);
                    AccessControl.serviceInfoMap.put(QueryServiceInfo.getKey(userName, tableName), qinfo);

                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                mysqlUtility.closeConn();
            }

        }
    }
}
