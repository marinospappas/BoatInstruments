package com.mpdev.android.boat;

import java.util.Arrays;

/**
 *
 * Class NmeaMessage
 * Processing of NMEA 0183 Sentences
 * 
 * Message Structure
 * ASCII	Hex	Dec	Use
 * <CR>	0x0d	13	Carriage return
 * <LF>	0x0a	10	Line feed, end delimiter
 * !	0x21	33	Start of encapsulation sentence delimiter (AIS)
 * $	0x24	36	Start delimiter
 * *	0x2a	42	Checksum delimiter
 * ,	0x2c	44	Field delimiter
 * \	0x5c	92	TAG block delimiter
 * ^	0x5e	94	Code delimiter for HEX representation of ISO/IEC 8859-1 (ASCII) characters
 * ~	0x7e	126	Reserved
 * 
 * Supported Sentences in this implementation:
 * Boat Speed:  		$IIVHW,,T,338.9,M,0,N,0,K*7A
 * App Wind:			$WIMWV,175.46,R,2.29,N,A*2B
 * True Wind:			$WIMWV,175.46,T,2.29,N,A*2B
 * Ground Wind: 		$--MWD,x.x,T,x.x,M,x.x,N,x.x,M*hh
 * Heading:				$HCHDG,338.9,,,0.7,W*3D
 * Water Temp:			$IIMTW,24.9,C*1C
 * Depth:				$SDDPT,7.36,0.3,*4A
 * Log:					$IIVLW,2310,N,45.1,N,,N,,N*4D
 * Next Waypoint:		$GPBWC,112356.00,5045.645,N,00107.027,W,180.0,T,181.0,M,2.10,N,1,A*4A
 * Position:			$GPGLL,5047.7438,N,00107.0246,W,112402,A,A*59
 * Posn/Time/SOG/COG:	$GPRMC,112402,A,5047.7438,N,00107.0246,W,0.1,0,170218,0.73,W,A*31
 * GPS Date/Time:   	$GPZDA,160853,03,04,2021,,*47
 * SOG / COG:			$GPVTG,0.73,T,,M,0.16,N,0.29,K,A*35
 * Fix data:            $GPSGNS,134107,3609.3621,N,00521.4806,W,A,10,1,43.5,47.3,,,V*6B
 * XTE:                 $GPXTE,A,A,0,L,N,A*2D
 *
 * @author marinos pappas 07 June 2020
 * 
 */

public class NmeaMessage {

	// field lengths
	private static final int LEN_TALKER = 2;
	private static final int LEN_FORMATTER = 3;
	private static final int LEN_CHKSUM = 2;
	
	// maximum/minimum limits
	private static final int MIN_MSG_SIZE = 1 + LEN_TALKER + LEN_FORMATTER + 1 + 1 + LEN_CHKSUM;
	
	// error codes
	enum MsgStatus {
		NMEA_MSG_OK,	// msg structure is OK but not necessarily the fields' contents
		NMEA_MSG_ERR_NULL,
		NMEA_MSG_ERR_MSGSHORT,
		NMEA_MSG_ERR_NODOLLAR,
		NMEA_MSG_ERR_NOCOMMA,
		NMEA_MSG_ERR_NOSTAR,
		NMEA_MSG_ERR_CHKSUMNAN,
		NMEA_MSG_ERR_WRONGCHKSUM,
		NMEA_MSG_ERR_MSGNOTSUPPORTED,
		NMEA_MSG_ERR_NUMFLDS
	}

	///////// NMEA message fields
	String talker;    
	String formatter;
	String[] fields;
	int	chkSum;
	MsgStatus status;
	String statusMsg;
	///////// end of message fields

	/**
	 * Default Constructor
	 */
	public NmeaMessage() {
		// initialise status
		status = MsgStatus.NMEA_MSG_ERR_NULL;
		statusMsg = "NMEA message [null]";
	}

