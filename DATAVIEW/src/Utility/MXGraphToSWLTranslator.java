package Utility;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides methods to translate mxgraph source code to equivalent SWL specs. Note, it works with modified 
 * version of mxgraph source code, where all '&' symbols are replaced with '^'. 
 * 
 * @author Andrey Kashlev
 *
 */
public class MXGraphToSWLTranslator {

	public static HashMap<String, Element> getMxCells(Element mxGraphEl) {
		HashMap<String, Element> mxCells = new HashMap<String, Element>();
		NodeList mxCellsList = mxGraphEl.getElementsByTagName("mxCell");
		for (int i = 0; i < mxCellsList.getLength(); i++) {
			Element currentCell = (Element) mxCellsList.item(i);
			String id = currentCell.getAttribute("id").trim();
			mxCells.put(id, currentCell);
		}
		return mxCells;
	}

	public static ArrayList<Element> getListOfInputPorts(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> inputPorts = new ArrayList<Element>();
		Element currInputPort = null;

		HashMap<Integer, Element> yCoordinateToInputPort = new HashMap<Integer, Element>();
		for (Element mxCell : mxCells.values()) {
			if (isInputPort(mxCell))
				yCoordinateToInputPort.put(getYCoordinate(mxCell), mxCell);
		}

		ArrayList<Element> sortedPortCells = getPortsSortedByYCoordinate(yCoordinateToInputPort);
		for (Element portCellElement : sortedPortCells) {
			currInputPort = mxCellToInputPort(portCellElement, doc);
			if (currInputPort != null)
				inputPorts.add(currInputPort);
		}

		return inputPorts;
	}

	public static Integer getYCoordinate(Element mxCell) {
		Integer result = new Integer(-1);
		Element mxGeometry = (Element) mxCell.getElementsByTagName("mxGeometry").item(0);
		String yCoordindateStr = mxGeometry.getAttribute("y").trim();
		if(yCoordindateStr.contains("."))
			yCoordindateStr = yCoordindateStr.substring(0, yCoordindateStr.indexOf("."));
		result = new Integer(yCoordindateStr);
		return result;
	}

	public static ArrayList<Element> getPortsSortedByYCoordinate(HashMap<Integer, Element> mxCellsOnlyIorOPorts) {
		Integer[] arr = new Integer[mxCellsOnlyIorOPorts.size()];
		int j = 0;
		for (Integer key : mxCellsOnlyIorOPorts.keySet()) {
			arr[j] = key;
			j++;
		}

		Arrays.sort(arr);
		ArrayList<Element> result = new ArrayList<Element>();
		for (int i = 0; i < arr.length; i++)
			result.add(mxCellsOnlyIorOPorts.get(arr[i]));
		return result;
	}

	public static ArrayList<Element> getListOfOutputPorts(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> outputPorts = new ArrayList<Element>();
		Element currOutputPort = null;

		HashMap<Integer, Element> yCoordinateToInputPort = new HashMap<Integer, Element>();
		for (Element mxCell : mxCells.values()) {
			if (isOutputPort(mxCell))
				yCoordinateToInputPort.put(getYCoordinate(mxCell), mxCell);
		}

		ArrayList<Element> sortedPortCells = getPortsSortedByYCoordinate(yCoordinateToInputPort);
		for (Element portCellElement : sortedPortCells) {
			currOutputPort = mxCellToOutputPort(portCellElement, doc);
			if (currOutputPort != null)
				outputPorts.add(currOutputPort);
		}

		return outputPorts;
	}

