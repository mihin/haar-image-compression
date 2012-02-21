package math.compress;

import java.util.ArrayList;
import java.util.List;

public class HTreeMap {
	private ArrayList<HCode> items;
	public HTreeMap() {
//		super();
		items = new ArrayList<HCode>();
	}
	public boolean add(int value, String code) {
		return items.add(new HCode(value, code));
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (items!=null){
			for (HCode item:items){
				sb.append(item.toString()+", ");
			}
		} else sb.append("null");
		return sb.toString();
	}
	
	public List<Boolean> getBits(int value){
		try {
			for (HCode c:items)
				if (value == c.getValue()) 
					return c.getBits();
		} catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
		return null;
	}
	public String getCodeString(int value){
		for (HCode c:items)
			if (value == c.getValue()) 
				return c.getCode();
		return null;
	}
	public int getValue(String code){
		for (HCode c:items)
			if (c.getCode().startsWith(code)){
				if (c.getCode().equals(code)) return c.getValue();
				else return -1;
			}
		return -2;
	}
}

class HCode{
	private int value;
	private String code;
	public HCode(int value, String code) {
		super();
		this.value = value;
		this.code = code;
	}
	@Override
	public String toString() {
		return value+"->\""+code+"\"";
	}
	public int getValue() {
		return value;
	}
	public String getCode() {
		return code;
	}
//	public boolean[] getBits() throws Exception {
//		boolean[] bits = new boolean[code.length()];
////		for (Character c:code.toCharArray()){
////			if (c.equals('0'))
////		}
//		for (int i=0;i<code.length();i++)	{
//			if (code.charAt(i)=='0') bits[i] = false; 
//			else if (code.charAt(i)=='1') bits[i] = true;
//			else throw new Exception("blabla");
//		}
//		return bits;
//	}
	public List<Boolean> getBits() throws Exception {
		final List<Boolean> res = new ArrayList<Boolean>();
		for (int i=0;i<code.length();i++)	{
			if (code.charAt(i)=='0') res.add(false); 
			else if (code.charAt(i)=='1') res.add(true);
			else throw new Exception("blabla");
		}
		return res;
	}
}