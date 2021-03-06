/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2009-2011 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.basicsim;

import java.util.ArrayList;

import com.jaamsim.events.EventManager;
import com.jaamsim.events.EventTraceListener;
import com.jaamsim.events.ProcessTarget;

class EventTraceRecord extends ArrayList<String> implements EventTraceListener {
	private String eventManagerName;
	private long internalTime;
	private String targetName;
	int traceLevel;

	public EventTraceRecord() {
		traceLevel = 0;
	}

	void parse() {
		String[] temp;

		// The first line of the trace is always <eventManagerName>\t<InternalSimulationTime>
		temp = this.get(0).split("\t");
		eventManagerName = temp[0];
		internalTime = Long.parseLong(temp[1]);

		// Try to parse a target entity and method form the second line
		temp = this.get(1).split("\t");

		// A regular event wakeup, parse target/method
		if (temp[0].endsWith("Event")) {
			targetName = temp[3];
			return;
		}

		if (temp[0].endsWith("WaitUntilEnded")) {
			targetName = temp[3];
			return;
		}

		if (temp[0].endsWith("StartProcess")) {
			targetName = temp[1];
			return;
		}

		if (temp[0].endsWith("SchedProcess")) {
			targetName = temp[3];
			return;
		}
	}

	private void append(String record) {
		StringBuilder rec = new StringBuilder();

		for (int i = 0; i < traceLevel; i++) {
			rec.append("  ");
		}
		rec.append(record);
		this.add(rec.toString());
	}

	private void addHeader(String name, long internalTime) {
		// Don't write anything if not at level 0
		if (traceLevel != 0)
			return;

		StringBuilder header = new StringBuilder(name).append("\t").append(internalTime);
		this.add(header.toString());
		traceLevel++;
	}

	@Override
	public void traceWait(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		traceLevel--;

		this.append(String.format("Wait\t%d\t%d\t%s", tick, priority, EventRecorder.getWaitDescription()));
	}

	@Override
	public void traceEvent(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		this.append(String.format("Event\t%d\t%d\t%s", tick, priority, t.getDescription()));

		traceLevel++;
	}

	@Override
	public void traceInterrupt(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		this.append(String.format("Int\t%d\t%d\t%s", tick, priority, t.getDescription()));
		traceLevel++;
	}

	@Override
	public void traceKill(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		this.append(String.format("Kill\t%d\t%d\t%s", tick, priority, t.getDescription()));
	}

	@Override
	public void traceWaitUntil(EventManager e, long tick) {
		this.addHeader(e.name, tick);
		traceLevel--;
		this.append("WaitUntil");
	}

	@Override
	public void traceWaitUntilEnded(EventManager e, long curTick, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		this.append(String.format("WaitUntilEnded\t%s", t.getDescription()));
	}

	@Override
	public void traceProcessStart(EventManager e, ProcessTarget t, long tick) {
		this.addHeader(e.name, tick);
		this.append(String.format("StartProcess\t%s", t.getDescription()));
		traceLevel++;
	}

	@Override
	public void traceProcessEnd(EventManager e, long tick) {
		this.addHeader(e.name, tick);
		traceLevel--;
		this.append("Exit");
	}

	@Override
	public void traceSchedProcess(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		this.addHeader(e.name, curTick);
		this.append(String.format("SchedProcess\t%d\t%d\t%s", tick, priority, t.getDescription()));
	}

	boolean isDefaultEventManager() {
		return eventManagerName.equals("DefaultEventManager");
	}

	long getInternalTime() {
		return internalTime;
	}

	/**
	 * Does a superficial comparison of two records, check number of entries,
	 * time/target/method and finally the basic contents of the record.
	 */
	boolean basicCompare(EventTraceRecord record) {
		if (record.size() != this.size())
			return false;

		if (record.internalTime != this.internalTime)
			return false;

		if (!record.targetName.equals(this.targetName))
			return false;

		return true;
	}
}
