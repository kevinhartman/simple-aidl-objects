package mn.hart.android.simpleaidl;

import android.os.Bundle;

public interface AIDLBundleable {
	
	public void contructFromInstanceData(Bundle instanceData);
	
	public void writeInstanceData(Bundle instanceData);
}