	public static ArrayList<Element> getListOfOutputMappings(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> outputMappings = new ArrayList<Element>();

		for (Element mxCell : mxCells.values()) {
			if (isOutputPort(mxCell)) {
				String portID = mxCell.getAttribute("id").trim();
				Element cellThisPortIsConnectedTo = null;
				// let's find the cellThisPortIsConnectedTo:
				for (Element currCell : mxCells.values()) {
					if (currCell.hasAttribute("edge") && currCell.getAttribute("edge").trim().equals("1")) {

						if (currCell.getAttribute("source").trim().equals(portID)) {
							String target = currCell.getAttribute("target").trim();
							Element sourcePortCell = mxCells.get(target);
							String sourcePortId = sourcePortCell.getAttribute("value").trim();
							sourcePortId = sourcePortId.substring(sourcePortId.indexOf("width:0px;height:0px;^quot;^gt;") + 31,
									sourcePortId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

							String parentId = sourcePortCell.getAttribute("parent");
							cellThisPortIsConnectedTo = mxCells.get(parentId);
							String sourceComponentId = getInstanceId(cellThisPortIsConnectedTo);
							Element outputMapping = doc.createElement("outputMapping");

							String fromAttribute = sourceComponentId + "." + sourcePortId;
							String toAttribute = "this." + getOutputPortIdFromCell(mxCell);
							;

							((Element) outputMapping).setAttribute("from", fromAttribute);
							((Element) outputMapping).setAttribute("to", toAttribute);
							if (!mappingIsAlreadyInTheList(outputMapping, outputMappings))
								outputMappings.add(outputMapping);
						} else if (currCell.getAttribute("target").trim().equals(portID)) {
							String sourcePortCellId = currCell.getAttribute("source").trim();
							Element sourcePortCell = mxCells.get(sourcePortCellId);
							String sourcePortId = sourcePortCell.getAttribute("value").trim();
							sourcePortId = sourcePortId.substring(sourcePortId.indexOf("width:0px;height:0px;^quot;^gt;") + 31,
									sourcePortId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

							String parentId = sourcePortCell.getAttribute("parent");
							cellThisPortIsConnectedTo = mxCells.get(parentId);
							String sourceComponentId = getInstanceId(cellThisPortIsConnectedTo);
							Element outputMapping = doc.createElement("outputMapping");

							String fromAttribute = sourceComponentId + "." + sourcePortId;
							String toAttribute = "this." + getOutputPortIdFromCell(mxCell);
							((Element) outputMapping).setAttribute("from", fromAttribute);

							((Element) outputMapping).setAttribute("to", toAttribute);
							if (!mappingIsAlreadyInTheList(outputMapping, outputMappings))
								outputMappings.add(outputMapping);
						}
					}
				}

			}
		}

		return outputMappings;
	}

	public static boolean mappingIsAlreadyInTheList(Element newMapping, ArrayList<Element> outputMappings) {
		for (Element currMapping : outputMappings) {
			if (currMapping.getAttribute("from").trim().equals(newMapping.getAttribute("from").trim())
					&& currMapping.getAttribute("to").trim().equals(newMapping.getAttribute("to").trim()))
				return true;
		}
		return false;
	}

	public static String getOutputPortIdFromCell(Element mxCell) {
		String result = "";
		String mxCellStr = XMLParser.nodeToString(mxCell);
		result = mxCellStr.substring(mxCellStr.indexOf(";^lt;span id=^quot;") + 19, mxCellStr.length() - 10);
		result = result.substring(0, result.indexOf("^quot;"));
		return result;
	}

	public static String getInputPortIdFromCell(Element mxCell) {
		String result = "";
		String mxCellStr = XMLParser.nodeToString(mxCell);
		result = mxCellStr.substring(mxCellStr.indexOf("div id=^quot;") + 13, mxCellStr.length() - 10);
		result = result.substring(0, result.indexOf("^quot;"));
		return result;
	}

	public static ArrayList<Element> getListOfInputMappings(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> inputMappings = new ArrayList<Element>();

		for (Element mxCell : mxCells.values()) {
			if (isInputPort(mxCell)) {
				String portID = mxCell.getAttribute("id").trim();
				Element cellThisPortIsConnectedTo = null;
				// let's find the cellThisPortIsConnectedTo:
				for (Element currCell : mxCells.values()) {
					if (currCell.hasAttribute("edge") && currCell.getAttribute("edge").trim().equals("1")) {

						if (currCell.getAttribute("source").trim().equals(portID)) {
							String target = currCell.getAttribute("target").trim();
							Element sourcePortCell = mxCells.get(target);
							String sourcePortId = sourcePortCell.getAttribute("value").trim();

							sourcePortId = sourcePortId.substring(sourcePortId.indexOf("width:0px;height:0px;^quot;^gt;") + 31,
									sourcePortId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

							String parentId = sourcePortCell.getAttribute("parent");
							cellThisPortIsConnectedTo = mxCells.get(parentId);
							String sourceComponentId = getInstanceId(cellThisPortIsConnectedTo);
							Element inputMapping = doc.createElement("inputMapping");

							String fromAttribute = "this." + getInputPortIdFromCell(mxCell);
							String toAttribute = sourceComponentId + "." + sourcePortId;

							((Element) inputMapping).setAttribute("from", fromAttribute);
							((Element) inputMapping).setAttribute("to", toAttribute);
							if (!mappingIsAlreadyInTheList(inputMapping, inputMappings))
								inputMappings.add(inputMapping);
						} else if (currCell.getAttribute("target").trim().equals(portID)) {
							String sourcePortCellId = currCell.getAttribute("source").trim();
							Element sourcePortCell = mxCells.get(sourcePortCellId);
							String sourcePortId = sourcePortCell.getAttribute("value");
							sourcePortId = sourcePortId.substring(sourcePortId.indexOf("width:0px;height:0px;^quot;^gt;") + 31,
									sourcePortId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

							String parentId = sourcePortCell.getAttribute("parent");
							cellThisPortIsConnectedTo = mxCells.get(parentId);
							String sourceComponentId = getInstanceId(cellThisPortIsConnectedTo);
							Element inputMapping = doc.createElement("inputMapping");

							String fromAttribute = "this." + getInputPortIdFromCell(mxCell);
							String toAttribute = sourceComponentId + "." + sourcePortId;
							((Element) inputMapping).setAttribute("from", fromAttribute);

							((Element) inputMapping).setAttribute("to", toAttribute);
							if (!mappingIsAlreadyInTheList(inputMapping, inputMappings))
								inputMappings.add(inputMapping);
						}
					}
				}

			}
		}

		return inputMappings;
	}

	public static boolean isInputPort(Element mxCell) {
		if (mxCell.getAttribute("value").contains("inputPort"))
			return true;
		return false;
	}

	public static boolean isOutputPort(Element mxCell) {
		if (mxCell.getAttribute("value").contains("outputPort"))
			return true;
		return false;
	}

	public static Element mxCellToInputPort(Element mxCell, Document doc) {
		if (isInputPort(mxCell)) {
			Node inputPort = doc.createElement("inputPort");

			String mxCellStr = XMLParser.nodeToString(mxCell);
			String idStr = mxCellStr.substring(mxCellStr.indexOf("div id=^quot;") + 13, mxCellStr.length());
			idStr = idStr.substring(0, idStr.indexOf("^quot;"));
			Node portID = doc.createElement("portID");
			portID.setTextContent(idStr);
			inputPort.appendChild(portID);

			String value = mxCell.getAttribute("value").trim();

			String portNameStr = value.substring(value.indexOf("rtl;padding-right:5px;^quot;^gt;") + 32, value.indexOf("^lt;/div^gt;^lt;img src"));

			Node portName = doc.createElement("portName");
			portName.setTextContent(portNameStr);
			inputPort.appendChild(portName);

			String portTypeStr = value.substring(value.indexOf("typeOfPort=") + 17, value.indexOf("^quot; style=^quot;float:left;display:inline;"));

			Node portType = doc.createElement("portType");
			portType.setTextContent(portTypeStr);
			inputPort.appendChild(portType);

			return (Element) inputPort;

		} else
			return null;
	}

	public static Element mxCellToOutputPort(Element mxCell, Document doc) {
		if (isOutputPort(mxCell)) {
			Node outputPort = doc.createElement("outputPort");

			String mxCellStr = XMLParser.nodeToString(mxCell);
			String idStr = mxCellStr.substring(mxCellStr.indexOf("span id=^quot;") + 14, mxCellStr.length());
			idStr = idStr.substring(0, idStr.indexOf("^quot;"));
			Node portID = doc.createElement("portID");
			portID.setTextContent(idStr);
			outputPort.appendChild(portID);
			String value = mxCell.getAttribute("value").trim();
			String portNameStr = value.substring(value.indexOf("style=^quot;font-size:11pt;^quot;^gt;") + 37,
					value.indexOf("^lt;/span^gt;^lt;/div^gt;"));

			Node portName = doc.createElement("portName");
			portName.setTextContent(portNameStr);
			outputPort.appendChild(portName);

			String portTypeStr = value.substring(value.indexOf("typeOfPort=^quot;") + 17, value.indexOf("^quot; style=^quot;font-size:11pt;"));
			Node portType = doc.createElement("portType");
			portType.setTextContent(portTypeStr);
			outputPort.appendChild(portType);

			return (Element) outputPort;

		} else
			return null;
	}

	public static boolean isWorkflowInstance(Element mxCell) {
		if (mxCell.getAttribute("value").trim().contains("workflowComponent"))
			return true;
		return false;
	}

	public static boolean isDataChannel(Element mxCell, HashMap<String, Element> mxCells) {
		if (mxCell.hasAttribute("source")
				&& mxCell.hasAttribute("target")
				&& !(mxCells.get(mxCell.getAttribute("source").trim())).getAttribute("parent").equals("1")
				&& !(mxCells.get(mxCell.getAttribute("target").trim())).getAttribute("parent").equals("1")
				&& !(mxCells.get(mxCells.get(mxCell.getAttribute("source").trim()).getAttribute("parent").trim()).getAttribute("value")
						.contains("dataProduct"))
				&& !(mxCells.get(mxCells.get(mxCell.getAttribute("target").trim()).getAttribute("parent").trim()).getAttribute("value")
						.contains("dataProduct"))) {

			return true;
		}
		return false;
	}

	public static Element mxCellToWorkflowInstance(Element mxCell, Document doc) {
		if (isWorkflowInstance(mxCell)) {
			String workflowName = getWFNameFromCell(mxCell);
			String instanceId = getInstanceId(mxCell);

			Node workflow = doc.createElement("workflow");
			workflow.setTextContent(workflowName);

			Node workflowInstance = doc.createElement("workflowInstance");
			((Element) workflowInstance).setAttribute("id", instanceId);

			workflowInstance.appendChild(workflow);

			return (Element) workflowInstance;

		} else
			return null;
	}

	public static String getWFNameFromCell(Element mxCell) {
		String result = "undefined";
		result = mxCell.getAttribute("value").trim();// .replace("^lt;h1 style=^quot;margin:0px;^quot;^gt;", "");
		result = result.substring(result.indexOf("^gt;workflowComponent") + 22, result.indexOf("^lt;/div^gt;"));
		return result;
	}

	public static String getInstanceId(Element mxCell) {
		String result = "undefined";
		result = getWFNameFromCell(mxCell) + mxCell.getAttribute("id").trim();
		return result;
	}

	public static Element mxCellToDataChannel(Element mxCell, HashMap<String, Element> mxCells, Document doc) {
		if (isDataChannel(mxCell, mxCells)) {
			Node dataChannel = doc.createElement("dataChannel");
			//determine which of the two cells (source or target) is from and to attribute of dataChannel:
			String fromCellId;
			String toCellId;
			Element mxGeometryOfSource = (Element) ((Element) mxCells.get(mxCell.getAttribute("source").trim())).getElementsByTagName("mxGeometry")
					.item(0);
			if (mxGeometryOfSource.hasAttribute("x") && mxGeometryOfSource.getAttribute("x").trim().equals("1")) {
				fromCellId = mxCell.getAttribute("source").trim();
				toCellId = mxCell.getAttribute("target").trim();
			} else {
				fromCellId = mxCell.getAttribute("target").trim();
				toCellId = mxCell.getAttribute("source").trim();
			}

			String fromAttributeValue = findFromAttribute(fromCellId, mxCells);
			String toAttributeValue = findToAttribute(toCellId, mxCells);
			((Element) dataChannel).setAttribute("from", fromAttributeValue);
			((Element) dataChannel).setAttribute("to", toAttributeValue);
			return (Element) dataChannel;
		} else
			return null;
	}

	public static String findFromAttribute(String id, HashMap<String, Element> mxCells) {
		Element fromPort = mxCells.get(id);

		String portId = fromPort.getAttribute("value").trim();
		portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

		String instanceId = getInstanceId(mxCells.get(fromPort.getAttribute("parent").trim()));
		return instanceId + "." + portId;
	}

	public static String findToAttribute(String id, HashMap<String, Element> mxCells) {
		Element toPort = mxCells.get(id);
		String portId = toPort.getAttribute("value").trim();
		portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

		String instanceId = getInstanceId(mxCells.get(toPort.getAttribute("parent").trim()));
		return instanceId + "." + portId;
	}

	public static ArrayList<Element> getListOfWorkflowInstances(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> workflowInstances = new ArrayList<Element>();
		Element currWorkflowInstance = null;
		for (Element mxCell : mxCells.values()) {
			currWorkflowInstance = null;
			currWorkflowInstance = mxCellToWorkflowInstance(mxCell, doc);
			if (currWorkflowInstance != null)
				workflowInstances.add(currWorkflowInstance);
		}

		return workflowInstances;
	}

	public static ArrayList<Element> getListOfDataChannels(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> dataChannels = new ArrayList<Element>();
		Element currDataChannel = null;
		for (Element mxCell : mxCells.values()) {
			currDataChannel = null;
			currDataChannel = mxCellToDataChannel(mxCell, mxCells, doc);
			if (currDataChannel != null)
				dataChannels.add(currDataChannel);
		}

		return dataChannels;
	}

	public static Document translateWorkflow(String name, Document diagram) throws Exception {

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();

		Node workflowSpec = doc.createElement("workflowSpec");
		doc.appendChild(workflowSpec);

		Node workflow = doc.createElement("workflow");
		workflowSpec.appendChild(workflow);

		((Element) workflow).setAttribute("name", name);
		((Element) workflow).setAttribute("root", "true");

		Node workflowInterface = doc.createElement("workflowInterface");
		workflow.appendChild(workflowInterface);

		Node workflowBody = doc.createElement("workflowBody");
		workflow.appendChild(workflowBody);

		Node workflowGraph = doc.createElement("workflowGraph");
		workflowBody.appendChild(workflowGraph);

		((Element) workflowBody).setAttribute("mode", "graph-based");

		try {
			// diagram = XMLParser.getDocument(diagramStr);

			Element rootElement = diagram.getDocumentElement();
			HashMap<String, Element> mxCells = getMxCells(rootElement);

			// creating input and output ports, adding them to interface, and adding interface to workflow element
			Node inputPorts = doc.createElement("inputPorts");
			for (Element port : getListOfInputPorts(mxCells, doc))
				inputPorts.appendChild(port);

			Node outputPorts = doc.createElement("outputPorts");
			for (Element port : getListOfOutputPorts(mxCells, doc))
				outputPorts.appendChild(port);

			workflowInterface.appendChild(inputPorts);
			workflowInterface.appendChild(outputPorts);

			Node workflowInstances = doc.createElement("workflowInstances");
			for (Element wfInstance : getListOfWorkflowInstances(mxCells, doc))
				workflowInstances.appendChild(wfInstance);

			workflowGraph.appendChild(workflowInstances);

			Node dataChannels = doc.createElement("dataChannels");
			for (Element dataChannel : getListOfDataChannels(mxCells, doc))
				dataChannels.appendChild(dataChannel);
			//
			workflowGraph.appendChild(dataChannels);

			Node G2W = doc.createElement("G2W");

			for (Element inputMapping : getListOfInputMappings(mxCells, doc))
				G2W.appendChild(inputMapping);

			for (Element outputMapping : getListOfOutputMappings(mxCells, doc))
				G2W.appendChild(outputMapping);

			workflowBody.appendChild(G2W);

			//System.out.println("$$$$$$$$$$$$$$4");
			//System.out.println(Utility.formatXML(doc));
		} catch (Exception e) {
			System.out.println("exception caught by Andrey: ");
			e.printStackTrace();
		}

		System.out.println("no worries");
		// System.out.println("diagram: \n" + diagramStr);
		// System.out.println("diagram length: " + diagramStr.length());
		return doc;

	}

	public static ArrayList<Element> getListOfOutputDPToPortMappings(HashMap<String, Element> mxCells, Document doc) {
		ArrayList<Element> inputDPToPortMappings = new ArrayList<Element>();
		for (Element mxCell : mxCells.values()) {
			Element mapping = mxCellToOutputDPToPortMapping(mxCell, mxCells, doc);
			if (mapping != null)
				inputDPToPortMappings.add(mapping);
		}

		return inputDPToPortMappings;
	}

	public static ArrayList<Element> getListOfInputDPToPortMappings(HashMap<String, Element> mxCells, Document doc) throws Exception {
		ArrayList<Element> inputDPToPortMappings = new ArrayList<Element>();
		for (Element mxCell : mxCells.values()) {
			Element mapping = mxCellToInputDPToPortMapping(mxCell, mxCells, doc);
			if (mapping != null)
				inputDPToPortMappings.add(mapping);
		}

		return inputDPToPortMappings;
	}

	public static Element mxCellToInputDPToPortMapping(Element mxCell, HashMap<String, Element> mxCells, Document doc) throws Exception {
		Element inputDP2PortMapping = null;

		if (isInputDPToPortMapping(mxCell, mxCells)) {
			inputDP2PortMapping = doc.createElement("inputDP2PortMapping");

			String fromAttribute = "";
			String toAttribute = "";
			// if dataproduct is source of this edge in mxgraph:
			Element sourcePort = mxCells.get(mxCell.getAttribute("source").trim());
			String sourcePortX = null;
			Element source = null;
			String sourceValue = null;
			if (sourcePort.getElementsByTagName("mxGeometry") != null && sourcePort.getElementsByTagName("mxGeometry").getLength() == 1)
				sourcePortX = ((Element) sourcePort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();
			if (sourcePort.hasAttribute("parent"))
				source = mxCells.get(sourcePort.getAttribute("parent").trim());
			if (source.hasAttribute("value"))
				sourceValue = source.getAttribute("value");
			if (source != null && sourceValue != null && sourceValue.contains("dataProduct") && sourcePortX.equals("1")) {
				//System.out.println(sourceValue);
				fromAttribute = sourceValue.substring(sourceValue.indexOf("dataProduct") + 12, sourceValue.length());
				//System.out.println(fromAttribute);
				fromAttribute = fromAttribute.substring(0, fromAttribute.indexOf("dataType") - 1);
				String fromAttributeType = sourceValue.substring(sourceValue.indexOf("dataType") + 9, sourceValue.lastIndexOf("^lt;/div^gt;"));
				Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());
				Element target = mxCells.get(targetPort.getAttribute("parent").trim());
				String portId = targetPort.getAttribute("value").trim();
				portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));
				toAttribute = getInstanceId(target) + "." + portId;

			}

			// if dataproduct is target of this edge in mxgraph:
			Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());
			String targetPortX = null;
			Element target = null;
			String targetValue = null;

			if (targetPort.getElementsByTagName("mxGeometry") != null && targetPort.getElementsByTagName("mxGeometry").getLength() == 1)
				targetPortX = ((Element) targetPort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();
			if (targetPort.hasAttribute("parent"))
				target = mxCells.get(targetPort.getAttribute("parent").trim());
			if (target.hasAttribute("value"))
				targetValue = target.getAttribute("value");

			if (target != null && targetValue != null && targetValue.contains("dataProduct") && targetPortX.equals("1")) {
				fromAttribute = targetValue.substring(targetValue.indexOf("dataProduct") + 12, targetValue.length());
				fromAttribute = fromAttribute.substring(0, fromAttribute.indexOf("dataType") - 1);
				Element sourcePort1 = mxCells.get(mxCell.getAttribute("source").trim());
				Element source1 = mxCells.get(sourcePort1.getAttribute("parent").trim());
				String portId = sourcePort1.getAttribute("value").trim();
				portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));
				toAttribute = getInstanceId(source1) + "." + portId;
			}
			inputDP2PortMapping.setAttribute("from", fromAttribute);
			inputDP2PortMapping.setAttribute("to", toAttribute);

		}
		return inputDP2PortMapping;
	}

