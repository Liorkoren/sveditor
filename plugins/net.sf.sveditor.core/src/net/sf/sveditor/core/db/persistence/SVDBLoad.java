package net.sf.sveditor.core.db.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.sveditor.core.db.SVDBConstraint;
import net.sf.sveditor.core.db.SVDBCoverGroup;
import net.sf.sveditor.core.db.SVDBFile;
import net.sf.sveditor.core.db.SVDBInclude;
import net.sf.sveditor.core.db.SVDBItem;
import net.sf.sveditor.core.db.SVDBItemType;
import net.sf.sveditor.core.db.SVDBMacroDef;
import net.sf.sveditor.core.db.SVDBModIfcClassDecl;
import net.sf.sveditor.core.db.SVDBModIfcClassParam;
import net.sf.sveditor.core.db.SVDBModIfcInstItem;
import net.sf.sveditor.core.db.SVDBPackageDecl;
import net.sf.sveditor.core.db.SVDBPreProcCond;
import net.sf.sveditor.core.db.SVDBProgramBlock;
import net.sf.sveditor.core.db.SVDBScopeItem;
import net.sf.sveditor.core.db.SVDBTaskFuncParam;
import net.sf.sveditor.core.db.SVDBTaskFuncScope;
import net.sf.sveditor.core.db.SVDBVarDeclItem;
import net.sf.sveditor.core.db.index.ISVDBIndex;

public class SVDBLoad implements IDBReader {
	private InputStream 		fIn;
	private int					fUngetCh;
	private StringBuilder		fTmpBuffer = new StringBuilder();
	private byte				fBuf[];
	private int					fBufIdx;
	private int					fBufSize;
	
	public SVDBLoad() {
		fBuf = new byte[1024*1024];
		fBufIdx  = 0;
		fBufSize = 0;
	}
	
	public String readBaseLocation(InputStream in) throws DBFormatException {
		fIn = in;
		fUngetCh = -1;
		fBufIdx  = 0;
		fBufSize = 0;
		
		String SDB = readTypeString();
		
		if (!"SDB".equals(SDB)) {
			throw new DBFormatException("Database not prefixed with SDB");
		}
		
		int ch;
		
		if ((ch = getch()) != '<') {
			throw new DBFormatException("Missing '<'");
		}
		
		fTmpBuffer.setLength(0);
		
		while ((ch = getch()) != -1 && ch != '>') {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch != '>') {
			throw new DBFormatException("Unterminated SDB record");
		}

		return fTmpBuffer.toString();
	}
	
	@SuppressWarnings("unchecked")
	public void load(ISVDBIndex index, InputStream in) throws DBFormatException {
		fIn = in;
		fUngetCh = -1;
		fBufIdx  = 0;
		fBufSize = 0;

		String SDB = readTypeString();
		
		if (!"SDB".equals(SDB)) {
			throw new DBFormatException("Database not prefixed with SDB");
		}
		
		int ch;
		
		if ((ch = getch()) != '<') {
			throw new DBFormatException("Missing '<'");
		}
		
		fTmpBuffer.setLength(0);
		
		while ((ch = getch()) != -1 && ch != '>') {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch != '>') {
			throw new DBFormatException("Unterminated SDB record");
		}
		
		// TODO: Check base location against index being loaded
		List<SVDBFile> pp_list = (List<SVDBFile>)readItemList(null, null);
		List<SVDBFile> db_list = (List<SVDBFile>)readItemList(null, null);
		
		System.out.println("SVDBLoad: pp_list.size=" + pp_list.size() + 
				" db_list.size=" + db_list.size());
		
		index.load(pp_list, db_list);
	}
	
	@SuppressWarnings("unchecked")
	public void load(Map<String, SVDBFile> pp_map, Map<String, SVDBFile> db_map, InputStream in)  throws DBFormatException {
		fIn = in;
		fUngetCh = -1;
		fBufIdx  = 0;
		fBufSize = 0;

		String SDB = readTypeString();
		
		if (!"SDB".equals(SDB)) {
			throw new DBFormatException("Database not prefixed with SDB");
		}
		
		int ch;
		
		if ((ch = getch()) != '<') {
			throw new DBFormatException("Missing '<'");
		}
		
		fTmpBuffer.setLength(0);
		
		while ((ch = getch()) != -1 && ch != '>') {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch != '>') {
			throw new DBFormatException("Unterminated SDB record");
		}
		
		// TODO: Check base location against index being loaded
		List<SVDBFile> pp_list = (List<SVDBFile>)readItemList(null, null);
		List<SVDBFile> db_list = (List<SVDBFile>)readItemList(null, null);
		
		for (SVDBFile f : pp_list) {
			pp_map.put(f.getFilePath(), f);
		}
		
		for (SVDBFile f : db_list) {
			db_map.put(f.getFilePath(), f);
		}
	}

