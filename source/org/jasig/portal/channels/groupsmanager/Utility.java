/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.channels.groupsmanager;

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  org.apache.log4j.Priority;
import  org.jasig.portal.services.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.security.*;
import  java.lang.*;
import  java.io.*;
import  java.util.*;
import  java.sql.Timestamp;
import  org.apache.xml.serialize.XMLSerializer;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.w3c.dom.Document;
import  javax.xml.parsers.*;

/**
 * A class holding utility functions used by the Groups Manager channel.
 */
public class Utility
      implements GroupsManagerConstants {

   /** Creates new Utility */
   public Utility () {
   }

   /**
    *
    * @param ownerID The user/group ID that created this IInitialGroupContext
    * @param ownerType The entity that created this IInitialGroupContext could be either a person (p) or a group (g).
    * @param groupID The Group ID referenced by this IInitialGroupContext.
    * @param ordinal Determined the order that the IInitialGroupContexts will be displayed.
    * @param expanded Determines whether the IInitialGroupContext will be expanded when first presented to the user.
    * @param dateCreated
    * @return IInitialGroupContext
    * @throws org.jasig.portal.channels.groupsmanager.ChainedException
    */
   public static IInitialGroupContext createInitialGroupContext (String ownerID, String ownerType,
         String groupID, int ordinal, boolean expanded, Timestamp dateCreated) throws ChainedException {
      return  RDBMInitialGroupContextStore.singleton().newInstance(ownerID, ownerType,
            groupID, ordinal, expanded, dateCreated);
   }

   /**
    * An attempt to extract all calls to logger to expedite assumed future
    * upgrades.
    * @param msgTypeStr
    * @param msg
    */
   public static void logMessage (String msgTypeStr, String msg) {
      // delay hesitates printing until a new millisecond is reached in order to
      // prevent overlapping messages during debugging

      /** @todo next 4 lines are for running test */
//      if (msg != null){
//         if (!msgTypeStr.equals("DEBUG")){
//            System.out.println(msgTypeStr+"::"+msg);
//            return;
//         }
//         else{
//            return;
//         }
//      }

      boolean delay = false;
      Priority msgType;
      if (msgTypeStr == null | msgTypeStr.equals(""))
         msgType = LogService.DEBUG;
      else
         msgType = Priority.toPriority(msgTypeStr.toUpperCase());

      if (delay && msgTypeStr.toUpperCase().equals("DEBUG")) {
         long ts1 = Calendar.getInstance().getTime().getTime();
         long ts2 = ts1;
         while(ts2 <= ts1){
            ts2 = Calendar.getInstance().getTime().getTime();
         }
      }
      LogService.instance().log(msgType, msg);
      return;
   }

   /**
    * Extracts the value of a key sandwiched by delimiters from a string.
    * @param fromDelim
    * @param source
    * @param toDelim
    * @return String
    */
   public static String parseStringDelimitedBy (String fromDelim, String source, String toDelim) {
      Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): fromDelim = " + fromDelim +
            " source = " + source + " toDelim = " + toDelim);
      String parsedString = null;
      String tagString = null;
      int idxFrom = source.indexOf(fromDelim);
      int idxTo;
      //Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): idxFrom = " + idxFrom);
      if (idxFrom > -1) {
         tagString = source.substring(idxFrom);
         //Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): tagString = " + tagString);
         idxTo = tagString.indexOf(toDelim);
         if (idxTo < 0) {
            parsedString = tagString.substring(fromDelim.length());
         }
         else {
            parsedString = tagString.substring(fromDelim.length(), tagString.indexOf(toDelim));
         }
      }
      Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): Returning parsedString = "
            + parsedString);
      return  parsedString;
   }

   /**
    * Prints a Document. Used for debugging.
    * @param aDoc
    * @param aMessage
    */
   public static void printDoc (Document aDoc, String aMessage) {
      String aMsg = (aMessage != null ? aMessage : "");
      try {
         Utility.logMessage("DEBUG", "****************************************");
         Utility.logMessage("DEBUG", aMsg + "\n" + toString(aDoc));
         Utility.logMessage("DEBUG", "########################################");
      } catch (Exception e) {
         Utility.logMessage("ERROR", e.toString());
      }
      return;
   }

   /**
    * Prints an Element. Used for debugging.
    * @param anElem
    * @param aMessage
    */
   public static void printElement (Element anElem, String aMessage) {
      Document prtDoc = GroupsManagerXML.getNewDocument();
      //org.w3c.dom.Node cpy = anElem.cloneNode(true);
      //prtDoc.adoptNode(cpy);
      Element cpy = (Element)prtDoc.importNode(anElem,true);
      prtDoc.appendChild(cpy);
      Utility.printDoc(prtDoc, aMessage);
      return;
   }

   /**
    * Retrieves a Group Member for the provided key and of the provided type.
    * @param key
    * @param type
    * @return IGroupMember
    */
   public static IGroupMember retrieveGroupMemberForKeyAndType (String key, String type) {
      IGroupMember gm = null;

      if (type.equals(GROUP_CLASSNAME)) {
         gm = GroupsManagerXML.retrieveGroup(key);
         Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForKeyAndType(): Retrieved group for Type: = "
               + type + " and key: " + key);
      }
      else {
         gm = GroupsManagerXML.retrieveEntity(key,type);
         Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForKeyAndType(): Retrieved entity for Type: = "
               + type + " and key: " + key);
      }
      return  gm;
   }

   /**
    * Represents a document as a string.
    * @param aDoc
    * @return String
    */
   public static String toString (Document aDoc) {
      StringWriter sw = new StringWriter();
      try {
         XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(aDoc,
               "UTF-8", true));
         serial.serialize(aDoc);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "Utility::asString(): " + e.toString());
      }
      return  sw.toString();
   }

   /**
    * given a group key, return an xml group id that matches it in the provided document
    *
    * @param grpKey
    * @param model
    * @return String
    */
   public static String translateKeytoID(String grpKey, Document model){
      Document viewDoc = model;
      String id = null;
      Element grpViewKeyElem;
      Iterator grpItr = GroupsManagerXML.getNodesByTagNameAndKey(viewDoc, GROUP_TAGNAME,
            grpKey);
      IEntityGroup gm = GroupsManagerXML.retrieveGroup(grpKey);
      if (gm != null) {
         if (!grpItr.hasNext()) {
            grpViewKeyElem = GroupsManagerXML.getGroupMemberXml(gm, true, null,
                  viewDoc);
            Element rootElem = viewDoc.getDocumentElement();
            rootElem.appendChild(grpViewKeyElem);
         }
         else {
            grpViewKeyElem = (Element)grpItr.next();
            GroupsManagerXML.getGroupMemberXml(gm, true, grpViewKeyElem, viewDoc);
         }
         id = grpViewKeyElem.getAttribute("id");
      }
      return id;
   }
}