	public static Element mxCellToOutputDPToPortMapping(Element mxCell, HashMap<String, Element> mxCells, Document doc) {
		Element outputDP2PortMapping = null;

		if (isOutputDPToPortMapping(mxCell, mxCells)) {
			outputDP2PortMapping = doc.createElement("outputDP2PortMapping");

			String fromAttribute = "";
			String toAttribute = "";
			// if dataproduct is source of this edge in mxgraph:
			Element sourcePort = mxCells.get(mxCell.getAttribute("source").trim());
			String sourcePortX = null;
			Element source = null;
			String sourceValue = null;
			if (sourcePort.getElementsByTagName("mxGeometry") != null && sourcePort.getElementsByTagName("mxGeometry").getLength() == 1)
				sourcePortX = ((Element) sourcePort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();
			if (sourcePort.hasAttribute("parent"))
				source = mxCells.get(sourcePort.getAttribute("parent").trim());
			if (source.hasAttribute("value"))
				sourceValue = source.getAttribute("value");
			if (source != null && sourceValue != null && sourceValue.contains("dataProduct") && !sourcePortX.equals("1")) {
				toAttribute = sourceValue.substring(sourceValue.indexOf("dataProduct") + 12, sourceValue.length());
				// toAttribute = toAttribute.substring(0, toAttribute.indexOf("^lt;/div^gt"));
				toAttribute = toAttribute.substring(0, toAttribute.indexOf("stubForOutputDP") - 1);
				Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());
				Element target = mxCells.get(targetPort.getAttribute("parent").trim());

				String portId = targetPort.getAttribute("value").trim();
				portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

				fromAttribute = getInstanceId(target) + "." + portId;
			}

			// if dataproduct is target of this edge in mxgraph:
			Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());
			String targetPortX = null;
			Element target = null;
			String targetValue = null;

			if (targetPort.getElementsByTagName("mxGeometry") != null && targetPort.getElementsByTagName("mxGeometry").getLength() == 1)
				targetPortX = ((Element) targetPort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();
			if (targetPort.hasAttribute("parent"))
				target = mxCells.get(targetPort.getAttribute("parent").trim());
			if (target.hasAttribute("value"))
				targetValue = target.getAttribute("value");

			if (target != null && targetValue != null && targetValue.contains("dataProduct") && !targetPortX.equals("1")) {
				toAttribute = targetValue.substring(targetValue.indexOf("dataProduct") + 12, targetValue.length());
				// toAttribute = toAttribute.substring(0, toAttribute.indexOf("^lt;/div^gt"));
				toAttribute = toAttribute.substring(0, toAttribute.indexOf("stubForOutputDP") - 1);
				Element sourcePort1 = mxCells.get(mxCell.getAttribute("source").trim());
				Element source1 = mxCells.get(sourcePort1.getAttribute("parent").trim());

				String portId = sourcePort1.getAttribute("value").trim();
				portId = portId.substring(portId.indexOf("width:0px;height:0px;^quot;^gt;") + 31, portId.indexOf("^lt;/div^gt;^lt;/div^gt;"));

				fromAttribute = getInstanceId(source1) + "." + portId;
			}
			outputDP2PortMapping.setAttribute("from", fromAttribute);
			outputDP2PortMapping.setAttribute("to", toAttribute);

		}
		return outputDP2PortMapping;
	}