	/**
	 * Constructor from message string
	 * @param message		the NMEA message string to be processed
	 */
	NmeaMessage(String message) {
		// call the message parser with validateChksum = true (default)
		parseMessage(message, true);
	}
	
	/**
	 * Constructor from message string and validateChksum flag
	 * @param message			the NMEA message string to be processed
	 * @param validateChecksum	if false don't look for or check the checksum
	 */
	NmeaMessage(String message, boolean validateChecksum) {
		// call the message parser
		parseMessage(message, validateChecksum);
	}

	/**
	 * set the NMEA values from a message string
	 * @param message			the NMEA message string to be processed
	 */
	public void set(String message) {
		// call the message parser with validateChksum = true (default)
		parseMessage(message, true);
	}

	/**
	 * set the NMEA values from a message string and validateChksum flag
	 * @param message			the NMEA message string to be processed
	 * @param validateChecksum	if false don't look for or check the checksum
	 */
	public void set(String message, boolean validateChecksum) {
		// call the message parser with validateChksum = true (default)
		parseMessage(message, validateChecksum);
	}

	/**
	 * parse the message and populate the message variables
	 * @param message			the NMEA message string to be processed
	 * @param validateChecksum	if false don't look for or check the checksum
	 */
	private void parseMessage(String message, boolean validateChecksum) {
		
		// initialise fields
		talker = null;
		formatter = null;
		fields = null;
		chkSum = -1;
		status = MsgStatus.NMEA_MSG_OK;
		statusMsg = "OK";
		
		// first check basic structure / format of the message
		if (checkMsgOk(message, validateChecksum)) {

			StringBuilder sb = new StringBuilder(message);
			int msgLen = sb.length();

			// get talker
			talker = sb.substring(1, LEN_TALKER+1);

			// get formatter
			formatter = sb.substring(LEN_TALKER+1, LEN_TALKER+1+LEN_FORMATTER);

			// get data fields
			int dataStart = LEN_TALKER+1+LEN_FORMATTER+1;
			int dataEnd;
			if (sb.charAt(msgLen-LEN_CHKSUM-1) == '*')
				dataEnd = message.length() - LEN_CHKSUM-1;
			else
				dataEnd = message.length();
			String dataStr = message.substring(dataStart, dataEnd);
			fields = dataStr.split(",", -1);

			// finally check the formatter string and number of fields
			int index = Arrays.asList(NmeaFormatterString.msgFmt).indexOf(formatter);
			if (index < 0) {
				status = MsgStatus.NMEA_MSG_ERR_MSGNOTSUPPORTED;
				statusMsg = "NMEA message [" + message + "] formatter [" + formatter + "] not supported";
			}
			else
			if (NmeaFormatterString.numFlds[index] != fields.length) {
				status = MsgStatus.NMEA_MSG_ERR_NUMFLDS;
				statusMsg = "NMEA message [" + message + "] number of fields different to what expected (" + NmeaFormatterString.numFlds[index] + " expected)";
			}
		}
	}
		
