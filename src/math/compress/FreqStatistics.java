package math.compress;

import java.util.ArrayList;
import java.util.Collections;

public class FreqStatistics {
	private ArrayList<StatisticsEntry> items;
	public FreqStatistics(int size){
		items = new ArrayList<StatisticsEntry>(size);
	}
	
	private boolean isBlocked = false;
	public void push(int value){
		assert !isBlocked;
		if (items.get(value)==null) items.add(value, new StatisticsEntry(value));
		items.get(value).inc();
	}
	public void sort(){
		isBlocked = true;
		
		System.out.println("FreqStatistics, sort(); items size = "+items.size());
		//clean null items
		items.removeAll(Collections.singletonList(null));
		System.out.println("FreqStatistics, sort(); remove null, items size = "+items.size());

		Collections.sort(items);
	}
	
	public StatisticsTreeEntry buildTree(){
		StatisticsTreeEntry treeRoot = null, minLeaf1, minLeaf2;
		while (items.size() > 1){
			minLeaf1 = new StatisticsTreeEntry(items.get(0));
			minLeaf2 = new StatisticsTreeEntry(items.get(1));
			treeRoot = new StatisticsTreeEntry(minLeaf1, minLeaf2);
			
			items.remove(0);
			items.remove(1);
			items.add(treeRoot);
			Collections.sort(items); 
		}
		items.clear();
		return treeRoot;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (StatisticsEntry entry:items){
			sb.append(entry!=null?entry.toString()+", ":"null, ");
		}
		return sb.toString();
	}
	

}
