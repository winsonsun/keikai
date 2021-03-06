/* PasteCellAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/25, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package io.keikai.ui.impl.undo;

import io.keikai.api.AreaRef;
import io.keikai.api.CellOperationUtil;
import io.keikai.api.Range;
import io.keikai.api.Ranges;
import io.keikai.api.impl.RangeImpl;
import io.keikai.api.model.EditableCellStyle;
import io.keikai.api.model.Sheet;
import io.keikai.model.CellRegion;
import io.keikai.range.impl.PasteRangeImpl;

/**
 * 
 * @author dennis
 * 
 */
public class PasteCellAction extends AbstractCellDataStyleAction {
	private static final long serialVersionUID = 603418025542199316L;
	
	protected final int _destRow,_destColumn,_destLastRow,_destLastColumn;
	protected final int _reservedDestLastRow,_reservedDestLastColumn;
	protected final Sheet _destSheet;
	protected final boolean _destWholeColumn; //ZSS-717
	protected final boolean _destWholeRow; //ZSS-1277
	
	private Range _pastedRange;
	
//	private final int rlastRow;
//	private final int rlastColumn;
	// ZSS-1277
	//@since 3.9.1
	public PasteCellAction(String label, 
			Sheet sheet, int srcRow, int srcColumn,int srcLastRow, int srcLastColumn, boolean srcWholeColumn, boolean srcWholeRow,
			Sheet destSheet, int destRow, int destColumn,int destLastRow, int destLastColumn, boolean destWholeColumn, boolean destWholeRow) {
		super(label, sheet, srcRow, srcColumn, srcLastRow, srcLastColumn,srcWholeColumn,srcWholeRow,RESERVE_ALL);
		
		this._destRow = destRow;
		this._destColumn = destColumn;
		this._destLastRow = destLastRow;
		this._destLastColumn = destLastColumn;
		
		int srcColNum = srcLastColumn-srcColumn;
		int srcRowNum = srcLastRow-srcRow;
		//enlarge and transpose
		int destWidth = Math.max(destLastColumn-destColumn, srcColNum);
		int destHeight = Math.max(destLastRow-destRow, srcRowNum);
		
		_reservedDestLastRow = _destRow + destHeight;
		_reservedDestLastColumn = _destColumn + destWidth;
		
		this._destSheet = destSheet;
		this._destWholeColumn = destWholeColumn; //ZSS-717
		this._destWholeRow = destWholeRow; //ZSS-1277
	}
	//ZSS-717
	//@since 3.8.3
	@Deprecated
	public PasteCellAction(String label, 
			Sheet sheet, int srcRow, int srcColumn,int srcLastRow, int srcLastColumn, boolean srcWholeColumn,
			Sheet destSheet, int destRow, int destColumn,int destLastRow, int destLastColumn, boolean destWholeColumn) {
		this(label,sheet,srcRow,srcColumn,srcLastRow,srcLastColumn,srcWholeColumn,false,
			destSheet, destRow, destColumn, destLastRow,destLastColumn,destWholeColumn, false);
	}
	@Deprecated
	public PasteCellAction(String label, 
			Sheet sheet, int srcRow, int srcColumn,int srcLastRow, int srcLastColumn, 
			Sheet destSheet, int destRow, int destColumn,int destLastRow, int destLastColumn) {
		this(label, 
			sheet, srcRow, srcColumn, srcLastRow, srcLastColumn, false,false,
			destSheet, destRow, destColumn, destLastRow, destLastColumn, false,false);
	}
	
	@Override
	protected boolean isSheetProtected(){
		return isAnyCellProtected(_destSheet, computePastingRegion());
	}
	
	@Override
	public Sheet getUndoSheet(){
		return _destSheet;
	}
	@Override
	public Sheet getRedoSheet(){
		return _destSheet;
	}

	@Override
	protected int getReservedRow(){
		return _destRow;
	}
	@Override
	protected int getReservedColumn(){
		return _destColumn;
	}
	@Override
	protected int getReservedLastRow(){
		return _reservedDestLastRow;
	}
	@Override
	protected int getReservedLastColumn(){
		return _reservedDestLastColumn;
	}
	protected Sheet getReservedSheet(){
		return _destSheet;
	}
	@Override
	public AreaRef getUndoSelection(){
		return _pastedRange==null?new AreaRef(_destRow,_destColumn,_destLastRow,_destLastColumn):
			new AreaRef(_pastedRange.getRow(),_pastedRange.getColumn(),_pastedRange.getLastRow(),_pastedRange.getLastColumn());
	}
	@Override
	public AreaRef getRedoSelection(){
		return _pastedRange==null?new AreaRef(_destRow,_destColumn,_destLastRow,_destLastColumn):
			new AreaRef(_pastedRange.getRow(),_pastedRange.getColumn(),_pastedRange.getLastRow(),_pastedRange.getLastColumn());
	}
	
	protected void applyAction() {
		//ZSS-717
		Range src = new RangeImpl(new PasteRangeImpl(_sheet.getInternalSheet(), _row, _column, _lastRow, _lastColumn, _wholeRow, _wholeColumn), _sheet); //ZSS-1277
		Range dest = new RangeImpl(new PasteRangeImpl(_destSheet.getInternalSheet(), _destRow, _destColumn, _destLastRow, _destLastColumn, _wholeRow, _destWholeColumn), _destSheet); //ZSS-1277
		_pastedRange = CellOperationUtil.paste(src, dest);
		if (isDstSheetProtected()){
			//recover overriden locked status  during copying from locked cells
			EditableCellStyle recoveredStyle = _pastedRange.getCellStyleHelper().createCellStyle(dest.getCellStyle());
			recoveredStyle.setLocked(false);
			_pastedRange.setCellStyle(recoveredStyle);
		}
		CellOperationUtil.fitFontHeightPoints(
				Ranges.range(_destSheet, dest.getRow(), dest.getColumn(),
				dest.getRow() + (_lastRow - _row), dest.getColumn() + (_lastColumn - _column)));
			
	}
	
	/**
	 * compute the pasting region based on source range if users just select 1 cell to paste.
	 * @return
	 */
	private CellRegion computePastingRegion(){
		CellRegion destinationRegion = new CellRegion(_destRow, _destColumn, _destLastRow, _destLastColumn);
		
		if (destinationRegion.isSingle()){
			return new CellRegion(_destRow, _destColumn, _destRow + (_lastRow - _row), _destColumn + (_lastColumn - _column));
		}else{
			return destinationRegion;
		}
	}

	protected boolean isDstSheetProtected(){
		try{
			return _destSheet.isProtected();
		}catch(Exception x){}
		return true;
	}
}
