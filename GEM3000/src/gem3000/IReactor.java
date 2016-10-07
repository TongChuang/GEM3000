package gem3000;

public interface IReactor {
   String parseMsg(String str);
   <T>T queryData(String str);
}
