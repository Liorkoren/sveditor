/****************************************************************************
 * Copyright (c) 2008-2010 Matthew Ballance and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Ballance - initial implementation
 ****************************************************************************/


package net.sf.sveditor.ui.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;

public class SVSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter {
	private SVSearchQuery		fQuery;
	
	public SVSearchResult(SVSearchQuery query) {
		fQuery = query;
	}

	public String getLabel() {
		return fQuery.getLabel();
	}

	public String getTooltip() {
		return "tooltip";
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
//		System.out.println("getEditorMatchAdapter()");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		System.out.println("isShownInEditor: " + match.getElement() + " " + editor.getTitle());
		// TODO Auto-generated method stub
		return false;
	}

	public Match[] computeContainedMatches(
			AbstractTextSearchResult result,
			IEditorPart editor) {
		System.out.println("computeContainedMatches: " + editor.getTitle());
		// TODO Auto-generated method stub
		return null;
	}

}
