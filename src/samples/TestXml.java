package samples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.terifan.bundle.Bundle;


public class TestXml
{
	public static void main(String... args)
	{
		try
		{
			Bundle bundle = new Bundle();

//			bundle.unmarshalXML(new ByteArrayInputStream("<root id=\"aa\"><arrayElement id=\"bb\">content</arrayElement><arrayElement id=\"cc\"><a id=\"dd\">1</a><a>2</a><a>3</a><b>text</b></arrayElement><element id=\"ee\">content</element></root>".getBytes()), true);

			try (InputStream in = TestXml.class.getResourceAsStream("test3.xml"))
			{
				bundle.unmarshalXML(in);
			}

			System.out.println(bundle.marshalJSON(false));

			System.out.println(bundle.marshalJSON(true).length());
			System.out.println(bundle.marshal().length);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
