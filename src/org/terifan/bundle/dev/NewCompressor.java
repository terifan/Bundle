package org.terifan.bundle.dev;

import java.util.ArrayList;
import java.util.TreeMap;
import org.terifan.xml.XmlDocument;
import org.terifan.xml.XmlElement;
import org.terifan.xml.XmlNode;
import org.terifan.xml.XmlNodeVisitor;
import org.terifan.xml.XmlText;


public class NewCompressor
{
	public static void main(String ... args)
	{
		try
		{
			XmlDocument xml = new XmlDocument(NewCompressor.class.getResource("test.xml"));

			TreeMap<String,ArrayList<String>> types = new TreeMap<>();
			TreeMap<String,ArrayList<String>> children = new TreeMap<>();
			TreeMap<String,ArrayList<String>> attribs = new TreeMap<>();

			xml.visit(new XmlNodeVisitor()
			{
				@Override
				public Object process(XmlNode aNode)
				{
					if (aNode instanceof XmlElement)
					{
						children.computeIfAbsent(aNode.getParent().getName(), e->new ArrayList<>()).add(aNode.getName());
					}
					else if (aNode instanceof XmlText)
					{
						types.computeIfAbsent(aNode.getParent().getName(), e->new ArrayList<>()).add(classify(((XmlText)aNode).getText().trim()));
					}
					return null;
				}

				@Override
				public Object attribute(XmlElement aNode, String aName, String aValue)
				{
					attribs.computeIfAbsent(aName, e->new ArrayList<>()).add(classify(aValue));
					return null;
				}

				private String classify(String aValue)
				{
					if ("true".equalsIgnoreCase(aValue) || "false".equalsIgnoreCase(aValue))
					{
						return "boolean";
					}
					if (aValue.matches("[0-9]{1,15}"))
					{
						return "int";
					}
					if (aValue.matches("[0-9\\.]{1,15}"))
					{
						return "float";
					}
					if (aValue.matches("[0-9]{4}-[0-1]{1}[0-9]{1}-[0-3]{1}[0-9]{1} [0-2]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}"))
					{
						return "datetime";
					}
					if (aValue.matches("[0-9]{4}-[0-1]{1}[0-9]{1}-[0-3]{1}[0-9]{1}"))
					{
						return "date";
					}
					if (aValue.matches("[0-2]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}"))
					{
						return "time";
					}
					return "string";
				}
			});

			System.out.println(attribs);
			System.out.println(types);
			System.out.println(children);

//			shipperContact=[emailAddress, phoneNumber, contactName, mobileNumber, phoneNumberExtension, faxNumber],
//			OmsRequest=[trip, requestId, userCompanyId, userEmail, trigger, userName, userId, userBranchId, target]
//			address=[address, city, countryCode, street, postalCode, latitude, locationCode, longitude]
//			goods=[marksAndNumbers, quantity, description, type]
//			loadAddress=[address, city, countryCode, street, postalCode, latitude, locationCode, longitude]
//			equipment=[owner, branchId, gpsId, mobileNumber, description, type, appCapable, licensePlateNumber, mobileDeviceId, primaryType, euroClass, aggregates, id]
//			#document=[OmsRequest]
//			consigneeContact=[emailAddress, phoneNumber, contactName, mobileNumber, phoneNumberExtension, faxNumber]
//			unloadAddress=[address, city, countryCode, street, postalCode, latitude, locationCode, longitude]
//			shipperAddress=[address, city, countryCode, street, postalCode, latitude, locationCode, longitude]
//			trip=[departureTime, loadingUnitType, volumeUnit, equipment, maxLoadSpace, arrivalDate, transportMovementId, maxLoadVolume, stop, haulier, freeTime, consignment, loadSpaceUnit, departureDate, maxLoadWeight, freeDate, startDate, weightUnit]
//			stop=[stopNumber, stopSequence, address, stopId, distanceKm, stopType]
//			consignment=[loadPlanArea, orderType, agentWaybillInstructions, goods, unloadReference, unloadStopId, loadAccessHours, freightMovementId, consigneeAddress, shipmentCollectDate, package, unloadAccessHours, shipperCustomerNumber, hazardous, expectedUnloadTime, loadAddress, serviceLevel, clioReference, shipperAddress, expectedLoadTime, shipmentDeliveryDate, volume, unloadInstructions, serviceLevelDescription, expectedUnloadDate, volumeUnit, distanceKm, unloadAddress, domesticWaybillNumber, agentJobNumber, deliveryDetailsChangedByImport, interfaceCompanyId, shipmentRef, loadSpaceUnit, grossWeightUnit, finalAgentReference, expectedLoadTimeEnd, loadStopId, shipperContact, deliveryTerms, loadSpace, consigneeContact, taxWeightUnit, expectedLoadDate, grossWeight, unloadPlanArea, taxWeight, loadReference, shipmentCollectTimeEnd, loadContact, unloadContact, limitedQuantities, consigneeCustomerNumber, shipmentCollectTime, dimensions]
//			haulier=[scaCarrierId, emailAddress, phoneNumber, name, scaEmailAddress, id]
//			unloadContact=[emailAddress, phoneNumber, contactName, mobileNumber, phoneNumberExtension, faxNumber]
//			loadContact=[emailAddress, phoneNumber, contactName, mobileNumber, phoneNumberExtension, faxNumber]
//			dimensions=[grossWeight, length, width, packageQuantity, packageType, height]
//			consigneeAddress=[address, city, countryCode, street, postalCode, latitude, locationCode, longitude]}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
