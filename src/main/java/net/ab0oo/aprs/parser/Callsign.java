/*
 * AVRS - http://avrs.sourceforge.net/
 *
 * Copyright (C) 2012 Georg Lukas, DO1GL
 *
 * AVRS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * AVRS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AVRS; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package net.ab0oo.aprs.parser;

import java.io.Serializable;

/**
 *
 * @author do1gl
 * This class represents a callsign with (optional) ssid
 */
public class Callsign implements Serializable {
	private static final long serialVersionUID = 1L;
	private String callsign;
	private int ssid = 0;

	/**
	 *
	 * @param call String representation of callsign with optional SSID in the form call-ssid
	 *
	 */
	public Callsign(String call) {
		String[] callssid = call.split("-");
		setCallsign(callssid[0]);
		if (callssid.length > 1) {
			try {
				setSsid(Integer.parseInt(callssid[1]));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Failed to parse the callsign " + call +" the SSID part of the call must be an integer");
			}
		}
	}

	public Callsign(byte[] data, int offset) {
		byte[] shifted = new byte[6];
		byte ssidbyte = data[offset + 6];
		for (int i = 0; i < 6; i++)
			shifted[i] = (byte)((data[offset + i]&0xff) >> 1);
		setCallsign(new String(shifted, 0, 6).trim());
		int ssidval = (ssidbyte & 0x1e) >> 1;
		if (ssidval != 0) {
			setSsid(ssidval);
		}
	}

	/**
	 * @return the callsign without the SSID
	 */
	public String getCallsign() {
		return callsign;
	}

	/**
	 * @param callsign the callsign to set without ssid
	 */
	public void setCallsign(String callsign) {
		if (callsign.contains("-")) {
			throw new IllegalArgumentException("this method does not accept a callsign with and SSID");
		}
		this.callsign = callsign.toUpperCase();
	}

	/**
	 * @return the ssid or 0 if none
	 */
	public int getSsid() {
		return ssid;
	}

	/**
	 * @param ssid the ssid to set must be a positive integer
	 */
	public void setSsid(int ssid) {
		if (ssid < 0) throw new IllegalArgumentException("SSID must be a positive number");
		this.ssid = ssid;
	}


	/**
	 * @return String representation in the format call-ssid if ssid > 0 else just the callsign
	 */
	@Override
	public String toString() {
		return callsign + (ssid == 0 ?  "" : "-" + ssid);
	}

	public byte[] toAX25() throws IllegalArgumentException {
		byte[] callbytes = callsign.getBytes();
		byte[] ax25 = new byte[7];
		// shift " " by one
		java.util.Arrays.fill(ax25, (byte)0x40);
		if (callbytes.length > 6)
			throw new IllegalArgumentException("Callsign " + callsign + " is too long for AX.25!");
		for (int i = 0; i < callbytes.length; i++) {
			ax25[i] = (byte)(callbytes[i] << 1);
		}
		// ssid byte: u11ssss0
		ax25[6] = (byte) (0x60 | ((ssid*2) & 0x1e));
		return ax25;
	}
}
