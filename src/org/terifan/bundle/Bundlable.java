package org.terifan.bundle;


/**
 * Classes that implement the Bundlable interface can be serialized to/from Bundles similar to the Externalizable interface.
 *
 * <p>
 * Example of marshaling of an object into a Bundle and an array of those objects into an Array:
 * <pre>
 * class MyFirstClass implements Bundlable
 * {
 *    private String mText;
 *    private double mNumber;
 *    public void readExternal(BundlableInput aInput)
 *    {
 *       Bundle in = aInput.bundle();
 *       mText = in.getString("text");
 *       mNumber = in.getDouble("number");
 *    }
 *    public void writeExternal(BundlableOutput aOutput)
 *    {
 *       aOutput.bundle().putString("text", mText).putNumber("number", mNumber);
 *    }
 * }
 *
 * class MySecondClass implements Bundlable
 * {
 *    private MyFirstClass[] mValues;
 *    private double mSomething;
 *    public void readExternal(BundlableInput aInput)
 *    {
 *       Array in = aInput.array();
 *       mValues = in.getBundlableArray(0, MyFirstClass.class);
 *       mSomething = in.getDouble(1, mSomething);
 *    }
 *    public void writeExternal(BundlableOutput aOutput)
 *    {
 *       aOutput.array(mValues, mSomething);
 *    }
 * }
 *
 * void test()
 * {
 *    MyFirstClass a = new MyFirstClass(); a.mNumber = 3; a.mText = "3a";
 *    MyFirstClass b = new MyFirstClass(); b.mNumber = 7; b.mText = "7a";
 *    MySecondClass c = new MySecondClass(); c.mValues = new MyFirstClass[]{a,b}; c.mSomething = 1.5;
 *
 *    Bundle z = new Bundle().putBundlable("a", c);
 *    System.out.println(z); // prints: {"a":[[{"text":"3a","number":3},{"text":"7a","number":7}],1.5]}
 *
 *    MySecondClass x = z.getBundlable("a", MySecondClass.class);
 *    System.out.println(x.mSomething);
 *    System.out.println(x.mValues[0].mText);
 *    System.out.println(x.mValues[1].mText);
 * }
 * </pre>
 */
public interface Bundlable
{
	void readExternal(BundlableInput aInput);

	void writeExternal(BundlableOutput aOutput);
}
