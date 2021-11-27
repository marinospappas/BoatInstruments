package com.mpdev.android.boatinstruments;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.NmeaMessage;

/**
 * Wrapper class that inclides all the data related objects
 * so that they can be passed around easily
 */
class InstrDataWrapper {

    // boat / nmea objects
    NmeaGw nmeaGw;
    BoatData boatData;
    NmeaMessage nmeaMessage;

    /** Constructor */
    InstrDataWrapper(NmeaGw nmeaGw, BoatData boatData, NmeaMessage nmeaMessage) {
        this.nmeaGw = nmeaGw;
        this.boatData = boatData;
        this.nmeaMessage = nmeaMessage;
    }
}
