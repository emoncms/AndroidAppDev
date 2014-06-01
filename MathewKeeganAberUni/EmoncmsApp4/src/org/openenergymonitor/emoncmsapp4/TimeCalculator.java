package org.openenergymonitor.emoncmsapp4;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TimeCalculator {

	public TimeCalculator() {
		
	}
	
	/* Deprecated? 27/8/13 */
	public Long dateToday() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long unixTimeStamp = c.getTimeInMillis();
		
		return unixTimeStamp;
	}
	
	/* Returns a Unix timestamp the requested number of days in the past */
	public Long pastDate(int noDays) {		
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        long elapsed = System.currentTimeMillis() - (noDays * DAY_IN_MS);
        
        return elapsed;

	}
}
