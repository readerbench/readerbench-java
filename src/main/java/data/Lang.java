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
package data;

import java.io.Serializable;

public enum Lang implements Serializable {
	eng, fr, it, jpn, ro, es, nl, la;

	public static final String[] SUPPORTED_LANGUAGES = { "English", "French", "Italian", "Spanish", "Romanian", "Dutch",
			"Latin" };

	public static Lang getLang(String language) {
		switch (language) {
		case "French":
			return Lang.fr;
		case "Italian":
			return Lang.it;
		case "Spanish":
			return Lang.es;
		case "Romanian":
			return Lang.ro;
		case "Dutch":
			return Lang.nl;
		case "Latin":
			return Lang.la;
		default:
			return Lang.eng;
		}
	}
}