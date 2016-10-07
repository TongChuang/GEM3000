package oracle_1;

public interface NewSqls {
	//初始化
	String lab_index_d="select instrument,index_id,method,name,sample_from,unitfrom lab_index where " +
			"instrument like '%XMZ-BN2%'OR instrument like '%XMZ-XP%'";
	String l_device_d = "select * from l_device where id in ('XMZ-XP','XMZ-BN2')";
	String l_ylxhdescribe_d = "select ylxh,profiletest from l_ylxhdescribe where in ('21','200010')";
	String lab_channel_d = "select testid,channel,sampletype from lab_channel where deviceid in ('RL7600','PREMIER300')";
	
	
	
	//-------------------------------------------------
	String l_device = "select * from l_device where id=?";
	String l_ylxhdescribe = "select ylxh,profiletest from l_ylxhdescribe where ksdm=?";
	String lab_channel = "select testid,channel,sampletype from lab_channel where deviceid=?";
	String lab_index="select instrument,index_id,method,name,sample_from,unit,isprint from lab_index where instrument like ?";
	String lab_reference = "select * from lab_testreference where testid in (?) order by testid, orderno asc";
    
	//查询
	String getSampleBy_code = "select age,sex, sampleno,cycle, ylxh,symstatus from l_sample where barcode=? and rownum<=1";
	String getsampleBy_No = "select age, sex, sampleno, ylxh, cycle, symstatus from l_sample where sampleno=? and rownum<=1";
	
	//保存
	String get_checker="select tester from l_tester_set where deviceid=? and segment=?";
	String update_sample="update l_sample set auditstatus=?,samplestatus=?,chkoper2=? where sampleno=?";
	String has_l_sample_update="select auditstatus,samplestatus from l_sample where sampleno=?";
	String hasTestResult_sql = "select * from l_testresult where sampleno=? and testid=? and rownum<=1";
	String insert_TestResult = "insert into l_testresult (sampleno,testid,deviceid,measuretime,operator,sampletype," +
			"testresult,teststatus,unit,testname,method,correctflag,refhi,reflo,resultflag,isprint) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String update_TestResult = "update l_testresult set testresult=?,deviceid=?, measuretime=?,operator=? where sampleno=? and testid=?";

    String new_update_result="update l_testresult set testresult=?,measuretime=? ,resultflag=? where sampleno=? and testid=?";
    String getSampleByCode="select age, sex, sampleno, cycle,ageunit from L_SAMPLE where barcode=? and rownum<=1";
    String getSampleByNO="select age, sex, sampleno, cycle,ageunit from L_SAMPLE where sampleno=? and rownum<=1";
    String insert_TestResult_log="insert into l_testresult_log (sampleno,testid,correctflag,deviceid,measuretime,operator,refhi,reflo,resultflag,sampletype," +
			"testresult,teststatus,unit,editmark,isprint,cloudmark,testname,method,id,logip,logger,logoperate) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,ID.Nextval,?,?,?)";
    
    //监视
    String monitor_sql="";
    
}
