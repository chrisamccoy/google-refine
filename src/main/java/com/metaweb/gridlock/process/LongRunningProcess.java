package com.metaweb.gridlock.process;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

abstract public class LongRunningProcess extends Process {
	final protected String 		_description;
	protected ProcessManager 	_manager;
	protected Thread 			_thread;
	protected int				_progress; // out of 100
	protected boolean			_canceled;
	
	protected LongRunningProcess(String description) {
		_description = description;
	}

	@Override
	public void cancel() {
		_canceled = true;
		if (_thread != null && _thread.isAlive()) {
			_thread.interrupt();
		}
	}
	
	@Override
	public void write(JSONWriter writer, Properties options)
			throws JSONException {
		
		writer.object();
		writer.key("description"); writer.value(_description);
		writer.key("immediate"); writer.value(false);
		writer.key("status"); writer.value(_thread == null ? "pending" : (_thread.isAlive() ? "running" : "done"));
		writer.key("progress"); writer.value(_progress);
		writer.endObject();
	}

	@Override
	public boolean isImmediate() {
		return false;
	}
	
	@Override
	public boolean isRunning() {
		return _thread != null && _thread.isAlive();
	}
	
	@Override
	public boolean isDone() {
		return _thread != null && !_thread.isAlive();
	}

	@Override
	public void performImmediate() {
		throw new RuntimeException("Not an immediate process");
	}

	@Override
	public void startPerforming(ProcessManager manager) {
		if (_thread == null) {
			_manager = manager;
			
			_thread = new Thread(getRunnable());
			_thread.start();
		}
	}
	
	abstract protected Runnable getRunnable();
}