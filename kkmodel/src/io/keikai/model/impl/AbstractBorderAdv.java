/* AbstractBorderAdv.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 6:09:28 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package io.keikai.model.impl;

import java.io.Serializable;

import io.keikai.model.SBook;
import io.keikai.model.SBorder;

/**
 * @author henri
 * @since 3.8.0
 */
public abstract class AbstractBorderAdv implements SBorder, Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * gets the string key of this font, the key should combine all the style value in short string as possible
	 */
	abstract String getStyleKey();
	
	//ZSS-1183
	//@since 3.9.0
	/*package*/ abstract SBorder cloneBorder(SBook book);
}
