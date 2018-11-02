package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import samples.Log;


public class LZJB
{
	private final static int MATCH_BITS = 6;
	private final static int MATCH_MIN = 2;
	private final static int MATCH_MAX = (1 << MATCH_BITS) + (MATCH_MIN - 1);
	private final static int WINDOW_SIZE = 1 << (16 - MATCH_BITS);
	private final static int OFFSET_MASK = WINDOW_SIZE - 1;
	private final static int REFS_COUNT = 8;

	private byte[] mWindow = new byte[0];
	private int[][] mRefs = new int[WINDOW_SIZE][REFS_COUNT];
	private int mWindowOffset;


	public LZJB()
	{
		for (int[] refs :mRefs)
		{
			Arrays.fill(refs, -1);
		}
	}


	public byte[] compress(byte[] aSrcBuffer, int aSrcLen) throws IOException
	{
		int srcX = 0;
		int copymap = 0;
		int copymask = 128;

		mWindow = Arrays.copyOfRange(mWindow, 0, mWindowOffset + aSrcLen);
		System.arraycopy(aSrcBuffer, 0, mWindow, mWindowOffset, aSrcLen);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayOutputStream work = new ByteArrayOutputStream();

		boolean first = true;

		while (srcX < aSrcLen)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				if (work.size() > 0)
				{
					baos.write(copymap);
					work.writeTo(baos);
					work.reset();
				}

				copymask = 1;
				copymap = 0;

				if (first)
				{
					copymask <<= 1;
					first = false;
				}
			}

			if (srcX >= aSrcLen - MATCH_MIN)
			{
				work.write(0xff & aSrcBuffer[srcX++]);
				continue;
			}

			int hash = ((0xff & aSrcBuffer[srcX]) << 16) + ((0xff & aSrcBuffer[srcX + 1]) << 8) + (0xff & aSrcBuffer[srcX + 2]);
			hash += hash >> 9;
			hash += hash >> 5;
			hash &= OFFSET_MASK;

			int bestLength = 0;
			int bestDist = 0;

			for (int i = 0; i < REFS_COUNT && mRefs[hash][i] > -1; i++)
			{
				int dist = mWindowOffset + srcX - mRefs[hash][i];
				int cpy = mRefs[hash][i];

				if (dist >= 0 && dist < WINDOW_SIZE && cpy + MATCH_MIN < mWindowOffset + srcX)
				{
					int mlen = 0;
					for (; srcX + mlen < aSrcLen && mlen < MATCH_MAX; mlen++)
					{
						if (aSrcBuffer[srcX + mlen] != mWindow[cpy + mlen])
						{
							break;
						}
					}

					if (mlen > bestLength)
					{
						bestLength = mlen;
						bestDist = dist;
					}
				}
			}

			System.arraycopy(mRefs[hash], 0, mRefs[hash], 1, REFS_COUNT - 1);
			mRefs[hash][0] = mWindowOffset + srcX;

