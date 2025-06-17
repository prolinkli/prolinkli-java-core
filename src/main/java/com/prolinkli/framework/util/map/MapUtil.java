package com.prolinkli.framework.util.map;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

	/**
	 * Merges two maps into a new map.
	 *
	 * @param <K>  the type of keys in the maps
	 * @param <V>  the type of values in the maps
	 * @param map1 the first map
	 * @param map2 the second map
	 * @return a new map containing all entries from both maps
	 */
	public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> mergedMap = new HashMap<>(map1);
		mergedMap.putAll(map2);
		return mergedMap;
	}

}
