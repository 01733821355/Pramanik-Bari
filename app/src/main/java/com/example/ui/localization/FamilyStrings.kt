package com.example.ui.localization

object FamilyStrings {
    fun appTitle(isBn: Boolean) = if (isBn) "প্রামাণিক বাড়ি" else "Pramanik Bari"
    fun appSubtitle(isBn: Boolean) = if (isBn) "ডিজিটাল বংশলতিকা ও পারিবারিক আর্কাইভ" else "Digital Family Tree & Genealogy System"

    // Tabs
    fun tabDashboard(isBn: Boolean) = if (isBn) "ড্যাশবোর্ড" else "Dashboard"
    fun tabTree(isBn: Boolean) = if (isBn) "বংশলতিকা" else "Family Tree"
    fun tabMembers(isBn: Boolean) = if (isBn) "সদস্য তালিকা" else "Members"
    fun tabEvents(isBn: Boolean) = if (isBn) "ইভেন্ট" else "Events"
    fun tabRelation(isBn: Boolean) = if (isBn) "সম্পর্ক নির্ণয়" else "Relationship"
    fun tabCloud(isBn: Boolean) = if (isBn) "ক্লাউড সিঙ্ক" else "Cloud Sync"

    // Statistics
    fun totalMembers(isBn: Boolean) = if (isBn) "মোট সদস্য" else "Total Members"
    fun maleMembers(isBn: Boolean) = if (isBn) "পুরুষ" else "Male"
    fun femaleMembers(isBn: Boolean) = if (isBn) "মহিলা" else "Female"
    fun livingMembers(isBn: Boolean) = if (isBn) "জীবিত সদস্য" else "Living"
    fun deceasedMembers(isBn: Boolean) = if (isBn) "প্রয়াত সদস্য" else "Deceased"
    fun totalGenerations(isBn: Boolean) = if (isBn) "মোট প্রজন্ম" else "Generations"
    fun recentlyAdded(isBn: Boolean) = if (isBn) "সম্প্রতি যুক্ত সদস্যবৃন্দ" else "Recently Added Members"

    // Actions & Form
    fun addMember(isBn: Boolean) = if (isBn) "নতুন সদস্য যোগ করুন" else "Add New Member"
    fun editMember(isBn: Boolean) = if (isBn) "তথ্য পরিবর্তন" else "Edit Member"
    fun deleteMember(isBn: Boolean) = if (isBn) "সদস্য মুছে ফেলুন" else "Delete Member"
    fun save(isBn: Boolean) = if (isBn) "সংরক্ষণ করুন" else "Save"
    fun cancel(isBn: Boolean) = if (isBn) "বাতিল" else "Cancel"
    fun search(isBn: Boolean) = if (isBn) "সদস্যের নাম বা গ্রাম খুঁজুন..." else "Search by name or village..."
    fun viewProfile(isBn: Boolean) = if (isBn) "পূর্ণাঙ্গ বিবরণ" else "View Profile"
    fun callPhone(isBn: Boolean) = if (isBn) "কল করুন" else "Call Phone"

    // Fields
    fun fullName(isBn: Boolean) = if (isBn) "পূর্ণ নাম" else "Full Name"
    fun memberId(isBn: Boolean) = if (isBn) "সদস্য আইডি" else "Member ID"
    fun gender(isBn: Boolean) = if (isBn) "লিঙ্গ" else "Gender"
    fun male(isBn: Boolean) = if (isBn) "পুরুষ" else "Male"
    fun female(isBn: Boolean) = if (isBn) "মহিলা" else "Female"
    fun other(isBn: Boolean) = if (isBn) "অন্যান্য" else "Other"
    fun dob(isBn: Boolean) = if (isBn) "জন্মতারিখ" else "Date of Birth"
    fun dod(isBn: Boolean) = if (isBn) "মৃত্যুতারিখ" else "Date of Death"
    fun isDeceased(isBn: Boolean) = if (isBn) "প্রয়াত হয়েছেন?" else "Is Deceased?"
    fun father(isBn: Boolean) = if (isBn) "পিতা" else "Father"
    fun mother(isBn: Boolean) = if (isBn) "মাতা" else "Mother"
    fun spouse(isBn: Boolean) = if (isBn) "স্বামী / স্ত্রী" else "Spouse"
    fun children(isBn: Boolean) = if (isBn) "সন্তান সন্ততি" else "Children"
    fun siblings(isBn: Boolean) = if (isBn) "ভাই-বোন" else "Siblings"
    fun grandfather(isBn: Boolean) = if (isBn) "দাদা / নানা" else "Grandfather"
    fun grandmother(isBn: Boolean) = if (isBn) "দাদি / নানি" else "Grandmother"
    fun village(isBn: Boolean) = if (isBn) "গ্রাম / এলাকা" else "Village"
    fun address(isBn: Boolean) = if (isBn) "ঠিকানা" else "Address"
    fun phone(isBn: Boolean) = if (isBn) "মোবাইল নম্বর" else "Mobile Number"
    fun occupation(isBn: Boolean) = if (isBn) "পেশা" else "Occupation"
    fun bloodGroup(isBn: Boolean) = if (isBn) "রক্তের গ্রুপ" else "Blood Group"
    fun biography(isBn: Boolean) = if (isBn) "সংক্ষিপ্ত জীবনী" else "Biography"
    fun notes(isBn: Boolean) = if (isBn) "অতিরিক্ত নোট" else "Additional Notes"
    fun photoUrl(isBn: Boolean) = if (isBn) "ছবির লিঙ্ক / ড্রাইভ URL" else "Photo URL / Drive URL"
    fun generation(isBn: Boolean) = if (isBn) "প্রজন্ম স্তর" else "Generation Level"

