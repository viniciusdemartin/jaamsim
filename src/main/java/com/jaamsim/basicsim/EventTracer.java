/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.jaamsim.events.EventManager;
import com.jaamsim.events.EventTraceListener;
import com.jaamsim.events.ProcessTarget;
import com.jaamsim.ui.LogBox;

class EventTracer implements EventTraceListener {
	private BufferedReader eventVerifyReader;
	private EventTraceRecord reader;
	private long bufferTime; // Internal sim time buffer has been filled to
	private final ArrayList<EventTraceRecord> eventBuffer;

	public EventTracer(String evtName) {
		eventBuffer = new ArrayList<>();
		bufferTime = 0;
		File evtFile = new File(evtName);
		try {
			eventVerifyReader = new BufferedReader(new FileReader(evtFile));
		}
		catch (FileNotFoundException e) {}
		if (eventVerifyReader == null)
			LogBox.logLine("Unable to open an event verification file.");

		reader = new EventTraceRecord();
	}

	private void fillBufferUntil(long internalTime) {
		while (bufferTime <= internalTime) {
			// Read a full trace record form the file, terminated at a blank line
			EventTraceRecord temp = new EventTraceRecord();
			while (true) {
				String line = null;
				try {
					line = eventVerifyReader.readLine();
				}
				catch (IOException e) {}

				if (line == null)
					break;

				temp.add(line);

				if (line.length() == 0)
					break;
			}

			if (temp.size() == 0)
				break;

			// Parse the key information from the record
			temp.parse();
			if (temp.isDefaultEventManager() && temp.getInternalTime() > bufferTime) {
				bufferTime = temp.getInternalTime();
			}
			eventBuffer.add(temp);
		}
	}

	private void findEventInBuffer(EventManager e, EventTraceRecord record) {
		// Ensure we have read enough from the log to find this record
		this.fillBufferUntil(record.getInternalTime());

		// Try an optimistic approach first looking for exact matches
		for (EventTraceRecord each : eventBuffer) {
			if (!each.basicCompare(record)) {
				continue;
			}

			for (int i = 1; i < record.size(); i++) {
				if (!record.get(i).equals(each.get(i))) {
					System.out.println("Difference in event stream detected");
					System.out.println("Received:");
					for (String line : record) {
						System.out.println(line);
					}

					System.out.println("Expected:");
					for (String line : each) {
						System.out.println(line);
					}

					System.out.println("Lines:");
					System.out.println("R:" + record.get(i));
					System.out.println("E:" + each.get(i));

					e.pause();
					new Throwable().printStackTrace();
					break;
				}
			}

			// Found the event, it compared OK, remove from the buffer
			eventBuffer.remove(each);
			//System.out.println("Buffersize:" + eventBuffer.size());
			return;
		}

		System.out.println("No matching event found for:");
		for (String line : record) {
			System.out.println(line);
		}
		for (EventTraceRecord rec : eventBuffer) {
			System.out.println("Buffered Record:");
			for (String line : rec) {
				System.out.println(line);
			}
			System.out.println();
		}
		e.pause();
	}

	private void finish(EventManager e) {
		if (reader.traceLevel != 1)
			return;

		reader.add("");
		reader.parse();
		findEventInBuffer(e, reader);
		reader.clear();
		reader.traceLevel--;
	}

	@Override
	public void traceWait(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		reader.traceWait(e, curTick, tick, priority, t);
		this.finish(e);
	}

	@Override
	public void traceEvent(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		reader.traceEvent(e, curTick, tick, priority, t);
		this.finish(e);
	}

	@Override
	public void traceSchedProcess(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		reader.traceSchedProcess(e, curTick, tick, priority, t);
		this.finish(e);
	}

	@Override
	public void traceProcessStart(EventManager e, ProcessTarget t, long tick) {
		reader.traceProcessStart(e, t, tick);
		this.finish(e);
	}

	@Override
	public void traceProcessEnd(EventManager e, long tick) {
		reader.traceProcessEnd(e, tick);
		this.finish(e);
	}

	@Override
	public void traceInterrupt(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		reader.traceInterrupt(e, curTick, tick, priority, t);
		this.finish(e);
	}

	@Override
	public void traceKill(EventManager e, long curTick, long tick, int priority, ProcessTarget t) {
		reader.traceKill(e, curTick, tick, priority, t);
		this.finish(e);
	}

	@Override
	public void traceWaitUntil(EventManager e, long tick) {
		reader.traceWaitUntil(e, tick);
		this.finish(e);
	}

	@Override
	public void traceWaitUntilEnded(EventManager e, long curTick, ProcessTarget t) {
		reader.traceWaitUntilEnded(e, curTick, t);
		this.finish(e);
	}
}
