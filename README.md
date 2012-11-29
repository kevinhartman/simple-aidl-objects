Simple AIDL Objects
===================

Simple AIDL Object offers a versatile solution for AIDL-defined methods that need to accept interfaces or superclasses, allowing design patterns to be used more easily across Android services and their clients.

by Kevin Hartman

Disclaimers
===========

* This is my personal approach to enabling AIDL-defined methods to accept interfaces and superclasses as parameters. This is possible without Simple AIDL Objects, but is, in my opinion, a lot messier. If you're looking to implement inheritance over AIDL without Simple AIDL Objects, I can help you! Check out <a href="http://kevinhartman.github.com/blog/2012/07/23/inheritance-through-ipc-using-aidl-in-android/">my blog</a> for a post describing how to go about doing that.

* Simple AIDL Objects does not address the developement concerns that surround an AIDL made interface at all; <b>you're still completely responsible for designing an effective AIDL implementation.</b>

Preface
=======
Android provides an interface definition language that developers can use directly in order to create and communicate with a Service, across processes, in a complex way that cannot be suited by using a simple Messenger.

Problem
=======
Methods that can be defined in an AIDL service (ex. a method within IPotatoSaladService.aidl) can, by default, only accept some very basic types as parameters. The full list can be found on the Android developer website. If the developer wishes to have a method accept custom types (ex. Potato), they must:

* Define a new AIDL interface for said type (ex. Potato.aidl)
* Implement Parcelable in the type's class (implementing all of its required methods, of course)
* Define a static CREATOR field with a Creator object that requires a concrete implementation, which must be set up as well...

That's a lot of complication.

Solution
========
Simple AIDL Objects!

##How it Simplifies:
This is Java. We shouldn't have to bend over backwards to satisfy requirements that the compiler doesn't even know about. This library hides that ugliness from the developer and won't spring any responsibilities on you that your compiler won't tell you about. 

* No longer do you need to define a .aidl file for the type. You just do nothing.
* Just implement AIDLBundleable or extend AIDLObject and define two methods in your new type's class.

Done.

##How it Handles Inheritance:

If your type implements AIDLBundleable, all you need to do is instantiate a new AIDLBundler with your type object as a parameter (whether it be an interface or concrete class) and pass the AIDLBundler to your AIDL service's methods. 

If your type extends AIDLObject, you can pass your type or its inheritors directly to your AIDL service's methods, and this library will take care of the rest for you, allowing you to use inheritance naturally.

Installation
============
For now, grab <b>simple-aidl-objects.jar</b> from root, and add it to your Android project's build path. <b>That's it!</b>
<i>Note that this is a research project and is <b>not meant for production.</b></i> See my blog for a safer approach to inheritance over AIDL, if that's why you're here ;)

Usage
=====
The examples provided for both solutions are strategy design pattern implementations, because that particular pattern requires simple inheritance and thus makes a good example. That's just one example- you can do anything you normally would with inheritance. In fact, Simple AIDL Objects is meant to be used even when you have no need for inheritance, and simply need to define a simple new type.

##Interface Solution
This is an example of how you can use the AIDLBundler solution included within this library to create a new type for use with a Strategy design pattern.

<b>IMyStrategyService.aidl:</b>
`````java
package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLBundler;

interface IMyStrategyService {
  void setStrategy(in AIDLBundler strategy);
}
`````
This is the AIDL file associated with your AIDL-using service. This library does not help you skip any steps here. <b>You're fully responsible for implementing a safe Service.</b> All you need to note here is that the method used to set the strategy to use in our service takes an AIDLBundler object.

<b>MyStrategy.java:</b>
`````java
package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLBundleable;

public abstract class MyStrategy implements AIDLBundleable {
  // Nothing here, because I'm too lazy to make our strategies
  // do something.
}
`````
This is the strategy abstract class that we can use to define some standard functionality for all of our concrete strategies.

<b>MyConcreteStrategy.java:</b>
`````java
package mn.hart.example;
import android.os.Bundle;
import android.util.Log;

public class MyConcreteStrategy extends MyStrategy {
  private final String INTERVAL_KEY = "interval";
  private long interval;
  
  /**
   * Notice how the constructor accepts a long. That's
   * not required by MyStrategy. We could've constructed this
   * object with whatever we'd wanted. Point is, subclasses
   * of our type can be constructed with variable constructors
   * that differ from one another. That took me a lot of work.
   */
  public MyConcreteStrategy(long intervalMillis) {
    this.interval = intervalMillis;
  }

  /**
   * Sets this object up on the other side. Should do
   * an initialization equivalent to that of the
   * constructor.
   */
  @Override
  public void contructFromInstanceData(Bundle instanceData) {
    this.interval = instanceData.getLong(INTERVAL_KEY);
  }

  /**
   * Stuff this object's information into a Bundle.
   * We will get this bundle back on the other side
   * and will use it to remake this object.
   */
  @Override
  public void writeInstanceData(Bundle instanceData) {
    instanceData.putLong(INTERVAL_KEY, interval);
  }
}
`````
This is a concrete strategy that happens to take a parameter of type long in its constructor. As noted in the comments within the actual code, constructors take whatever you'd like as parameters. Just like they would if you weren't implementing your strategy pattern over AIDL.

<b>MyStrategyActivity.java:</b>
`````java
package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLBundler;

...

      public void onServiceConnected(ComponentName className, IBinder service) {
        IMyStrategyService mIMyStrategyService = IMyStrategyService.Stub.asInterface(service);
        
        try {
          mIMyStrategyService.setStrategy(new AIDLBundler(new MyConcreteStrategy(100L)));
        } catch (RemoteException e) {
          // Fail
        }
      }
      
...

`````
Notice how the strategy is set using the AIDLBundler above. A particular concrete strategy is passed, but we could've passed any concrete strategy that we'd wanted.

