package mn.hart.android.simpleaidl;


import java.lang.reflect.Constructor;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public abstract class AIDLObject implements Parcelable {
	
	public int describeContents() {
        return 0;
    }
	
	/**
	 * Save the implementation's name so it can selected on service-side
	 * @param out The parcel to parcelify the strategy to
	 */
	public void writeToParcel(Parcel out, int flags) {
		Bundle instanceData = new Bundle();
		writeInstanceData(instanceData);
		
    	out.writeString(this.getClass().getName());
    	out.writeBundle(instanceData);
    	
    }


	public static final Creator<AIDLObject> CREATOR
            = new Parcelable.Creator<AIDLObject>() {
    	
    	/**
    	 * Instantiate the desired subclass by name
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

        public AIDLObject[] newArray(int size) {
            return new AIDLObject[size];
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

	protected abstract void contructFromInstanceData(Bundle instanceData);
	
	protected abstract void writeInstanceData(Bundle instanceData);
	
	

        
}
