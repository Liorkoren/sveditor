package net.sf.sveditor.core.argfile.filter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.sveditor.core.argfile.parser.SVArgFileLexer;
import net.sf.sveditor.core.argfile.parser.SVArgFileParser;
import net.sf.sveditor.core.db.SVDBFile;
import net.sf.sveditor.core.db.SVDBMarker;
import net.sf.sveditor.core.db.index.SVDBWSFileSystemProvider;
import net.sf.sveditor.core.parser.SVParseException;
import net.sf.sveditor.core.scanutils.StringTextScanner;

public class StringArgFileFilter {
	private String				fBaseLocation;
	
	public StringArgFileFilter() {
		fBaseLocation = "";
	}

	public String filter(String content, ArgFileFilterList filter) {
		SVArgFileParser parser = new SVArgFileParser(
				fBaseLocation, fBaseLocation, new SVDBWSFileSystemProvider());

		SVDBFile file = new SVDBFile("");
		List<SVDBMarker> markers = new ArrayList<SVDBMarker>();
	
		SVArgFileLexer lexer = new SVArgFileLexer();
		lexer.init(null, new StringTextScanner(content));
		
		parser.init(lexer, "");
	
		try {
			parser.parse(file, markers);
		} catch (SVParseException e) {}
	
		if (filter != null) {
			file = filter.filter(file);
		}
		
		ArgFileWriter writer = new ArgFileWriter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		writer.write(file, out);
		
		return out.toString();
	}
}
