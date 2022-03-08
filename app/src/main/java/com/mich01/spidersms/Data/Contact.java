package com.mich01.spidersms.Data;

public class Contact
{
    private final String CID;
    private final String ContactNames;
    private final String PubKey;
    private final int CType;

    public Contact(String cid, String contactNames, String pubKey, int cType) {
        CID = cid;
        ContactNames = contactNames;
        PubKey = pubKey;
        CType = cType;
    }

    public String getPubKey() {
        return PubKey;
    }

    public String getCID() {
        return CID;
    }

    public String getContactNames() {
        return ContactNames;
    }
    public int getCType() {
        return CType;
    }
}
