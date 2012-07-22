package mn.hart.android.simpleaidl;

import android.os.Bundle;

/**
 * Provides constructs that allow an object to pack and
 * recreate itself before and after IPC, respectively.
 * into a Bundle and r
 * @author Kevin Hartman <kevin@hart.mn>
 * @version 1.0
 */
public interface AIDLBundleable {
	
    /**
     * Set up instance using provided data.
     * @param instanceData Bundle created by this object before IPC.
     */
	public void contructFromInstanceData(Bundle instanceData);
	
	/**
	 * Write instance data prior to shuttling.
	 * @param instanceData Bundle in which to write instance data.
	 */
	public void writeInstanceData(Bundle instanceData);
}
