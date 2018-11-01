package org.terifan.bundle;

import java.util.Arrays;
import samples.Log;


public class LZJB
{
	private final static int MATCH_BITS = 5;
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


	public int compress(byte[] aSrcBuffer, byte[] aDstBuffer, int aSrcLen)
	{
		int src = mWindowOffset;
		int dst = 0;
		int copymapOffset = 0;
		int copymask = 128;
		int end = mWindowOffset + aSrcLen;

		mWindow = Arrays.copyOfRange(mWindow, 0, end);
		System.arraycopy(aSrcBuffer, 0, mWindow, mWindowOffset, aSrcLen);

		boolean first = true;
		
		while (src < end)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymapOffset = dst;
				aDstBuffer[dst++] = 0;
				
				if (first)
				{
					copymask <<= 1;
					first = false;
				}
			}

			if (src >= end - MATCH_MIN)
			{
				aDstBuffer[dst++] = mWindow[src++];
				continue;
			}

			int hash = ((0xff & mWindow[src]) << 16) + ((0xff & mWindow[src + 1]) << 8) + (0xff & mWindow[src + 2]);
			hash += hash >> 9;
			hash += hash >> 5;
			hash &= OFFSET_MASK;

			int bestLength = 0;
			int bestDist = 0;

			for (int i = 0; i < REFS_COUNT && mRefs[hash][i] > -1; i++)
			{
				int dist = src - mRefs[hash][i];
				int cpy = mRefs[hash][i];

				if (dist >= 0 && dist < WINDOW_SIZE && cpy + MATCH_MIN < src)
				{
					int mlen = 0;
					for (; src + mlen < end && mlen < MATCH_MAX + 255; mlen++)
					{
						if (mWindow[src + mlen] != mWindow[cpy + mlen])
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

			System.arraycopy(mRefs[hash], 0, mRefs[hash], 1, mRefs[hash].length - 1);
			mRefs[hash][0] = src;

			if (bestLength > 0)
			{
				aDstBuffer[copymapOffset] |= copymask;

				if (bestLength >= MATCH_MAX)
				{
					aDstBuffer[dst++] = (byte)((((1 << MATCH_BITS) - 1) << (8 - MATCH_BITS)) | (bestDist >> 8));
					aDstBuffer[dst++] = (byte)bestDist;
					aDstBuffer[dst++] = (byte)(bestLength - MATCH_MAX);
				}
				else
				{
					aDstBuffer[dst++] = (byte)(((bestLength - MATCH_MIN) << (8 - MATCH_BITS)) | (bestDist >> 8));
					aDstBuffer[dst++] = (byte)bestDist;
				}
				src += bestLength;
			}
			else
			{
				aDstBuffer[dst++] = mWindow[src++];
			}
		}

		mWindowOffset += aSrcLen;

//		if (dst == 3)
//		{
//			aDstBuffer[dst - 2] = ;
//		}
		
		return dst;
	}


	public void decompress(byte[] aSrcBuffer, byte[] aDstBuffer, int aDstLen)
	{
		int src = 0;
		int dst = 0;
		int end = aDstLen;
		int copymap = 0;
		int copymask = 128;

		boolean first = true;

		mWindow = Arrays.copyOfRange(mWindow, 0, mWindowOffset + end);

		while (dst < end)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymap = 255 & aSrcBuffer[src++];
				
				if (first)
				{
					copymask <<= 1;
					first = false;
				}
			}
			if ((copymap & copymask) != 0)
			{
				int mlen = ((255 & aSrcBuffer[src]) >> (8 - MATCH_BITS)) + MATCH_MIN;
				int dist = (((255 & aSrcBuffer[src]) << 8) | (255 & aSrcBuffer[src + 1])) & OFFSET_MASK;
				src += 2;
				if (mlen == MATCH_MAX)
				{
					mlen = MATCH_MAX + (255 & aSrcBuffer[src++]);
				}
				int cpy = mWindowOffset - dist;
				if (cpy < 0)
				{
					throw new RuntimeException();
				}
				while (--mlen >= 0 && dst < end)
				{
					mWindow[mWindowOffset++] = mWindow[cpy];
					aDstBuffer[dst++] = mWindow[cpy++];
				}
			}
			else
			{
				mWindow[mWindowOffset++] = aSrcBuffer[src];
				aDstBuffer[dst++] = aSrcBuffer[src++];
			}
		}
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

				byte[] packed = new byte[(src.length + 7) * 9 / 8];
				byte[] unpacked = new byte[src.length];

				int len = compressor.compress(src, packed, src.length);
				
				sumPacked += len;
				sumUnpacked += src.length;

				System.out.println(len + " / " + src.length);

				decompressor.decompress(packed, unpacked, unpacked.length);

				if (!new String(unpacked).equals(new String(src)))
				{
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

			byte[] packed1 = new byte[(src1.length + 7) * 9 / 8];
			byte[] packed2 = new byte[(src2.length + 7) * 9 / 8];
			byte[] packed3 = new byte[(src3.length + 7) * 9 / 8];
			byte[] unpacked1 = new byte[src1.length];
			byte[] unpacked2 = new byte[src2.length];
			byte[] unpacked3 = new byte[src3.length];

			LZJB lzjb = new LZJB();

			int len1 = lzjb.compress(src1, packed1, src1.length);
			int len2 = lzjb.compress(src2, packed2, src2.length);
			int len3 = lzjb.compress(src3, packed3, src3.length);

			Log.hexDump(Arrays.copyOfRange(packed1, 0, len1));
			Log.hexDump(Arrays.copyOfRange(packed2, 0, len2));
			Log.hexDump(Arrays.copyOfRange(packed3, 0, len3));

			System.out.println(len1 + " / " + src1.length);
			System.out.println(len2 + " / " + src2.length);
			System.out.println(len3 + " / " + src3.length);

			lzjb = new LZJB();
			lzjb.decompress(packed1, unpacked1, unpacked1.length);
			lzjb.decompress(packed2, unpacked2, unpacked2.length);
			lzjb.decompress(packed3, unpacked3, unpacked3.length);

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
