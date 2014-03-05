package com.gromstudio.treckar.util;


/**
 * This utility helps managing flags as an integer
 * 
 * <p>
 * It helps to perform the common bitwise operations.
 * <ul>
 * <li>add flag</li>
 * <li>remove flag</li>
 * <li>clear all flags</li>
 * <li>test flag</li>
 * </ul>
 * </p>
 * @author clicmobile
 *
 */
public class Flags {

	int mFlags = 0x00000000;
	
	public Flags() {
		this(0);
	}
	
	public Flags(int flags) {
		mFlags = flags;
	}

	
	public boolean isFlagSet(int mask) {
		return (mFlags&mask)==mask;
	}
	
	public void addFlag(int mask) {
		mFlags |= mask;
	}
	
	public void removeFlag(int mask) {
		mFlags &= ~mask;
	}
	
	public void clearFlag() {
		mFlags = 0x00;
	}
	
	public void setFlags(int mask) {
		mFlags = mask;
	}

}
