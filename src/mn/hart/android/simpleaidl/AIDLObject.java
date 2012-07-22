package mn.hart.android.simpleaidl;

import java.lang.reflect.Constructor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Packs itself prior to its shuttling across processes, 
 * and is responsible for orchestrating its own reconstruction
 * on the other side.
 * @author Kevin Hartman <kevin@hart.mn>
 * @version 1.0
 */
public abstract class AIDLObject implements Parcelable {
	
	/**
	 * Specify special flags for marshaling process
	 */
	public int describeContents() {
        return 0;
    }
	
	/**
	 * Write this object's data bundle and save
	 * that, and its name, in the parcel
	 * that will be used in reconstruction.
	 * @param out The parcel in which to save its data.
	 */
	public void writeToParcel(Parcel out, int flags) {
		Bundle instanceData = new Bundle();
		writeInstanceData(instanceData);
		
    	out.writeString(this.getClass().getName());
    	out.writeBundle(instanceData);
    	
    }

	/**
	 * Creator object that the AIDL generated service class
	 * will be looking for when it's time to recreate this
	 * AIDLObject on the other side.
	 */
	public static final Creator<AIDLObject> CREATOR
            = new Parcelable.Creator<AIDLObject>() {
    	
    	/**
    	 * Instantiate the desired AIDLObject subclass by name and provide
    	 * it with its data bundle.
    	 * @param in The AIDLObject's data.
    	 * @return An AIDLObject, or null if error.
    	 */
        public AIDLObject createFromParcel(Parcel in) {
        	String className = in.readString();
        	Bundle instanceData = in.readBundle();
        	
        	try {        		
                Constructor<?> implementerConstructor = AndroidMagicConstructorMaker.make(Class.forName(className));
                implementerConstructor.setAccessible(true);
                AIDLObject implementer = (AIDLObject) implementerConstructor.newInstance();
                
        		implementer.contructFromInstanceData(instanceData);
        		return implementer;
        		
        	} catch (Exception e) {
        		Log.e("AIDLObject.CREATOR.createFromParcel", e.getCause().getMessage());
        	}
        	
        	return null;
        }

        /**
         * Required by Parcelable
         */
        public AIDLObject[] newArray(int size) {
            return new AIDLObject[size];
        }
    };
    
    /**
     * Create a new no-args constructor for any class by its name.
     * @author Kevin Hartman <kevin@hart.mn>
     * @version 1.0
     *
     */
    private static class AndroidMagicConstructorMaker {

    	@SuppressWarnings("unchecked")
    	public static <T> Constructor<T> make(Class<T> clazz) throws Exception {
    		Constructor<?> constr = Constructor.class.getDeclaredConstructor(
    				Class.class, // Class<T> declaringClass
    				Class[].class, // Class<?>[] parameterTypes
    				Class[].class, // Class<?>[] checkedExceptions
    				int.class); // int slot
    		constr.setAccessible(true);

    		return (Constructor<T>) constr.newInstance(clazz, new Class[0],
    				new Class[0], 1);
    	}
    }

    /**
     * Set up instance using provided data.
     * @param instanceData Bundle created by this object before IPC.
     */
	protected abstract void contructFromInstanceData(Bundle instanceData);
	
	/**
	 * Write instance data prior to shuttling.
	 * @param instanceData Bundle in which to write instance data.
	 */
	protected abstract void writeInstanceData(Bundle instanceData);
	
	

        
}