			if (bestLength >= MATCH_MIN)
			{
				copymap |= copymask;
				work.write(0xff & ((((bestLength - MATCH_MIN) << (8 - MATCH_BITS)) | (bestDist >> 8))));
				work.write(0xff & bestDist);
				srcX += bestLength;
			}
			else
			{
				work.write(0xff & aSrcBuffer[srcX++]);
			}
		}

		mWindowOffset += aSrcLen;

		baos.write(copymap);
		work.writeTo(baos);
		
		byte[] tmp = baos.toByteArray();

		if (tmp.length == 3 && tmp[0] == 2 && (tmp[1] & 0x80) == 0)
		{
			tmp = new byte[]
			{
				(byte)((0xff & tmp[1] << 1) | 0x01),
				tmp[2]
			};
		}
		else
		{
			tmp = Arrays.copyOfRange(tmp, 0, tmp.length + 1);
			System.arraycopy(tmp, 0, tmp, 1, tmp.length - 1);
			tmp[0] = (byte)((tmp.length - 1) << 1);
		}
		
		return tmp;
	}


	public byte[] decompress(byte[] aSrcBuffer)
	{
		int src = 0;
		int end = 0;
		int copymap = 0;
		int copymask = 128;

		boolean first = true;

		if ((aSrcBuffer[0] & 0x01) == 0x01)
		{
			aSrcBuffer = new byte[]
			{
				(byte)0x02,
				(byte)((0xff & aSrcBuffer[0]) >> 1),
				(byte)aSrcBuffer[1]
			};

			end = 2;
		}
		else
		{
			end = (0xff & aSrcBuffer[0]) >> 1;
			src++;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		while (src <= end)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymap = 0xff & aSrcBuffer[src++];

				if (first)
				{
					copymask <<= 1;
					first = false;
				}
			}
			if ((copymap & copymask) != 0)
			{
				int a = 0xff & aSrcBuffer[src++];
				int b = 0xff & aSrcBuffer[src++];

				int mlen = (a >> (8 - MATCH_BITS)) + MATCH_MIN;
				int dist = ((a << 8) | b) & OFFSET_MASK;
				
				int cpy = mWindowOffset - dist;
				if (cpy < 0)
				{
					throw new RuntimeException();
				}
				while (--mlen >= 0)
				{
					mWindow = Arrays.copyOfRange(mWindow, 0, mWindowOffset + 1);
					mWindow[mWindowOffset++] = mWindow[cpy];
					baos.write(0xff & mWindow[cpy++]);
				}
			}
			else
			{
				mWindow = Arrays.copyOfRange(mWindow, 0, mWindowOffset + 1);
				mWindow[mWindowOffset++] = aSrcBuffer[src];
				baos.write(0xff & aSrcBuffer[src++]);
			}
		}
		
		return baos.toByteArray();
	}


	public static void main(String... args)
	{
		try
		{
			LZJB compressor = new LZJB();
			LZJB decompressor = new LZJB();

			int sumPacked = 0;
			int sumUnpacked = 0;

			for (String s : new String[]{"TransmissionHeader","ApiKey","DevKey","ApiKey","Version","v1","Version","TransmissionType","TransmissionType","TransmissionType","TransmissionCreateDateTime","TransmissionCreateDateTime","TransactionCount","TransactionCount","SenderName","SenderName","SenderName","ReceiverName","ReceiverName","ReceiverName","SenderTransmissionNo","SenderTransmissionNo","SenderTransmissionNo","SuppressTransmissionAck","SuppressTransmissionAck","StopProcessOnError","StopProcessOnError","ProcessGrouping","ProcessGroup","ProcessGroup","ProcessGroup","ProcessGroupOwner","ProcessGroupOwner","ProcessGroupOwner","InSequence","InSequence","StopProcessOnError","StopProcessOnError","ProcessGrouping","TransmissionHeader","TransmissionBody","TransactionElement","TransactionHeader","TransactionType","TransactionType","TransactionType","SenderTransactionId","SenderTransactionId","SenderTransactionId","ProcessInfo","ProcessGroup","ProcessGroup","ProcessGroup","ProcessSequence","ProcessSequence","ProcessSequence","ProcessInfo","SendReason","Remark","RemarkText","RemarkText","RemarkText","Remark","Identifier","IdentifierText","IdentifierText","IdentifierText","Identifier","ObjectType","ObjectType","ObjectType","SendReason","Reference","Reference","Reference","TransactionHeader","RegisterShipmentOrder","ShipmentOrderSubmittedEvent","ShipmentOrder","OrderDateTime","OrderDateTime","TransportInformation","ServiceLevel","Handle","standard","Handle","ServiceLevel","TermsOfTransport","IncoTerm","Handle","fob","Handle","IncoTerm","LocationRefSummary","LocationIdentifiers","LocationIdentifier","DomainIdentifier","Location","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","IdentifierAuthority","LocationIdentifier","LocationIdentifiers","PartyRole","Handle","consignor","Handle","PartyRole","TimeSpan","From","Date","Date","HasTime","HasTime","From","To","Date","Date","HasTime","HasTime","To","TimeSpan","LocationRefSummary","TermsDateTime","TermsDateTime","TermsOfTransport","TransportMode","Handle","sea","Handle","TransportMode","TransportProduct","Handle","bulk","Handle","TransportProduct","TransportInformation","Contacts","ShipmentOrderContact","PartyRole","Handle","consignor","Handle","PartyRole","CommunicationMethod","Handle","email","Handle","CommunicationMethod","PhoneNumber","PhoneNumber","Name","Name","Addresses","PhoneAddress","PhoneNumber","PhoneNumber","PhysicalType","PhoneAddress","Addresses","ShipmentOrderContact","ShipmentOrderContact","PartyRole","Handle","consignee","Handle","PartyRole","CommunicationMethod","Handle","email","Handle","CommunicationMethod","PhoneNumber","Name","Intern Kontakt","Name","Addresses","PhoneAddress","PhoneNumber","PhysicalType","PhoneAddress","Addresses","ShipmentOrderContact","Contacts","Locations","LocationRefSummary","LocationIdentifiers","LocationIdentifier","DomainIdentifier","Location","DomainIdentifier","Identifier","33937","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","LocationIdentifier","LocationIdentifiers","PartyRole","Handle","consignor","Handle","PartyRole","TimeSpan","From","Date","Date","HasTime","HasTime","From","To","Date","Date","HasTime","HasTime","To","TimeSpan","LocationRefSummary","LocationRefSummary","LocationIdentifiers","LocationIdentifier","DomainIdentifier","Location","DomainIdentifier","Identifier","LYS","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","LocationIdentifier","LocationIdentifiers","PartyRole","Handle","consignee","Handle","PartyRole","TimeSpan","From","Date","Date","HasTime","HasTime","From","To","Date","Date","HasTime","HasTime","To","TimeSpan","LocationRefSummary","Locations","Remark","Remark","ShipmentOrderIdentifiers","ShipmentOrderIdentifier","DomainIdentifier","OrderNumber","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderIdentifier","ShipmentOrderIdentifier","DomainIdentifier","OrderType","DomainIdentifier","Identifier","SubOrder","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderIdentifier","ShipmentOrderIdentifiers","ShipmentOrderOrderLines","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1111","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","40000","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1106","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","1000000","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1103","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1115","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1117","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1118","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1119","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLine","Article","ContractArticleIdentifiers","ContractArticleIdentifier","DomainIdentifier","Article","DomainIdentifier","Identifier","1120","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ContractArticleIdentifier","ContractArticleIdentifiers","Article","Identifiers","ShipmentOrderOrderLineIdentifier","DomainIdentifier","OrderLine","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentOrderOrderLineIdentifier","Identifiers","Quantity","Quantity","Description","ShipmentOrderOrderLine","ShipmentOrderOrderLines","Measurements","ShipmentMeasurement","MeasurementType","Handle","loading_duration","Handle","MeasurementType","Value","Value","ShipmentMeasurement","ShipmentMeasurement","MeasurementType","Handle","unloading_duration","Handle","MeasurementType","Value","0.25","Value","ShipmentMeasurement","Measurements","OrderNumber","OrderNumber","ShipmentOrder","RegisterShipmentRef","Identifiers","ShipmentIdentifier","DomainIdentifier","OrderNumber","DomainIdentifier","Identifier","Identifier","IdentifierAuthority","LXIR","IdentifierAuthority","ShipmentIdentifier","Identifiers","RegisterShipmentRef","ShipmentOrderSubmittedEvent","RegisterShipmentOrder","TransactionElement","TransmissionBody","Transmission"})
			{
				byte[] src = s.getBytes();

				byte[] packed = compressor.compress(src, src.length);

				sumPacked += packed.length;
				sumUnpacked += src.length;

				System.out.println(packed.length + " / " + src.length);

				byte[] unpacked = decompressor.decompress(packed);

				if (!new String(unpacked).equals(new String(src)))
				{
					System.out.println(new String(src));
					System.out.println(new String(unpacked));

					throw new IllegalStateException();
				}
			}

			System.out.println("----------------");
			System.out.println(sumPacked + " / " + sumUnpacked);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}


	public static void xmain(String... args)
	{
		try
		{
//			byte[] src1 = "For us, it's a really exciting outcome, because this novel litigation approach worked and would get us a resolution really quickly, and it gave us a way to get our client's data deleted. We were prepared for much more pushback. It's incredibly useful to have this tool in our toolkit for when phones are taken in the future. I can't see any reason why this couldn't be done whenever another traveler is facing this sort of phone seizure.".getBytes();
//			byte[] src2 = "litigation".getBytes();
//			byte[] src3 = "approach".getBytes();

			byte[] src1 = "test".getBytes();
			byte[] src2 = "apatest".getBytes();
			byte[] src3 = "testapa".getBytes();

			LZJB lzjb = new LZJB();

			byte[] packed1 = lzjb.compress(src1, src1.length);
			byte[] packed2 = lzjb.compress(src2, src2.length);
			byte[] packed3 = lzjb.compress(src3, src3.length);

			Log.hexDump(packed1);
			Log.hexDump(packed2);
			Log.hexDump(packed3);

			System.out.println(packed1.length + " / " + src1.length);
			System.out.println(packed1.length + " / " + src2.length);
			System.out.println(packed1.length + " / " + src3.length);

			lzjb = new LZJB();
			byte[] unpacked1 = lzjb.decompress(packed1);
			byte[] unpacked2 = lzjb.decompress(packed2);
			byte[] unpacked3 = lzjb.decompress(packed3);

			System.out.println();
			System.out.println(new String(unpacked1).equals(new String(src1)));
			System.out.println(new String(unpacked2).equals(new String(src2)));
			System.out.println(new String(unpacked3).equals(new String(src3)));

			System.out.println();
//			System.out.println(new String(unpacked1));
//			System.out.println(new String(unpacked2));
//			System.out.println(new String(unpacked3));
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
}
