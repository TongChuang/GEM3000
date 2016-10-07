package oracle;

import oracle.sql.TIMESTAMP;

public class ResultLog {
	String HINT;
	String SAMPLENO;
	String TESTID;
	String CORRECTFLAG;
	String DEVICEID;
	oracle.sql.TIMESTAMP MEASURETIME;
	String OPERATOR;
	String REFHI;
	String REFLO;
	String RESULTFLAG;
	String SAMPLETYPE;
	String TESTRESULT;
	int TESTSTATUS;
	String UNIT;
	int EDITMARK;
	int ISPRINT;
	int CLOUDMARK;
	String TESTNAME;
	String METHOD;
	int ID;
	String LOGIP;
	String LOGGER;
	oracle.sql.TIMESTAMP LOGTIME;
	String LOGOPERATE;

	public ResultLog(){
		
	}
	public ResultLog(String hINT, String sAMPLENO, String tESTID,
			String cORRECTFLAG, String dEVICEID, TIMESTAMP mEASURETIME,
			String oPERATOR, String rEFHI, String rEFLO, String rESULTFLAG,
			String sAMPLETYPE, String tESTRESULT, int tESTSTATUS, String uNIT,
			int eDITMARK, int iSPRINT, int cLOUDMARK, String tESTNAME,
			String mETHOD, int iD, String lOGIP, String lOGGER,
			TIMESTAMP lOGTIME, String lOGOPERATE) {
		super();
		HINT = hINT;
		SAMPLENO = sAMPLENO;
		TESTID = tESTID;
		CORRECTFLAG = cORRECTFLAG;
		DEVICEID = dEVICEID;
		MEASURETIME = mEASURETIME;
		OPERATOR = oPERATOR;
		REFHI = rEFHI;
		REFLO = rEFLO;
		RESULTFLAG = rESULTFLAG;
		SAMPLETYPE = sAMPLETYPE;
		TESTRESULT = tESTRESULT;
		TESTSTATUS = tESTSTATUS;
		UNIT = uNIT;
		EDITMARK = eDITMARK;
		ISPRINT = iSPRINT;
		CLOUDMARK = cLOUDMARK;
		TESTNAME = tESTNAME;
		METHOD = mETHOD;
		ID = iD;
		LOGIP = lOGIP;
		LOGGER = lOGGER;
		LOGTIME = lOGTIME;
		LOGOPERATE = lOGOPERATE;
	}

}
