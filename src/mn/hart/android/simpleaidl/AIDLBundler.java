package mn.hart.android.simpleaidl;


import java.lang.reflect.Constructor;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class AIDLBundler implements Parcelable {
	private AIDLBundleable objectable;
	
	public AIDLBundler(AIDLBundleable objectable) {
		this.objectable = objectable;
	}
	
	public AIDLBundleable getBundleable() {
		return objectable;
	}
	
	public int describeContents() {
        return 0;
    }
	
	/**
	 * Save the implementation's name so it can selected on service-side
	 * @param out The parcel to parcelify the strategy to
	 */
	public void writeToParcel(Parcel out, int flags) {
		Bundle instanceData = new Bundle();
		objectable.writeInstanceData(instanceData);
		
    	out.writeString(objectable.getClass().getName());
    	out.writeBundle(instanceData);
    	
    }


	public static final Creator<AIDLBundler> CREATOR
            = new Parcelable.Creator<AIDLBundler>() {
    	
    	/**
    	 * Instantiate the desired subclass by name
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

        public AIDLBundler[] newArray(int size) {
            return new AIDLBundler[size];
        }
    };
	
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

        
}
