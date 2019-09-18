package com.adobe.cq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
  
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
  
import javax.jcr.Repository; 
import javax.jcr.SimpleCredentials; 
import javax.jcr.Node; 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
   
import org.apache.jackrabbit.commons.JcrUtils;
  
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
  
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import javax.jcr.RepositoryException;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.JcrUtils;
  
import javax.jcr.Session;
import javax.jcr.Node; 
 
 
//Sling Imports
import org.apache.sling.api.resource.ResourceResolverFactory ; 
import org.apache.sling.api.resource.ResourceResolver; 
import org.apache.sling.api.resource.Resource; 
 
//This is a component so it can provide or consume services
@Component
// This component provides the service defined through the interface
@Service
 
public class CustomerServiceImp implements CustomerService {
 
/** Default log. */
protected final Logger log = LoggerFactory.getLogger(this.getClass());
     
//Inject a Sling ResourceResolverFactory
@Reference
private ResourceResolverFactory resolverFactory;
 
private Session session;
     
//Stores customer data in the Adobe CQ JCR
public int injestCustData(String firstName, String lastName, String address, String desc)
{        
  
int num  = 0; 
try { 
              
    //Invoke the adaptTo method to create a Session 
    ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
    session = resourceResolver.adaptTo(Session.class);
               
  //Create a node that represents the root node
  Node root = session.getRootNode(); 
                     
  //Get the content node in the JCR
  Node content = root.getNode("content");
                      
 //Determine if the content/customer node exists
 Node customerRoot = null;
 int custRec = doesCustExist(content);
                                            
 //-1 means that content/customer does not exist
 if (custRec == -1)
     //content/customer does not exist -- create it
    customerRoot = content.addNode("customer","sling:OrderedFolder");
 else
   //content/customer does exist -- retrieve it
   customerRoot = content.getNode("customer");
                                 
   int custId = custRec+1; //assign a new id to the customer node
                      
   //Store content from the client JSP in the JCR
   Node custNode = customerRoot.addNode("customer"+firstName+lastName+custId,"nt:unstructured"); 
               
  //make sure name of node is unique
  custNode.setProperty("id", custId); 
  custNode.setProperty("firstName", firstName); 
  custNode.setProperty("lastName", lastName); 
  custNode.setProperty("address", address);  
  custNode.setProperty("desc", desc);
                                    
  // Save the session changes and log out
  session.save(); 
  session.logout();
  return custId; 
}
       
 catch(Exception  e){
     log.error("RepositoryException: " + e);
  }
return 0 ; 
 } 
 
/*
 * Determines if the content/customer node exists 
 * This method returns these values:
 * -1 - if customer does not exist
 * 0 - if content/customer node exists; however, contains no children
 * number - the number of children that the content/customer node contains
*/
private int doesCustExist(Node content)
{
    try
    {
        int index = 0 ; 
        int childRecs = 0 ; 
         
    java.lang.Iterable<Node> custNode = JcrUtils.getChildNodes(content, "customer");
    Iterator it = custNode.iterator();
              
    //only going to be 1 content/customer node if it exists
    if (it.hasNext())
        {
        //Count the number of child nodes in content/customer
        Node customerRoot = content.getNode("customer");
        Iterable itCust = JcrUtils.getChildNodes(customerRoot); 
        Iterator childNodeIt = itCust.iterator();
             
        //Count the number of customer child nodes 
        while (childNodeIt.hasNext())
        {
            childRecs++;
            childNodeIt.next();
        }
         return childRecs; 
       }
    else
        return -1; //content/customer does not exist
    }
    catch(Exception e)
    {
    e.printStackTrace();
    }
    return 0;
 }
}
