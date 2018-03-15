package com.readerbench.gma.models;

import java.util.*;
import java.util.Map.Entry;

public class MapUtils {

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Entry<K, V>> list = new LinkedList(map.entrySet());

		Comparator comparator = new Comparator<Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return -1*(o1.getValue()).compareTo(o2.getValue());
			}
		};

		Collections.sort(list, comparator);


		Map<K, V> result = new HashMap<K, V>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
