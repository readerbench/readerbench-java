package edu.cmu.lti.ws4j.util;


final public class WS4JConfiguration {
	private static final WS4JConfiguration instance = new WS4JConfiguration();
	private boolean trace;
	private boolean cache;
	private int maxCacheSize;
	private String infoContent;
	private boolean stem;
	private String stopList;
	//private String leskRelation;
	private boolean leskNormalize;
	private boolean mfs;

	/**
	 * Private constructor 
	 */
	private WS4JConfiguration(){
		try {
			cache = true;
			trace = true;
			maxCacheSize = 1000;
			infoContent = "resources/config/WN/ic-semcor.dat";
			stem = true;
			stopList = "resources/config/Stopwords/stopwords_en.txt";
			leskNormalize = true;
			mfs = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Singleton pattern
	 * @return singleton object
	 */
	public static WS4JConfiguration getInstance(){
		return WS4JConfiguration.instance;
	}

	/**
	 * @return cache
	 */
	public boolean useCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}
	
	/**
	 * @param trace the trace to set
	 */
	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	/**
	 * @return trace
	 */
	public boolean useTrace() {
		return trace;
	}

	/**
	 * @return the infoContent
	 */
	public String getInfoContent() {
		return infoContent;
	}

	/**
	 * @return the stem
	 */
	public boolean useStem() {
		return stem;
	}
	
	/**
	 * @return the stopList
	 */
	public String getStopList() {
		return stopList;
	}

	/**
	 * @param stopList the stopList to set
	 */
	public void setStopList(String stopList) {
		this.stopList = stopList;
	}
	
	/* (not yet supported)
	 * @return the leskRelation
	 
	public String getLeskRelation() {
		return leskRelation;
	}
	*/
	
	/**
	 * @return the stem
	 */
	public boolean useLeskNomalizer() {
		return leskNormalize;
	}
	
	/**
	 * @param leskNormalize the leskNormalize to set
	 */
	public void setLeskNormalize(boolean leskNormalize) {
		this.leskNormalize = leskNormalize;
	}

	/**
	 * @return the maxCacheSize
	 */
	public int getMaxCacheSize() {
		return maxCacheSize;
	}
	

	/**
	 * @return the stem
	 */
	public boolean useMFS() {
		return mfs;
	}

	/**
	 * @param mfs the mfs to set
	 */
	public void setMFS(boolean mfs) {
		this.mfs = mfs;
	}

	/**
	 * @param stem the stem to set
	 */
	public void setStem(boolean stem) {
		this.stem = stem;
	}
	
}