	/**
	 * checks message structure and format 
	 * and sets the status variable to OK if all Ok or to an error value accordingly
	 * also sets the statusMsg string to the appropriate error message or OK
	 * @param message		the NMEA message string to be processed
	 * @param verifyChkSum	if false don't look for or check the checksum
	 * @return				true if message format ok
	 */
	private boolean checkMsgOk(String message, boolean verifyChkSum) {
		
		status = MsgStatus.NMEA_MSG_OK;

		// null check
		if (message == null) {
			status = MsgStatus.NMEA_MSG_ERR_NULL;
			statusMsg = "NMEA message [null]";
			return false;
		}

		// message size
		int msgLen = message.length();
		int minSize = MIN_MSG_SIZE;
		if (!verifyChkSum) {
			minSize = MIN_MSG_SIZE - LEN_CHKSUM - 1;
		}
		if (msgLen < minSize) {
			status = MsgStatus.NMEA_MSG_ERR_MSGSHORT;
			statusMsg = "NMEA message [" + message + "] too short (min " + minSize + ") required";
			return false;
		}

		// '$' in the beginning
		if (!message.startsWith("$")) {
			status = MsgStatus.NMEA_MSG_ERR_NODOLLAR;
			statusMsg = "NMEA message [" + message + "] no start delimiter ('$' expected)";
			return false;
		}
	
		// ',' after the formatter
		if (!message.startsWith(",", LEN_TALKER+LEN_FORMATTER+1)) {
			status = MsgStatus.NMEA_MSG_ERR_NOCOMMA;
			statusMsg = "NMEA message [" + message + "] no field delimiter (',' expected)";
			return false;
		}
			
		// checksum

		boolean chksumFound = false;
		// '*' before checksum
		if (message.startsWith("*", msgLen-LEN_CHKSUM-1))
			chksumFound = true;

		if (verifyChkSum && ! chksumFound) {
			status = MsgStatus.NMEA_MSG_ERR_NOSTAR;
			statusMsg = "NMEA message [" + message + "] no checksum delimiter ('*' expected)";
			return false;
		}

		String chksumStr = message.substring(msgLen-LEN_CHKSUM, msgLen);
		// checksum numeric
		try {
			chkSum = Integer.parseInt(chksumStr, 16);
		}
		catch (Exception e) {
			if (verifyChkSum) {
				status = MsgStatus.NMEA_MSG_ERR_CHKSUMNAN;
				statusMsg = "NMEA message [" + message + "] checksum not numeric (hex number expected)";
				return false;
			}
		}

		// checksum value
		if (verifyChkSum && checkSum(message) != chkSum) {
			status = MsgStatus.NMEA_MSG_ERR_WRONGCHKSUM;
			statusMsg = "NMEA message [" + message + "] checksum mismatch (" + checkSum(message) + " expected)";
			return false;
		}

		// return OK
		return true;
	}
		
	/**
	 * NMEA Checksum of message
	 * The checksum at the end of each sentence is the XOR of all of the bytes in the sentence, 
	 * excluding the initial dollar sign (and of course excluding the checksum itself)
	 * 
	 * @param message		the NMEA message char array
	 * @return				the checksum of the message (0x00-0xff)
	 */
	public int checkSum(String message) {
		
		if (message == null)
			return -1;
		
		int chcksum = 0;
		char[] cArr = message.toCharArray();
		
		// skip the '$' in the beginning and the actual checksum '*hh' at the end
		for (int i = 1; i < cArr.length-3; ++i) {
			// XOR all the characters of the message
			chcksum ^= cArr[i];
			// ensure only one byte is actually calculated
			chcksum &= 0xff;
		}
		return chcksum;
	}
			
	/**
	 * convert NMEA message object back to string
	 */
	@Override
	public String toString() {
		StringBuilder sb;
		
		if (talker == null & formatter == null && fields == null)
			return null;
		
		sb = new StringBuilder("$");
		sb.append(talker);
		sb.append(formatter);
		for (String s : fields) {
			sb.append(",").append(s);
		}
		if (chkSum >= 0)
			sb.append("*").append(Integer.toHexString(chkSum).toUpperCase());
		return sb.toString();
	}

	/**
	 * prints the NMEA message for debugging or info
	 */
	public void printMsg() {
		System.out.println("Contents of message");
		System.out.println("-------------------");
		System.out.println("Talker:    [" + talker + "]");
		System.out.println("Formatter: [" + formatter + "]");
		System.out.println("Fields:");
		if (fields == null)
			System.out.println("           [null]");
		else
			for (int i = 0; i < fields.length; ++i)
				System.out.format("      %02d   [%s]%n", i, fields[i]);
		if (chkSum >= 0)
			System.out.println("Checksum:  [" + Integer.toHexString(chkSum).toUpperCase() + "]");
		System.out.println("Msg Status:[" + status + "]");
		System.out.println("-------------------");
	}
	
} /////////////////// Class NmeaMessage //////////////////////////////