	public static boolean isInputDPToPortMapping(Element mxCell, HashMap<String, Element> mxCells) {
		if (!mxCell.hasAttribute("source"))
			return false;
		if (!mxCell.hasAttribute("target"))
			return false;

		Element sourcePort = mxCells.get(mxCell.getAttribute("source").trim());

		String sourcePortX = null;
		Element source = null;
		String sourceValue = null;

		if (sourcePort.getElementsByTagName("mxGeometry") != null && sourcePort.getElementsByTagName("mxGeometry").getLength() == 1)
			sourcePortX = ((Element) sourcePort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();

		if (sourcePort.hasAttribute("parent"))
			source = mxCells.get(sourcePort.getAttribute("parent").trim());

		if (source.hasAttribute("value"))
			sourceValue = source.getAttribute("value");

		Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());

		String targetPortX = null;
		Element target = null;
		String targetValue = null;

		if (targetPort.getElementsByTagName("mxGeometry") != null && targetPort.getElementsByTagName("mxGeometry").getLength() == 1)
			targetPortX = ((Element) targetPort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();

		if (targetPort.hasAttribute("parent"))
			target = mxCells.get(targetPort.getAttribute("parent").trim());

		if (target.hasAttribute("value"))
			targetValue = target.getAttribute("value");

		if ((source != null && sourceValue != null && sourceValue.contains("dataProduct"))
				&& (target != null && targetValue != null && targetValue.contains("dataProduct")))
			return false;

		if ((source != null && sourceValue != null && sourceValue.contains("dataProduct") && sourcePortX.equals("1"))
				|| (target != null && targetValue != null && targetValue.contains("dataProduct") && targetPortX.equals("1")))
			return true;

		return false;
	}

	public static boolean isOutputDPToPortMapping(Element mxCell, HashMap<String, Element> mxCells) {
		if (!mxCell.hasAttribute("source"))
			return false;
		if (!mxCell.hasAttribute("target"))
			return false;

		Element sourcePort = mxCells.get(mxCell.getAttribute("source").trim());

		String sourcePortX = null;
		Element source = null;
		String sourceValue = null;

		if (sourcePort.getElementsByTagName("mxGeometry") != null && sourcePort.getElementsByTagName("mxGeometry").getLength() == 1)
			sourcePortX = ((Element) sourcePort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();

		if (sourcePort.hasAttribute("parent"))
			source = mxCells.get(sourcePort.getAttribute("parent").trim());

		if (source.hasAttribute("value"))
			sourceValue = source.getAttribute("value");

		Element targetPort = mxCells.get(mxCell.getAttribute("target").trim());

		String targetPortX = null;
		Element target = null;
		String targetValue = null;

		if (targetPort.getElementsByTagName("mxGeometry") != null && targetPort.getElementsByTagName("mxGeometry").getLength() == 1)
			targetPortX = ((Element) targetPort.getElementsByTagName("mxGeometry").item(0)).getAttribute("x").trim();

		if (targetPort.hasAttribute("parent"))
			target = mxCells.get(targetPort.getAttribute("parent").trim());

		if (target.hasAttribute("value"))
			targetValue = target.getAttribute("value");

		if ((source != null && sourceValue != null && sourceValue.contains("dataProduct") && !sourcePortX.equals("1"))
				|| (target != null && targetValue != null && targetValue.contains("dataProduct") && !targetPortX.equals("1")))
			return true;

		return false;
	}

	public static Document translateExperiment(String name, Document mxGraphSource) throws Exception {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();

		Node experimentSpec = doc.createElement("experimentSpec");
		doc.appendChild(experimentSpec);

		Node experiment = doc.createElement("experiment");
		experimentSpec.appendChild(experiment);

		((Element) experiment).setAttribute("name", name);

		Node workflowBody = doc.createElement("workflowBody");
		experiment.appendChild(workflowBody);

		Node workflowGraph = doc.createElement("workflowGraph");
		workflowBody.appendChild(workflowGraph);

		Node workflowInstances = doc.createElement("workflowInstances");

		Element rootElement = mxGraphSource.getDocumentElement();
		HashMap<String, Element> mxCells = getMxCells(rootElement);

		for (Element wfInstance : getListOfWorkflowInstances(mxCells, doc))
			workflowInstances.appendChild(wfInstance);

		workflowGraph.appendChild(workflowInstances);

		Node dataChannels = doc.createElement("dataChannels");
		for (Element dataChannel : getListOfDataChannels(mxCells, doc))
			dataChannels.appendChild(dataChannel);
		//
		workflowGraph.appendChild(dataChannels);

		Element dataProductsToPorts = doc.createElement("dataProductsToPorts");
		workflowBody.appendChild(dataProductsToPorts);

		for (Element mapping : getListOfInputDPToPortMappings(mxCells, doc)) {
			dataProductsToPorts.appendChild(mapping);
		}

		for (Element mapping : getListOfOutputDPToPortMappings(mxCells, doc)) {
			dataProductsToPorts.appendChild(mapping);
		}

		//System.out.println("%%%%%%% result experiment:\n" + Utility.nodeToString(doc));

		return doc;
	}

	public static void main(String[] args) throws Exception {
	
	}

}