	public int readInt() throws DBFormatException {
		String type = readTypeString();
		
		if (!"I".equals(type)) {
			throw new DBFormatException(
					"Bad format for integer: \"" + type + "\"");
		}
		
		int ch = getch(); // expect '<'
		
		
		int ret = readRawHexInt();
		
		if ((ch = getch()) != '>') { // expect '>'
			throw new DBFormatException(
					"Unterminated integer: \"" + (char)ch + "\"");
		}

		return ret;
	}

	public SVDBItemType readItemType() throws DBFormatException {
		String type = readTypeString();
		
		if (!"IT".equals(type)) {
			throw new DBFormatException(
					"Bad format for item type: \"" + type + "\"");
		}
		
		int ch = getch();
		
		fTmpBuffer.setLength(0);
		
		while ((ch = getch()) != -1 && ch != '>') {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch != '>') {
			throw new DBFormatException(
					"Unterminated item type: \"" + (char)ch + "\"");
		}
		
		SVDBItemType ret = null;
		
		try {
			ret = SVDBItemType.valueOf(fTmpBuffer.toString());
		} catch (Exception e) {
			System.out.println("[ERROR] value \"" + fTmpBuffer.toString() + "\" isn't an SVDBItemType value");
		}
		
		return ret;
	}

	public long readLong() throws DBFormatException {
		String type = readTypeString();
		
		if (!"L".equals(type)) {
			throw new DBFormatException(
					"Bad format for long: \"" + type + "\"");
		}
		
		int ch = getch();
		
		fTmpBuffer.setLength(0);
		
		while ((ch = getch()) != -1 && ch != '>') {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch != '>') {
			throw new DBFormatException(
					"Unterminated integer: \"" + (char)ch + "\"");
		}
		
		return Long.parseLong(fTmpBuffer.toString(), 16);
	}

