package oracle_1;

public class DBConfig {
	public static String jdbc="jdbc:oracle:thin:@";
	public static String host="10.31.96.38:1521:orcl";  //10.31.96.38
	public static String userName = "lab";
	public static String userPwd = "lab";
	public static String driverName="oracle.jdbc.driver.OracleDriver";
	
	public static String deviceid=""; 
	public static String segment="";
	public static String tester="";
	
	public static String old_jdbc="jdbc:sqlserver://";
	public static String old_host="10.31.96.37:1433;";         //jdbc:sqlserver://10.31.96.37:1433;
	public static String old_userName = "sa";
	public static String old_userPwd = "Abc123";
	public static String old_dbName="DatabaseName=Elisdb";
	public static String old_csDbName="DatabaseName=Elisdb_test";
	public static String old_driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
}
