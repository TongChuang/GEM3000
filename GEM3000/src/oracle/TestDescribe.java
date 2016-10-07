package oracle;

public class TestDescribe {
	// deviceid
	public String instrument;
	public String unit;
	public String index_id; // testid
	public String sample_from; // sampletype
	public String name; // testname
	public String method;

	@Override
	public String toString() {
		return "TestDescribe [instrument=" + instrument + ", unit=" + unit
				+ ", index_id=" + index_id + ", sample_from=" + sample_from + ", name="
				+ name + ", method=" + method + "]";
	}

}
