package mn.hart.android.simpleaidl;

import java.lang.reflect.Constructor;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Handles the packing of an AIDLBundleable implementer
 * prior to its shuttling across processes, and is responsible
 * for orchestrating that implementer's reconstruction on
 * the other side.
 * @author Kevin Hartman <kevin@hart.mn>
 * @version 1.0
 */
public class AIDLBundler implements Parcelable {
	private AIDLBundleable bundleable;
	
	/**
	 * Constructs an AIDLBundler to handle the bundling
	 * and unpacking of an AIDLBundleable implementer
	 * before and after its interprocess transmission,
	 * respectively.
	 * 
	 * NOTE: This constructor is also called when the
	 * AIDLBundler object itself is recreated on the other
	 * side.
	 * @param bundleable The AIDLBundleable to be shuttled.
	 */
	public AIDLBundler(AIDLBundleable bundleable) {
		this.bundleable = bundleable;
	}
	
	/**
	 * Get the AIDLBundleable that is housed by this
	 * AIDLBundler.
	 * 
	 * NOTE: A call to this method from the local side
	 * will not return the same object as a call to this
	 * method from the remote side. However, these objects
	 * should be equivalent if bundling and unpacking is
	 * done appropriately in the AIDLBundleable implementer
	 * class.
	 * @return The AIDLBundleable housed by this AIDLBundler
	 */
	public AIDLBundleable getBundleable() {
		return bundleable;
	}
	
	/**
	 * Specify special flags for marshaling process
	 */
	public int describeContents() {
        return 0;
    }
	
	/**
	 * Write the AIDLBundleable's data bundle and save
	 * that, and the AIDLBundleable's name, in the parcel
	 * that will be used in reconstruction.
	 * @param out The parcel in which to save the AIDLBundleable's data
	 */
	public void writeToParcel(Parcel out, int flags) {
		Bundle instanceData = new Bundle();
		bundleable.writeInstanceData(instanceData);
		
    	out.writeString(bundleable.getClass().getName());
    	out.writeBundle(instanceData);
    	
    }

	/**
	 * Creator object that the AIDL generated service class
	 * will be looking for when it's time to recreate this
	 * AIDLBundler on the other side.
	 */
	public static final Creator<AIDLBundler> CREATOR
            = new Parcelable.Creator<AIDLBundler>() {
    	
    	/**
    	 * Instantiate the desired AIDLBundleable by name and provide
    	 * it with its data bundle.
    	 * @param in The AIDLBundleable's data.
    	 * @return An AIDLBundler, holding the desired AIDLBundleable or null if error.
    	 */
        public AIDLBundler createFromParcel(Parcel in) {
        	String className = in.readString();
        	Bundle instanceData = in.readBundle();
        	
        	try {        		
                Constructor<?> implementerConstructor = AndroidMagicConstructorMaker.make(Class.forName(className));
                implementerConstructor.setAccessible(true);
                
                AIDLBundleable implementer = (AIDLBundleable) implementerConstructor.newInstance();
        		implementer.contructFromInstanceData(instanceData);

                AIDLBundler subclasser = new AIDLBundler(implementer);
                                
        		return subclasser;
        		
        	} catch (Exception e) {
        		Log.e("AIDLObject.CREATOR.createFromParcel", e.getCause().getMessage());
        	}
        	
        	return null;
        }

        /**
         * Required by Parcelable
         */
        public AIDLBundler[] newArray(int size) {
            return new AIDLBundler[size];
        }
    };
	
    /**
     * Create a new no-args constructor for any class by its name.
     * any of its constructors.
     * @author Kevin Hartman <kevin@hart.mn>
     * @version 1.0
     *
     */
    private static class AndroidMagicConstructorMaker {
    	/**
    	 * 
    	 * @param clazz The class for which to create a constructor.
    	 * @return A no-args constructor.
    	 * @throws Exception
    	 */
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

        
}