<b>MyStrategyService.java:</b>
`````java
/**
 * A client activity that can use the service remotely.
 */

package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLBundler;

...

public class MyStrategyService extends Service {
  MyStrategy strategy = null;
  
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
  
  private synchronized void setStrategySafe(MyStrategy myStrategy) {
    strategy = myStrategy;
  }
  
  private final IMyStrategyService.Stub mBinder = new IMyStrategyService.Stub() {
    public void setStrategy(AIDLBundler aidlBundler) throws RemoteException {
      setStrategySafe((MyStrategy) aidlBundler.getBundleable());
    }
  };
}
`````
Notice how the instance is retrieved from the AIDLBundler. An explicit cast to our interface type is required.


And that's it for that one!


##Abstract Class Solution
This is an example of how you can use the AIDLObject solution included within this library to create a new type for use with a Strategy design pattern. With this option, you're able to use inheritance directly, without the AIDLBundler wrapper. The caveat, however, is that you must extend AIDLObject, rather than just implement AIDLBundleable.


<b>IMyStrategyService.aidl:</b>
`````java
package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLObject;

interface IMyStrategyService {
  void setStrategy(in AIDLObject strategy);
}
`````
This is the AIDL file associated with your AIDL-using service. Again, this library does not help you skip any steps here. <b>You're fully responsible for implementing a safe Service.</b> All you need to note here is that the method used to set the strategy to use in our service takes an AIDLObject object, which we will extend later.

<b>MyStrategy.java:</b>
`````java
package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLObject;

public abstract class MyStrategy extends AIDLObject {
  // Nothing here, because I'm too lazy to make our strategies
  // do something.
}
`````
This is the strategy abstract class that we can use to define some standard functionality for all of our concrete strategies.

<b>MyConcreteStrategy.java:</b>
`````java
package mn.hart.example;
import android.os.Bundle;
import android.util.Log;

public class MyConcreteStrategy extends MyStrategy {
  private final String INTERVAL_KEY = "interval";
  private long interval;
  
  /**
   * Notice how the constructor accepts a long. That's
   * not required by MyStrategy. We could've constructed this
   * object with whatever we'd wanted. Point is, subclasses
   * of our type can be constructed with variable constructors
   * that differ from one another. That took me a lot of work.
   */
  public MyConcreteStrategy(long intervalMillis) {
    this.interval = intervalMillis;
  }

  /**
   * Sets this object up on the other side. Should do
   * an initialization equivalent to that of the
   * constructor.
   */
  @Override
  public void contructFromInstanceData(Bundle instanceData) {
    this.interval = instanceData.getLong(INTERVAL_KEY);
  }

  /**
   * Stuff this object's information into a Bundle.
   * We will get this bundle back on the other side
   * and will use it to remake this object.
   */
  @Override
  public void writeInstanceData(Bundle instanceData) {
    instanceData.putLong(INTERVAL_KEY, interval);
  }
}
`````
This is a concrete strategy that happens to take a parameter of type long in its constructor. As noted in the comments within the actual code, constructors take whatever you'd like as parameters. Just like they would if you weren't implementing your strategy pattern over AIDL.

<b>MyStrategyActivity.java:</b>
`````java
package mn.hart.example;

...

      public void onServiceConnected(ComponentName className, IBinder service) {
        IMyStrategyService mIMyStrategyService = IMyStrategyService.Stub.asInterface(service);
        
        try {
          mIMyStrategyService.setStrategy(new MyConcreteStrategy(100L));
        } catch (RemoteException e) {
          // Fail
        }
      }
      
...

`````
Notice how the strategy is set simply as a concrete strategy, just as one would expect in a strategy pattern that was not facilitated using AIDL. A particular concrete strategy is passed, but we could've passed any concrete strategy that we'd wanted.

<b>MyStrategyService.java:</b>
`````java
/**
 * A client activity that can use the service remotely.
 */

package mn.hart.example;
import mn.hart.android.simpleaidl.AIDLObject;

...

public class MyStrategyService extends Service {
  MyStrategy strategy = null;
  
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
  
  private synchronized void setStrategySafe(MyStrategy myStrategy) {
    this.strategy = myStrategy;
  }
  
  private final IMyStrategyService.Stub mBinder = new IMyStrategyService.Stub() {
    public void setStrategy(AIDLObject aidlObject) throws RemoteException {
      setStrategySafe((MyStrategy) aidlObject);
    }
  };
}
`````
An explicit cast to our interface type is required from AIDLObject.

Limitations
===========

##Explicit Casting
Using the AIDLBundler and Bundleable interface solution, an explicit cast to your desired type on the Service side is required:


`````java
...
YourType type = (YourType) aidlBundler.getBundleable();   // Where YourType implements AIDLBundleable
`````

This is due to an implementation detail of AIDL itself. Specifically, generics are not accounted for in the design of AIDL.

The explicit casting means that it's possible to pass particular AIDLBundler and AIDLObject objects to service methods that should not be allowed to accept them- which will result in a runtime error. With very mild caution, this should be easily avoidable.

##Not so Standard Reflection
Because this library uses some less than standard reflection in order to make your life so simple, there's a possiblity that it may not work with particular versions of Android. It is currently tested only on Android 4.0.3 and 4.1.1, but it's <b>very</b> likely that it should work just fine on all versions of Android. Test it out and let me know! If there are problems for a specific version of Android that you'd like to use, let me know about that too, and I'll do my best to get it working.

##Current Development State
Simple AIDL Objects is in a useable state, but I can't guarantee that I'll continue working on it. I'll do my best to fix bugs that people may find in it / add support for other Android versions if I find out that this isn't working on everything, if I can.