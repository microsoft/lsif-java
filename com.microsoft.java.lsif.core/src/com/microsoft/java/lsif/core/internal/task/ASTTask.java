/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.task;

import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;

public class ASTTask {

	private TaskType taskType;

	private IndexerContext context;

	public ASTTask(TaskType taskType, IndexerContext context) {
		this.taskType = taskType;
		this.context = context;
	}

	/**
	 * @return the taskType
	 */
	public TaskType getTaskType() {
		return taskType;
	}

	/**
	 * @return the context
	 */
	public IndexerContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(IndexerContext context) {
		this.context = context;
	}
}
