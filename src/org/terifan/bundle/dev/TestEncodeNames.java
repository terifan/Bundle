package org.terifan.bundle.dev;

import java.util.ArrayList;
import java.util.Arrays;

public class TestEncodeNames
{
	public static void main(String... args)
	{
		try
		{
//			String[] names = "Reference,Document Type,Account,Received,Ingested,Dispatched,Reference Type,ID,Changed,Version,Count,Size,Length,File,Filename,Name,Created,Create DateTime,DateTime".split(",");

			// 58
//			String[] names = "Changed,Count,Created,Create Time,Coordinate,Color,Client,Closed,Clear,Colorful".split(",");

			String[] names = "ImageWidth,ImageHeight,ImageDescription,Orientation,Make,Model,XResolution,YResolution,ResolutionUnit,Software,DateTime,WhitePoint,PrimaryChromaticities,YCbCrCoefficients,YCbCrPositioning,ReferenceBlackWhite,Copyright,ExifOffset,ExposureTime,FNumber,ExposureProgram,ISOSpeedRatings,ExifVersion,DateTimeOriginal,DateTimeDigitized,ComponentConfiguration,CompressedBitsPerPixel,ShutterSpeedValue,ApertureValue,BrightnessValue,ExposureBiasValue,MaxApertureValue,SubjectDistance,MeteringMode,LightSource,Flash,FocalLength,MakerNote,UserComment,FlashPixVersion,ColorSpace,ExifImageWidth,ExifImageHeight,RelatedSoundFile,ExifInteroperabilityOffset,FocalPlaneXResolution,FocalPlaneYResolution,FocalPlaneResolutionUnit,SensingMethod,FileSource,SceneType,ThumbWidth,ThumbHeight,ThumbBitsPerSample,ThumbCompression,ThumbPhotometricInterpretation,ThumbStripOffsets,ThumbSamplesPerPixel,ThumbRowsPerStrip,ThumbStripByteConunts,ThumbXResolution,ThumbYResolution,ThumbPlanarConfiguration,ThumbResolutionUnit,ThumbJpegIFOffset,ThumbJpegIFByteCount,ThumbYCbCrCoefficients,ThumbYCbCrSubSampling,ThumbYCbCrPositioning,ThumbReferenceBlackWhite,RatingNumber,RatingPercent,ImageNumber,Title,ImageUniqueID,Comment,Author,Tags,Subject".split(",");

//			for (int i = 0; i < names.length; i++)
//			{
//				String s = "";
//				for (int j = names[i].length(); --j >= 0;)
//				{
//					s += names[i].charAt(j);
//				}
//				names[i] = s;
//			}

			Arrays.sort(names);

			Node root = new Node();

			for (String name : names)
			{
				System.out.println(name);

				Node bestChild = null;
				Node node = root;
				int lastBest = 0;

				for (;;)
				{
					int bestMatches = 0;

					for (Node child : node.children)
					{
						int len = Math.min(name.length(), child.text.length());
						int matches = 0;
						for (int i = 0; i < len; i++)
						{
							if (name.charAt(i) != child.text.charAt(i))
							{
								break;
							}
							matches++;
						}

						if (matches > lastBest)
						{
							bestMatches = matches;
							bestChild = child;
						}
					}

					if (bestMatches>0)System.out.println("\t"+name+" "+bestChild.text+" "+bestMatches);

					if (bestMatches <= lastBest)
					{
						break;
					}

					lastBest = bestMatches;
					node = bestChild;
				}

				if (bestChild == null)
				{
					root.children.add(new Node(name,lastBest));
				}
				else
				{
					bestChild.children.add(new Node(name,lastBest));
				}
			}

			System.out.println();

			root.print();

//			System.out.println();
//
//			root.serialize();
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static class Node
	{
		String text;
		ArrayList<Node> children = new ArrayList<>();
		private int matches;


		public Node()
		{
		}


		public Node(String aText, int lastBest)
		{
			this.text = aText;
			this.matches = lastBest;
		}


		public void print()
		{
			print(0);
		}


		private void print(int aIndent)
		{
			for (int i = 0; i <aIndent; i++)
			{
				System.out.print(".. ");
			}
			System.out.println(text+" ("+matches+")");

			for (Node child : children)
			{
				child.print(aIndent + 1);
			}
		}


		public void serialize()
		{
			serialize(0);
		}


		private void serialize(int aMatches)
		{
			if (text != null)
			{
				System.out.print(matches - aMatches);
				System.out.print(children.size());
				System.out.print(text.substring(matches));
				System.out.print(",");
			}

			for (Node child : children)
			{
				child.serialize(matches);
			}
		}
	}
}
