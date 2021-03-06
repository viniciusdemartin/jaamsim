/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2011 Ausenco Engineering Canada Inc.
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
package com.jaamsim.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.jaamsim.basicsim.Entity;

/**
 * Class KeyListInput for storing a list of entities of class V, with an optional key of class K1
 */
public class KeyListInput<K1 extends Entity, V extends Entity> extends ListInput<ArrayList<V>> {

	private Class<K1> keyClass;
	private Class<V> valClass;
	private HashMap<K1,ArrayList<V>> hashMap;
	private ArrayList<V> noKeyValue; // the value when there is no key

	public KeyListInput(Class<K1> kClass, Class<V> vClass, String keyword, String cat, ArrayList<V> def) {
		super(keyword, cat, def);
		keyClass = kClass;
		valClass = vClass;
		hashMap = new HashMap<>();
		noKeyValue = def;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void copyFrom(Input<?> in) {
		super.copyFrom(in);
		KeyListInput<K1, V> inp = (KeyListInput<K1, V>) in;
		hashMap = inp.hashMap;
		noKeyValue = inp.noKeyValue;
	}

	@Override
	public void parse(KeywordIndex kw)
	throws InputErrorException {
		for (KeywordIndex each : kw.getSubArgs())
			this.innerParse(each);
	}

	private void innerParse(KeywordIndex kw) {
		ArrayList<String> input = new ArrayList<>(kw.numArgs());
		for (int i = 0; i < kw.numArgs(); i++)
			input.add(kw.getArg(i));

		ArrayList<K1> list;
		try {
			// Determine the key(s)
			list = Input.parseEntityList(input.subList(0, 1), keyClass, true);
		}
		catch (InputErrorException e) {
			// A key was not provided.  Set the "no key" value
			noKeyValue = Input.parseEntityList( input, valClass, true );
			return;
		}

		// The input is of the form: <Key> <value1 value2 value3...>
		// Determine the value
		ArrayList<V> val = Input.parseEntityList( input.subList(1,input.size()), valClass, true );

		// Set the value for the given keys
		for( int i = 0; i < list.size(); i++ ) {
			hashMap.put( list.get(i), val );
		}
	}

	@Override
	public ArrayList<V> getValue() {
		return null;
	}

	public ArrayList<V> getValueFor( K1 k1 ) {
		ArrayList<V> val = hashMap.get( k1 );
		if( val == null ) {
			return noKeyValue;
		}
		else {
			return val;
		}
	}

	@Override
	public String getDefaultString() {
		if (defValue == null)
			return "";

		if (defValue.size() == 0)
			return "";

		StringBuilder tmp = new StringBuilder(defValue.get(0).getName());
		for (int i = 1; i < defValue.size(); i++) {
			tmp.append(SEPARATOR);
			tmp.append(defValue.get(i).getName());
		}
		return tmp.toString();
	}

	@Override
	public void reset() {
		super.reset();
		hashMap.clear();
		noKeyValue = this.getDefaultValue();
	}

	@Override
	public int getListSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<String> getValidOptions() {
		ArrayList<String> list = new ArrayList<>();
		for(V each: Entity.getClonesOfIterator(valClass) ) {
			if(each.testFlag(Entity.FLAG_GENERATED))
				continue;

			list.add(each.getName());
		}
		Collections.sort(list);
		return list;
	}
}
