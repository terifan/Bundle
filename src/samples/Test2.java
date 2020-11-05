package samples;

import org.terifan.bundle.Bundle;


public class Test2
{
	public static void main(String... args)
	{
		try
		{
			Bundle b = new Bundle().unmarshalJSON("{\"default\":\"DWB\",\"options\":[[[\"CMR\",\"CMR\"],[\"DWB\",\"Domestic Waybill\"]],[[\"OTHER\",\"Other\"],[\"POD\",\"Proof-of-delivery\"],[\"CLM\",\"Claim\"],[\"DLN\",\"Delivery Note\"]]]}");

			System.out.println(b.getArray("options").getArray(0).getArray(0).getString(0));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
