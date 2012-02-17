package math.compress;

import java.util.ArrayList;
import java.util.Collections;

public class FreqStatistics {
	private ArrayList<StatisticsEntry> items;
	private StatisticsEntry [] items2;
	public FreqStatistics(int size){
		items = new ArrayList<StatisticsEntry>(size);
		items2 = new StatisticsEntry[size];
	}
	
	private boolean isBlocked = false;
	public void push(int value){
		assert !isBlocked;
//		if (items.get(value)==null) items.add(value, new StatisticsEntry(value));
//		items.get(value).inc();
		
		if (items2[value] == null) items2[value] = new StatisticsEntry(value);
		items2[value].inc();
	}
	public void sort(){
		isBlocked = true;
		
		for (StatisticsEntry entry:items2){
			if (entry!=null) items.add(entry);
		}
		
		System.out.println("FreqStatistics, sort(); items size = "+items.size());
		//clean null items
//		items.removeAll(Collections.singletonList(null));
//		System.out.println("FreqStatistics, sort(); remove null, items size = "+items.size());

		Collections.sort(items);
		System.out.println("FreqStatistics, sort(); items size = "+items.size());
	}
	
	public StatisticsTreeEntry buildTree(){
		StatisticsTreeEntry treeRoot = null, minLeaf1, minLeaf2;
		StatisticsEntry item1, item2;
		while (items.size() > 1){
			item1 = items.remove(0);
			item2 = items.remove(0);
			minLeaf1 = (item1 instanceof StatisticsTreeEntry)?(StatisticsTreeEntry) item1:new StatisticsTreeEntry(item1);
			minLeaf2 = (item2 instanceof StatisticsTreeEntry)?(StatisticsTreeEntry) item2:new StatisticsTreeEntry(item2);
			treeRoot = new StatisticsTreeEntry(minLeaf1, minLeaf2);
//			System.out.println(items.size()+" items, leaf1="+minLeaf1+", leaf2="+minLeaf2+", node="+treeRoot);
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
	
	public void free(){
		items = null; 
		items2 = null;
	}
}
