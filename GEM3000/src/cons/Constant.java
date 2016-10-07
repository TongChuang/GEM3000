package cons;

public interface Constant {
	String is_ack = String.valueOf((char)06);
	String is_enq = String.valueOf((char)05);
	String is_eot = String.valueOf((char)04);
	String is_etx = String.valueOf((char)03);
	String is_stx = String.valueOf((char)02);
	String is_nak = String.valueOf((char)21);
	String is_etb = String.valueOf((char)23);
	String is_ret=String.valueOf((char)10);  //    LF
	String is_car=String.valueOf((char)13);  //    CR   \r    回车
	
	String is_vl = String.valueOf((char)124); //  垂线 ｜
	String is_bs = String.valueOf((char)92); //   反斜杠  \
	String is_caret = String.valueOf((char)94); // 脱字符 ^
	String is_amp = String.valueOf((char)38); // 和号 &
	char ret = (char)10;  //    \n
	char car = (char)13;  //    \r
	
	String log_ad = "D://Commincation/templog.txt";
	String resdata_ad = "C://lis/liscomm/out/";
	String data_ad = "C://lis/liscomm/comm1.db";
	String config = "C://lis/liscomm/java/config.txt";
}
