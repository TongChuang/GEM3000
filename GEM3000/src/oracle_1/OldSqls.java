package oracle_1;

public interface OldSqls {
	String sql_1 = "select yqdh,cdrq,ybbh from lis_ybxx where ybid=?";
	String sql_2 = "select xmdh from lis_xmcdz where yqdh=? and cdrq=? and ybbh=?";
	String sql_3 = "select yqxmdh from xt_yqxmdh where yqdh=? and xmdh=?";
	String monitor = "select ybid,brbq from lis_ybxx where yqdh=? and cdrq=? and ybzt=?";
	String updateById = "update c set c.xmcdz=?,c.yscdz=? from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c"
			+ "where a.ybid=? and a.cdrq=c.cdrq and a.ybbh=c.ybbh and b.yqxmdh=? and b.yqdh='RUNDA' and b.xmdh=c.xmdh";
	String updateByBh = "update c set c.xmcdz=?,c.yscdz=? from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c "
			+ "where a.ybbh=?  and a.cdrq=? and a.cdrq=c.cdrq and a.ybbh=c.ybbh and b.yqxmdh=? and b.yqdh='RUNDA' and b.xmdh=c.xmdh";
	String check_ByBh="select c.yscdz from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c where ";
	
	//初始化
	String xt_xmb="select yqdh,xmdh,xmmc,xmdw,testmethod from xt_xmb where yqdh=?";
	String xt_yqxmdh="select yqxmdh,xmdh from xt_yqxmdh where yqdh=?";
	
	String getSampleNoByCode="select ybbh from lis_ybxx where ybid=?";
}
