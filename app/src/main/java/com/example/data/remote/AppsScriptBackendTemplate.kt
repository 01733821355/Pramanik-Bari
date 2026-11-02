package com.example.data.remote

object AppsScriptBackendTemplate {
    const val OWNER_EMAIL = "mdbappyhossain2510@gmail.com"
    const val SHEET_NAME = "Pramanik_Bari_Family_Tree"
    const val FOLDER_NAME = "Pramanik_Bari_Member_Photos"
    const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbzVQTViltm1eL8qN5X9gdfy_VgMEnYAWyvftLXGZwoWL49AbabtpNAL3VqTZ1N1B44/exec"

    val SCRIPT_CODE: String = """
/**
 * Pramanik Bari - Digital Family Tree & Events Backend
 * Owner: mdbappyhossain2510@gmail.com
 * Powered by Google Apps Script (Google Sheets & Google Drive)
 * 
 * ========================================================
 * 📌 AUTHORIZATION & DEPLOYMENT INSTRUCTIONS:
 * 1. Open your Google Sheet -> Extensions -> Apps Script
 * 2. Delete all existing code in Code.gs and paste this ENTIRE code
 * 3. At the top toolbar dropdown (next to "Debug"), select function:
 *       "setupAndAuthorizeDrive"
 * 4. Click the "Run" button (▶ Run)
 * 5. Google will show: "Authorization required" -> Click "Review permissions"
 * 6. Select your Google account (mdbappyhossain2510@gmail.com)
 * 7. Click "Advanced" (or 'উন্নত') -> Click "Go to Untitled project (unsafe)"
 * 8. Click "Allow" (অনুমতি দিন)
 * 9. Look at Execution log: It will say "✅ All permissions authorized successfully!"
 * 
 * 10. Now click "Deploy" (ডিপ্লয়) -> "Manage deployments" -> Edit (pencil icon)
 *     or "New deployment":
 *     - Select type: "Web app"
 *     - Description: "Pramanik Bari v1"
 *     - Execute as: "Me" (mdbappyhossain2510@gmail.com)
 *     - Who has access: "Anyone" (যেকোনো ব্যক্তি)
 * 11. Click "Deploy" and copy the Web App URL.
 * ========================================================
 */

var SHEET_NAME = "Pramanik_Bari_Family_Tree";
var EVENTS_SHEET_NAME = "Pramanik_Bari_Events";
var FOLDER_NAME = "Pramanik_Bari_Member_Photos";

var HEADERS = [
  "id", "fullName", "gender", "dob", "dod", "isDeceased",
  "photoUrl", "driveFileId", "fatherId", "fatherName",
  "motherId", "motherName", "spouseId", "spouseName",
  "childrenIds", "brotherSisterIds", "grandfatherName", "grandmotherName",
  "village", "address", "phone", "occupation", "bloodGroup",
  "biography", "notes", "generation", "updatedAt"
];

var EVENT_HEADERS = [
  "id", "title", "date", "location", "description",
  "photoUrls", "photoDriveIds", "createdByName", "updatedAt"
];

/**
 * ⚡ RUN THIS FUNCTION FROM EDITOR TO AUTHORIZE GOOGLE DRIVE & SHEETS
 */
function setupAndAuthorizeDrive() {
  Logger.log("Starting Pramanik Bari authorization & setup...");
  
  // 1. Authorize & Initialize Google Sheets
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = getSheet();
  var eventSheet = getEventsSheet();
  Logger.log("✅ Google Sheets access verified: " + ss.getName());
  
  // 2. Authorize & Initialize Google Drive Folder for Photos
  var folder = getDriveFolder();
  Logger.log("✅ Google Drive Folder verified: " + folder.getName() + " (ID: " + folder.getId() + ")");
  
  // 3. Test Drive upload & view permission
  folder.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  
  Logger.log("🎉 SUCCESS: All permissions (Google Drive & Sheets) authorized successfully!");
  Logger.log("Now deploy as Web App with 'Execute as: Me' and 'Who has access: Anyone'.");
  
  return "Setup & Authorization Completed Successfully!";
}

function doGet(e) {
  if (!e) e = { parameter: {} };
  var action = (e.parameter && e.parameter.action) ? e.parameter.action : "getMembers";
  
  if (action === "ping") {
    return createJsonResponse({
      status: "success",
      message: "Pramanik Bari Family Tree Cloud Backend is active!",
      owner: "mdbappyhossain2510@gmail.com",
      timestamp: new Date().toISOString()
    });
  } else if (action === "getEvents") {
    return getEventsResponse();
  }
  
  return getMembersResponse();
}

function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return createJsonResponse({
        status: "error",
        message: "No POST body received. If testing in editor, run setupAndAuthorizeDrive() instead."
      });
    }

    var rawData = e.postData.contents;
    var data = JSON.parse(rawData);
    var action = data.action || "";

    if (action === "saveMember") {
      return saveMember(data);
    } else if (action === "deleteMember") {
      return deleteMember(data.id);
    } else if (action === "saveEvent") {
      return saveEvent(data);
    } else if (action === "deleteEvent") {
      return deleteEvent(data.id);
    } else if (action === "uploadPhoto") {
      return uploadPhoto(data);
    } else {
      return createJsonResponse({ status: "error", message: "Invalid action: " + action });
    }
  } catch (err) {
    return createJsonResponse({ status: "error", message: err.toString() });
  }
}

function getSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
    sheet.appendRow(HEADERS);
    sheet.setFrozenRows(1);
    sheet.getRange(1, 1, 1, HEADERS.length).setFontWeight("bold").setBackground("#0D3325").setFontColor("#FFFFFF");
  }
  return sheet;
}

function getDriveFolder() {
  var folders = DriveApp.getFoldersByName(FOLDER_NAME);
  if (folders.hasNext()) {
    var folder = folders.next();
    folder.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
    return folder;
  }
  var newFolder = DriveApp.createFolder(FOLDER_NAME);
  newFolder.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  return newFolder;
}

function getMembersResponse() {
  var sheet = getSheet();
  var data = sheet.getDataRange().getValues();
  if (data.length <= 1) {
    return createJsonResponse({ status: "success", data: [] });
  }
  
  var headers = data[0];
  var members = [];
  
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    if (!row[0]) continue;
    var member = {};
    for (var j = 0; j < headers.length; j++) {
      var key = headers[j];
      var val = row[j];
      if (key === "isDeceased") {
        member[key] = (val === true || val === "true" || val === "TRUE");
      } else if (key === "generation") {
        member[key] = parseInt(val) || 1;
      } else if (key === "updatedAt") {
        member[key] = parseInt(val) || new Date().getTime();
      } else if (key === "childrenIds" || key === "brotherSisterIds") {
        if (typeof val === "string" && val.trim().length > 0) {
          try {
            member[key] = JSON.parse(val);
          } catch(e) {
            member[key] = val.split(",").map(function(s){return s.trim();});
          }
        } else {
          member[key] = [];
        }
      } else {
        member[key] = val ? String(val).trim() : "";
      }
    }
    members.push(member);
  }
  
  return createJsonResponse({ status: "success", data: members });
}

function saveMember(member) {
  var sheet = getSheet();
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]).trim() === String(member.id).trim()) {
      targetRow = i + 1;
      break;
    }
  }
  
  var childrenStr = Array.isArray(member.childrenIds) ? JSON.stringify(member.childrenIds) : (member.childrenIds || "[]");
  var siblingsStr = Array.isArray(member.brotherSisterIds) ? JSON.stringify(member.brotherSisterIds) : (member.brotherSisterIds || "[]");
  
  var rowValues = [
    member.id || "",
    member.fullName || "",
    member.gender || "MALE",
    member.dob || "",
    member.dod || "",
    member.isDeceased ? true : false,
    member.photoUrl || "",
    member.driveFileId || "",
    member.fatherId || "",
    member.fatherName || "",
    member.motherId || "",
    member.motherName || "",
    member.spouseId || "",
    member.spouseName || "",
    childrenStr,
    siblingsStr,
    member.grandfatherName || "",
    member.grandmotherName || "",
    member.village || "",
    member.address || "",
    member.phone || "",
    member.occupation || "",
    member.bloodGroup || "",
    member.biography || "",
    member.notes || "",
    parseInt(member.generation) || 1,
    new Date().getTime()
  ];
  
  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowValues.length).setValues([rowValues]);
  } else {
    sheet.appendRow(rowValues);
  }
  
  return createJsonResponse({ status: "success", message: "Member saved successfully", id: member.id });
}

function deleteMember(id) {
  if (!id) {
    return createJsonResponse({ status: "error", message: "Missing member ID" });
  }
  var sheet = getSheet();
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]).trim() === String(id).trim()) {
      sheet.deleteRow(i + 1);
      return createJsonResponse({ status: "success", message: "Member deleted", id: id });
    }
  }
  return createJsonResponse({ status: "error", message: "Member not found" });
}

function uploadPhoto(data) {
  var memberId = data.memberId || "unknown";
  var fileName = data.fileName || ("photo_" + memberId + "_" + new Date().getTime() + ".jpg");
  var mimeType = data.mimeType || "image/jpeg";
  var base64Data = data.base64Data;
  
  if (!base64Data) {
    return createJsonResponse({ status: "error", message: "No base64 image data received" });
  }
  
  var decodedBytes = Utilities.base64Decode(base64Data);
  var blob = Utilities.newBlob(decodedBytes, mimeType, fileName);
  var folder = getDriveFolder();
  var file = folder.createFile(blob);
  file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  
  var fileId = file.getId();
  var directUrl = "https://lh3.googleusercontent.com/d/" + fileId;
  
  // Update photo in sheet if member exists
  var sheet = getSheet();
  var sheetData = sheet.getDataRange().getValues();
  for (var i = 1; i < sheetData.length; i++) {
    if (String(sheetData[i][0]).trim() === String(memberId).trim()) {
      sheet.getRange(i + 1, 7).setValue(directUrl);     // photoUrl col
      sheet.getRange(i + 1, 8).setValue(fileId);        // driveFileId col
      break;
    }
  }
  
  return createJsonResponse({
    status: "success",
    fileId: fileId,
    driveUrl: directUrl,
    message: "Photo uploaded permanently to Google Drive"
  });
}

function getEventsSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(EVENTS_SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(EVENTS_SHEET_NAME);
    sheet.appendRow(EVENT_HEADERS);
    sheet.setFrozenRows(1);
    sheet.getRange(1, 1, 1, EVENT_HEADERS.length).setFontWeight("bold").setBackground("#B8860B").setFontColor("#FFFFFF");
  }
  return sheet;
}

function getEventsResponse() {
  var sheet = getEventsSheet();
  var data = sheet.getDataRange().getValues();
  if (data.length <= 1) {
    return createJsonResponse({ status: "success", data: [] });
  }
  
  var headers = data[0];
  var events = [];
  
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    if (!row[0]) continue;
    var evt = {};
    for (var j = 0; j < headers.length; j++) {
      var key = headers[j];
      var val = row[j];
      if (key === "photoUrls" || key === "photoDriveIds") {
        if (typeof val === "string" && val.trim().length > 0) {
          try {
            evt[key] = JSON.parse(val);
          } catch(e) {
            evt[key] = val.split("|||").map(function(s){return s.trim();});
          }
        } else {
          evt[key] = [];
        }
      } else if (key === "updatedAt") {
        evt[key] = parseInt(val) || new Date().getTime();
      } else {
        evt[key] = val ? String(val).trim() : "";
      }
    }
    events.push(evt);
  }
  
  return createJsonResponse({ status: "success", data: events });
}

function saveEvent(event) {
  var sheet = getEventsSheet();
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]).trim() === String(event.id).trim()) {
      targetRow = i + 1;
      break;
    }
  }
  
  var urlsStr = Array.isArray(event.photoUrls) ? JSON.stringify(event.photoUrls) : (event.photoUrls || "[]");
  var driveIdsStr = Array.isArray(event.photoDriveIds) ? JSON.stringify(event.photoDriveIds) : (event.photoDriveIds || "[]");
  
  var rowValues = [
    event.id || ("EVT-" + new Date().getTime()),
    event.title || "",
    event.date || "",
    event.location || "",
    event.description || "",
    urlsStr,
    driveIdsStr,
    event.createdByName || "",
    new Date().getTime()
  ];
  
  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowValues.length).setValues([rowValues]);
  } else {
    sheet.appendRow(rowValues);
  }
  
  return createJsonResponse({ status: "success", message: "Event saved successfully", id: rowValues[0] });
}

function deleteEvent(id) {
  if (!id) {
    return createJsonResponse({ status: "error", message: "Missing event ID" });
  }
  var sheet = getEventsSheet();
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]).trim() === String(id).trim()) {
      sheet.deleteRow(i + 1);
      return createJsonResponse({ status: "success", message: "Event deleted", id: id });
    }
  }
  return createJsonResponse({ status: "error", message: "Event not found" });
}

function createJsonResponse(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
""".trimIndent()
}
