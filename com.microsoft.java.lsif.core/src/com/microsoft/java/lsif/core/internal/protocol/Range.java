/*******************************************************************************
* Copyright (c) 2021 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.lsif.core.internal.protocol;

import org.eclipse.lsp4j.Position;

public class Range extends Vertex {

	private Position start;

	private Position end;

	private Tag tag;

	public Range(String id, Position start, Position end) {
		super(id, Vertex.RANGE);
		this.start = start;
		this.end = end;
	}

	public Range(String id, Position start, Position end, Tag tag) {
		this(id, start, end);
		this.tag = tag;
	}

	public static Range fromLspRange(String id, org.eclipse.lsp4j.Range lspRange) {
		return new Range(id, lspRange.getStart(), lspRange.getEnd());
	}

	public static Range fromLspRange(String id, org.eclipse.lsp4j.Range lspRange, Tag tag) {
		return new Range(id, lspRange.getStart(), lspRange.getEnd(), tag);
	}

	public Position getStart() {
		return this.start;
	}

	public Position getEnd() {
		return this.end;
	}

	public Tag getTag() {
		return this.tag;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Range other = (Range) obj;
		if (this.start == null) {
			if (other.start != null) {
				return false;
			}
		} else if (!this.start.equals(other.start)) {
			return false;
		}
		if (this.end == null) {
			if (other.end != null) {
				return false;
			}
		} else if (!this.end.equals(other.end)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.start == null) ? 0 : this.start.hashCode());
		return prime * result + ((this.end == null) ? 0 : this.end.hashCode());
	}
}
