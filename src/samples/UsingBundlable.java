package samples;

import java.io.IOException;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.Bundle;


public class UsingBundlable
{
	public static void main(String ... args)
	{
		try
		{
			String pson;

			{
				Person person = new Person("stig",
					new Friend("olle",
						new Hobby("diving"),
						new Hobby("baking")),
					new Friend("kurt",
						new Hobby("skating")));

				Bundle bundle = new Bundle();
				person.writeExternal(bundle);

				pson = bundle.marshalPSON(true);
			}

			System.out.println(pson);

			{
				Bundle bundle = new Bundle().unmarshalPSON(pson);

				Person person = new Person();
				person.readExternal(bundle);

				System.out.println(person.name);
				for (Friend p : person.friends)
				{
					System.out.println("   " + p.name);
					for (Hobby h : p.hobbies)
					{
						System.out.println("      " + h.name);
					}
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}

	public static class Person implements Bundlable
	{
		String name;
		Friend[] friends;

		public Person()
		{
		}

		public Person(String aName, Friend... aFriends)
		{
			this.name = aName;
			this.friends = aFriends;
		}

		@Override
		public void readExternal(Bundle aBundle) throws IOException
		{
			name = aBundle.getString("name");
			friends = aBundle.getBundlableArray("friends", Friend.class);
		}

		@Override
		public void writeExternal(Bundle aBundle) throws IOException
		{
			aBundle.putString("name", name);
			aBundle.putBundlableArray("friends", friends);
		}
	}

	public static class Friend implements Bundlable
	{
		String name;
		Hobby[] hobbies;

		public Friend()
		{
		}

		public Friend(String aName, Hobby... aHobbies)
		{
			this.name = aName;
			this.hobbies = aHobbies;
		}

		@Override
		public void readExternal(Bundle aBundle) throws IOException
		{
			name = aBundle.getString("name");
			hobbies = aBundle.getBundlableArray("hobbies", Hobby.class);
		}

		@Override
		public void writeExternal(Bundle aBundle) throws IOException
		{
			aBundle.putString("name", name);
			aBundle.putBundlableArray("hobbies", hobbies);
		}
	}

	public static class Hobby implements Bundlable
	{
		String name;

		public Hobby()
		{
		}

		public Hobby(String aName)
		{
			this.name = aName;
		}

		@Override
		public void readExternal(Bundle aBundle) throws IOException
		{
			name = aBundle.getString("name");
		}

		@Override
		public void writeExternal(Bundle aBundle) throws IOException
		{
			aBundle.putString("name", name);
		}
	}
}
