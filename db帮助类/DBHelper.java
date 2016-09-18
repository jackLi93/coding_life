package cn.migu.adp.dmp;

import java.sql.*;

/**
 * Created by jacklee on 2016/9/18.
 * this class is used to get connection and control data of database
 * 需要注意如下几点:
 * 1.需要将驱动导入，
 * 2.命令行模式下能编译该类，但是无法运行该类，原因在于此程序打包去掉即可
 */
public class DBHelper {

    public static  final String MYSQL_DRIVER="com.mysql.jdbc.Driver";
    //数据库url地址格式："jdbc:mysql://127.0.0.1/student";
    private static String DB_URL_FORMAT = "jdbc:mysql://%s:%d/?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true&noAccessToProcedureBodies=true&Pooling=false&autoReconnect=true&maxReconnects=3&initialTimeout=6";
    private static final int QUERY_TIMEOUT = 600 * 1000;
    private String ip;
    private  int port;
    private String userName;
    private  String passwd;
    private Connection conn;

    public DBHelper(String ip, int port, String userName, String passwd) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.passwd = passwd;
    }

    /**
     * open db
     * @return
     */
    public boolean openDB(){
        String connUrl = String.format(DB_URL_FORMAT, this.ip, this.port);
            try {
                Class.forName(MYSQL_DRIVER);
                conn = DriverManager.getConnection(connUrl,this.userName,this.passwd);
                conn.setAutoCommit(false);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return  false;
            } catch (SQLException e) {
                e.printStackTrace();
                return  false;
            }
        return  true;
    }

    /**
     * get sql querry result_set
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet execSQL(String sql) throws SQLException {
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(QUERY_TIMEOUT);
        return statement.executeQuery(sql);
    }
    public  boolean close(){
        if(conn!=null){
            try {
                conn.close();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }else {
            return  true;
        }


    }

    public static void main(String []args){
        DBHelper dbHelper = new DBHelper("127.0.0.1",3306,"jack","jack666");
        if(dbHelper.openDB()){
            String sql = "select * from table test";

            try {
                ResultSet resultSet = dbHelper.execSQL(sql);
                System.out.print(resultSet.getFetchSize());
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                dbHelper.close();
            }

        }

    }
}