	public String readString() throws DBFormatException {
		String type = readTypeString();
		
		if (!"S".equals(type)) {
			throw new DBFormatException(
					"Bad format for string: \"" + type + "\"");
		}
		
		int ch;
		
		// Expect the next char to be '<' as well
		if ((ch = getch()) != '<') {
			throw new DBFormatException("STRING");
		}
		
		fTmpBuffer.setLength(0);
		
		ch = getch(); // expect '<'
		
		int size = readRawInt();
		
		if ((ch = getch()) != '>') {
			throw new DBFormatException(
					"Unterminated string size: \"" + (char)ch + "\"");
		}
		
		if (size == -1) {
			if ((ch = getch()) != '>') {
				throw new DBFormatException("Unterminated null string");
			}
			return null;
		} else {
			fTmpBuffer.setLength(0);
			while ((ch = getch()) != -1 && size-- > 0) {
				fTmpBuffer.append((char)ch);
			}

			// if ((ch = getch()) != '>') {
			if (ch != '>') {
				System.out.println("string thus far is: " + 
						fTmpBuffer.toString());
				throw new DBFormatException(
						"Unterminated string: \"" + (char)ch + "\"");
			}

			return fTmpBuffer.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List readItemList(
			SVDBFile			file,
			SVDBScopeItem		parent) throws DBFormatException {
		String type = readTypeString();
		
		if (!"SIL".equals(type)) {
			throw new DBFormatException(
					"Bad format for item list: \"" + type + "\"");
		}
		
		int ch;
		int size;
		
		if ((ch = getch()) != '<') { // expect '<'
			throw new DBFormatException("Missing '<' on item-list size (" + (char)ch + ")"); 
		}

		size = readRawInt();
		
		if ((ch = getch()) != '>') {
			throw new DBFormatException("Missing '>' on item-list size");
		}
		
		if (size == -1) {
			return null;
		} else {
			List ret = new ArrayList();
			while (size-- > 0) {
				ret.add(readSVDBItem(file, parent));
			}
			
			return ret;
		}
	}
	
	public List<String> readStringList() throws DBFormatException {
		String type = readTypeString();
		
		if (!"SSL".equals(type)) {
			throw new DBFormatException(
					"Bad format for string-list: \"" + type + "\"");
		}
		
		int ch;
		int size;
		
		if ((ch = getch()) != '<') { // expect '<'
			throw new DBFormatException(
					"Bad format for string-list: expecting '<' (" + (char)ch + ")");
		}
		
		size = readRawInt();
		
		ch = getch(); // expect '>'
		
		if (size == -1) {
			return null;
		} else {
			List<String> ret = new ArrayList<String>();

			while (size-- > 0) {
				ret.add(readString());
			}
			
			return ret;
		}
	}
	
	private int readRawInt() throws DBFormatException {
		int ret = 0;
		int ch;
		
		if ((ch = getch()) == '-') {
			if ((ch = getch()) == '1') {
				return -1;
			} else {
				throw new DBFormatException("Only -1 is supported as negative number");
			}
		} else {
			unget(ch);
			
			while ((ch = getch()) != -1 && ch >= '0' && ch <= '9') {
				ret *= 10;
				ret += (ch - '0');
			}
			
			unget(ch);
			
		}
		
		return ret;
	}

	private int readRawHexInt() throws DBFormatException {
		int ret = 0;
		int ch;
		
		if ((ch = getch()) == '-') {
			if ((ch = getch()) == '1') {
				return -1;
			} else {
				throw new DBFormatException("Only -1 is supported as negative number");
			}
		} else {
			unget(ch);
			
			while ((ch = getch()) != -1 && 
					((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
				ret *= 16;
				if ((ch >= '0' && ch <= '9')) {
					ret += (ch - '0');
				} else {
					ret += (10 + (ch - 'a'));
				}
			}
			
			unget(ch);
			
			return ret;
		}
	}

	private SVDBItem readSVDBItem(
			SVDBFile			file,
			SVDBScopeItem		parent
			) throws DBFormatException {
		SVDBItem ret = null;
		
		SVDBItemType type   = readItemType();
		
		switch (type) {
			case Class:
				ret = new SVDBModIfcClassDecl(file, parent, type, this);
				break;
				
			case Program:
				ret = new SVDBProgramBlock(file, parent, type, this);
				break;
				
			case Covergroup:
				ret = new SVDBCoverGroup(file, parent, type, this);
				break;
				
			case Coverpoint:
				ret = new SVDBItem(file, parent, type, this);
				break;
				
			case File:
				ret = new SVDBFile(file, parent, type, this);
				break;
				
			case Function:
				ret = new SVDBTaskFuncScope(file, parent, type, this);
				break;
				
			case Include:
				ret = new SVDBInclude(file, parent, type, this);
				break;
				
			case Interface:
				ret = new SVDBModIfcClassDecl(file, parent, type, this);
				break;
				
			case Macro:
				ret = new SVDBMacroDef(file, parent, type, this);
				break;
				
			case ModIfcClassParam:
				ret = new SVDBModIfcClassParam(file, parent, type, this);
				break;
				
			case ModIfcInst:
				ret = new SVDBModIfcInstItem(file, parent, type, this);
				break;
				
			case Module:
				ret = new SVDBModIfcClassDecl(file, parent, type, this);
				break;
				
			case PackageDecl:
				ret = new SVDBPackageDecl(file, parent, type, this);
				break;
				
			case PreProcCond:
				ret = new SVDBPreProcCond(file, parent, type, this);
				break;
				
			case Property:
				ret = new SVDBScopeItem(file, parent, type, this);
				break;
				
			case Sequence:
				ret = new SVDBScopeItem(file, parent, type, this);
				break;
				
			case Struct:
				ret = new SVDBModIfcClassDecl(file, parent, type, this);
				break;
				
			case Task:
				ret = new SVDBTaskFuncScope(file, parent, type, this);
				break;
				
			case TaskFuncParam:
				ret = new SVDBTaskFuncParam(file, parent, type, this);
				break;
				
			case VarDecl:
				ret = new SVDBVarDeclItem(file, parent, type, this);
				break;
				
			case Constraint:
				ret = new SVDBConstraint(file, parent, type, this);
				
			default:
				System.out.println("[ERROR] unimplemented SVDBLoad type " + type);
				break;
		}
		
		return ret;
	}
	
	private String readTypeString() {
		fTmpBuffer.setLength(0);
		
		int ch;
		int count = 0;
		
		while ((ch = getch()) != -1 && ch != '<' && ++count < 16) {
			fTmpBuffer.append((char)ch);
		}
		
		if (ch == '<') {
			unget(ch);
			return fTmpBuffer.toString();
		} else {
			return null;
		}
	}
	
	
	private int getch() {
		int ch = -1;
		
		if (fUngetCh != -1) {
			ch = fUngetCh;
			fUngetCh = -1;
		} else {
			if (fBufIdx >= fBufSize) {
				try {
					fBufSize = fIn.read(fBuf, 0, fBuf.length);
					fBufIdx  = 0;
				} catch (IOException e) { }
			}
			
			if (fBufIdx < fBufSize) {
				ch = fBuf[fBufIdx++];
			}
		}
		
		return ch;
	}
	
	private void unget(int ch) {
		fUngetCh = ch;
	}
}
