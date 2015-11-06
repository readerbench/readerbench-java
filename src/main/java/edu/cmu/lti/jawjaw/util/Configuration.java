/*
 * Copyright 2009 Carnegie Mellon University
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.cmu.lti.jawjaw.util;

import edu.cmu.lti.jawjaw.pobj.Lang;

public class Configuration {

	private static Configuration instance_en = null;
	private static Configuration instance_fr = null;
	private static Configuration instance_it = null;
	private static Configuration instance_ro = null;

	public static final boolean USE_CACHE = true;
	public static final int MAX_CACHE_SIZE = 100000;

	private String wordnet;
	private boolean memoryDB;
	private int dbCacheSize;

	private Configuration(String wordnet, boolean memoryDB, int dbCacheSize) {
		this.wordnet = wordnet;
		this.memoryDB = memoryDB;
		this.dbCacheSize = dbCacheSize;
	}

	/**
	 * Singleton pattern
	 * 
	 * @return singleton object
	 */
	public static Configuration getInstance(Lang lang) {
		switch (lang) {
		case fr:
			return getWordNetFr();
		case it:
			return getWordNetIt();
		case ro:
			return getWordNetRo();
		default:
			return getWordNetEn();
		}
	}

	public static Configuration getWordNetEn() {
		if (instance_en == null)
			instance_en = new Configuration("resources/config/WN/wnjpn-0.9.db", false, 100000);
		return instance_en;
	}

	public static Configuration getWordNetFr() {
		if (instance_fr == null)
			instance_fr = new Configuration("resources/config/WN/wolf-0.1.6.db", false, 100000);
		return instance_fr;
	}

	public static Configuration getWordNetIt() {
		if (instance_it == null)
			instance_it = new Configuration("resources/config/WN/italian_wn.db", false, 100000);
		return instance_it;
	}

	public static Configuration getWordNetRo() {
		if (instance_ro == null)
			instance_ro = new Configuration("resources/config/WN/wnrom.db", false, 100000);
		return instance_ro;
	}

	/**
	 * @return the dbCacheSize
	 */
	public int getDbCacheSize() {
		return dbCacheSize;
	}

	/**
	 * @param dbCacheSize
	 *            the dbCacheSize to set
	 */
	public void setDbCacheSize(int dbCacheSize) {
		this.dbCacheSize = dbCacheSize;
	}

	/**
	 * @return the wordnet
	 */
	public String getWordnet() {
		return wordnet;
	}

	/**
	 * @return the memoryDB
	 */
	public boolean useMemoryDB() {
		return memoryDB;
	}

	/**
	 * @param memoryDB
	 *            the memoryDB to set
	 */
	public void setMemoryDB(boolean memoryDB) {
		this.memoryDB = memoryDB;
	}
}