    // Roles
    fun adminMode(isBn: Boolean) = if (isBn) "অ্যাডমিন মোড" else "Admin Mode"
    fun viewerMode(isBn: Boolean) = if (isBn) "দর্শক মোড" else "Viewer Mode"
    fun switchRole(isBn: Boolean) = if (isBn) "ভূমিকা পরিবর্তন" else "Switch Role"
    fun adminOnlyWarning(isBn: Boolean) = if (isBn) "এই ফিচারটি শুধুমাত্র অ্যাডমিনের জন্য অনুমোদিত।" else "This action is restricted to Admins."

    // Tree features
    fun zoomIn(isBn: Boolean) = if (isBn) "জুম ইন (+)" else "Zoom In (+)"
    fun zoomOut(isBn: Boolean) = if (isBn) "জুম আউট (-)" else "Zoom Out (-)"
    fun resetView(isBn: Boolean) = if (isBn) "রিসেট ভিউ" else "Reset View"
    fun expandAll(isBn: Boolean) = if (isBn) "শাখা খুলুন" else "Expand Branches"
    fun collapseAll(isBn: Boolean) = if (isBn) "শাখা সংক্ষেপ করুন" else "Collapse Branches"
    fun highlightedLineage(isBn: Boolean) = if (isBn) "নির্বাচিত বংশধারা চিহ্নিত" else "Selected Lineage Highlighted"

    // Relationship Finder
    fun selectPersonA(isBn: Boolean) = if (isBn) "প্রথম সদস্য নির্বাচন করুন" else "Select First Member"
    fun selectPersonB(isBn: Boolean) = if (isBn) "দ্বিতীয় সদস্য নির্বাচন করুন" else "Select Second Member"
    fun calculateRelation(isBn: Boolean) = if (isBn) "আত্মীয়তার সম্পর্ক নির্ণয় করুন" else "Find Relationship"
    fun relationshipChain(isBn: Boolean) = if (isBn) "সম্পর্কের ধারাবাহিক চেইন:" else "Kinship Chain:"

    // Cloud & Google Sheets
    fun cloudSyncTitle(isBn: Boolean) = if (isBn) "গুগল শিট ও গুগল ড্রাইভ সংযোগ" else "Google Sheets & Google Drive Sync"
    fun appsScriptUrl(isBn: Boolean) = if (isBn) "Apps Script ওয়েব অ্যাপ URL" else "Apps Script Web App URL"
    fun syncNow(isBn: Boolean) = if (isBn) "এখনই সিঙ্ক করুন" else "Sync with Cloud"
    fun copyScript(isBn: Boolean) = if (isBn) "ব্যাকএন্ড কোড কপি করুন" else "Copy Apps Script Code"
    fun driveOwnerInfo(isBn: Boolean) = if (isBn) "গুগল ড্রাইভ ও শিটের স্বত্বাধিকারী:" else "Google Drive & Sheets Owner:"
    fun syncSuccess(count: Int, isBn: Boolean) = if (isBn) "$count জন সদস্য সফলভাবে সিঙ্ক হয়েছে!" else "Successfully synced $count members!"
}
